package com.franciscogiraldo.fcog.grability.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.sync.SyncAdapter;
import com.franciscogiraldo.fcog.grability.utils.Constantes;
import com.franciscogiraldo.fcog.grability.utils.RegistrationIntentService;
import com.franciscogiraldo.fcog.grability.utils.NetworkUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by fcog on 10/1/15.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_splash);

        SyncAdapter.inicializarSyncAdapter(getApplicationContext());

        int internetConection = NetworkUtil.getConnectivityStatus(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //este if es verdadero la 1era vez que se abre la app y se ha recibido el token de GCM
        if (internetConection != 0 && !sharedPreferences.getBoolean(Constantes.SENT_TOKEN_TO_SERVER, false)) {

            //Register app in Google Cloud Messaging
            activarGCM();
        }

        //este if es verdadero la 1era vez que se abre la app
        if (internetConection != 0 && !sharedPreferences.contains(Constantes.PRIMERA_VEZ)) {

            sharedPreferences.edit().putBoolean(Constantes.PRIMERA_VEZ, true).apply();

            //descargar info de la web
            SyncAdapter.sincronizarAhora(getApplicationContext(), false);
        }
        else{
            /* New Handler to start the Menu-Activity
            * and close this Splash-Screen after some seconds.*/
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    // closing splash activity
                    finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    //Register app in Google Cloud Messaging
    private void activarGCM(){

        if (checkPlayServices()) {

            BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                    boolean sentToken = sharedPreferences.getBoolean(Constantes.SENT_TOKEN_TO_SERVER, false);

                    if (sentToken) {
                        Log.i(TAG, "Token retrieved and sent to server! You can now use gcmsender to send downstream messages to this app.");
                    } else {
                        Log.i(TAG, "An error occurred while either fetching the InstanceID token, sending the fetched token to the server or subscribing to the PubSub topic. Please try running the sample again.");
                    }
                }
            };

            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }


    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Sync finished, should refresh nao!!");

            Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent2);

            // closing splash activity
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncFinishedReceiver, new IntentFilter(SyncAdapter.SYNC_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncFinishedReceiver);
    }

}
