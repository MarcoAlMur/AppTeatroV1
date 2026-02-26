package com.example.appteatrov1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class CarritoActivity extends AppCompatActivity {

    private int idSesion;
    private ArrayList<Integer> listaButacas;
    private LinearLayout layoutResumen;
    private TextView tvTotal, tvConcierto, tvCiudad, tvFecha;
    private ConnectionClass connectionClass;
    private double total = 0;
    private String nombreConcierto;
    private String ciudadConcierto;
    private String fechaSesion;
    private String horaSesion;
    private ArrayList<String> lineasResumen = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrito);

        layoutResumen = findViewById(R.id.layoutResumen);
        tvTotal = findViewById(R.id.tvTotal);
        tvConcierto = findViewById(R.id.tvConcierto);
        tvCiudad = findViewById(R.id.tvCiudad);
        tvFecha = findViewById(R.id.tvFecha);
        Button btnPagar = findViewById(R.id.btnPagar);
        Button btnVolver = findViewById(R.id.btnVolver);

        idSesion = getIntent().getIntExtra("id_sesion", -1);
        listaButacas = getIntent().getIntegerArrayListExtra("butaca");


        if (listaButacas == null || listaButacas.isEmpty()) {
            Toast.makeText(this, "No hay butacas seleccionadas", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectionClass= new ConnectionClass();

        cargarResumen();

        btnPagar.setOnClickListener(v->relizarVenta());
        btnVolver.setOnClickListener(v->finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarResumen() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            try {
                Connection con = connectionClass.CONN();
                total = 0;
                lineasResumen.clear();

                runOnUiThread(() -> layoutResumen.removeAllViews());

                String infoSesionSQL = "SELECT s.fecha, s.hora, c.nombre AS concierto, c.artista, c.ciudad " +
                        "FROM sesion s JOIN concierto c ON s.id_concierto = c.id_concierto " +
                        "WHERE s.id_sesion = ?";
                PreparedStatement psInfo = con.prepareStatement(infoSesionSQL);
                psInfo.setInt(1, idSesion);
                ResultSet rsInfo = psInfo.executeQuery();

                if (rsInfo.next()) {
                    String concierto = rsInfo.getString("concierto");
                    String artista = rsInfo.getString("artista");
                    String ciudad = rsInfo.getString("ciudad");
                    String fecha = rsInfo.getString("fecha");
                    String hora = rsInfo.getString("hora");

                    nombreConcierto = concierto + " - " + artista;
                    ciudadConcierto = ciudad;
                    fechaSesion = fecha;
                    horaSesion = hora;

                    runOnUiThread(() ->{
                        tvConcierto.setText(concierto + " - " + artista);
                        tvCiudad.setText(ciudad);
                        tvFecha.setText(fecha + "-" + hora);
                    });
                }
                rsInfo.close();
                psInfo.close();

                for (int idButaca : listaButacas) {
                    String sql = "SELECT b.fila, b.numero, z.nombre AS zona, z.precio " +
                            "FROM butaca b " +
                            "JOIN zona z ON b.id_zona = z.id_zona " +
                            "WHERE b.id_butaca = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, idButaca);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        String fila = rs.getString("fila");
                        int numero = rs.getInt("numero");
                        String zona = rs.getString("zona");
                        double precio = rs.getDouble("precio");

                        total += precio;

                        String lineaTexto = "Butaca " + fila + "-" + numero + " | Zona: " + zona + " | Precio " + precio + "€";
                        lineasResumen.add(lineaTexto);
                        runOnUiThread(() ->{
                            TextView tv = new TextView(this);
                            tv.setText(lineaTexto);
                            layoutResumen.addView(tv);
                        });
                    }
                    rs.close();
                    ps.close();
                }
                runOnUiThread(() ->
                        tvTotal.setText("Total: " + total + "€")
                );
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void relizarVenta() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = connectionClass.CONN();

                String sqlVenta = "INSERT INTO venta (fecha_venta, total) VALUES (NOW(), ?)";
                PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
                psVenta.setDouble(1, total);
                psVenta.executeUpdate();

                ResultSet rs = psVenta.getGeneratedKeys();
                rs.next();
                int idVentaGenerada = rs.getInt(1);

                rs.close();
                psVenta.close();

                String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_sesion, id_butaca, precio) VALUES (?, ?, ?, ?)";
                PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);

                String updateEstado = "UPDATE butaca_sesion SET estado = 'VENDIDA' WHERE id_sesion = ? AND id_butaca = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateEstado);

                for (int idButaca : listaButacas) {
                    String precioQuery = "SELECT z.precio FROM butaca b JOIN zona z ON b.id_zona = z.id_zona WHERE b.id_butaca = ?";
                    PreparedStatement psPrecio = con.prepareStatement(precioQuery);
                    psPrecio.setInt(1, idButaca);
                    ResultSet rsPrecio = psPrecio.executeQuery();
                    rsPrecio.next();
                    double precio = rsPrecio.getDouble("precio");

                    rsPrecio.close();
                    psPrecio.close();

                    psDetalle.setInt(1, idVentaGenerada);
                    psDetalle.setInt(2, idSesion);
                    psDetalle.setInt(3, idButaca);
                    psDetalle.setDouble(4, precio);
                    psDetalle.executeUpdate();

                    psUpdate.setInt(1, idSesion);
                    psUpdate.setInt(2, idButaca);
                    psUpdate.executeUpdate();

                }
                psDetalle.close();
                psUpdate.close();
                con.close();

                runOnUiThread(() -> {
                    Intent intent = new Intent(CarritoActivity.this, CompraRealizadaActivity.class);
                    intent.putExtra("id_venta", idVentaGenerada);
                    intent.putExtra("id_sesion", idSesion);
                    intent.putExtra("total", total);
                    intent.putIntegerArrayListExtra("butacas", listaButacas);
                    intent.putExtra("concierto", nombreConcierto);
                    intent.putExtra("ciudad", ciudadConcierto);
                    intent.putExtra("fechaSesion", fechaSesion);
                    intent.putExtra("horaSesion", horaSesion);
                    intent.putStringArrayListExtra("resumenButacas", lineasResumen);
                    startActivity(intent);
                    finish();

                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
}