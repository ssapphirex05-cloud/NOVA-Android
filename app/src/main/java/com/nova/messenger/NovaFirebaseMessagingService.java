package com.nova.messenger;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NovaFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        PushRegistration.onNewToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String kind = data.get("kind");
        long conversationId = parseLong(data.get("conversationId"));
        int requestId = (int)Math.min(Integer.MAX_VALUE, Math.max(0, parseLong(data.get("requestId"))));

        if ((title == null || title.isEmpty()) && remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
        }
        if ((body == null || body.isEmpty()) && remoteMessage.getNotification() != null) {
            body = remoteMessage.getNotification().getBody();
        }
        NotificationHelper.show(this, title, body, kind, conversationId, requestId);
    }

    private static long parseLong(String value) {
        try { return Long.parseLong(value == null ? "0" : value); }
        catch (Exception ignored) { return 0; }
    }
}
