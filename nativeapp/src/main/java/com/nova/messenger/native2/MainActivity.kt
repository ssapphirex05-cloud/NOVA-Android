package com.nova.messenger.native2

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

private object NovaColors {
    val Bg = Color(0xFF080C12)
    val Bg2 = Color(0xFF0A1019)
    val Panel = Color(0xFF0D141E)
    val PanelSolid = Color(0xFF0F1722)
    val Panel2 = Color(0xFF111B28)
    val Panel3 = Color(0xFF172334)
    val Text = Color(0xFFF5F8FC)
    val Muted = Color(0xFF8795A8)
    val Muted2 = Color(0xFF66758A)
    val Accent = Color(0xFF3390EC)
    val Accent2 = Color(0xFF53B6FF)
    val Good = Color(0xFF28D59D)
    val Danger = Color(0xFFFF6B7D)
    val Line = Color.White.copy(alpha = 0.075f)
}

data class NovaUser(
    val id: Long = 0,
    val username: String = "",
    val displayName: String = "",
    val statusText: String = "",
    val accent: String = "violet",
    val avatarUrl: String? = null,
    val online: Boolean = false
)

data class AuthResponse(val token: String = "", val user: NovaUser = NovaUser())
data class MeResponse(val user: NovaUser = NovaUser())

data class LastMessage(
    val id: Long = 0,
    val body: String = "",
    val senderId: Long = 0,
    val createdAt: String = ""
)

data class Conversation(
    val id: Long = 0,
    val title: String = "",
    val peer: NovaUser? = null,
    val system: Boolean = false,
    val lastMessage: LastMessage? = null,
    val unread: Int = 0
)

data class ConversationsResponse(val conversations: List<Conversation> = emptyList())

data class MessageSender(
    val id: Long = 0,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null
)

data class TypingUser(
    val id: Long = 0,
    val displayName: String = "",
    val mode: String = "typing"
)

data class NovaMessage(
    val id: Long = 0,
    val conversationId: Long = 0,
    val sender: MessageSender = MessageSender(),
    val body: String = "",
    val createdAt: String = "",
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val readByPeer: Boolean = false
)

data class MessagesResponse(
    val messages: List<NovaMessage> = emptyList(),
    val typingUsers: List<TypingUser> = emptyList()
)

data class MessageEnvelope(val message: NovaMessage = NovaMessage())
data class OkResponse(val ok: Boolean = true)
data class PushResponse(val ok: Boolean = true)

data class LoginBody(val username: String, val password: String)
data class RegisterBody(val username: String, val displayName: String, val password: String)
data class SendBody(val body: String, val clientMessageId: String)
data class ReadBody(val messageId: Long)
data class TypingBody(val active: Boolean, val mode: String = "typing")
data class PushBody(val token: String, val deviceId: String, val appVersion: String)

private interface NovaApi {
    @POST("api.php")
    suspend fun login(@Query("route") route: String = "auth/login", @Body body: LoginBody): AuthResponse

    @POST("api.php")
    suspend fun register(@Query("route") route: String = "auth/register", @Body body: RegisterBody): AuthResponse

    @GET("api.php")
    suspend fun me(@Query("route") route: String = "me"): MeResponse

    @GET("api.php")
    suspend fun conversations(@Query("route") route: String = "conversations"): ConversationsResponse

    @GET("api.php")
    suspend fun messages(@Query("route") route: String): MessagesResponse

    @POST("api.php")
    suspend fun send(@Query("route") route: String, @Body body: SendBody): MessageEnvelope

    @POST("api.php")
    suspend fun read(@Query("route") route: String, @Body body: ReadBody): OkResponse

    @POST("api.php")
    suspend fun typing(@Query("route") route: String, @Body body: TypingBody): OkResponse

    @POST("api.php")
    suspend fun heartbeat(@Query("route") route: String = "heartbeat", @Body body: Map<String, String> = emptyMap()): OkResponse

    @POST("api.php")
    suspend fun push(@Query("route") route: String = "push/android/subscribe", @Body body: PushBody): PushResponse
}

private class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("nova_native_session", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            val edit = prefs.edit()
            if (value.isNullOrBlank()) edit.remove("token") else edit.putString("token", value)
            edit.apply()
        }

    var fcmToken: String?
        get() = prefs.getString("fcm", null)
        set(value) {
            val edit = prefs.edit()
            if (value.isNullOrBlank()) edit.remove("fcm") else edit.putString("fcm", value)
            edit.apply()
        }
}

