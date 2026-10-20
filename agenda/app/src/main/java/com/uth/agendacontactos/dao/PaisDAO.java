package com.uth.agendacontactos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uth.agendacontactos.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

/** DAO de países (punto extra: agregar países desde la app). */
public class PaisDAO {

    private final DBHelper helper;

    public PaisDAO(Context context) {
        helper = new DBHelper(context);
    }

    /** Devuelve los países con el formato "Honduras (504)". */
    public List<String> listar() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.query(DBHelper.T_PAISES, null, null, null, null, null, DBHelper.P_ID + " ASC");
        try {
            while (cur.moveToNext()) {
                String nombre = cur.getString(cur.getColumnIndexOrThrow(DBHelper.P_NOMBRE));
                String codigo = cur.getString(cur.getColumnIndexOrThrow(DBHelper.P_CODIGO));
                lista.add(nombre + " (" + codigo + ")");
            }
        } finally {
            cur.close();
            db.close();
        }
        return lista;
    }

    /** @return true si se insertó; false si el país ya existía. */
    public boolean insertar(String nombre, String codigo) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.P_NOMBRE, nombre);
            cv.put(DBHelper.P_CODIGO, codigo);
            return db.insert(DBHelper.T_PAISES, null, cv) != -1;
        } finally {
            db.close();
        }
    }
}
