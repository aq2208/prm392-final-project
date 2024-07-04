package com.example.socialmediaapp.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Notification extends ContextWrapper {

    private static final String Id = "id";
    private static final String Name = "FireshApp";
    private NotificationManager manager;

    public Notification(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(Id,Name,NotificationManager.IMPORTANCE_HIGH);
         notificationChannel.enableLights(true);
         notificationChannel.enableVibration(true);
         notificationChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
         getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotifications(String title, String body, PendingIntent pendingIntent,
                                                             Uri soundUri, String icon){
        return new android.app.Notification.Builder(getApplicationContext(), Id)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }
}
