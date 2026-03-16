package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etEmail, etNuevaPass, etCodigo;
    private int codigoGenerado;
    private Button btnActualizar, btnVolver;
    private ConnectionClass connectionClass;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        etEmail = findViewById(R.id.etEmailRecuperacion);
        etNuevaPass = findViewById(R.id.etNuevaPassword);
        etCodigo = findViewById(R.id.etCodigoVerificacion);
        btnActualizar = findViewById(R.id.btnActualizarPass);
        btnVolver = findViewById(R.id.btnVolverRecuperacion);

        connectionClass = new ConnectionClass();

        // --- SIMULACRO DE ENVÍO DE CORREO ---
        // Generamos un código de 6 dígitos al azar
        codigoGenerado = new Random().nextInt(900000) + 100000;

        // Lo mostramos en un Toast para que el usuario sepa cuál es (Simulando el SMS/Email)
        Toast.makeText(this, "Código de verificación enviado: " + codigoGenerado, Toast.LENGTH_LONG).show();

        btnActualizar.setOnClickListener(v -> procesoRecuperacion());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void procesoRecuperacion() {
        String emailInput = etEmail.getText().toString().trim();
        String passwordInput = etNuevaPass.getText().toString().trim();
        String codigoInput = etCodigo.getText().toString().trim();

        // 1. Validar campos vacíos
        if (emailInput.isEmpty() || passwordInput.isEmpty() || codigoInput.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        // 2. VALIDACIÓN DEL CÓDIGO DE SEGURIDAD
        if (!codigoInput.equals(String.valueOf(codigoGenerado))) {
            Toast.makeText(this, "El código de verificación es incorrecto", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    // Verificar si el usuario existe
                    String sqlCheck = "SELECT email FROM usuario WHERE email = ?";
                    PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                    psCheck.setString(1, emailInput);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        // 3. Si el código fue correcto y el email existe, actualizamos
                        String sqlUpdate = "UPDATE usuario SET contraseña = ? WHERE email = ?";
                        PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                        psUpdate.setString(1, passwordInput);
                        psUpdate.setString(2, emailInput);
                        psUpdate.executeUpdate();


                    runOnUiThread(() -> {
                            Toast.makeText(this, "Contraseña restablecida con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                        );
                    }
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error en la base de datos", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}