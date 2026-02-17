package com.example.appteatrov1;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.ViewHolder> {

    private List<SesionClass> lista;
    private Context context;

    public SesionAdapter(List<SesionClass> lista, Context context) {

        this.lista = lista;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sesion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SesionClass s = lista.get(position);

        holder.tvFecha.setText("Fecha: " + s.getFecha());
        holder.tvHora.setText("Hora: " + s.getHora());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ButacasActivity.class);
            intent.putExtra("id_sesion", s.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
        }
    }
}
