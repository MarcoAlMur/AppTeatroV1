package com.example.appteatrov1;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
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
import java.util.Locale;

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

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
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
                tv.setTextColor(Color.WHITE);
                layoutResumen.addView(tv);
            }
        }

        btnDescargar = findViewById(R.id.btnDescargar);
        btnDescargar.setOnClickListener(v -> generarPDF(concierto, ciudad, fechaSesion, horaSesion, nombreUsuario));

        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Intent intentVolver = new Intent(CompraRealizadaActivity.this, ClienteActivity.class);
            intentVolver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentVolver);
            finish();
        });

    }

    private void generarPDF(String concierto, String ciudad, String fechaSesion, String horaSesion, String nombreUsuario) {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint fondo = new Paint();
            fondo.setColor(Color.parseColor("#F6F1E8"));
            canvas.drawRect(0, 0, 595, 842, fondo);

            Paint borde = new Paint();
            borde.setColor(Color.parseColor("#D8CBB8"));
            borde.setStyle(Paint.Style.STROKE);
            borde.setStrokeWidth(3f);
            canvas.drawRoundRect(35, 35, 560, 807, 18, 18, borde);

            Paint paintTitulo = new Paint();
            paintTitulo.setColor(Color.parseColor("#1E1E1E"));
            paintTitulo.setTextSize(26f);
            paintTitulo.setFakeBoldText(true);

            Paint paintSub = new Paint();
            paintSub.setColor(Color.parseColor("#7A6A58"));
            paintSub.setTextSize(12f);
            paintSub.setFakeBoldText(true);

            Paint paintLabel = new Paint();
            paintLabel.setColor(Color.parseColor("#8A7B6A"));
            paintLabel.setTextSize(12f);
            paintLabel.setFakeBoldText(true);

            Paint paintTexto = new Paint();
            paintTexto.setColor(Color.parseColor("#222222"));
            paintTexto.setTextSize(15f);

            Paint paintTextoBold = new Paint();
            paintTextoBold.setColor(Color.parseColor("#222222"));
            paintTextoBold.setTextSize(16f);
            paintTextoBold.setFakeBoldText(true);

            Paint separador = new Paint();
            separador.setColor(Color.parseColor("#D8CBB8"));
            separador.setStrokeWidth(2f);

            String referencia = generarReferencia();
            String tipoTicket = obtenerTipoTicket();
            String butacasTexto = obtenerButacasTexto();

            int y = 85;

            drawCenteredText(canvas, "RECIBO DE COMPRA", 297, y, paintTitulo);
            y += 24;
            drawCenteredText(canvas, "APP TEATRO", 297, y, paintSub);

            y += 28;
            canvas.drawLine(65, y, 530, y, separador);
            y += 35;

            canvas.drawText("REFERENCIA", 65, y, paintLabel);
            canvas.drawText(referencia, 180, y, paintTextoBold);
            y += 28;

            canvas.drawText("USUARIO", 65, y, paintLabel);
            canvas.drawText(nombreUsuario, 180, y, paintTexto);
            y += 28;

            canvas.drawText("CONCIERTO", 65, y, paintLabel);
            canvas.drawText(valorSeguro(concierto), 180, y, paintTexto);
            y += 28;

            canvas.drawText("CIUDAD", 65, y, paintLabel);
            canvas.drawText(valorSeguro(ciudad), 180, y, paintTexto);
            y += 28;

            canvas.drawText("FECHA", 65, y, paintLabel);
            canvas.drawText(valorSeguro(fechaSesion), 180, y, paintTexto);
            y += 28;

            canvas.drawText("HORA", 65, y, paintLabel);
            canvas.drawText(valorSeguro(horaSesion), 180, y, paintTexto);
            y += 28;

            canvas.drawText("TIPO TICKET", 65, y, paintLabel);
            canvas.drawText(tipoTicket, 180, y, paintTexto);
            y += 28;

            canvas.drawText("BUTACA", 65, y, paintLabel);
            canvas.drawText(butacasTexto, 180, y, paintTexto);
            y += 28;

            canvas.drawText("TOTAL", 65, y, paintLabel);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f€", total), 180, y, paintTextoBold);

            y += 30;
            canvas.drawLine(65, y, 530, y, separador);

            y += 40;
            Paint caja = new Paint();
            caja.setColor(Color.parseColor("#EFE7DA"));
            canvas.drawRoundRect(65, y, 530, y + 140, 14, 14, caja);

            int detalleY = y + 30;
            canvas.drawText("Detalle de la compra", 85, detalleY, paintTextoBold);
            detalleY += 26;

            if (resumenButacas != null && !resumenButacas.isEmpty()) {
                for (String linea : resumenButacas) {
                    canvas.drawText(limitarTexto(linea, 58), 85, detalleY, paintTexto);
                    detalleY += 22;
                }
            } else {
                canvas.drawText("Sin detalle de butacas", 85, detalleY, paintTexto);
            }

            y += 180;
            canvas.drawLine(65, y, 530, y, separador);

            y += 28;
            drawCenteredText(canvas, "Entrada valida para 1 acceso", 297, y, paintTextoBold);

            y += 28;
            drawFakeBarcode(canvas, 170, y, 255, 78);

            y += 98;
            drawCenteredText(canvas, referencia, 297, y, paintSub);

            y += 34;
            drawCenteredText(canvas, "Presentar en la entrada", 297, y, paintLabel);

            document.finishPage(page);

            String nombreArchivo = "entrada_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                    ".pdf";

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
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

    private String obtenerTipoTicket() {
        if (resumenButacas == null || resumenButacas.isEmpty()) {
            return "General";
        }

        String primeraLinea = resumenButacas.get(0);

        if (primeraLinea.contains("Zona:") && primeraLinea.contains("| Precio")) {
            int inicio = primeraLinea.indexOf("Zona:") + 5;
            int fin = primeraLinea.indexOf("| Precio");
            if (fin > inicio) {
                return primeraLinea.substring(inicio, fin).trim();
            }
        }

        return "General";
    }

    private String obtenerButacasTexto() {
        if (resumenButacas == null || resumenButacas.isEmpty()) {
            return "-";
        }

        String primeraLinea = resumenButacas.get(0);

        if (primeraLinea.contains("|")) {
            return primeraLinea.substring(0, primeraLinea.indexOf("|")).trim();
        }

        return primeraLinea;
    }

    private String generarReferencia() {
        return "AT-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
    }

    private String valorSeguro(String texto) {
        return texto == null ? "-" : texto;
    }

    private String limitarTexto(String texto, int max) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max - 3) + "...";
    }

    private void drawCenteredText(Canvas canvas, String texto, float centroX, float y, Paint paint) {
        float ancho = paint.measureText(texto);
        canvas.drawText(texto, centroX - (ancho / 2), y, paint);
    }

    private void drawFakeBarcode(Canvas canvas, int startX, int startY, int width, int height) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);

        int[] barras = {
                3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 2, 4, 1, 2, 3, 1, 4, 2,
                2, 1, 3, 4, 1, 2, 1, 3, 4, 2, 1, 2, 3, 1, 4, 3, 2, 1, 2, 4
        };

        int x = startX;
        for (int i = 0; i < barras.length; i++) {
            int w = barras[i] * 2;
            if (i % 2 == 0) {
                canvas.drawRect(x, startY, x + w, startY + height, p);
            }
            x += w;
            if (x > startX + width) {
                break;
            }
        }
    }
}