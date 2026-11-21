package com.annas.aifloating;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "annas_ai_3d_java_channel";
    public static final int SERVICE_NOTIF_ID = 4026;
    public static final int RESULT_NOTIF_ID = 4027;

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifikasi status Floating AI 3D dan hasil jawaban Google AI");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static Notification buildServiceNotification(Context context) {
        ensureChannel(context);
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_floating_3d_ai)
                .setContentTitle(context.getString(R.string.notif_title))
                .setContentText(context.getString(R.string.notif_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    public static void showAnswerNotification(Context context, String title, String answer) {
        ensureChannel(context);
        String safeAnswer = answer == null ? "" : answer;
        String preview = safeAnswer.replace("\n", " ");
        if (preview.length() > 140) {
            preview = preview.substring(0, 140);
        }

        Notification notif = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_floating_3d_ai)
                .setContentTitle(title)
                .setContentText(preview)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeAnswer))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(RESULT_NOTIF_ID, notif);
        }
    }
}
