package com.example.appteatrov1;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.YuvImage;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CompraRealizadaActivity extends AppCompatActivity {
    private LinearLayout layoutResumen;
    private TextView tvTotal, tvConcierto, tvCiudad, tvFecha, tvUsuario;
    private Button btnDescargar, btnVolver;
    private double total;
    private ArrayList<Integer> listaButacas;
    private ArrayList<String> resumenButacas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compra_realizada);

        layoutResumen = findViewById(R.id.layoutResumen);
        tvTotal = findViewById(R.id.tvTotal);
        tvConcierto = findViewById(R.id.tvConcierto);
        tvCiudad = findViewById(R.id.tvCiudad);
        tvFecha = findViewById(R.id.tvFecha);
        tvUsuario = findViewById(R.id.tvUsuario);

        Intent intent = getIntent();
        String concierto = intent.getStringExtra("concierto");
        String ciudad = intent.getStringExtra("ciudad");
        String fechaSesion = intent.getStringExtra("fechaSesion");
        String horaSesion = intent.getStringExtra("horaSesion");
        listaButacas = intent.getIntegerArrayListExtra("butacas");
        resumenButacas = intent.getStringArrayListExtra("resumenButacas");
        total = intent.getDoubleExtra("total", 0);

        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombre", "Usuario");

        tvUsuario.setText("Usuario: " + nombreUsuario);
        tvConcierto.setText("Concierto: " + concierto);
        tvCiudad.setText("Ciudad: " + ciudad);
        tvFecha.setText("Fecha y hora: " + fechaSesion + "-" + horaSesion);
        tvTotal.setText("Total: " + total + "€");

        layoutResumen.removeAllViews();
        if (resumenButacas != null) {
            for (String linea : resumenButacas) {
                TextView tv = new TextView(this);
                tv.setText(linea);
                tv.setTextSize(16f);
                layoutResumen.addView(tv);
            }
        }
        /*if (listaButacas != null) {
            for (Integer butaca : listaButacas) {
                TextView tv = new TextView(this);
                tv.setText("Butaca: " + butaca);
                tv.setTextSize(16f);
                tv.setTextColor(Color.DKGRAY);
                layoutResumen.addView(tv);
            }
        }*/

        btnDescargar = findViewById(R.id.btnDescargar);
        btnDescargar.setOnClickListener(v -> generarPDF(concierto, ciudad, fechaSesion, horaSesion, nombreUsuario));

        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Intent intentVolver = new Intent(CompraRealizadaActivity.this, ClienteActivity.class);
            intentVolver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentVolver);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void generarPDF(String concierto, String  ciudad, String fechaSesion, String horaSesion, String nombreUsuario) {
        try {
           PdfDocument document = new PdfDocument();
           PdfDocument.PageInfo pageInfo =
                   new PdfDocument.PageInfo.Builder(595, 842, 1).create();
           PdfDocument.Page page = document.startPage(pageInfo);

           Paint paint = new Paint();
           int y = 50;
           paint.setTextSize(24f);
           paint.setFakeBoldText(true);
           page.getCanvas().drawText("Recibo de Compra", 200, y, paint);
           y += 40;

           paint.setTextSize(18f);
           paint.setFakeBoldText(false);
           page.getCanvas().drawText("Usuario: " + nombreUsuario, 50, y, paint);
           y += 25;
           page.getCanvas().drawText("Concierto: " + concierto,50, y, paint);
           y += 25;
           page.getCanvas().drawText("Ciudad: " + ciudad, 50, y, paint);
           y += 25;
           page.getCanvas().drawText("Fecha y hora: " + fechaSesion + "-" + horaSesion, 50, y, paint);
           y += 40;

           paint.setTextSize(16f);
           paint.setColor(Color.BLACK);
            for (String linea : resumenButacas) {
                page.getCanvas().drawText(linea, 50, y, paint);
                y += 25;
            }
            /*if (listaButacas != null) {
                for (Integer butaca : listaButacas) {
                    page.getCanvas().drawText("Butaca: " + butaca, 50, y, paint);
                    y += 20;
                }
            }*/
           y += 20;
           paint.setTextSize(18f);
           paint.setFakeBoldText(true);
           page.getCanvas().drawText("Total: " + total + "€", 50, y, paint);

           document.finishPage(page);

           String nombreArchivo = "entrada_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    nombreArchivo
            );

           document.writeTo(new FileOutputStream(file));
           document.close();

           Toast.makeText(this, "PDF guardado en: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

           Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setDataAndType(uri, "application/pdf");
           intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
           startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show();
        }
    }
}