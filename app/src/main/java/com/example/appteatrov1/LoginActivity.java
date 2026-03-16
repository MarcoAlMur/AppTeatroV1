package com.example.appteatrov1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView btnLogin, btnVolver;
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnVolver = findViewById(R.id.btnVolver);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        connectionClass = new ConnectionClass();

        btnLogin.setOnClickListener(v -> loginUsuario());

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        TextView tvOlvidar = findViewById(R.id.tvOlvidarPass);
        if (tvOlvidar != null) {
            tvOlvidar.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, CambiarContrasenaActivity.class);
                startActivity(intent);
            });
        }

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
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
                    return;
                }

                String sql = "SELECT nombre, rol FROM usuario WHERE email = ? AND contrasena = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String nombreUsuario = rs.getString("nombre");
                    String rolUsuario = rs.getString("rol");

                    SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nombre", nombreUsuario);
                    editor.putString("rol", rolUsuario);
                    editor.putString("email", email);
                    editor.apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if (rolUsuario != null && rolUsuario.equalsIgnoreCase("ADMIN")) {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, ClienteActivity.class);
                        }
                        startActivity(intent);
                        finish();
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
                        Toast.makeText(this, "Error de acceso", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
