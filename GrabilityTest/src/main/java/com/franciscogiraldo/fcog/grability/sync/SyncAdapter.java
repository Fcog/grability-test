package com.franciscogiraldo.fcog.grability.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.provider.GrabilityContract;
import com.franciscogiraldo.fcog.grability.utils.Constantes;
import com.franciscogiraldo.fcog.grability.web.App;
import com.franciscogiraldo.fcog.grability.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Maneja la transferencia de datos entre el servidor y el cliente
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public static final String SYNC_FINISHED = "ACTION_FINISHED_SYNC";

    ContentResolver resolver;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        resolver = context.getContentResolver();
    }

    /**
     * Constructor para mantener compatibilidad en versiones inferiores a 3.0
     */
    public SyncAdapter( Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        resolver = context.getContentResolver();
    }

    public static void inicializarSyncAdapter(Context context) {
        obtenerCuentaASincronizar(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, final SyncResult syncResult) {

        Log.i(TAG, "onPerformSync()...");

        realizarSincronizacionLocal(syncResult);

        getContext().getContentResolver().notifyChange(GrabilityContract.CONTENT_URI1, null, false);
    }

    private void realizarSincronizacionLocal(final SyncResult syncResult) {

        Log.i(TAG, "Actualizando el cliente.");

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                Constantes.API_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        actualizarDatosLocales(response, syncResult);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }
        );

        req.setRetryPolicy(new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }


    /**
     * Actualiza los registros locales a través de una comparación con los datos
     * del servidor
     *
     * @param response   Respuesta en formato Json obtenida del servidor
     * @param syncResult Registros de la sincronización
     */
    private void actualizarDatosLocales(JSONObject response, SyncResult syncResult) {

        JSONArray apps = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        List<App> data = new ArrayList<App>();

        try {
            // Obtener array "apps"
            JSONObject feed  = response.getJSONObject("feed");
            apps = feed.getJSONArray("entry");

            //Iterate the jsonArray
            for(int i=0; i < apps.length(); i++){

                JSONObject appObject = apps.getJSONObject(i);

                int app_id = appObject.getJSONObject("id").getJSONObject("attributes").getInt("im:id");
                String titulo = appObject.getJSONObject("title").getString("label");
                String imagen = appObject.getJSONArray("im:image").getJSONObject(2).getString("label");
                String descripcion = appObject.getJSONObject("summary").getString("label");
                String categoria = appObject.getJSONObject("category").getJSONObject("attributes").getString("label");
                String enlace = appObject.getJSONObject("link").getJSONObject("attributes").getString("href");
                int precio = appObject.getJSONObject("im:price").getJSONObject("attributes").getInt("amount");

                App app = new App(app_id, titulo, imagen, descripcion, categoria, enlace, precio);

                data.add(app);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getContext().getString(R.string.error_descargar_datos), Toast.LENGTH_LONG).show();
        }


        // Lista para recolección de operaciones pendientes
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // Tabla hash para guardar las promociones recibidas del servidor
        HashMap<String, App> AppsMapeo = new HashMap<String, App>();

        for (App p : data) {
            AppsMapeo.put(String.valueOf(p.app_id), p);
        }

        // Consultar registros locales actuales
        Uri uri = GrabilityContract.CONTENT_URI1;

        String select = GrabilityContract.Columnas.APP_ID + " IS NOT NULL";

        Cursor c = resolver.query(uri, Constantes.PROJECTION, select, null, null);

        //quitar promociones nuevas anteriores
        ContentValues values = new ContentValues();
        values.put(GrabilityContract.Columnas.NUEVA, 0);
        resolver.update(uri, values, null, null);

        //assert c != null;
        if (c==null) throw new AssertionError("Object cursor cannot be null");

        Log.i(TAG, "Se encontraron " + c.getCount() + " registros locales.");

        // Encontrar datos obsoletos
        // se comparan los datos locales con los remotos. Si el mismo id de app es encontrado se checkea la fecha de actualización
        // para hacer la respectiva actualizacion de la información
        String app_id;

        while (c.moveToNext()) {

            syncResult.stats.numEntries++;

            app_id = c.getString(Constantes.COLUMNA_APP_ID);

            App match = AppsMapeo.get(app_id);

            // La app existe en local pero no en remoto
            if (match == null) {
                // Debido a que la entrada no existe, es removida de la base de datos
                Uri deleteUriPromocion = GrabilityContract.CONTENT_URI1.buildUpon().appendPath(app_id).build();

                Log.i(TAG, "Programando eliminación de: " + deleteUriPromocion);
                ops.add(ContentProviderOperation.newDelete(deleteUriPromocion).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Insertar las apps que no existen en local (los items del mapeado (AppsMapeo) restantes)
        for (App p : AppsMapeo.values()) {

            Log.i(TAG, "Programando inserción de la app: " + p.app_id);

            ops.add(ContentProviderOperation.newInsert(GrabilityContract.CONTENT_URI1)
                    .withValue(GrabilityContract.Columnas.APP_ID, p.app_id)
                    .withValue(GrabilityContract.Columnas.TITULO, p.titulo)
                    .withValue(GrabilityContract.Columnas.IMAGEN, p.imagen)
                    .withValue(GrabilityContract.Columnas.DESCRIPCION, p.descripcion)
                    .withValue(GrabilityContract.Columnas.CATEGORIA, p.categoria)
                    .withValue(GrabilityContract.Columnas.ENLACE, p.enlace)
                    .withValue(GrabilityContract.Columnas.PRECIO, p.precio)
                    .withValue(GrabilityContract.Columnas.NUEVA, 1)
                    .build());
            syncResult.stats.numInserts++;
        }

        //Notificar sobre apps nuevas a excepcion de la 1era vez q se abre el app
        if (AppsMapeo.size() > 0 && sharedPreferences.getBoolean(Constantes.CONFIG_NOTIFICACIONES, true) &&  !sharedPreferences.getBoolean(Constantes.PRIMERA_VEZ, true)) {
            Notificaciones.despachar_notificaciones(new ArrayList<App>(AppsMapeo.values()));
        }

        //si hay algo para sincronizar
        if (syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0) {

            Log.i(TAG, "Aplicando operaciones...");

            try {
                resolver.applyBatch(GrabilityContract.AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }

            resolver.notifyChange(GrabilityContract.CONTENT_URI1, null, false);

            Log.i(TAG, "Sincronización finalizada.");

            //si se descargaron los datos de sincronizacion se cambia la variable de 1era vez
            sharedPreferences.edit().putBoolean(Constantes.PRIMERA_VEZ, false).apply();

            //notificar al splash screen que se descargaron los datos por 1era vez
            Intent i = new Intent(SYNC_FINISHED);
            getContext().sendBroadcast(i);

        } else {
            Log.i(TAG, "No se requiere sincronización");
        }

    }



    /**
     * Inicia manualmente la sincronización
     *
     * @param context    Contexto para crear la petición de sincronización
     * @param onlyUpload Usa true para sincronizar el servidor o false para sincronizar el cliente
     */
    public static void sincronizarAhora(Context context, boolean onlyUpload) {
        Log.i(TAG, "Realizando petición de sincronización manual.");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        if (onlyUpload)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);

        ContentResolver.requestSync(obtenerCuentaASincronizar(context), context.getString(R.string.provider_authority), bundle);
    }



    /**
     * Crea u obtiene una cuenta existente
     *
     * @param context Contexto para acceder al administrador de cuentas
     * @return cuenta auxiliar.
     */
    public static Account obtenerCuentaASincronizar(Context context) {
        // Obtener instancia del administrador de cuentas
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Crear cuenta por defecto
        Account newAccount = new Account(context.getString(R.string.app_name), Constantes.ACCOUNT_TYPE);

        // Comprobar existencia de la cuenta
        if (null == accountManager.getPassword(newAccount)) {

            // Añadir la cuenta al account manager sin password y sin datos de usuario
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;

        }
        Log.i(TAG, "Cuenta de usuario obtenida.");
        return newAccount;
    }

}
