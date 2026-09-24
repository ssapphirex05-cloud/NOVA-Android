package com.nova.messenger;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

public final class NotificationHelper {
    public static final String CHANNEL_ID = "nova_messages";
    private static final String CHANNEL_NAME = "Сообщения NOVA";

    private NotificationHelper() {}

    public static void ensureChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Личные сообщения и запросы в контакты NOVA");
        channel.enableVibration(true);
        channel.setLightColor(Color.rgb(69, 163, 244));
        manager.createNotificationChannel(channel);
    }

    public static void show(Context context, String title, String body, String kind, long conversationId, int requestId) {
        if (MainActivity.isAppForeground()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ensureChannel(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_PUSH_KIND, kind == null ? "message" : kind);
        intent.putExtra(MainActivity.EXTRA_PUSH_CONVERSATION_ID, conversationId);
        intent.putExtra(MainActivity.EXTRA_PUSH_REQUEST_ID, requestId);

        int pendingCode = requestId > 0 ? requestId : (conversationId > 0 ? (int)(conversationId % Integer.MAX_VALUE) : (int)(System.currentTimeMillis() & 0x7fffffff));
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            pendingCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String safeTitle = title == null || title.trim().isEmpty() ? "NOVA" : title.trim();
        String safeBody = body == null || body.trim().isEmpty() ? "Новое событие" : body.trim();
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setColor(Color.rgb(69, 163, 244))
            .setContentTitle(safeTitle)
            .setContentText(safeBody)
            .setStyle(new Notification.BigTextStyle().bigText(safeBody))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(kind == null ? "nova" : "nova-" + kind, pendingCode, notification);
    }
}
