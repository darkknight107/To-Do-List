package com.example.darkknight.doit2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //opens the application main activity when the notification is tapped
        Intent notificationIntent= new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder= TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent= stackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(context);

        Notification notification= builder.setContentTitle("Stay Focused!")
                .setContentText("Task is due soon!")
                .setTicker("A task is due in an hour!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
