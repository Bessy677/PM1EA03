package com.uth.agendacontactos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Crea y versiona la base de datos SQLite de la aplicación. */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "agenda.db";
    public static final int DB_VERSION = 1;

    public static final String T_CONTACTOS = "contactos";
    public static final String C_ID = "id";
    public static final String C_PAIS = "pais";
    public static final String C_NOMBRE = "nombre";
    public static final String C_TELEFONO = "telefono";
    public static final String C_NOTA = "nota";
    public static final String C_IMAGEN = "imagen";

    public static final String T_PAISES = "paises";
    public static final String P_ID = "id";
    public static final String P_NOMBRE = "nombre";
    public static final String P_CODIGO = "codigo";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_CONTACTOS + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_PAIS + " TEXT NOT NULL, "
                + C_NOMBRE + " TEXT NOT NULL, "
                + C_TELEFONO + " TEXT NOT NULL, "
                + C_NOTA + " TEXT NOT NULL, "
                + C_IMAGEN + " TEXT)");

        db.execSQL("CREATE TABLE " + T_PAISES + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NOMBRE + " TEXT NOT NULL UNIQUE, "
                + P_CODIGO + " TEXT NOT NULL)");

        sembrarPais(db, "Honduras", "504");
        sembrarPais(db, "Costa Rica", "506");
        sembrarPais(db, "Guatemala", "502");
        sembrarPais(db, "El Salvador", "503");
    }

    private void sembrarPais(SQLiteDatabase db, String nombre, String codigo) {
        ContentValues cv = new ContentValues();
        cv.put(P_NOMBRE, nombre);
        cv.put(P_CODIGO, codigo);
        db.insert(T_PAISES, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_CONTACTOS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PAISES);
        onCreate(db);
    }
}
