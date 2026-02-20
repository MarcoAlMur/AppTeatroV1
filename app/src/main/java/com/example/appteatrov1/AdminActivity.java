package com.example.appteatrov1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
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
        adapter = new ConciertoAdminAdapter(listaConciertos, (id, posicion) -> {
            eliminarConciertoDeBD(id, posicion);
        });
        recyclerView.setAdapter(adapter);

        // --- BOTÓN AÑADIR NUEVO ---
        Button btnAnadir = findViewById(R.id.btnIrAnadir);
        btnAnadir.setOnClickListener(v -> {
            startActivity(new Intent(this, AnadirConciertoActivity.class));
        });

        // --- BOTÓN CERRAR SESIÓN (TU BOTÓN MORADO) ---
        Button btnLogout = findViewById(R.id.btnCerrarSesion);
        btnLogout.setOnClickListener(v -> {
            // Regresamos al MainActivity (Login)
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);

            // Flags para que el usuario no pueda volver atrás al panel admin
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void cargarConciertos() {
        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    List<ConciertoClass> nuevaLista = new ArrayList<>();
                    String query = "SELECT id_concierto, nombre, artista, ciudad FROM concierto";
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        nuevaLista.add(new ConciertoClass(
                                rs.getInt("id_concierto"),
                                rs.getString("nombre"),
                                rs.getString("artista"),
                                rs.getString("ciudad")
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

    private void eliminarConciertoDeBD(int id, int posicion) {
        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    String query = "DELETE FROM concierto WHERE id_concierto = ?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, id);
                    int filas = ps.executeUpdate();
                    if (filas > 0) {
                        runOnUiThread(() -> {
                            if (posicion < listaConciertos.size()) {
                                listaConciertos.remove(posicion);
                                adapter.notifyItemRemoved(posicion);
                                adapter.notifyItemRangeChanged(posicion, listaConciertos.size());
                            }
                        });
                    }
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