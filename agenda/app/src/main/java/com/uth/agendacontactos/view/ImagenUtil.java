package com.uth.agendacontactos.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/** Utilidad para cargar fotografías ya reducidas y con la orientación correcta. */
public class ImagenUtil {

    public static Bitmap cargar(String ruta, int ancho, int alto) {
        if (ruta == null) return null;
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, op);
        int muestra = 1;
        while (op.outWidth / (muestra * 2) >= ancho && op.outHeight / (muestra * 2) >= alto) {
            muestra *= 2;
        }
        op.inJustDecodeBounds = false;
        op.inSampleSize = muestra;
        Bitmap bmp = BitmapFactory.decodeFile(ruta, op);
        if (bmp == null) return null;

        int grados = 0;
        try {
            int o = new ExifInterface(ruta).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (o == ExifInterface.ORIENTATION_ROTATE_90) grados = 90;
            else if (o == ExifInterface.ORIENTATION_ROTATE_180) grados = 180;
            else if (o == ExifInterface.ORIENTATION_ROTATE_270) grados = 270;
        } catch (IOException ignorado) { }

        if (grados != 0) {
            Matrix m = new Matrix();
            m.postRotate(grados);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }
        return bmp;
    }
}