private class NovaRepository(private val context: Context) {
    val session = SessionStore(context)

    private val api: NovaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "NOVA-Android-Native/" + BuildConfig.VERSION_NAME)
                val token = session.token
                if (!token.isNullOrBlank()) request.header("Authorization", "Bearer " + token)
                chain.proceed(request.build())
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
    suspend fun conversations(): List<Conversation> = api.conversations().conversations

    suspend fun messages(id: Long): MessagesResponse =
        api.messages(route = "conversations/" + id + "/messages")

    suspend fun send(id: Long, text: String): NovaMessage =
        api.send(
            route = "conversations/" + id + "/messages",
            body = SendBody(text, "android-native-" + UUID.randomUUID().toString())
        ).message

    suspend fun read(id: Long, messageId: Long) {
        api.read(route = "conversations/" + id + "/read", body = ReadBody(messageId))
    }

    suspend fun typing(id: Long, active: Boolean) {
        api.typing(route = "conversations/" + id + "/typing", body = TypingBody(active))
    }

    suspend fun heartbeat() {
        api.heartbeat()
    }

    suspend fun registerPush(token: String) {
        session.fcmToken = token
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        api.push(body = PushBody(token, "android-" + androidId, BuildConfig.VERSION_NAME))
    }

    fun mediaUrl(path: String?): String? {
        val value = path?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return BuildConfig.NOVA_URL + value.trimStart('/')
    }

    companion object {
        fun errorMessage(error: Throwable): String {
            if (error is HttpException) {
                val raw = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
                if (!raw.isNullOrBlank()) {
                    val message = runCatching {
                        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
                    }.getOrNull()
                    if (!message.isNullOrBlank()) return message
                }
                return "Ошибка сервера NOVA: " + error.code()
            }
            return error.message ?: "Не удалось связаться с NOVA"
        }
    }
}

