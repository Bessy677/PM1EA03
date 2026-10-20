package com.uth.agendacontactos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uth.agendacontactos.db.DBHelper;
import com.uth.agendacontactos.model.Contacto;

import java.util.ArrayList;
import java.util.List;

/** DAO: único punto de acceso a la tabla de contactos en SQLite. */
public class ContactoDAO {

    private final DBHelper helper;

    public ContactoDAO(Context context) {
        helper = new DBHelper(context);
    }

    private ContentValues aValores(Contacto c) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.C_PAIS, c.getPais());
        cv.put(DBHelper.C_NOMBRE, c.getNombre());
        cv.put(DBHelper.C_TELEFONO, c.getTelefono());
        cv.put(DBHelper.C_NOTA, c.getNota());
        cv.put(DBHelper.C_IMAGEN, c.getImagen());
        return cv;
    }

    private Contacto desdeCursor(Cursor cur) {
        return new Contacto(
                cur.getInt(cur.getColumnIndexOrThrow(DBHelper.C_ID)),
                cur.getString(cur.getColumnIndexOrThrow(DBHelper.C_PAIS)),
                cur.getString(cur.getColumnIndexOrThrow(DBHelper.C_NOMBRE)),
                cur.getString(cur.getColumnIndexOrThrow(DBHelper.C_TELEFONO)),
                cur.getString(cur.getColumnIndexOrThrow(DBHelper.C_NOTA)),
                cur.getString(cur.getColumnIndexOrThrow(DBHelper.C_IMAGEN)));
    }

    public long insertar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            return db.insert(DBHelper.T_CONTACTOS, null, aValores(c));
        } finally {
            db.close();
        }
    }

    public int actualizar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            return db.update(DBHelper.T_CONTACTOS, aValores(c),
                    DBHelper.C_ID + "=?", new String[]{String.valueOf(c.getId())});
        } finally {
            db.close();
        }
    }

    public int eliminar(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            return db.delete(DBHelper.T_CONTACTOS,
                    DBHelper.C_ID + "=?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public Contacto obtener(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.query(DBHelper.T_CONTACTOS, null,
                DBHelper.C_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            return cur.moveToFirst() ? desdeCursor(cur) : null;
        } finally {
            cur.close();
            db.close();
        }
    }

    /** Lista los contactos; si {@code filtro} no está vacío, busca por nombre o teléfono. */
    public List<Contacto> listar(String filtro) {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur;
        if (filtro == null || filtro.trim().isEmpty()) {
            cur = db.query(DBHelper.T_CONTACTOS, null, null, null, null, null,
                    DBHelper.C_NOMBRE + " COLLATE NOCASE ASC");
        } else {
            String like = "%" + filtro.trim() + "%";
            cur = db.query(DBHelper.T_CONTACTOS, null,
                    DBHelper.C_NOMBRE + " LIKE ? OR " + DBHelper.C_TELEFONO + " LIKE ?",
                    new String[]{like, like}, null, null,
                    DBHelper.C_NOMBRE + " COLLATE NOCASE ASC");
        }
        try {
            while (cur.moveToNext()) {
                lista.add(desdeCursor(cur));
            }
        } finally {
            cur.close();
            db.close();
        }
        return lista;
    }
}
