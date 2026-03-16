package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import  androidx.recyclerview.widget.LinearLayoutManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClienteActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<ConciertoClass> lista;
    ConciertoAdapterClass adapter;
    ConnectionClass connectionClass;
    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente);

        emailUsuario = getIntent().getStringExtra("email");

        recyclerView = findViewById(R.id.recyclerConciertos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lista = new ArrayList<>();
        adapter = new ConciertoAdapterClass(lista);
        recyclerView.setAdapter(adapter);

        connectionClass = new ConnectionClass();

        Button btnCambiarPass = findViewById(R.id.btnCambiarPass);
        btnCambiarPass.setOnClickListener(v -> {
            Intent intent = new Intent(this, CambiarContrasenaActivity.class);
            intent.putExtra("email", emailUsuario);
            startActivity(intent);
        });

        cargarConciertos();

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(ClienteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //LIMPIA LAS ACTIVIDADES ANTERIORES
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarConciertos() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            try {
                Connection con = connectionClass.CONN();
                if (con != null) {
                    lista.clear();

                    String sql = "SELECT id_concierto, nombre, artista, ciudad, cartel FROM concierto";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id_concierto");
                        String nombre = rs.getString("nombre");
                        String artista = rs.getString("artista");
                        String ciudad = rs.getString("ciudad");
                        String cartel = rs.getString("cartel");

                        lista.add(new ConciertoClass(id, nombre, artista, ciudad, cartel));
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                    rs.close();
                    ps.close();
                    con.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}