data class NovaState(
    val booting: Boolean = true,
    val busy: Boolean = false,
    val user: NovaUser? = null,
    val conversations: List<Conversation> = emptyList(),
    val selected: Conversation? = null,
    val messages: List<NovaMessage> = emptyList(),
    val typingUsers: List<TypingUser> = emptyList(),
    val error: String? = null
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NovaRepository(application)
    private val mutable = MutableStateFlow(NovaState())
    val state: StateFlow<NovaState> = mutable.asStateFlow()

    private var pollJob: Job? = null
    private var typingOffJob: Job? = null
    private var lastTypingPing = 0L
    private var lastRead = 0L
    private var pendingConversation: Long? = null

    init {
        viewModelScope.launch {
            if (repository.session.token.isNullOrBlank()) {
                mutable.value = NovaState(booting = false)
                return@launch
            }
            runCatching { repository.me() }
                .onSuccess {
                    mutable.value = mutable.value.copy(booting = false, user = it)
                    refreshConversations()
                    startPolling()
                    syncPush()
                }
                .onFailure {
                    repository.session.token = null
                    mutable.value = NovaState(booting = false)
                }
        }
    }

    fun login(username: String, password: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.login(username, password) }
                .onSuccess {
                    mutable.value = mutable.value.copy(busy = false, user = it.user)
                    refreshConversations()
                    startPolling()
                    syncPush()
                }
                .onFailure {
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(it))
                }
        }
    }

    fun register(username: String, displayName: String, password: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.register(username, displayName, password) }
                .onSuccess {
                    mutable.value = mutable.value.copy(busy = false, user = it.user)
                    refreshConversations()
                    startPolling()
                    syncPush()
                }
                .onFailure {
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(it))
                }
        }
    }

    fun logout() {
        pollJob?.cancel()
        repository.session.token = null
        mutable.value = NovaState(booting = false)
    }

    fun dismissError() {
        mutable.value = mutable.value.copy(error = null)
    }

    fun openConversation(conversation: Conversation) {
        lastRead = 0L
        mutable.value = mutable.value.copy(selected = conversation, messages = emptyList(), typingUsers = emptyList())
        viewModelScope.launch { refreshMessages() }
    }

    fun openConversationById(id: Long) {
        if (id <= 0) return
        pendingConversation = id
        val found = mutable.value.conversations.firstOrNull { it.id == id }
        if (found != null) {
            pendingConversation = null
            openConversation(found)
        } else {
            refreshConversations()
        }
    }

    fun closeConversation() {
        val id = mutable.value.selected?.id
        if (id != null) viewModelScope.launch { runCatching { repository.typing(id, false) } }
        mutable.value = mutable.value.copy(selected = null, messages = emptyList(), typingUsers = emptyList())
    }

    fun send(text: String) {
        val conversation = mutable.value.selected ?: return
        val clean = text.trim()
        if (clean.isEmpty() || mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true)
            runCatching { repository.send(conversation.id, clean) }
                .onSuccess {
                    mutable.value = mutable.value.copy(busy = false)
                    runCatching { repository.typing(conversation.id, false) }
                    refreshMessages()
                    refreshConversations()
                }
                .onFailure {
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(it))
                }
        }
    }

    fun draftChanged(hasText: Boolean) {
        val id = mutable.value.selected?.id ?: return
        typingOffJob?.cancel()
        val now = SystemClock.elapsedRealtime()
        if (hasText && now - lastTypingPing > 1400) {
            lastTypingPing = now
            viewModelScope.launch { runCatching { repository.typing(id, true) } }
        }
        typingOffJob = viewModelScope.launch {
            delay(2600)
            runCatching { repository.typing(id, false) }
        }
    }

    private fun refreshConversations() {
        viewModelScope.launch {
            runCatching { repository.conversations() }.onSuccess { list ->
                val currentId = mutable.value.selected?.id
                val selected = currentId?.let { id -> list.firstOrNull { it.id == id } } ?: mutable.value.selected
                mutable.value = mutable.value.copy(conversations = list, selected = selected)
                val pending = pendingConversation
                if (pending != null) {
                    val conversation = list.firstOrNull { it.id == pending }
                    if (conversation != null) {
                        pendingConversation = null
                        openConversation(conversation)
                    }
                }
            }
        }
    }

    private suspend fun refreshMessages() {
        val conversation = mutable.value.selected ?: return
        runCatching { repository.messages(conversation.id) }.onSuccess { result ->
            if (mutable.value.selected?.id != conversation.id) return@onSuccess
            mutable.value = mutable.value.copy(messages = result.messages, typingUsers = result.typingUsers)
            val lastId = result.messages.lastOrNull()?.id ?: 0L
            if (lastId > lastRead) {
                lastRead = lastId
                runCatching { repository.read(conversation.id, lastId) }
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            var tick = 0
            while (isActive && !repository.session.token.isNullOrBlank()) {
                delay(1800)
                runCatching { repository.conversations() }.onSuccess { list ->
                    val id = mutable.value.selected?.id
                    val selected = id?.let { value -> list.firstOrNull { it.id == value } } ?: mutable.value.selected
                    mutable.value = mutable.value.copy(conversations = list, selected = selected)
                }
                if (mutable.value.selected != null) refreshMessages()
                tick++
                if (tick % 8 == 0) runCatching { repository.heartbeat() }
            }
        }
    }

    private fun syncPush() {
        if (!BuildConfig.FIREBASE_CONFIGURED) return
        runCatching {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { registerPushToken(it) }
        }
    }

    fun registerPushToken(token: String) {
        if (token.isBlank() || repository.session.token.isNullOrBlank()) return
        viewModelScope.launch { runCatching { repository.registerPush(token) } }
    }

    fun mediaUrl(path: String?): String? = repository.mediaUrl(path)
}

class MainActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NovaNotifications.ensureChannel(this)
        requestNotificationPermission()
        setContent {
            NovaTheme {
                NovaApp(viewModel)
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_CONVERSATION_ID, 0L) ?: 0L
        if (id > 0) viewModel.openConversationById(id)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 3101)
        }
    }

    override fun onStart() {
        super.onStart()
        foreground = true
    }

    override fun onStop() {
        foreground = false
        super.onStop()
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "nova_conversation_id"
        @Volatile var foreground = false
    }
}

private val NovaScheme = darkColorScheme(
    primary = NovaColors.Accent,
    secondary = NovaColors.Accent2,
    background = NovaColors.Bg,
    surface = NovaColors.PanelSolid,
    onPrimary = Color.White,
    onBackground = NovaColors.Text,
    onSurface = NovaColors.Text
)

