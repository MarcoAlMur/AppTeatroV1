package com.example.appteatrov1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase especializada en la gestión de bajas de conciertos.
 * Implementa la lógica de borrado en cascada y transaccionalidad.
 */
public class EliminarConcierto {

    private ConnectionClass connectionClass;
    private Context context;

    public EliminarConcierto(Context context) {
        this.context = context;
        this.connectionClass = new ConnectionClass();
    }

    /**
     * Elimina un concierto y todas sus sesiones asociadas de forma atómica.
     * * @idConcierto ID del concierto a eliminar.
     * @posicion Posición del elemento en la lista visual.
     * @adapter Referencia al adaptador para actualizar la UI.
     * @lista Referencia a la lista de datos del RecyclerView.
     */
    public void eliminarConciertoCompleto(int idConcierto, int posicion, RecyclerView.Adapter adapter, List lista) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection con = null;
            try {
                con = connectionClass.CONN();
                if (con != null) {
                    // DESACTIVAR AUTO-COMMIT: Iniciamos la transacción
                    con.setAutoCommit(false);

                    // PASO 1: Eliminar sesiones vinculadas (Integridad Referencial)
                    String sqlSesiones = "DELETE FROM sesion WHERE id_concierto = ?";
                    PreparedStatement ps1 = con.prepareStatement(sqlSesiones);
                    ps1.setInt(1, idConcierto);
                    ps1.executeUpdate();

                    // PASO 2: Eliminar el registro del concierto
                    String sqlConcierto = "DELETE FROM concierto WHERE id_concierto = ?";
                    PreparedStatement ps2 = con.prepareStatement(sqlConcierto);
                    ps2.setInt(1, idConcierto);
                    ps2.executeUpdate();

                    // FINALIZAR TRANSACCIÓN: Aplicar todos los cambios
                    con.commit();

                    // Actualizar la interfaz de usuario en el hilo principal
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Concierto y sesiones eliminados con éxito", Toast.LENGTH_SHORT).show();
                        if (lista.size() > posicion) {
                            lista.remove(posicion);
                            adapter.notifyItemRemoved(posicion);
                        }
                    });

                    ps1.close();
                    ps2.close();
                    con.close();
                }
            } catch (Exception e) {
                // ROLLBACK: Si algo falla, revertimos los cambios para no dejar datos huérfanos
                try { if (con != null) con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }

                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error técnico al eliminar el concierto", Toast.LENGTH_SHORT).show());
            }
        });
    }
}