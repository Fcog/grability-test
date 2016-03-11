package com.franciscogiraldo.fcog.grability.utils;

import com.franciscogiraldo.fcog.grability.provider.GrabilityContract;

/**
 * Created by fcog on 9/11/15.
 */
public class Constantes {

    /**
     * Dirección IP de genymotion o AVD
     */

    /**
     * URLs del Web Service
     */
    //public static final String GET_URL = API_URL + "datos2.php";
    public static final String API_URL = "https://itunes.apple.com/us/rss/topfreeapplications/limit=20/json";
    /**
     * URLs de registro para GSM
     */
    public static final String REGISTER_GCM_URL = "http://199.195.116.112/EP/clubselecta/gcm/register.php";


    /**
     * Tipo de cuenta para la sincronización
     */
    public static final String ACCOUNT_TYPE = "com.franciscogiraldo.grability.account";


    /**
     * Proyección para las consultas
     */
    public static final String[] PROJECTION = new String[]{
            GrabilityContract.Columnas._ID,
            GrabilityContract.Columnas.APP_ID,
            GrabilityContract.Columnas.TITULO,
            GrabilityContract.Columnas.IMAGEN,
            GrabilityContract.Columnas.DESCRIPCION,
            GrabilityContract.Columnas.CATEGORIA,
            GrabilityContract.Columnas.ENLACE,
            GrabilityContract.Columnas.PRECIO,
            GrabilityContract.Columnas.NUEVA,
            GrabilityContract.Columnas.FAVORITA
    };


    // Indices para las columnas indicadas en la proyección
    public static final int COLUMNA_APP_ID = 1;
    public static final int COLUMNA_TITULO = 2;
    public static final int COLUMNA_IMAGEN = 3;
    public static final int COLUMNA_DESCRIPCION = 4;
    public static final int COLUMNA_CATEGORIA = 5;
    public static final int COLUMNA_ENLACE = 6;
    public static final int COLUMNA_PRECIO = 7;
    public static final int COLUMNA_NUEVA = 8;
    public static final int COLUMNA_FAVORITA = 9;


    //preferencias para Google Cloud Messanging
    public static final String GCM_SENDERID = "786723231556";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public static final String CONFIG_NOTIFICACIONES = "checkbox_preference";

    public static final String PRIMERA_VEZ = "primera_vez";
    public static final String DEFAULT_VIEW = "default_view";
    public static final int LIST = 0;
    public static final int GRID = 1;
}