@Composable
private fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NovaScheme, typography = Typography(), content = content)
}

@Composable
private fun NovaApp(viewModel: NovaViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val error = state.error
        if (!error.isNullOrBlank()) {
            snack.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaColors.Bg)
            .systemBarsPadding()
    ) {
        when {
            state.booting -> CircularProgressIndicator(
                color = NovaColors.Accent2,
                modifier = Modifier.align(Alignment.Center)
            )
            state.user == null -> AuthScreen(
                busy = state.busy,
                onLogin = viewModel::login,
                onRegister = viewModel::register
            )
            state.selected != null -> ChatScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onBack = viewModel::closeConversation,
                onSend = viewModel::send,
                onDraftChanged = viewModel::draftChanged
            )
            else -> ConversationScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onOpen = viewModel::openConversation,
                onLogout = viewModel::logout
            )
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun AuthScreen(
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var register by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaColors.Bg2)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NovaLogo(54)
            Column(Modifier.padding(start = 12.dp)) {
                Text("NOVA", color = NovaColors.Text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("Messenger", color = NovaColors.Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            if (register) "Создать аккаунт" else "Войти в NOVA",
            color = NovaColors.Text,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Один аккаунт для веба и Android.",
            color = NovaColors.Muted,
            fontSize = 13.5.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NovaColors.Panel2)
                .border(1.dp, NovaColors.Line, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            AuthTab("Вход", !register, Modifier.weight(1f)) { register = false }
            AuthTab("Регистрация", register, Modifier.weight(1f)) { register = true }
        }

        Spacer(Modifier.height(14.dp))
        NovaInput(username, { username = it }, "Username", false, KeyboardType.Ascii)
        if (register) {
            Spacer(Modifier.height(12.dp))
            NovaInput(displayName, { displayName = it }, "Имя", false, KeyboardType.Text)
        }
        Spacer(Modifier.height(12.dp))
        NovaInput(password, { password = it }, "Пароль", true, KeyboardType.Password)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF45A3F4), Color(0xFF2F82CF))))
                .clickable(enabled = !busy) {
                    if (register) onRegister(username, displayName, password) else onLogin(username, password)
                },
            contentAlignment = Alignment.Center
        ) {
            if (busy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text(
                    if (register) "Создать аккаунт" else "Войти",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AuthTab(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) NovaColors.Panel3 else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (active) NovaColors.Text else NovaColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NovaInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    password: Boolean,
    type: KeyboardType
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = NovaColors.Text, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = type),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(NovaColors.Panel2)
            .border(1.dp, NovaColors.Line, RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, color = NovaColors.Muted2, fontSize = 14.sp)
                inner()
            }
        }
    )
}

