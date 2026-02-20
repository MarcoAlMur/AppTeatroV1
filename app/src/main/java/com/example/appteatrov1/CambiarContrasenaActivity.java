package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etEmail, etNuevaPass;
    private Button btnActualizar, btnVolver;
    private ConnectionClass connectionClass;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        etEmail = findViewById(R.id.etEmailRecuperacion);
        etNuevaPass = findViewById(R.id.etNuevaPassword);
        btnActualizar = findViewById(R.id.btnActualizarPass);
        btnVolver = findViewById(R.id.btnVolverRecuperacion);

        connectionClass = new ConnectionClass();

        // Si venimos desde ClienteActivity, el email ya estará en el Intent
        String emailRecibido = getIntent().getStringExtra("email");
        if (emailRecibido != null) {
            etEmail.setText(emailRecibido);
            etEmail.setEnabled(false); // No dejamos cambiar el email si ya está logueado
        }

        btnActualizar.setOnClickListener(v -> validarYActualizar());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void validarYActualizar() {
        String email = etEmail.getText().toString().trim();
        String nuevaPass = etNuevaPass.getText().toString().trim();

        if (email.isEmpty() || nuevaPass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    // 1. Verificamos si el usuario existe
                    String checkSql = "SELECT id_usuario FROM usuarios WHERE email = ?";
                    PreparedStatement checkPs = con.prepareStatement(checkSql);
                    checkPs.setString(1, email);
                    ResultSet rs = checkPs.executeQuery();

                    if (rs.next()) {
                        // 2. Si existe, actualizamos la contraseña
                        String updateSql = "UPDATE usuarios SET password = ? WHERE email = ?";
                        PreparedStatement updatePs = con.prepareStatement(updateSql);
                        updatePs.setString(1, nuevaPass);
                        updatePs.setString(2, email);
                        updatePs.executeUpdate();

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show());
                    }
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}