package com.example.appteatrov1;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class ConciertoAdapterClass extends RecyclerView.Adapter<ConciertoAdapterClass.ViewHolder> {

    private List<ConciertoClass> lista;

    public ConciertoAdapterClass(List<ConciertoClass> lista) {
        this.lista = lista;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_concierto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConciertoClass c = lista.get(position);
        holder.tvNombre.setText(c.getNombre());
        holder.tvArtista.setText("Artista: " + c.getArtista());
        holder.tvCiudad.setText("Ciudad: " + c.getCiudad());

        // ⭐ SUBRAYADO elegante en "Ver sesiones disponibles"
        holder.btnVerSesiones.setPaintFlags(
                holder.btnVerSesiones.getPaintFlags()
                        | android.graphics.Paint.UNDERLINE_TEXT_FLAG
        );

        holder.btnVerSesiones.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SesionesActivity.class);

            intent.putExtra("id_concierto", c.getId());
            intent.putExtra("nombre_concierto", c.getNombre());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvArtista, tvCiudad;
        Button btnVerSesiones;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvArtista = itemView.findViewById(R.id.tvArtista);
            tvCiudad = itemView.findViewById(R.id.tvCiudad);
            btnVerSesiones = itemView.findViewById(R.id.btnVerSesiones);
        }
    }
}
