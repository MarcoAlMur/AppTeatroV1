package com.example.appteatrov1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SesionAdminAdapter extends RecyclerView.Adapter<SesionAdminAdapter.ViewHolder> {

    private List<SesionClass> lista;
    private OnSesionClickListener listener;
    private String rolUsuario; // <-- Añadimos el rol

    public interface OnSesionClickListener {
        void onEliminarClick(int id, int posicion);
        void onEditarClick(SesionClass sesion);
        void onSesionClick(SesionClass sesion); // <-- Para que el cliente pueda seleccionar la sesión
    }

    // Modificamos el constructor para recibir el rol
    public SesionAdminAdapter(List<SesionClass> lista, String rolUsuario, OnSesionClickListener listener) {
        this.lista = lista;
        this.rolUsuario = rolUsuario;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sesion_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SesionClass sesion = lista.get(position);
        holder.tvFecha.setText(sesion.getFecha());
        holder.tvHora.setText(sesion.getHora());

        // LÓGICA DE VISIBILIDAD SEGÚN ROL
        if (rolUsuario != null && rolUsuario.equalsIgnoreCase("ADMIN")) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEditar.setVisibility(View.VISIBLE);

            holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(sesion.getId(), position));
            holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(sesion));
        } else {
            // Si es CLIENTE, escondemos los botones de edición
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnEditar.setVisibility(View.GONE);

            // Al hacer clic en la fila completa, el cliente va a comprar/ver butacas
            holder.itemView.setOnClickListener(v -> listener.onSesionClick(sesion));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora;
        ImageButton btnEliminar, btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            btnEliminar = itemView.findViewById(R.id.btnEliminarSesion);
            btnEditar = itemView.findViewById(R.id.btnEditarSesion);
        }
    }
}