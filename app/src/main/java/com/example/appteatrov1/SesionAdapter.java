package com.example.appteatrov1;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.ViewHolder> {

    private List<SesionClass> lista;

    public SesionAdapter(List<SesionClass> lista) {
        this.lista = lista;
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
