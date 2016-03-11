package com.franciscogiraldo.fcog.grability.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by fcog on 9/11/15.
 */
public class GrabilityProvider extends ContentProvider {
    /**
     * Nombre de la base de datos
     */
    private static final String DATABASE_NAME = "grability.db";
    /**
     * Versión actual de la base de datos
     */
    private static final int DATABASE_VERSION = 1;
    /**
     * Instancia global del Content Resolver
     */
    private ContentResolver resolver;
    /**
     * Instancia del administrador de BD
     */
    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {

        // Inicializando gestor BD
        databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);

        resolver = getContext().getContentResolver();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Obtener base de datos
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Comparar Uri
        int match = GrabilityContract.uriMatcher.match(uri);

        Cursor c;

        switch (match) {

            case GrabilityContract.APP_ALLROWS:

                // Consultando todos los registros
                c = db.query(GrabilityContract.APP, projection, selection, selectionArgs, null, null, sortOrder);

                c.setNotificationUri(resolver, GrabilityContract.CONTENT_URI1);

                break;

            case GrabilityContract.APP_SINGLE_ROW:

                // Consultando un solo registro basado en el Id del Uri
                long promocion_id = ContentUris.parseId(uri);

                c = db.query(GrabilityContract.APP, projection, GrabilityContract.Columnas.APP_ID + " = " + promocion_id, selectionArgs, null, null, sortOrder);

                c.setNotificationUri(resolver, GrabilityContract.CONTENT_URI1);

                break;


            default:
                throw new IllegalArgumentException("URI no soportada: " + uri);
        }
        return c;

    }

    @Override
    public String getType(Uri uri) {

        switch (GrabilityContract.uriMatcher.match(uri)) {

            case GrabilityContract.APP_ALLROWS:
                return GrabilityContract.MULTIPLE_MIME1;

            case GrabilityContract.APP_SINGLE_ROW:
                return GrabilityContract.SINGLE_MIME1;

            default:
                throw new IllegalArgumentException("Tipo de gasto desconocido: " + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (GrabilityContract.uriMatcher.match(uri)) {

            case GrabilityContract.APP_ALLROWS:

                // Inserción de nueva fila en BD
                long row_promocion_id = db.insert(GrabilityContract.APP, null, values);

                if (row_promocion_id > 0) {
                    Uri uri_promocion = ContentUris.withAppendedId(GrabilityContract.CONTENT_URI1, row_promocion_id);

                    resolver.notifyChange(uri_promocion, null, false);

                    return uri_promocion;
                }
                break;
        }

        throw new SQLException("Falla al insertar fila en : " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int match = GrabilityContract.uriMatcher.match(uri);
        int affected;

        switch (match) {

            case GrabilityContract.APP_ALLROWS:

                affected = db.delete(GrabilityContract.APP, selection, selectionArgs);
                break;

            case GrabilityContract.APP_SINGLE_ROW:

                long promocion_id = ContentUris.parseId(uri);

                affected = db.delete(
                        GrabilityContract.APP,
                        GrabilityContract.Columnas.APP_ID + "=" + promocion_id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs
                );

                // Notificar cambio asociado a la uri
                resolver.notifyChange(uri, null, false);
                break;

            default:
                throw new IllegalArgumentException("Elemento gasto desconocido: " + uri);
        }

        return affected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int affected;

        switch (GrabilityContract.uriMatcher.match(uri)) {

            case GrabilityContract.APP_ALLROWS:

                affected = db.update(GrabilityContract.APP, values, selection, selectionArgs);
                break;

            case GrabilityContract.APP_SINGLE_ROW:

                String promocion_id = uri.getPathSegments().get(1);

                affected = db.update(
                        GrabilityContract.APP, values,
                        GrabilityContract.Columnas.APP_ID + "=" + promocion_id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs
                );
                break;

            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        resolver.notifyChange(uri, null, false);

        return affected;
    }
}
