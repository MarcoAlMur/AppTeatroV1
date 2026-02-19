package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ButacasActivity extends AppCompatActivity {

    GridLayout gridPatio, gridPalco, gridVip;
    ConnectionClass connectionClass;
    private int idsesion;
    ArrayList<Integer> butacasSeleccionadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_butacas);
        gridPatio = findViewById(R.id.gridPatio);
        gridPalco = findViewById(R.id.gridPalco);
        gridVip = findViewById(R.id.gridVip);

        idsesion = getIntent().getIntExtra("id_sesion", -1);
        if (idsesion == -1) {
            Toast.makeText(this, "Error, sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectionClass = new ConnectionClass();

        cargarButacasBD();

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarButacasBD() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            try {
                Connection con = connectionClass.CONN();

                if(con != null) {
                    String sql = "SELECT b.id_butaca, b.fila, b.numero, z.nombre, bs.estado " +
                            "FROM butaca b " +
                            "JOIN zona z ON b.id_zona = z.id_zona " +
                            "JOIN butaca_sesion bs ON b.id_butaca = bs.id_butaca " +
                            "WHERE bs.id_sesion = ? " +
                            "ORDER BY z.nombre, b.fila, b.numero";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, idsesion);
                    ResultSet rs = ps.executeQuery();

                    runOnUiThread(() -> {
                        gridPatio.removeAllViews();
                        gridPalco.removeAllViews();
                        gridVip.removeAllViews();
                    });
                    while (rs.next()) {
                        int idButaca = rs.getInt("id_butaca");
                        String fila = rs.getString("fila");
                        int numero = rs.getInt("numero");
                        String zona = rs.getString("nombre");
                        String estado = rs.getString("estado");

                        runOnUiThread(() -> {
                            Button btn = new Button(ButacasActivity.this);
                            btn.setText(fila + "-" + numero);
                            btn.setTag(R.id.tag_seleccionada, false);
                            btn.setTag(R.id.tag_id_butaca, idButaca);

                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 160;
                            params.height = 160;
                            params.setMargins(8,8,8,8);
                            btn.setLayoutParams(params);

                            asgnarColorSegunEstado(btn, estado);
                            btn.setOnClickListener(v -> seleccionarButaca(btn, estado));

                            if (zona.equalsIgnoreCase("Patio")) {
                                gridPatio.addView(btn);
                            } else if (zona.equalsIgnoreCase("Palco")) {
                                gridPalco.addView(btn);
                            } else {
                                gridVip.addView(btn);
                            }
                        });
                    }
                    rs.close();
                    ps.close();
                    con.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void asgnarColorSegunEstado(Button btn, String estado) {
        switch (estado) {
            case "DISPONIBLE":
                btn.setBackgroundColor(getColor(R.color.verde_disponible));
                break;
            case "VENDIDA":
                btn.setBackgroundColor(getColor(R.color.rojo_vendida));
                btn.setEnabled(false);
                break;
            case "BLOQUEADA":
                btn.setBackgroundColor(getColor(R.color.azul_bloqueada));
                btn.setEnabled(false);
                break;
        }
    }

    private void seleccionarButaca(Button btn, String estadoActual) {
        if (!estadoActual.equals("DISPONIBLE")) {
            return;
        }

        boolean seleccionada = (boolean) btn.getTag(R.id.tag_seleccionada);
        int idButaca = (int) btn.getTag(R.id.tag_id_butaca);

        if (!seleccionada) {
            btn.setBackgroundColor(getColor(R.color.naranja_seleccionada));
            btn.setTag(R.id.tag_seleccionada, true);
            butacasSeleccionadas.add(idButaca);
        } else {
            btn.setBackgroundColor(getColor(R.color.verde_disponible));
            btn.setTag(R.id.tag_seleccionada, false);
            butacasSeleccionadas.remove(Integer.valueOf(idButaca));
        }
    }
}