package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class SesionesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<SesionClass> lista;
    SesionAdapter adapter;
    ConnectionClass connectionClass;
    int idConcierto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sesiones);

        idConcierto = getIntent().getIntExtra("id_concierto", -1);
        String nombreConcierto = getIntent().getStringExtra("nombre_concierto");

        TextView tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Sesiones - " + nombreConcierto);

        recyclerView = findViewById(R.id.recyclerSesiones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lista = new ArrayList<>();
        adapter = new SesionAdapter(lista, this);
        recyclerView.setAdapter(adapter);

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        connectionClass = new ConnectionClass();

        cargarSeiones();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarSeiones() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(()->{
            try {
                Connection con = connectionClass.CONN();

                if (con != null) {
                    lista.clear();

                    String sql = "SELECT id_sesion, fecha, hora FROM sesion WHERE id_concierto = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, idConcierto);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id_sesion");
                        String fecha = rs.getString("fecha");
                        String hora = rs.getString("hora");

                        lista.add(new SesionClass(id, fecha, hora));
                    }

                    runOnUiThread(()-> adapter.notifyDataSetChanged());
                    rs.close();
                    ps.close();
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}