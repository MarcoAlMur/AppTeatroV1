package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ButacasActivity extends AppCompatActivity {
    enum EstadoButaca {
        DISPONIBLE,
        BLOQUEADA
    }
    private int idsesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_butacas);
        GridLayout gridPatio = findViewById(R.id.gridPatio);
        GridLayout gridPalco = findViewById(R.id.gridPalco);
        GridLayout gridVip = findViewById(R.id.gridVip);
        crearButacas(gridPatio, 4,4, "Patio");
        crearButacas(gridPalco, 2, 2, "Palco");
        crearButacas(gridVip, 2, 2, "VIP");

        idsesion = getIntent().getIntExtra("id_sesion", -1);
        if (idsesion == -1) {
            Toast.makeText(this, "Error, sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void crearButacas(GridLayout grid, int filas, int columnas, String zona) {

        for(int i = 0; i < filas; i++) {

            for (int j = 0; j < columnas; j++) {

                Button btn = new Button(this);

                // ⭐ TEXTO tipo cine: fila + numero
                char fila = (char) ('A' + i);   // A, B, C...
                int numero = j + 1;

                btn.setText(fila + "" + numero);   // ejemplo A1
                btn.setTextColor(android.graphics.Color.WHITE);
                btn.setTextSize(12);
                btn.setGravity(android.view.Gravity.CENTER);

                btn.setTag(EstadoButaca.DISPONIBLE);

                //  BUTACA LIBRE
                btn.setBackgroundResource(R.drawable.butaca_libre);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 150;
                params.height = 150;
                params.setMargins(8,8,8,8);
                btn.setLayoutParams(params);

                btn.setOnClickListener(v -> cambiarEstado(btn));

                grid.addView(btn);
            }
        }
    }
    private void cambiarEstado(Button btn) {

        EstadoButaca estado = (EstadoButaca) btn.getTag();

        if (estado == EstadoButaca.DISPONIBLE) {
            btn.setTag(EstadoButaca.BLOQUEADA);
            btn.setBackgroundResource(R.drawable.butaca_ocupada);
        } else {
            btn.setTag(EstadoButaca.DISPONIBLE);
            btn.setBackgroundResource(R.drawable.butaca_libre);
        }
    }

}