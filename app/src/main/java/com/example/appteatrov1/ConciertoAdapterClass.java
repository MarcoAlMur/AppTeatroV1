package com.example.appteatrov1;

import android.content.Intent;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import java.util.List;
public class ConciertoAdapterClass extends RecyclerView.Adapter<ConciertoAdapterClass.ViewHolder> {

    private List<ConciertoClass> lista;

    public ConciertoAdapterClass(List<ConciertoClass> lista) {
        this.lista = lista;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_concierto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConciertoClass c = lista.get(position);
        Context context = holder.itemView.getContext();
        holder.tvNombre.setText(c.getNombre());
        holder.tvArtista.setText("Artista: " + c.getArtista());
        holder.tvCiudad.setText("Ciudad: " + c.getCiudad());

        String nombreImagen = c.getCartel();
        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            int resId = context.getResources().getIdentifier(nombreImagen, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imgCartel.setImageResource(resId);
            }
        }

        holder.btnVerSesiones.setPaintFlags(holder.btnVerSesiones.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        holder.btnVerSesiones.setOnClickListener(v -> {
            Intent intent = new Intent(context, SesionesActivity.class);

            intent.putExtra("id_concierto", c.getId());
            intent.putExtra("nombre_concierto", c.getNombre());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvArtista, tvCiudad;
        Button btnVerSesiones;
        ImageView imgCartel;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvArtista = itemView.findViewById(R.id.tvArtista);
            tvCiudad = itemView.findViewById(R.id.tvCiudad);
            btnVerSesiones = itemView.findViewById(R.id.btnVerSesiones);
            imgCartel = itemView.findViewById(R.id.imgCartel);
        }
    }
}
