package com.example.appteatrov1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnadirConciertoActivity extends AppCompatActivity {

    private EditText etNombre, etArtista, etCiudad;
    private Button btnGuardar, btnCancelar; // Añadimos btnCancelar
    private ConnectionClass connectionClass;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_concierto);

        // 1. Vincular con el XML
        etNombre = findViewById(R.id.etNombreConcierto);
        etArtista = findViewById(R.id.etArtistaConcierto);
        etCiudad = findViewById(R.id.etCiudadConcierto);
        btnGuardar = findViewById(R.id.btnGuardarConcierto);

        // 2. BUSCAR EL BOTÓN CANCELAR (Asegúrate de que este ID sea el mismo que en tu XML)
        btnCancelar = findViewById(R.id.btnVolverAdmin);

        connectionClass = new ConnectionClass();

        // 3. CONFIGURAR EVENTO CANCELAR
        btnCancelar.setOnClickListener(v -> {
            // Cerramos la actividad sin hacer nada
            finish();
        });

        // Configurar el evento de guardar
        btnGuardar.setOnClickListener(v -> guardarNuevoConcierto());
    }

    private void guardarNuevoConcierto() {
        String nom = etNombre.getText().toString().trim();
        String art = etArtista.getText().toString().trim();
        String ciu = etCiudad.getText().toString().trim();

        if (nom.isEmpty() || art.isEmpty() || ciu.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desactivamos el botón para evitar que el usuario pulse mil veces mientras guarda
        btnGuardar.setEnabled(false);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    String sql = "INSERT INTO concierto (nombre, artista, ciudad) VALUES (?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, nom);
                    ps.setString(2, art);
                    ps.setString(3, ciu);

                    ps.executeUpdate();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Concierto añadido con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                    ps.close();
                    con.close();
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnGuardar.setEnabled(true);
                });
            }
        });
    }
}