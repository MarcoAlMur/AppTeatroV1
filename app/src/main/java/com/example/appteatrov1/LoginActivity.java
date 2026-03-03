package com.example.appteatrov1;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnVolver;
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicialización de vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnVolver = findViewById(R.id.btnVolver);
        connectionClass = new ConnectionClass();

        // Acción de Login
        btnLogin.setOnClickListener(v -> loginUsuario());

        // Acción de Volver
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // Recuperar contraseña
        TextView tvOlvidar = findViewById(R.id.tvOlvidarPass);
        if (tvOlvidar != null) {
            tvOlvidar.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, CambiarContrasenaActivity.class);
                startActivity(intent);
            });
        }

        // Lógica para mostrar/ocultar contraseña al tocar el icono del ojo
        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etPassword.getCompoundDrawables()[2] != null) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width())) {
                        if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        } else {
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                        etPassword.setSelection(etPassword.getText().length());
                        return true;
                    }
                }
            }
            return false;
        });

        // Ajuste de insets para EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginUsuario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Consulta SQL adaptada a tu tabla 'usuario' (columnas: email, contrasena, nombre, rol)
                String sql = "SELECT nombre, rol FROM usuario WHERE email = ? AND contrasena = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String nombreUsuario = rs.getString("nombre");
                    String rolUsuario = rs.getString("rol");

                    // Guardar los datos de la sesión localmente
                    SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nombre", nombreUsuario);
                    editor.putString("rol", rolUsuario);
                    editor.putString("email", email);
                    editor.apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();

                        // Redirección según el rol obtenido de la base de datos
                        Intent intent;
                        if (rolUsuario != null && rolUsuario.equalsIgnoreCase("ADMIN")) {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, ClienteActivity.class);
                        }

                        startActivity(intent);
                        finish(); // Cerrar login para que no se pueda volver atrás
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    );
                }
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error crítico: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}