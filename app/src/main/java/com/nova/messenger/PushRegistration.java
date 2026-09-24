package com.nova.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PushRegistration {
    private static final String PREFS = "nova_native_push";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private PushRegistration() {}

    public static boolean isFirebaseReady(Context context) {
        if (!BuildConfig.FIREBASE_CONFIGURED) return false;
        try {
            List<FirebaseApp> apps = FirebaseApp.getApps(context);
            return apps != null && !apps.isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void register(Context context, String authToken) {
        String token = authToken == null ? "" : authToken.trim();
        if (token.isEmpty()) return;
        Context app = context.getApplicationContext();
        prefs(app).edit().putString(KEY_AUTH_TOKEN, token).apply();
        if (!isFirebaseReady(app)) return;
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null || task.getResult().trim().isEmpty()) return;
                String fcmToken = task.getResult().trim();
                prefs(app).edit().putString(KEY_FCM_TOKEN, fcmToken).apply();
                subscribe(app, token, fcmToken);
            });
        } catch (Throwable ignored) {}
    }

    public static void unregister(Context context, String authToken) {
        Context app = context.getApplicationContext();
        SharedPreferences p = prefs(app);
        String auth = authToken == null || authToken.trim().isEmpty() ? p.getString(KEY_AUTH_TOKEN, "") : authToken.trim();
        String fcm = p.getString(KEY_FCM_TOKEN, "");
        p.edit().remove(KEY_AUTH_TOKEN).apply();
        if (!auth.isEmpty() && !fcm.isEmpty()) unsubscribe(app, auth, fcm);
    }

    public static void onNewToken(Context context, String fcmToken) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) return;
        Context app = context.getApplicationContext();
        String normalized = fcmToken.trim();
        SharedPreferences p = prefs(app);
        p.edit().putString(KEY_FCM_TOKEN, normalized).apply();
        String auth = p.getString(KEY_AUTH_TOKEN, "");
        if (!auth.isEmpty()) subscribe(app, auth, normalized);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String deviceId(Context context) {
        SharedPreferences p = prefs(context);
        String id = p.getString(KEY_DEVICE_ID, "");
        if (id != null && !id.isEmpty()) return id;
        id = UUID.randomUUID().toString();
        p.edit().putString(KEY_DEVICE_ID, id).apply();
        return id;
    }

    private static void subscribe(Context context, String authToken, String fcmToken) {
        EXECUTOR.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("token", fcmToken);
                body.put("deviceId", deviceId(context));
                body.put("appVersion", BuildConfig.VERSION_NAME);
                request("POST", authToken, body.toString());
            } catch (Throwable ignored) {}
        });
    }

    private static void unsubscribe(Context context, String authToken, String fcmToken) {
        EXECUTOR.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("token", fcmToken);
                body.put("deviceId", deviceId(context));
                request("DELETE", authToken, body.toString());
            } catch (Throwable ignored) {}
        });
    }

    private static void request(String method, String authToken, String json) throws Exception {
        URL url = new URL(BuildConfig.NOVA_URL + "api.php?route=push/android/subscribe");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(9000);
        connection.setReadTimeout(9000);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
        if (json != null && !json.isEmpty()) {
            connection.setDoOutput(true);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
        }
        try {
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED || status == HttpURLConnection.HTTP_FORBIDDEN) {
                // The web session expired or was revoked. Do not keep retrying a stale bearer token.
                // A later successful web login will register again through the bridge.
            }
        } finally {
            connection.disconnect();
        }
    }
}
