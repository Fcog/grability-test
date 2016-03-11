package com.franciscogiraldo.fcog.grability.web;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.franciscogiraldo.fcog.grability.provider.GrabilityContract;
import com.franciscogiraldo.fcog.grability.utils.Constantes;

import java.util.ArrayList;

/**
 * Created by fcog on 9/11/15.
 */
public class App {
    public int app_id;
    public String imagen;
    public String titulo;
    public String descripcion;
    public String categoria;
    public String enlace;
    public int precio;
    public int favorita;

    public static ArrayList<App> app_all;

    public App(
            int app_id,
            String titulo,
            String imagen,
            String descripcion,
            String categoria,
            String enlace,
            int precio
    ) {
        this.app_id = app_id;
        this.titulo = titulo;
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.enlace = enlace;
        this.precio = precio;
    }

    public App(ContentResolver resolver, int app_id){

        // Consultar registros locales actuales de promocion
        Uri uri = GrabilityContract.CONTENT_URI1;

        String select = GrabilityContract.Columnas.APP_ID + "=" + app_id;

        Cursor c = resolver.query(uri, Constantes.PROJECTION, select, null, null);

        //assert c != null;
        if (c==null) throw new AssertionError("Object cursor cannot be null");

        while (c.moveToNext()) {

            this.app_id = c.getInt(Constantes.COLUMNA_APP_ID);
            this.titulo = c.getString(Constantes.COLUMNA_TITULO);
            this.imagen = c.getString(Constantes.COLUMNA_IMAGEN);
            this.descripcion = c.getString(Constantes.COLUMNA_DESCRIPCION);
            this.categoria = c.getString(Constantes.COLUMNA_CATEGORIA);
            this.enlace = c.getString(Constantes.COLUMNA_ENLACE);
            this.precio = c.getInt(Constantes.COLUMNA_PRECIO);
            this.favorita = c.getInt(Constantes.COLUMNA_FAVORITA);
        }

        c.close();
    }

    public void AddFavorito(ContentResolver resolver){

        // Consultar registros locales actuales de puntos
        Uri existingUriPunto = GrabilityContract.CONTENT_URI1.buildUpon().appendPath(String.valueOf(this.app_id)).build();

        ContentValues values = new ContentValues();

        if (this.favorita == 0){
            values.put(GrabilityContract.Columnas.FAVORITA, 1);
        }
        else{
            values.put(GrabilityContract.Columnas.FAVORITA, 0);
        }

        resolver.update(existingUriPunto, values, null, null);
    }

    // Consultar registros locales actuales
    public static void loadAll(ContentResolver resolver){

        Uri uri = GrabilityContract.CONTENT_URI1;

        Cursor c = resolver.query(uri, Constantes.PROJECTION, null, null, null);

        app_all = new ArrayList<App>();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

            // The Cursor is now set to the right position
            app_all.add(new App(
                    c.getInt(Constantes.COLUMNA_APP_ID),
                    c.getString(Constantes.COLUMNA_TITULO),
                    c.getString(Constantes.COLUMNA_IMAGEN),
                    c.getString(Constantes.COLUMNA_DESCRIPCION),
                    c.getString(Constantes.COLUMNA_CATEGORIA),
                    c.getString(Constantes.COLUMNA_ENLACE),
                    c.getInt(Constantes.COLUMNA_PRECIO)
            ));
        }

        c.close();
    }

}