@Composable
private fun ConversationScreen(
    state: NovaState,
    mediaUrl: (String?) -> String?,
    onOpen: (Conversation) -> Unit,
    onLogout: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) state.conversations else state.conversations.filter {
        it.title.contains(search, true) || (it.peer?.username?.contains(search, true) == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaColors.Bg2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color(0xDB090F17))
                .border(0.5.dp, NovaColors.Line)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NovaLogo(40)
            Column(Modifier.padding(start = 10.dp)) {
                Text("NOVA", color = NovaColors.Text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text("Messenger", color = NovaColors.Muted, fontSize = 9.5.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Выйти",
                color = NovaColors.Muted,
                fontSize = 10.sp,
                modifier = Modifier.clickable(onClick = onLogout).padding(8.dp)
            )
            Spacer(Modifier.width(4.dp))
            NovaAvatar(state.user, mediaUrl(state.user?.avatarUrl), 39)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(NovaColors.Panel2)
                .border(1.dp, NovaColors.Line, RoundedCornerShape(17.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = NovaColors.Muted, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = TextStyle(color = NovaColors.Text, fontSize = 13.5.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (search.isEmpty()) Text("Поиск", color = NovaColors.Muted2, fontSize = 13.5.sp)
                        inner()
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "СООБЩЕНИЯ",
                color = NovaColors.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                filtered.size.toString(),
                color = NovaColors.Muted,
                fontSize = 10.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(NovaColors.Panel2)
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp)
        ) {
            items(filtered, key = { it.id }) { conversation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .clickable { onOpen(conversation) }
                        .padding(horizontal = 11.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        NovaAvatar(conversation.peer ?: NovaUser(displayName = conversation.title), mediaUrl(conversation.peer?.avatarUrl), 58)
                        if (conversation.peer?.online == true) OnlineDot(Modifier.align(Alignment.BottomEnd))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            conversation.title,
                            color = NovaColors.Text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            conversation.lastMessage?.body ?: "Начните диалог",
                            color = NovaColors.Muted,
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(relativeTime(conversation.lastMessage?.createdAt), color = NovaColors.Muted2, fontSize = 10.5.sp)
                        if (conversation.unread > 0) {
                            Spacer(Modifier.height(7.dp))
                            Box(
                                modifier = Modifier
                                    .height(21.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(NovaColors.Accent)
                                    .padding(horizontal = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    conversation.unread.coerceAtMost(99).toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    state: NovaState,
    mediaUrl: (String?) -> String?,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onDraftChanged: (Boolean) -> Unit
) {
    val conversation = state.selected ?: return
    val currentUser = state.user ?: return
    var draft by remember(conversation.id) { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaColors.Bg2)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xB80B121B))
                .border(0.5.dp, NovaColors.Line)
                .padding(start = 7.dp, end = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                "Назад",
                tint = NovaColors.Text,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack)
                    .padding(6.dp)
            )
            Box {
                NovaAvatar(conversation.peer, mediaUrl(conversation.peer?.avatarUrl), 42)
                if (conversation.peer?.online == true) OnlineDot(Modifier.align(Alignment.BottomEnd))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 9.dp)
            ) {
                Text(
                    conversation.title,
                    color = NovaColors.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle(state),
                    color = if (state.typingUsers.isNotEmpty()) NovaColors.Accent2 else NovaColors.Muted,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Icon(Icons.Rounded.MoreVert, "Меню", tint = NovaColors.Muted, modifier = Modifier.size(36.dp).padding(7.dp))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    mine = message.sender.id == currentUser.id,
                    mediaUrl = mediaUrl
                )
            }
        }

        if (state.typingUsers.isNotEmpty()) {
            Text(
                subtitle(state),
                color = NovaColors.Accent2,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 12.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 7.dp, end = 7.dp, bottom = 7.dp)
                .height(58.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0xED0E1C2A))
                .border(1.dp, Color(0x246397C1), RoundedCornerShape(19.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.AttachFile, "Вложение", tint = Color(0xFF91A6BA), modifier = Modifier.size(36.dp).padding(8.dp))
            BasicTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    onDraftChanged(it.isNotBlank())
                },
                textStyle = TextStyle(color = Color(0xFFEFF7FF), fontSize = 13.sp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp),
                maxLines = 4,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (draft.isEmpty()) Text("Сообщение...", color = Color(0xFF72869A), fontSize = 13.sp)
                        inner()
                    }
                }
            )
            Icon(
                Icons.Rounded.SentimentSatisfiedAlt,
                "Эмодзи",
                tint = Color(0xFF91A6BA),
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .clickable {
                        draft += "🙂"
                        onDraftChanged(true)
                    }
                    .padding(8.dp)
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (draft.isBlank()) NovaColors.Panel3
                        else Brush.linearGradient(listOf(Color(0xFF2F9CE5), Color(0xFF2485D1)))
                    )
                    .clickable(enabled = draft.isNotBlank() && !state.busy) {
                        val text = draft
                        draft = ""
                        onDraftChanged(false)
                        onSend(text)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Send, "Отправить", tint = if (draft.isBlank()) NovaColors.Muted else Color.White, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: NovaMessage, mine: Boolean, mediaUrl: (String?) -> String?) {
    val shape = if (mine) {
        RoundedCornerShape(topStart = 17.dp, topEnd = 17.dp, bottomStart = 17.dp, bottomEnd = 5.dp)
    } else {
        RoundedCornerShape(topStart = 17.dp, topEnd = 17.dp, bottomStart = 5.dp, bottomEnd = 17.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!mine) {
            NovaAvatar(
                NovaUser(
                    id = message.sender.id,
                    username = message.sender.username,
                    displayName = message.sender.displayName,
                    avatarUrl = message.sender.avatarUrl
                ),
                mediaUrl(message.sender.avatarUrl),
                30
            )
            Spacer(Modifier.width(6.dp))
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .clip(shape)
                    .then(
                        if (mine) Modifier.background(
                            Brush.linearGradient(listOf(Color(0xFF338FDC), Color(0xFF2878BD)))
                        ) else Modifier
                            .background(NovaColors.PanelSolid)
                            .border(1.dp, NovaColors.Line, shape)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (!mine) {
                    Text(
                        message.sender.displayName,
                        color = NovaColors.Accent2,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (message.deletedAt != null) {
                    Text(
                        "Сообщение удалено",
                        color = if (mine) Color.White.copy(alpha = 0.68f) else NovaColors.Muted,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp
                    )
                } else {
                    Text(
                        message.body,
                        color = if (mine) Color.White else NovaColors.Text,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.editedAt != null) {
                        Text(
                            "изм. ",
                            color = if (mine) Color.White.copy(alpha = 0.62f) else NovaColors.Muted2,
                            fontSize = 8.5.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        relativeTime(message.createdAt),
                        color = if (mine) Color.White.copy(alpha = 0.64f) else NovaColors.Muted2,
                        fontSize = 8.8.sp
                    )
                    if (mine) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (message.readByPeer) "✓✓" else "✓",
                            color = Color.White.copy(alpha = if (message.readByPeer) 0.95f else 0.62f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaLogo(size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.31f).dp))
            .background(Brush.linearGradient(listOf(Color(0xFF5BB8FF), NovaColors.Accent, Color(0xFF3867E8)))),
        contentAlignment = Alignment.Center
    ) {
        Text("N", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size * 0.38f).sp)
    }
}

@Composable
private fun NovaAvatar(user: NovaUser?, imageUrl: String?, size: Int) {
    val initials = user?.displayName
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.take(1).uppercase() }
        ?.takeIf { it.isNotBlank() }
        ?: "N"

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF5D7CFF), Color(0xFF2E8ADF)))),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(model = imageUrl, contentDescription = user?.displayName, modifier = Modifier.fillMaxSize())
        } else {
            Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.30f).sp)
        }
    }
}

