package com.nova.messenger.native2

import android.content.Context
import android.provider.Settings
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("nova_native_session", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove("token") else putString("token", value)
            }.apply()
        }

    var fcmToken: String?
        get() = prefs.getString("fcm_token", null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove("fcm_token") else putString("fcm_token", value)
            }.apply()
        }
}

class NovaRepository(private val context: Context) {
    val session = SessionStore(context)

    private val api: NovaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "NOVA-Android-Native/" + BuildConfig.VERSION_NAME)
                session.token?.takeIf { it.isNotBlank() }?.let {
                    builder.header("Authorization", "Bearer " + it)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.NOVA_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NovaApi::class.java)
    }

    suspend fun login(username: String, password: String): AuthResponse {
        val result = api.login(body = LoginBody(username.trim(), password))
        session.token = result.token
        return result
    }

    suspend fun register(username: String, displayName: String, password: String): AuthResponse {
        val result = api.register(body = RegisterBody(username.trim(), displayName.trim(), password))
        session.token = result.token
        return result
    }

    suspend fun me(): NovaUser = api.me().user

    suspend fun updateProfile(displayName: String, bio: String, statusText: String): NovaUser {
        val result = api.updateMe(
            body = ProfileUpdateBody(
                displayName = displayName.trim(),
                bio = bio.trim(),
                statusText = statusText.trim()
            )
        )
        if (result.token.isNotBlank()) session.token = result.token
        return result.user
    }

    suspend fun conversations(): List<Conversation> = api.conversations().conversations

    suspend fun messages(conversationId: Long): MessagesResponse =
        api.messages(route = "conversations/" + conversationId + "/messages")

    suspend fun sendMessage(
        conversationId: Long,
        text: String,
        replyToId: Long? = null
    ): NovaMessage = api.sendMessage(
        route = "conversations/" + conversationId + "/messages",
        body = SendMessageBody(
            body = text,
            replyToId = replyToId,
            clientMessageId = "android-native-" + UUID.randomUUID().toString()
        )
    ).message

    suspend fun markRead(conversationId: Long, messageId: Long) {
        api.markRead(
            route = "conversations/" + conversationId + "/read",
            body = ReadBody(messageId)
        )
    }

    suspend fun setTyping(
        conversationId: Long,
        active: Boolean,
        mode: String = "typing"
    ) {
        api.typing(
            route = "conversations/" + conversationId + "/typing",
            body = TypingBody(active = active, mode = mode)
        )
    }

    suspend fun heartbeat() {
        api.heartbeat()
    }

    suspend fun friends(): FriendsResponse = api.friends()

    suspend fun searchUsers(query: String): List<NovaUser> =
        api.searchUsers(query = query).users

    suspend fun requestFriend(userId: Long): FriendRequestResult =
        api.requestFriend(body = FriendRequestBody(userId))

    suspend fun respondFriend(requestId: Long, action: String): FriendRespondResult =
        api.respondFriend(body = FriendRespondBody(requestId, action))

    suspend fun removeFriend(userId: Long): FriendRemoveResult =
        api.removeFriend(body = FriendRemoveBody(userId))

    suspend fun openDm(userId: Long): Long =
        api.openDm(body = DmBody(userId)).conversationId

    suspend fun registerPush(token: String) {
        session.fcmToken = token
        api.registerAndroidPush(
            body = PushBody(
                token = token,
                deviceId = deviceId(),
                appVersion = BuildConfig.VERSION_NAME
            )
        )
    }

    suspend fun unregisterPush() {
        val pushToken = session.fcmToken ?: return
        api.unregisterAndroidPush(
            body = PushBody(
                token = pushToken,
                deviceId = deviceId(),
                appVersion = BuildConfig.VERSION_NAME
            )
        )
    }

    fun clearSession() {
        session.token = null
    }

    fun absoluteMediaUrl(path: String?): String? {
        val value = path?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("https://") || value.startsWith("http://")) return value
        return BuildConfig.NOVA_URL + value.trimStart('/')
    }

    private fun deviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        return "android-" + androidId
    }

    companion object {
        fun errorMessage(error: Throwable): String {
            if (error is HttpException) {
                val raw = runCatching {
                    error.response()?.errorBody()?.string()
                }.getOrNull()

                if (!raw.isNullOrBlank()) {
                    val apiError = runCatching {
                        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
                    }.getOrNull()
                    if (!apiError.isNullOrBlank()) return apiError
                }
                return "Сервер NOVA вернул ошибку " + error.code()
            }
            return error.message?.takeIf { it.isNotBlank() }
                ?: "Не удалось связаться с NOVA"
        }
    }
}
