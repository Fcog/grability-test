package com.franciscogiraldo.fcog.grability.sync;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.franciscogiraldo.fcog.grability.ui.activity.AlertasActivity;
import com.franciscogiraldo.fcog.grability.ui.activity.MainActivity;
import com.franciscogiraldo.fcog.grability.utils.MyApplication;
import com.franciscogiraldo.fcog.grability.web.App;
import com.franciscogiraldo.fcog.grability.R;

import java.util.List;

/**
 * Created by fcog on 9/18/15.
 */
public class Notificaciones {

    static Application application = (MyApplication) MyApplication.getAppContext();

    private static final String NOTIFICATION_GROUP = "apps_nuevas";

    public static void despachar_notificaciones(List<App> apps){

        int appsNuevas = apps.size();

        Log.i("Notificaciones", String.valueOf(appsNuevas));

        if (appsNuevas > 1){
            sendMultipleNotification(apps);
        }
        else if (appsNuevas == 1){
            sendSimpleNotification(apps.get(0));
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     */
    private static void sendMultipleNotification(List<App> apps) {

        int total_apps = apps.size();

        //variable mas ya que solo se muestran maximo 3 apps en la notificacion
        int mas = 0;
        String texto_mas = "";

        if (total_apps > 3){
            mas = total_apps - 3;
        }

        Intent intent = new Intent(application, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(application, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle().setBigContentTitle(total_apps + " nuevas promociones");

        int parada = 3;

        if (total_apps < 3){
            parada = total_apps;
        }

        for (int i=0; i<parada ; i++ ) {
            inboxStyle.addLine(Html.fromHtml("<big><b>" + apps.get(i).titulo + "</b></big>  "));
        }

        if (mas == 0){
            texto_mas = application.getString(R.string.app_name);
        }
        else if (mas == 1){
            texto_mas = "ver 1 app más";
        }
        else{
            texto_mas = "ver " + mas + " apps más";
        }

        inboxStyle.setSummaryText(texto_mas);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(application)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(application.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(total_apps + " nuevas apps")
                .setContentText(application.getString(R.string.app_name))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private static void sendSimpleNotification(App app) {

        Intent intent = new Intent(application, AlertasActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(application, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(application)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(application.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(application.getString(R.string.notificacion_simple_titulo))
                .setContentText(Html.fromHtml("<big><b>" + app.titulo + "</b></big>  "))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
