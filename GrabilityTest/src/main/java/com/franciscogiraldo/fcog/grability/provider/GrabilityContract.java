package com.franciscogiraldo.fcog.grability.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fcog on 9/10/15.
 */
public class GrabilityContract {
    /**
     * Autoridad del Content Provider
     */
    public final static String AUTHORITY = "com.franciscogiraldo.grability";
    /**
     * Representación de la tabla a consultar
     */
    public static final String APP = "app";

    /**
     * Tipo MIME que retorna la consulta de una sola fila
     */
    public final static String SINGLE_MIME1 = "vnd.android.cursor.item/vnd." + AUTHORITY + APP;
    /**
     * Tipo MIME que retorna la consulta de URI
     */
    public final static String MULTIPLE_MIME1 = "vnd.android.cursor.dir/vnd." + AUTHORITY + APP;
    /**
     * URI de contenido principal
     */
    public final static Uri CONTENT_URI1 = Uri.parse("content://" + AUTHORITY + "/" + APP);
    /**
     * Comparador de URIs de contenido
     */
    public static final UriMatcher uriMatcher;
    /**
     * Código para URIs
     */
    public static final int APP_ALLROWS = 1;
    public static final int APP_SINGLE_ROW = 2;

    // Asignación de URIs
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, APP, APP_ALLROWS);
        uriMatcher.addURI(AUTHORITY, APP + "/#", APP_SINGLE_ROW);
    }

    // Valores para la columna ESTADO
    public static final int ESTADO_OK = 0;
    public static final int ESTADO_SYNC = 1;


    /**
     * Estructura de la tabla
     */
    public static class Columnas implements BaseColumns {

        private Columnas() {
            // Sin instancias
        }

        //columnas de tabla promocion
        public final static String APP_ID = "app_id";
        public final static String TITULO = "titulo";
        public final static String IMAGEN = "imagen";
        public final static String DESCRIPCION = "descripcion";
        public final static String PRECIO = "precio";
        public final static String ENLACE = "enlace";
        public final static String CATEGORIA = "categoria";
        public final static String FAVORITA = "favorita";
        public final static String NUEVA = "nueva";

        public static final String ESTADO = "estado";
    }
}
