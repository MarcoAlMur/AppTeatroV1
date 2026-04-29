package com.example.appteatrov1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConciertoAdminAdapter extends RecyclerView.Adapter<ConciertoAdminAdapter.ViewHolder> {

    private List<ConciertoClass> lista;
    private OnConciertoClickListener listener;

    // Interfaz actualizada para gestionar tanto la navegación como la eliminación
    public interface OnConciertoClickListener {
        void onConciertoClick(ConciertoClass concierto);
        void onEliminarClick(int id, int posicion);
    }

    public ConciertoAdminAdapter(List<ConciertoClass> lista, OnConciertoClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_concierto_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConciertoClass concierto = lista.get(position);
        holder.tvNombre.setText(concierto.getNombre());
        holder.tvArtista.setText(concierto.getArtista());

        String nombreImagen = concierto.getCartel();
        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            int resId = holder.itemView.getContext()
                    .getResources()
                    .getIdentifier(nombreImagen, "drawable", holder.itemView.getContext().getPackageName());
            holder.imgCartel.setImageResource(resId != 0 ? resId : android.R.color.transparent);
        }

        // ACCIÓN 1: Ir a la gestión de sesiones
        holder.btnGestionar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConciertoClick(concierto);
            }
        });

        // ACCIÓN 2: Eliminar concierto (Nueva funcionalidad)
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(concierto.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvArtista;
        Button btnGestionar, btnEliminar; // Ambos definidos como Button según el XML nuevo
        ImageView imgCartel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // IMPORTANTE: Todos llevan itemView.findViewById para evitar errores
            tvNombre = itemView.findViewById(R.id.tvNombreAdmin);
            tvArtista = itemView.findViewById(R.id.tvArtistaAdmin);
            btnGestionar = itemView.findViewById(R.id.btnGestionarSesiones);
            imgCartel = itemView.findViewById(R.id.imgCartelAdmin);
            btnEliminar = itemView.findViewById(R.id.btnEliminarConciertoItem);
        }
    }
}