package com.example.appteatrov1;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConciertoAdminAdapter extends RecyclerView.Adapter<ConciertoAdminAdapter.ViewHolder> {

    private List<ConciertoClass> lista;
    private OnEliminarClickListener listener;

    // Interfaz para pasar el evento de clic a la Activity
    public interface OnEliminarClickListener {
        void onEliminarClick(int id, int posicion);
    }

    public ConciertoAdminAdapter(List<ConciertoClass> lista, OnEliminarClickListener listener) {
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
            if (resId != 0) {
                holder.imgCartel.setImageResource(resId);
            } else {
                holder.imgCartel.setImageDrawable(null);
            }
        } else {
            holder.imgCartel.setImageDrawable(null);
        }

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                int posicionActual = holder.getBindingAdapterPosition();
                if (posicionActual != RecyclerView.NO_POSITION) {
                    listener.onEliminarClick(
                            lista.get(posicionActual).getId(),
                            posicionActual
                    );
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvArtista;
        Button btnEliminar;
        ImageView imgCartel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreAdmin);
            tvArtista = itemView.findViewById(R.id.tvArtistaAdmin);
            btnEliminar = itemView.findViewById(R.id.btnEliminarConcierto);
            imgCartel = itemView.findViewById(R.id.imgCartelAdmin);
        }
    }
}