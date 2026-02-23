package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
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

    enum EstadoButaca {
        DISPONIBLE,
        VENDIDA,
        BLOQUEADA,
        SELECCIONADA
    }

    private int idsesion;
    private GridLayout gridPatio, gridPalco, gridVip;
    private ArrayList<Integer> butacasSeleccionadas = new ArrayList<>();
    private ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_butacas);

        // Referencias a GridLayout
        gridPatio = findViewById(R.id.gridPatio);
        gridPalco = findViewById(R.id.gridPalco);
        gridVip = findViewById(R.id.gridVip);

        // Recibir id de sesión
        idsesion = getIntent().getIntExtra("id_sesion", -1);
        if (idsesion == -1) {
            Toast.makeText(this, "Error, sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Botón Volver
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        // Botón Confirmar Compra
        Button btnConfirmar = findViewById(R.id.btnConfirmarCompra);
        btnConfirmar.setOnClickListener(v -> confirmarCompra());

        // Edge to edge (para padding con barra de navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectionClass = new ConnectionClass();

        // Cargar las butacas desde la BD
        cargarButacasBD();
    }

    private void cargarButacasBD() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();

                if (con != null) {
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
                        String estadoDB = rs.getString("estado");

                        runOnUiThread(() -> {
                            Button btn = new Button(ButacasActivity.this);
                            btn.setText(fila + numero);
                            btn.setTag(EstadoButaca.DISPONIBLE); // default
                            btn.setTag(R.id.tag_id_butaca, idButaca);

                            // Tamaño tipo cine
                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 150;
                            params.height = 150;
                            params.setMargins(8,8,8,8);
                            btn.setLayoutParams(params);

                            // Asignar color/estado según BD
                            switch (estadoDB) {
                                case "DISPONIBLE":
                                    btn.setBackgroundResource(R.drawable.butaca_libre);
                                    btn.setEnabled(true);
                                    break;
                                case "VENDIDA":
                                    btn.setBackgroundResource(R.drawable.butaca_ocupada);
                                    btn.setEnabled(false);
                                    break;
                                case "BLOQUEADA":
                                    btn.setBackgroundResource(R.drawable.butaca_bloqueada);
                                    btn.setEnabled(false);
                                    break;
                            }

                            // Click para seleccionar/desseleccionar
                            btn.setOnClickListener(v -> seleccionarButaca(btn, estadoDB));

                            // Añadir al GridLayout correcto
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

    private void seleccionarButaca(Button btn, String estadoActual) {
        EstadoButaca estado = (EstadoButaca) btn.getTag();

        if (estadoActual.equals("DISPONIBLE")) {
            if (estado != EstadoButaca.SELECCIONADA) {
                btn.setBackgroundResource(R.drawable.butaca_seleccionada);
                btn.setTag(EstadoButaca.SELECCIONADA);
                butacasSeleccionadas.add((int) btn.getTag(R.id.tag_id_butaca));
            } else {
                btn.setBackgroundResource(R.drawable.butaca_libre);
                btn.setTag(EstadoButaca.DISPONIBLE);
                butacasSeleccionadas.remove(Integer.valueOf((int) btn.getTag(R.id.tag_id_butaca)));
            }
        }
    }

    private void confirmarCompra() {
        if (butacasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar una butaca", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();

                String sql = "UPDATE butaca_sesion SET estado = 'VENDIDA' " +
                        "WHERE id_sesion = ? AND id_butaca = ?";

                PreparedStatement ps = con.prepareStatement(sql);

                for (int idButaca : butacasSeleccionadas) {
                    ps.setInt(1, idsesion);
                    ps.setInt(2, idButaca);
                    ps.executeUpdate();
                }

                ps.close();
                con.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Compra realizada correctamente", Toast.LENGTH_SHORT).show();
                    butacasSeleccionadas.clear();
                    cargarButacasBD(); // recargar las butacas actualizadas
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}