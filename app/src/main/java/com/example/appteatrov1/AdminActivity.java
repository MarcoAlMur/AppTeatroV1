package com.example.appteatrov1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ConciertoAdminAdapter adapter;
    private List<ConciertoClass> listaConciertos;
    private ConnectionClass connectionClass;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        connectionClass = new ConnectionClass();
        recyclerView = findViewById(R.id.recyclerAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaConciertos = new ArrayList<>();
        // CONFIGURACIÓN DEL ADAPTADOR PARA GESTIONAR SESIONES
        adapter = new ConciertoAdminAdapter(listaConciertos, concierto -> {
            Intent intent = new Intent(AdminActivity.this, SesionesActivity.class);
            intent.putExtra("id_concierto", concierto.getId());
            intent.putExtra("nombre_concierto", concierto.getNombre());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        Button btnAnadir = findViewById(R.id.btnIrAnadir);
        btnAnadir.setOnClickListener(v -> {
            startActivity(new Intent(this, AnadirConciertoActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnCerrarSesion);
        btnLogout.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        // Borramos los datos de SharedPreferences al salir
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void cargarConciertos() {
        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    List<ConciertoClass> nuevaLista = new ArrayList<>();
                    String query = "SELECT id_concierto, nombre, artista, ciudad, cartel FROM concierto";
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        nuevaLista.add(new ConciertoClass(
                                rs.getInt("id_concierto"),
                                rs.getString("nombre"),
                                rs.getString("artista"),
                                rs.getString("ciudad"),
                                rs.getString("cartel")
                        ));
                    }
                    runOnUiThread(() -> {
                        listaConciertos.clear();
                        listaConciertos.addAll(nuevaLista);
                        adapter.notifyDataSetChanged();
                    });
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarConciertos();
    }
}