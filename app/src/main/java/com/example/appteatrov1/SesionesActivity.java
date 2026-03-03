package com.example.appteatrov1;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SesionesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<SesionClass> lista;
    private SesionAdapter adapter;
    private ConnectionClass connectionClass;
    private int idConcierto;
    private String rolUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesiones);

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        rolUsuario = prefs.getString("rol", "CLIENTE");

        idConcierto = getIntent().getIntExtra("id_concierto", -1);
        String nombreConcierto = getIntent().getStringExtra("nombre_concierto");

        TextView tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText(getString(R.string.titulo_sesiones, (nombreConcierto != null ? nombreConcierto : "")));

        Button btnAnadir = findViewById(R.id.btnAnadirSesionManual);
        if ("ADMIN".equalsIgnoreCase(rolUsuario)) {
            btnAnadir.setVisibility(View.VISIBLE);
            btnAnadir.setOnClickListener(v -> mostrarDialogoSesion(null));
        } else {
            btnAnadir.setVisibility(View.GONE);
        }

        recyclerView = findViewById(R.id.recyclerSesiones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lista = new ArrayList<>();

        adapter = new SesionAdapter(lista, this, rolUsuario, new SesionAdapter.OnSesionClickListener() {
            @Override
            public void onEliminarClick(int id, int posicion) {
                new AlertDialog.Builder(SesionesActivity.this)
                        .setTitle(getString(R.string.eliminar_titulo))
                        .setMessage(getString(R.string.eliminar_mensaje))
                        .setPositiveButton(getString(R.string.opcion_si), (d, w) -> eliminarSesion(id))
                        .setNegativeButton(getString(R.string.opcion_no), null).show();
            }

            @Override
            public void onEditarClick(SesionClass sesion) {
                mostrarDialogoSesion(sesion);
            }

            @Override
            public void onSesionClick(SesionClass sesion) {
                if (!"ADMIN".equalsIgnoreCase(rolUsuario)) {
                    Intent intent = new Intent(SesionesActivity.this, ButacasActivity.class);
                    intent.putExtra("id_sesion", sesion.getId());
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        connectionClass = new ConnectionClass();
        cargarSeiones();
    }

    private void mostrarDialogoSesion(SesionClass sesionExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.mostrar_dialogo_sesion, null);
        builder.setView(dialogView);

        EditText etFecha = dialogView.findViewById(R.id.etFechaDialogo);
        EditText etHora = dialogView.findViewById(R.id.etHoraDialogo);

        if (sesionExistente != null) {
            etFecha.setText(sesionExistente.getFecha());
            etHora.setText(sesionExistente.getHora());
        }

        etFecha.setOnClickListener(v -> abrirCalendario(etFecha));
        etHora.setOnClickListener(v -> abrirReloj(etHora));

        builder.setPositiveButton(getString(R.string.btn_guardar), (dialog, which) -> {
            String f = etFecha.getText().toString();
            String h = etHora.getText().toString();
            if (!f.isEmpty() && !h.isEmpty()) {
                guardarSesion(sesionExistente == null ? -1 : sesionExistente.getId(), f, h);
            } else {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(getString(R.string.btn_cancelar), null);
        builder.show();
    }

    private void abrirCalendario(EditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSel = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
            et.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void abrirReloj(EditText et) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String horaSel = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
            et.setText(horaSel);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void cargarSeiones() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (Connection con = connectionClass.CONN()) {
                if (con != null) {
                    lista.clear();
                    String sql = "SELECT id_sesion, fecha, hora FROM sesion WHERE id_concierto = ? ORDER BY fecha, hora";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, idConcierto);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        lista.add(new SesionClass(rs.getInt("id_sesion"), rs.getString("fecha"), rs.getString("hora")));
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void guardarSesion(int id, String f, String h) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (Connection con = connectionClass.CONN()) {
                if (con != null) {
                    String sql = (id == -1) ?
                            "INSERT INTO sesion (fecha, hora, id_concierto) VALUES (?, ?, ?)" :
                            "UPDATE sesion SET fecha = ?, hora = ? WHERE id_sesion = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, f);
                    ps.setString(2, h);
                    ps.setInt(3, (id == -1) ? idConcierto : id);
                    ps.executeUpdate();
                    runOnUiThread(() -> {
                        cargarSeiones();
                        Toast.makeText(this, getString(R.string.msg_sesion_guardada), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void eliminarSesion(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (Connection con = connectionClass.CONN()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM sesion WHERE id_sesion = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    runOnUiThread(this::cargarSeiones);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}