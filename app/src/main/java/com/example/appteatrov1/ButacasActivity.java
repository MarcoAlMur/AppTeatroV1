package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.content.Intent;

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
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v ->{
            Intent intent = new Intent(ButacasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
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
             btn.setText(zona + "_" + i + "_" + j);
             btn.setTag(EstadoButaca.DISPONIBLE);
             btn.setBackgroundColor(getColor(R.color.verde_disponible));

             GridLayout.LayoutParams params = new GridLayout.LayoutParams();
             params.width = 160;
             params.height = 160;
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
            btn.setBackgroundColor(getColor(R.color.rojo_bloqueado));
        } else {
            btn.setTag(EstadoButaca.DISPONIBLE);
            btn.setBackgroundColor(getColor(R.color.verde_disponible));
        }
    }
}