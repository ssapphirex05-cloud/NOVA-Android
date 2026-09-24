package com.nova.messenger.native2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object NovaNotificationHelper {
    private const val CHANNEL_ID = "nova_messages"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Сообщения NOVA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Новые сообщения и запросы в контакты"
                enableVibration(true)
            }
        )
    }

    fun show(
        context: Context,
        title: String,
        body: String,
        conversationId: Long
    ) {
        ensureChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (conversationId > 0) {
                putExtra(MainActivity.EXTRA_CONVERSATION_ID, conversationId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setContentTitle(title.ifBlank { "NOVA" })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(
            if (conversationId > 0) conversationId.toInt()
            else (System.currentTimeMillis() and 0x7fffffff).toInt(),
            notification
        )
    }
}

class NovaFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repository = NovaRepository(applicationContext)
        repository.session.fcmToken = token
        if (!repository.session.token.isNullOrBlank()) {
            scope.launch {
                runCatching { repository.registerPush(token) }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (MainActivity.isForeground) return

        val title = message.data["title"]
            ?: message.notification?.title
            ?: "NOVA"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: "Новое сообщение"
        val conversationId = message.data["conversationId"]?.toLongOrNull() ?: 0L

        NovaNotificationHelper.show(
            context = applicationContext,
            title = title,
            body = body,
            conversationId = conversationId
        )
    }
}
