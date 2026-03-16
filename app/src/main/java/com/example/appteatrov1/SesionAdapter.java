package com.example.appteatrov1;


import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.ViewHolder> {

    private List<SesionClass> lista;
    private Context context;
    private OnSesionClickListener listener;
    private String rol;

    public interface OnSesionClickListener {
        void onEliminarClick(int id, int posicion);
        void onEditarClick(SesionClass sesion);
        void onSesionClick(SesionClass sesion);
    }
    public SesionAdapter(List<SesionClass> lista, Context context, String rol, OnSesionClickListener listener) {

        this.lista = lista;
        this.context = context;
        this.rol = rol;
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

        if ("ADMIN".equalsIgnoreCase(rol)) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(sesion.getId(), position));
            holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(sesion));
            holder.itemView.setOnClickListener(null);
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnEditar.setVisibility(View.GONE);
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
