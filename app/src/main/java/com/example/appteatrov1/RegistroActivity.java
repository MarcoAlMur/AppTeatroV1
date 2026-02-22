package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etApellido, etTelefono, etEmail, etPassword;
    Button btnCrearCuenta;
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        connectionClass = new ConnectionClass();

        btnCrearCuenta.setOnClickListener(v -> registarUsuario());

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                        );
                    return;
                }

                String sql = "INSERT INTO usuario (nombre, apellido, telefono, email, contrasena, rol) " +
                        "VALUES (?, ?, ?, ?, ?, 'CLIENTE')";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, apellido);
                ps.setString(3, telefono);
                ps.setString(4, email);
                ps.setString(5, password);
                ps.executeUpdate();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                    finish();
                });

                con.close();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error de registro", Toast.LENGTH_SHORT).show()
                    );
            }
        });
    }
}