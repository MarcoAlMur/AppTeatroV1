package com.example.appteatrov1;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
public class ImagenDialog {

    public static void mostrar(Context context, int resId) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_imagen);

        ImageView img = dialog.findViewById(R.id.imgGrande);
        img.setImageResource(resId);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.show();

        img.setOnClickListener(v -> dialog.dismiss());
    }
}
