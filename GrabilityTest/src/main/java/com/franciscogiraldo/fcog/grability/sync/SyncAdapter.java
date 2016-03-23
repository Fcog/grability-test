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
import android.database.sqlite.SQLiteDatabase;
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
import com.franciscogiraldo.fcog.grability.db.App;
import com.franciscogiraldo.fcog.grability.db.AppContentProvider;
import com.franciscogiraldo.fcog.grability.db.AppDao;
import com.franciscogiraldo.fcog.grability.db.DaoMaster;
import com.franciscogiraldo.fcog.grability.db.DaoSession;
import com.franciscogiraldo.fcog.grability.utils.Constantes;
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

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private AppDao appDao;

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

        StartSyncProcess(syncResult);

        getContext().getContentResolver().notifyChange(AppContentProvider.CONTENT_URI, null, false);
    }

    private void StartSyncProcess(final SyncResult syncResult) {

        Log.i(TAG, "1. Starting sync process");
        Log.i(TAG, "2. Fetching data to the server");

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                Constantes.API_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ProcessFetchedData(response, syncResult);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Sync Error: Fetching data error " + error.toString());
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
    private void ProcessFetchedData(JSONObject response, SyncResult syncResult) {

        Log.i(TAG, "3. Processing fetched data");

        List<App> appsList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        /**
         * Parse apps data from JSON to POJO
         */
        try {
            JSONArray apps = null;
            JSONObject feed  = response.getJSONObject("feed");
            apps = feed.getJSONArray("entry");

            for(int i=0; i < apps.length(); i++){

                JSONObject appJSONObject = apps.getJSONObject(i);

                Long appId = appJSONObject.getJSONObject("id").getJSONObject("attributes").getLong("im:id");
                String title = appJSONObject.getJSONObject("title").getString("label");
                String image = appJSONObject.getJSONArray("im:image").getJSONObject(2).getString("label");
                String description = appJSONObject.getJSONObject("summary").getString("label");
                String category = appJSONObject.getJSONObject("category").getJSONObject("attributes").getString("label");
                String link = appJSONObject.getJSONObject("link").getJSONObject("attributes").getString("href");
                int price = appJSONObject.getJSONObject("im:price").getJSONObject("attributes").getInt("amount");

                App appObject = new App(
                        appId,
                        title,
                        image,
                        description,
                        category,
                        link,
                        price,
                        0,
                        0
                );

                appsList.add(appObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getContext().getString(R.string.error_descargar_datos), Toast.LENGTH_LONG).show();
        }

        /**
         * ArrayList to store the syncing operations
         */
        ArrayList<ContentProviderOperation> opsList = new ArrayList<>();

        /**
         * Store the apps data fetched from the server in a Hash Map
         */
        HashMap<String, App> AppsHashMap = new HashMap<>();

        for (App app : appsList) {
            AppsHashMap.put(String.valueOf(app.getId()), app);
        }

        /**
         * Connection to AppDao
         */
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getContext(), "grability.db", null);
        db = helper.getWritableDatabase();

        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        appDao = daoSession.getAppDao();

        String idColumn = AppDao.Properties.Id.columnName;
        String titleColumn = AppDao.Properties.Title.columnName;
        String imageColumn = AppDao.Properties.Image.columnName;
        String descriptionColumn = AppDao.Properties.Description.columnName;
        String categoryColumn = AppDao.Properties.Category.columnName;
        String linkColumn = AppDao.Properties.Link.columnName;
        String priceColumn = AppDao.Properties.Price.columnName;
        String newSyncColumn = AppDao.Properties.NewSync.columnName;

        Uri appContentProviderUri = AppContentProvider.CONTENT_URI;

        /**
         * Update local apps data. Set all apps as NOT New.
         */

        ContentValues values = new ContentValues();
        values.put(newSyncColumn, 0);

        resolver.update(appContentProviderUri, values, null, null);

        /**
         * Get local apps data
         */
        String select = idColumn + " IS NOT NULL";

        final String[] columns = new String[]{
                idColumn
        };

        Cursor c = resolver.query(appContentProviderUri, columns, select, null, null);

        if (c == null) throw new AssertionError("Sync Error: Fetching local data error, object cursor cannot be null");

        Log.i(TAG, "Local app records found " + c.getCount());

        /**
         * Find obsolete local data
         * Compares local and remote data
         * If same app id is found then check modified date
         * If app id not found then remove from local
         */
        String localAppId;

        while (c.moveToNext()) {

            syncResult.stats.numEntries++;

            localAppId = c.getString(Constantes.COLUMNA_APP_ID);

            App match = AppsHashMap.get(localAppId);

            /**
             * App id not found then remove from local DB
             */
            if (match == null) {
                Uri appContentProviderDeleteUri = appContentProviderUri.buildUpon().appendPath(localAppId).build();

                Log.i(TAG, "Programming app removal: " + localAppId);
                opsList.add(ContentProviderOperation.newDelete(appContentProviderDeleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        /**
         * Add new remote apps data
         */
        for (App app : AppsHashMap.values()) {

            Log.i(TAG, "Programming app addition: " + app.getId());

            opsList.add(ContentProviderOperation.newInsert(appContentProviderUri)
                    .withValue(idColumn, app.getId())
                    .withValue(titleColumn, app.getTitle())
                    .withValue(imageColumn, app.getImage())
                    .withValue(descriptionColumn, app.getDescription())
                    .withValue(categoryColumn, app.getCategory())
                    .withValue(linkColumn, app.getLink())
                    .withValue(priceColumn, app.getPrice())
                    .withValue(newSyncColumn, 1)
                    .build());

            syncResult.stats.numInserts++;
        }

        /**
         * Notify new apps except when the app is opened for the 1st time
         */
/*        if (AppsHashMap.size() > 0 &&
                sharedPreferences.getBoolean(Constantes.CONFIG_NOTIFICACIONES, true) &&
                !sharedPreferences.getBoolean(Constantes.FIRST_TIME, true)
                ) {
            Notifications.dispatch(new ArrayList<App>(AppsHashMap.values()));
        }*/

        /**
         * If there is data to sync
         */
        if (syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0) {

            Log.i(TAG, "Applying batch operation...");

            try {
                resolver.applyBatch(AppContentProvider.AUTHORITY, opsList);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }

            resolver.notifyChange(appContentProviderUri, null, false);

            Log.i(TAG, "Sync process ended.");

            /**
             * If data was synchronized correctly then save in Preferences
             */
            sharedPreferences.edit().putBoolean(Constantes.FIRST_TIME, false).apply();

            /**
             * Notify splash activity
             */
            Intent i = new Intent(SYNC_FINISHED);
            getContext().sendBroadcast(i);

        } else {
            Log.i(TAG, "No sync required.");
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
