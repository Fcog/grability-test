package com.franciscogiraldo.fcog.grability.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase envoltura para el gestor de Bases de datos
 */
class DatabaseHelper extends SQLiteOpenHelper {


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase database) {
        createTable(database); // Crear la tabla "gasto"
    }

    /**
     * Crear tabla en la base de datos
     *
     * @param database Instancia de la base de datos
     */
    private void createTable(SQLiteDatabase database) {
        String cmd1 = "CREATE TABLE " + GrabilityContract.APP + " (" +
                GrabilityContract.Columnas._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GrabilityContract.Columnas.APP_ID + " INTEGER NOT NULL DEFAULT 0, " +
                GrabilityContract.Columnas.TITULO + " TEXT, " +
                GrabilityContract.Columnas.IMAGEN + " TEXT, " +
                GrabilityContract.Columnas.DESCRIPCION + " TEXT, " +
                GrabilityContract.Columnas.CATEGORIA + " TEXT, " +
                GrabilityContract.Columnas.ENLACE + " TEXT, " +
                GrabilityContract.Columnas.PRECIO + " INTEGER NOT NULL DEFAULT 0," +
                GrabilityContract.Columnas.NUEVA + " INTEGER NOT NULL DEFAULT 0," +
                GrabilityContract.Columnas.FAVORITA + " INTEGER NOT NULL DEFAULT 0)";


        database.execSQL(cmd1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + GrabilityContract.APP);
        }
        catch (SQLiteException e) {
        }

        onCreate(db);
    }

}