@Composable
private fun OnlineDot(modifier: Modifier) {
    Box(
        modifier = modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(NovaColors.Good)
            .border(BorderStroke(2.dp, NovaColors.PanelSolid), CircleShape)
    )
}

private fun subtitle(state: NovaState): String {
    val typing = state.typingUsers.firstOrNull()
    if (typing != null) {
        return when (typing.mode) {
            "voice-recording" -> typing.displayName + " записывает голосовое…"
            "voice-sending" -> typing.displayName + " отправляет голосовое…"
            else -> typing.displayName + " печатает…"
        }
    }
    if (state.selected?.system == true) return "официальный канал"
    return if (state.selected?.peer?.online == true) "онлайн" else "был(а) недавно"
}

private fun relativeTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val local = Instant.parse(iso).atZone(ZoneId.systemDefault())
        val now = ZonedDateTime.now()
        if (local.toLocalDate() == now.toLocalDate()) {
            String.format("%02d:%02d", local.hour, local.minute)
        } else {
            String.format("%02d.%02d", local.dayOfMonth, local.monthValue)
        }
    }.getOrDefault("")
}

private object NovaNotifications {
    private const val CHANNEL = "nova_messages"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Сообщения NOVA", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    fun show(context: Context, title: String, body: String, conversationId: Long) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (conversationId > 0) putExtra(MainActivity.EXTRA_CONVERSATION_ID, conversationId)
        }
        val pending = PendingIntent.getActivity(
            context,
            conversationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(
            if (conversationId > 0) conversationId.toInt() else 1001,
            notification
        )
    }
}

class NovaFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repository = NovaRepository(applicationContext)
        repository.session.fcmToken = token
        if (!repository.session.token.isNullOrBlank()) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                runCatching { repository.registerPush(token) }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (MainActivity.foreground) return
        val title = message.data["title"] ?: message.notification?.title ?: "NOVA"
        val body = message.data["body"] ?: message.notification?.body ?: "Новое сообщение"
        val conversationId = message.data["conversationId"]?.toLongOrNull() ?: 0L
        NovaNotifications.show(applicationContext, title, body, conversationId)
    }
}
