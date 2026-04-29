package com.example.appteatrov1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog; // Necesario para la confirmación
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

/** Actividad principal del Administrador.
 * Se encarga de listar los conciertos, permitir añadir nuevos y gestionar la sesión
 */

public class AdminActivity extends AppCompatActivity {

    /* Componentes de la Interfaz (UI) */
    private RecyclerView recyclerView;
    private ConciertoAdminAdapter adapter;
    /* Datos y Conexión */
    private List<ConciertoClass> listaConciertos;
    private ConnectionClass connectionClass;

    // Nueva clase para gestionar la eliminación
    private EliminarConcierto eliminador;

    // ExecutorService para ejecutar consultas SQL fuera del hilo principal (UI Thread)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Infla el diseño XML de la actividad

        // Inicializamos la clase de conexión, la de eliminación y la lista de datos
        connectionClass = new ConnectionClass();
        eliminador = new EliminarConcierto(this);

        // 1. Configuración del RecyclerView
        recyclerView = findViewById(R.id.recyclerAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaConciertos = new ArrayList<>();

        // 2. CONFIGURACIÓN DEL ADAPTADOR ACTUALIZADA
        // Ahora pasamos un listener que gestiona dos eventos: Click normal y Eliminar
        adapter = new ConciertoAdminAdapter(listaConciertos, new ConciertoAdminAdapter.OnConciertoClickListener() {
            @Override
            public void onConciertoClick(ConciertoClass concierto) {
                // Cuando se pulsa un concierto, vamos a la gestión de sus sesiones
                Intent intent = new Intent(AdminActivity.this, SesionesActivity.class);
                intent.putExtra("id_concierto", concierto.getId());
                intent.putExtra("nombre_concierto", concierto.getNombre());
                startActivity(intent);
            }

            @Override
            public void onEliminarClick(int id, int posicion) {
                // Cuadro de diálogo para confirmar la acción antes de borrar de la DB
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("Eliminar Concierto")
                        .setMessage("¿Estás seguro? Esta acción eliminará permanentemente el concierto y todas sus sesiones asociadas.")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            // Llamamos al método de la clase EliminarConcierto
                            eliminador.eliminarConciertoCompleto(id, posicion, adapter, listaConciertos);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        // 3. Botón para navegar a la pantalla de añadir concierto
        Button btnAnadir = findViewById(R.id.btnIrAnadir);
        btnAnadir.setOnClickListener(v -> {
            startActivity(new Intent(this, AnadirConciertoActivity.class));
        });

        // 4. Botón de Cerrar Sesión
        Button btnLogout = findViewById(R.id.btnCerrarSesion);
        btnLogout.setOnClickListener(v -> cerrarSesion());
    }

    /**
     * Limpia las credenciales del usuario y lo devuelve al Login.
     */
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Consulta la base de datos de forma asíncrona para obtener la lista de conciertos.
     */
    private void cargarConciertos() {
        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN(); // Establecemos conexión JDBC
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