package com.nova.messenger.native2.ui

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nova.messenger.native2.Conversation
import com.nova.messenger.native2.FriendRequestItem
import com.nova.messenger.native2.NovaUiState
import com.nova.messenger.native2.NovaUser
import com.nova.messenger.native2.RootTab
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ChatsScreen(
    state: NovaUiState,
    mediaUrl: (String?) -> String?,
    onOpen: (Conversation) -> Unit,
    onTab: (RootTab) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }

    val searched = if (search.isBlank()) {
        state.conversations
    } else {
        state.conversations.filter {
            it.title.contains(search, ignoreCase = true) ||
                (it.peer?.username?.contains(search, ignoreCase = true) == true) ||
                (it.lastMessage?.body?.contains(search, ignoreCase = true) == true)
        }
    }

    val filtered = when (filter) {
        "new" -> searched.filter { it.unread > 0 }
        "online" -> searched.filter { it.peer?.online == true }
        else -> searched
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaPalette.Bg2)
    ) {
        BrandHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 12.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D1D2A))
                .border(1.dp, Color(0xFF18354A), RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = NovaPalette.Text,
                modifier = Modifier.padding(start = 14.dp).size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = TextStyle(color = NovaPalette.Text, fontSize = 14.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (search.isEmpty()) {
                            Text("Поиск по чатам", color = Color(0xFF7E90A6), fontSize = 14.sp)
                        }
                        inner()
                    }
                }
            )
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF0B3148))
                    .border(1.dp, Color(0xFF145777), RoundedCornerShape(13.dp))
                    .clickable { onTab(RootTab.CONTACTS) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PersonAdd,
                    contentDescription = "Добавить контакт",
                    tint = NovaPalette.Accent2,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Чаты", color = NovaPalette.Text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            CountChip(state.conversations.size)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterButton("Все", filter == "all", Modifier.weight(1f)) { filter = "all" }
            FilterButton("Новые", filter == "new", Modifier.weight(1f)) { filter = "new" }
            FilterButton("Онлайн", filter == "online", Modifier.weight(1f)) { filter = "online" }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp)
        ) {
            items(filtered, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    myUserId = state.user?.id ?: 0L,
                    mediaUrl = mediaUrl,
                    onOpen = onOpen
                )
            }
        }

        BottomNav(
            active = RootTab.CHATS,
            incomingCount = state.friends.incomingCount,
            onTab = onTab
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    myUserId: Long,
    mediaUrl: (String?) -> String?,
    onOpen: (Conversation) -> Unit
) {
    val last = conversation.lastMessage
    val mine = last?.senderId == myUserId && myUserId > 0
    val preview = when {
        last == null -> if (conversation.system) "Официальный канал NOVA" else "Начните диалог"
        last.body.isBlank() -> "Вложение"
        else -> last.body
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpen(conversation) }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            NovaAvatar(
                user = conversation.peer ?: NovaUser(displayName = conversation.title, accent = "pink"),
                imageUrl = mediaUrl(conversation.peer?.avatarUrl),
                size = 52
            )
            if (conversation.peer?.online == true) {
                OnlineDot(Modifier.align(Alignment.BottomEnd))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                conversation.title,
                color = NovaPalette.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(modifier = Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                if (mine) {
                    Text(
                        if (conversation.peerReadMessageId >= (last?.id ?: Long.MAX_VALUE)) "✓✓" else "✓",
                        color = NovaPalette.Muted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    preview,
                    color = Color(0xFF8191A6),
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(relativeTime(last?.createdAt), color = Color(0xFF6C7E94), fontSize = 10.5.sp)
            if (conversation.unread > 0) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(NovaPalette.Accent)
                        .padding(horizontal = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.unread.coerceAtMost(99).toString(),
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun ContactsScreen(
    state: NovaUiState,
    mediaUrl: (String?) -> String?,
    onSearch: (String) -> Unit,
    onRequest: (NovaUser) -> Unit,
    onRespond: (FriendRequestItem, String) -> Unit,
    onOpenFriend: (NovaUser, Long) -> Unit,
    onTab: (RootTab) -> Unit
) {
    var section by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        ModalHeader(
            eyebrow = "CONTACTS",
            title = "Контакты",
            description = "Находи людей по username, добавляй их в\nконтакты и открывай диалоги только с контактами.",
            onClose = { onTab(RootTab.CHATS) }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            SegmentTab("Контакты", Icons.Rounded.PeopleOutline, section == 0, Modifier.weight(1f)) { section = 0 }
            SegmentTab(
                if (state.friends.incomingCount > 0) "Запросы " + state.friends.incomingCount else "Запросы",
                Icons.Rounded.ChatBubbleOutline,
                section == 1,
                Modifier.weight(1f)
            ) { section = 1 }
            SegmentTab("Добавить", Icons.Rounded.PersonAdd, section == 2, Modifier.weight(1f)) { section = 2 }
        }

        if (section != 1) {
            SearchField(
                value = state.contactSearch,
                onValueChange = onSearch,
                placeholder = if (section == 0) "Найти контакты по username..." else "Найти пользователя по username...",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
            )
        }

        if (section == 0) {
            InfoCard("Здесь показываются только твои контакты. Нажми\nна пользователя, чтобы открыть переписку.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 7.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (section) {
                0 -> {
                    val query = state.contactSearch.trim()
                    val contacts = if (query.isBlank()) {
                        state.friends.friends
                    } else {
                        state.friends.friends.filter {
                            it.user.displayName.contains(query, true) || it.user.username.contains(query, true)
                        }
                    }

                    items(contacts, key = { "friend-" + it.user.id }) { item ->
                        ContactCard(
                            user = item.user,
                            mediaUrl = mediaUrl,
                            onClick = { onOpenFriend(item.user, item.conversationId) }
                        )
                    }
                }

                1 -> {
                    if (state.friends.incoming.isNotEmpty()) {
                        item { SectionLabel("ВХОДЯЩИЕ") }
                        items(state.friends.incoming, key = { "incoming-" + it.id }) { request ->
                            RequestCard(
                                request = request,
                                mediaUrl = mediaUrl,
                                onAccept = { onRespond(request, "accept") },
                                onDecline = { onRespond(request, "decline") }
                            )
                        }
                    }

                    if (state.friends.outgoing.isNotEmpty()) {
                        item { SectionLabel("ИСХОДЯЩИЕ") }
                        items(state.friends.outgoing, key = { "outgoing-" + it.id }) { request ->
                            ContactCard(user = request.user, mediaUrl = mediaUrl, trailingText = "Отправлено")
                        }
                    }
                }

                else -> {
                    if (state.contactSearch.trim().length < 2) {
                        item {
                            InfoCard(
                                "Введи хотя бы два символа username, чтобы\nнайти нового пользователя NOVA.",
                                outsidePadding = false
                            )
                        }
                    } else {
                        items(state.searchUsers, key = { "search-" + it.id }) { user ->
                            AddUserCard(
                                user = user,
                                mediaUrl = mediaUrl,
                                onRequest = { onRequest(user) },
                                onOpenFriend = { onOpenFriend(user, user.conversationId) }
                            )
                        }
                    }
                }
            }
        }

        BottomNav(active = RootTab.CONTACTS, incomingCount = state.friends.incomingCount, onTab = onTab)
    }
}

@Composable
fun ProfileScreen(
    state: NovaUiState,
    mediaUrl: (String?) -> String?,
    onSave: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onTab: (RootTab) -> Unit
) {
    val context = LocalContext.current
    val user = state.user ?: return
    var editing by remember(user.id) { mutableStateOf(false) }
    var displayName by remember(user.id, user.displayName) { mutableStateOf(user.displayName) }
    var statusText by remember(user.id, user.statusText) { mutableStateOf(user.statusText) }
    var bio by remember(user.id, user.bio) { mutableStateOf(user.bio) }

    Column(modifier = Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        ModalHeader(eyebrow = "PROFILE", title = "Профиль", onClose = { onTab(RootTab.CHATS) })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { ProfileHero(user = user, mediaUrl = mediaUrl) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileActionButton(
                        text = "Редактировать профиль",
                        icon = Icons.Rounded.Edit,
                        primary = true,
                        modifier = Modifier.weight(1.25f)
                    ) { editing = !editing }

                    ProfileActionButton(
                        text = "Поделиться",
                        icon = Icons.Rounded.Share,
                        primary = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "NOVA — @" + user.username)
                        }
                        context.startActivity(Intent.createChooser(share, "Поделиться профилем"))
                    }
                }
            }

            if (editing) {
                item {
                    EditProfileCard(
                        displayName = displayName,
                        statusText = statusText,
                        bio = bio,
                        busy = state.busy,
                        onDisplayName = { displayName = it },
                        onStatus = { statusText = it },
                        onBio = { bio = it },
                        onSave = {
                            onSave(displayName, bio, statusText)
                            editing = false
                        }
                    )
                }
            }

            item {
                ProfileInfoCard(
                    icon = Icons.Rounded.Info,
                    title = "О себе",
                    value = user.bio.ifBlank { "Пока ничего не добавлено." }
                )
            }

            item { ProfileDetailsCard(user) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1D29))
                        .border(1.dp, Color(0xFF1B3346), RoundedCornerShape(16.dp))
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null, tint = NovaPalette.Danger, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Выйти из аккаунта", color = NovaPalette.Danger, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        BottomNav(active = RootTab.PROFILE, incomingCount = state.friends.incomingCount, onTab = onTab)
    }
}

@Composable
fun SettingsScreen(
    state: NovaUiState,
    mediaUrl: (String?) -> String?,
    onCompactChanged: (Boolean) -> Unit,
    onBubbleSizeChanged: (Int) -> Unit,
    onBubbleThemeChanged: (String) -> Unit,
    onTab: (RootTab) -> Unit
) {
    val user = state.user
    var section by remember { mutableIntStateOf(0) }
    var bubbleSlider by remember(state.bubbleSize) {
        mutableStateOf(state.bubbleSize.toFloat())
    }

    Column(modifier = Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        ModalHeader(
            eyebrow = "SETTINGS",
            title = "Настройки",
            onClose = { onTab(RootTab.CHATS) }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 13.dp,
                end = 13.dp,
                top = 12.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF092330), Color(0xFF0A1823))
                            )
                        )
                        .border(1.dp, Color(0xFF155068), RoundedCornerShape(22.dp))
                        .clickable { onTab(RootTab.PROFILE) }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NovaAvatar(
                        user = user,
                        imageUrl = mediaUrl(user?.avatarUrl),
                        size = 58
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            user?.displayName ?: "NOVA",
                            color = NovaPalette.Text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "@" + user?.username.orEmpty(),
                            color = NovaPalette.Accent2,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            "Аккаунт и параметры NOVA",
                            color = NovaPalette.Muted,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF07324A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = NovaPalette.Accent2,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    SettingsTab("Вид", section == 0, Modifier.weight(0.8f)) { section = 0 }
                    SettingsTab("Чаты", section == 1, Modifier.weight(0.9f)) { section = 1 }
                    SettingsTab("Приватность", section == 2, Modifier.weight(1.45f)) { section = 2 }
                    SettingsTab("Система", section == 3, Modifier.weight(1.15f)) { section = 3 }
                }
            }

            when (section) {
                0 -> {
                    item { SectionLabel("ВНЕШНИЙ ВИД") }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF091520))
                                .border(1.dp, Color(0xFF173246), RoundedCornerShape(22.dp))
                                .padding(8.dp)
                        ) {
                            SettingRow(
                                title = "Тема приложения",
                                subtitle = "NOVA Dark",
                                trailing = {
                                    Box(
                                        Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(Color(0xFF142434)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.DarkMode,
                                            contentDescription = null,
                                            tint = Color(0xFF8EA1B6),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            SettingRow(
                                title = "Компактный режим",
                                subtitle = "Более плотный список чатов и сообщений",
                                trailing = {
                                    Icon(
                                        if (state.compact) Icons.Rounded.ToggleOn else Icons.Rounded.ToggleOff,
                                        contentDescription = null,
                                        tint = if (state.compact) NovaPalette.Accent2 else Color(0xFF365269),
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clickable {
                                                onCompactChanged(!state.compact)
                                            }
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Column {
                            Text(
                                "Фон и сообщения",
                                color = NovaPalette.Text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Text(
                                "Тот же полноценный предпросмотр, что и на ПК.",
                                color = NovaPalette.Muted,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    item {
                        ChatStylePreview(
                            bubbleTheme = state.bubbleTheme,
                            bubbleSize = state.bubbleSize
                        )
                    }

                    item {
                        BubbleSizePanel(
                            value = bubbleSlider,
                            onValueChange = {
                                bubbleSlider = it
                                onBubbleSizeChanged(it.toInt())
                            },
                            onPreset = {
                                bubbleSlider = it.toFloat()
                                onBubbleSizeChanged(it)
                            }
                        )
                    }

                    item {
                        BubbleThemePanel(
                            selected = state.bubbleTheme,
                            onSelect = onBubbleThemeChanged
                        )
                    }
                }

                1 -> {
                    item { SectionLabel("ЧАТЫ") }
                    item {
                        SettingsInfoPanel(
                            "Сообщения и медиа",
                            "Размер и тема пузырьков уже применяются сразу во всех нативных диалогах."
                        )
                    }
                }

                2 -> {
                    item { SectionLabel("ПРИВАТНОСТЬ") }
                    item {
                        SettingsInfoPanel(
                            "Приватность",
                            "Кто видит онлайн, время последнего посещения и кто может добавлять в контакты."
                        )
                    }
                }

                else -> {
                    item { SectionLabel("СИСТЕМА") }
                    item {
                        SettingsInfoPanel(
                            "NOVA Native",
                            "Android 2.0.0 alpha · общий backend с веб-NOVA."
                        )
                    }
                    item {
                        SettingsInfoPanel(
                            "Уведомления",
                            "FCM используется для фоновой доставки сообщений."
                        )
                    }
                }
            }
        }

        BottomNav(
            active = RootTab.SETTINGS,
            incomingCount = state.friends.incomingCount,
            onTab = onTab
        )
    }
}

@Composable
private fun ChatStylePreview(
    bubbleTheme: String,
    bubbleSize: Int
) {
    val scale = bubbleSize.coerceIn(80, 130) / 100f
    val mine = when (bubbleTheme) {
        "glass" -> Brush.verticalGradient(listOf(Color(0xA82799DC), Color(0x941167A5)))
        "graphite" -> Brush.verticalGradient(listOf(Color(0xFF334B5B), Color(0xFF263A48)))
        "violet" -> Brush.linearGradient(listOf(Color(0xFF7658D9), Color(0xFF4B75DF)))
        "sakura" -> Brush.linearGradient(listOf(Color(0xFFDE6A9A), Color(0xFFB84C7E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF268FD4), Color(0xFF196FB1)))
    }
    val theirs = when (bubbleTheme) {
        "graphite" -> Brush.verticalGradient(listOf(Color(0xFF303943), Color(0xFF252D35)))
        "violet" -> Brush.verticalGradient(listOf(Color(0xFF302C4D), Color(0xFF24253D)))
        "sakura" -> Brush.verticalGradient(listOf(Color(0xFF3A2933), Color(0xFF30222B)))
        else -> Brush.verticalGradient(listOf(Color(0xC21B2F40), Color(0xB811212F)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF082235), Color(0xFF071927))
                )
            )
            .border(1.dp, Color(0xFF13506A), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        Text(
            "Сегодня",
            color = NovaPalette.Muted,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF0B2537))
                .padding(horizontal = 11.dp, vertical = 5.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(14.dp))
                .background(theirs)
                .padding(
                    horizontal = (10f * scale).dp,
                    vertical = (8f * scale).dp
                )
        ) {
            Text(
                "Привет!",
                color = Color.White,
                fontSize = (11.6f * scale).sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "14:55",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = (8.6f * scale).sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(14.dp))
                .background(mine)
                .padding(
                    horizontal = (10f * scale).dp,
                    vertical = (8f * scale).dp
                )
        ) {
            Text(
                "Всё отлично, работаю",
                color = Color.White,
                fontSize = (11.6f * scale).sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "14:57 ✓✓",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = (8.6f * scale).sp
            )
        }
    }
}

@Composable
private fun BubbleSizePanel(
    value: Float,
    onValueChange: (Float) -> Unit,
    onPreset: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF071923))
            .border(1.dp, Color(0xFF173B50), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "СООБЩЕНИЯ",
                    color = NovaPalette.Accent2,
                    fontSize = 8.8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Text(
                    "Размер пузырьков",
                    color = NovaPalette.Text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "Размер меняется сразу и в предпросмотре, и во всех чатах.",
                    color = NovaPalette.Muted,
                    fontSize = 9.7.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF082E44))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        value.toInt().toString() + "%",
                        color = NovaPalette.Text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        when {
                            value < 93 -> "Компакт"
                            value < 108 -> "Стандарт"
                            value < 123 -> "Крупные"
                            else -> "Максимум"
                        },
                        color = NovaPalette.Accent2,
                        fontSize = 8.8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf(
                85 to "Компактные",
                100 to "Стандарт",
                115 to "Крупные",
                130 to "Максимум"
            ).forEach { (size, label) ->
                val active = kotlin.math.abs(value - size) <= 3f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (active) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF278FD2), Color(0xFF1C6FB0))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF102738), Color(0xFF102738))
                                )
                            }
                        )
                        .clickable { onPreset(size) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (active) Color.White else Color(0xFF91A9BB),
                        fontSize = 8.7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("A", color = NovaPalette.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 80f..130f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE9ECEF),
                    activeTrackColor = NovaPalette.Accent,
                    inactiveTrackColor = Color(0xFF332F39)
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text("A", color = NovaPalette.Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BubbleThemePanel(
    selected: String,
    onSelect: (String) -> Unit
) {
    val themes = listOf(
        Triple("nova", "NOVA", "Фирменный синий"),
        Triple("glass", "Glass", "Прозрачное стекло"),
        Triple("graphite", "Graphite", "Графитовый"),
        Triple("violet", "Violet", "Фиолетовый"),
        Triple("sakura", "Sakura", "Розовый")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF071923))
            .border(1.dp, Color(0xFF173B50), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Text(
            "СТИЛЬ",
            color = NovaPalette.Accent2,
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.6.sp
        )
        Text(
            "Тема пузырьков",
            color = NovaPalette.Text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            "Оформление сообщений меняется сразу и остаётся только на этом устройстве.",
            color = NovaPalette.Muted,
            fontSize = 9.7.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )

        themes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (id, name, subtitle) ->
                    BubbleThemeCard(
                        id = id,
                        name = name,
                        subtitle = subtitle,
                        active = selected == id,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(id) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BubbleThemeCard(
    id: String,
    name: String,
    subtitle: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val mine = when (id) {
        "glass" -> Brush.verticalGradient(listOf(Color(0xA82799DC), Color(0x941167A5)))
        "graphite" -> Brush.verticalGradient(listOf(Color(0xFF334B5B), Color(0xFF263A48)))
        "violet" -> Brush.linearGradient(listOf(Color(0xFF7658D9), Color(0xFF4B75DF)))
        "sakura" -> Brush.linearGradient(listOf(Color(0xFFDE6A9A), Color(0xFFB84C7E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF268FD4), Color(0xFF196FB1)))
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(if (active) Color(0xFF0A2C40) else Color(0xFF06141F))
            .border(
                1.dp,
                if (active) Color(0xFF14AEEB) else Color(0xFF173144),
                RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF06121E), Color(0xFF081D2B))
                    )
                )
        ) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(9.dp)
                    .width(70.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF253442))
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(9.dp)
                    .width(82.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(mine)
            )
        }

        Text(
            name,
            color = NovaPalette.Text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            subtitle,
            color = NovaPalette.Muted,
            fontSize = 8.2.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color(0xFF06131D))
            .border(0.5.dp, Color(0xFF112938))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0056FF), Color(0xFF00C7FF))))
                .border(1.dp, Color(0xFF21D3FF), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("N", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }

        Column(Modifier.padding(start = 13.dp)) {
            Text("N O V A", color = NovaPalette.Text, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("Мессенджер · 3.8.89", color = Color(0xFF8193A8), fontSize = 10.5.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF0A1E2B)).border(1.dp, Color(0xFF1A3648), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF00D6AF)))
        }
    }
}

@Composable
private fun ModalHeader(
    eyebrow: String,
    title: String,
    description: String? = null,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF061722))
            .border(0.5.dp, Color(0xFF102D3E))
            .padding(start = 25.dp, end = 22.dp, top = 22.dp, bottom = 18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow, color = NovaPalette.Accent2, fontSize = 9.5.sp, fontWeight = FontWeight.Black, letterSpacing = 2.2.sp)
                Text(title, color = NovaPalette.Text, fontSize = 27.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp))
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF0D1B27))
                    .border(1.dp, Color(0xFF1B3142), RoundedCornerShape(15.dp))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Закрыть", tint = Color(0xFF8CA0B5), modifier = Modifier.size(25.dp))
            }
        }

        if (!description.isNullOrBlank()) {
            Text(description, color = Color(0xFF8295AA), fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFF0B1B28))
            .border(1.dp, Color(0xFF17405A), RoundedCornerShape(17.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF8299AF), modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = NovaPalette.Text, fontSize = 13.5.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, color = Color(0xFF6F8398), fontSize = 13.5.sp)
                    inner()
                }
            }
        )
    }
}

@Composable
private fun FilterButton(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Color(0xFF0A2B3D) else Color(0xFF0A141F))
            .border(1.dp, if (active) Color(0xFF0C6E94) else Color(0xFF172737), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (active) NovaPalette.Accent2 else NovaPalette.Muted, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun CountChip(count: Int) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF102737))
            .border(1.dp, Color(0xFF173D54), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(count.toString(), color = Color(0xFF7E91A7), fontSize = 9.5.sp)
    }
}

@Composable
private fun SegmentTab(
    text: String,
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Color(0xFF0B3148) else Color(0xFF091620))
            .border(1.dp, if (active) Color(0xFF0EA7E4) else Color(0xFF173144), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (active) NovaPalette.Accent2 else NovaPalette.Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = if (active) NovaPalette.Text else NovaPalette.Muted, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun InfoCard(text: String, outsidePadding: Boolean = true) {
    Row(
        modifier = Modifier
            .then(if (outsidePadding) Modifier.padding(horizontal = 22.dp, vertical = 6.dp) else Modifier)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF091722))
            .border(1.dp, Color(0xFF183448), RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).border(1.dp, Color(0xFF315A73), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null, tint = Color(0xFF8EA4B8), modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(9.dp))
        Text(text, color = Color(0xFF8194A8), fontSize = 10.5.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun ContactCard(
    user: NovaUser,
    mediaUrl: (String?) -> String?,
    onClick: (() -> Unit)? = null,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF071722))
            .border(1.dp, Color(0xFF123247), RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            NovaAvatar(user = user, imageUrl = mediaUrl(user.avatarUrl), size = 47)
            if (user.online) OnlineDot(Modifier.align(Alignment.BottomEnd))
        }

        Spacer(Modifier.width(11.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.displayName, color = NovaPalette.Text, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (user.online) {
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF00D6AE)))
                }
            }
            Text("@" + user.username, color = Color(0xFF7E91A6), fontSize = 10.5.sp, modifier = Modifier.padding(top = 3.dp))
        }

        if (trailingText != null) {
            Text(trailingText, color = NovaPalette.Muted, fontSize = 9.5.sp)
        } else if (onClick != null) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0A1B28))
                    .border(1.dp, Color(0xFF173649), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFF7D9DB2), modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: FriendRequestItem,
    mediaUrl: (String?) -> String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF071722))
            .border(1.dp, Color(0xFF123247), RoundedCornerShape(18.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NovaAvatar(request.user, mediaUrl(request.user.avatarUrl), 47)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(request.user.displayName, color = NovaPalette.Text, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
            Text("@" + request.user.username, color = NovaPalette.Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
        }
        RoundAction(Icons.Rounded.CheckCircle, NovaPalette.Good, onAccept)
        Spacer(Modifier.width(5.dp))
        RoundAction(Icons.Rounded.Close, NovaPalette.Danger, onDecline)
    }
}

@Composable
private fun AddUserCard(
    user: NovaUser,
    mediaUrl: (String?) -> String?,
    onRequest: () -> Unit,
    onOpenFriend: () -> Unit
) {
    ContactCard(
        user = user,
        mediaUrl = mediaUrl,
        onClick = if (user.relation == "friend" && user.conversationId > 0) onOpenFriend else null,
        trailingText = when (user.relation) {
            "friend" -> "В контактах"
            "outgoing" -> "Отправлено"
            "incoming" -> "Входящий"
            else -> null
        }
    )

    if (user.relation != "friend" && user.relation != "outgoing" && user.relation != "incoming") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "+ Добавить",
                color = NovaPalette.Accent2,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 3.dp, end = 8.dp).clickable(onClick = onRequest).padding(5.dp)
            )
        }
    }
}

@Composable
private fun ProfileHero(user: NovaUser, mediaUrl: (String?) -> String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF174662), Color(0xFF0D2332), Color(0xFF07141D))))
            .border(1.dp, Color(0xFF2B7699), RoundedCornerShape(24.dp))
    ) {
        val cover = mediaUrl(user.coverUrl)
        if (!cover.isNullOrBlank()) {
            AsyncImage(model = cover, contentDescription = "Обложка", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xC908131B))))
        )

        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(15.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier.size(91.dp).clip(CircleShape).background(Color(0xFF07121A))
                    .border(3.dp, Color(0xFF00C9FF), CircleShape).padding(4.dp)
            ) {
                NovaAvatar(user = user, imageUrl = mediaUrl(user.avatarUrl), size = 83)
            }

            Spacer(Modifier.width(13.dp))

            Column(modifier = Modifier.padding(bottom = 7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.displayName, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    if (user.system || user.systemRole.isNotBlank()) {
                        Spacer(Modifier.width(7.dp))
                        Box(Modifier.size(21.dp).clip(CircleShape).background(Color(0xFF17B8FF)), contentAlignment = Alignment.Center) {
                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Text("@" + user.username, color = Color(0xFF15C2FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))

                Row(modifier = Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape)
                            .background(if (user.online) Color(0xFF00D6AF) else Color(0xFF64798D))
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        if (user.online) "в сети" else "не в сети",
                        color = if (user.online) Color(0xFF66E7CB) else Color(0xFF8A9BAC),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    icon: ImageVector,
    primary: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(17.dp))
            .then(
                if (primary) {
                    Modifier.background(Brush.linearGradient(listOf(Color(0xFF16C6FF), Color(0xFF079AE8))))
                } else {
                    Modifier.background(Color(0xFF091A25)).border(1.dp, Color(0xFF17435B), RoundedCornerShape(17.dp))
                }
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun EditProfileCard(
    displayName: String,
    statusText: String,
    bio: String,
    busy: Boolean,
    onDisplayName: (String) -> Unit,
    onStatus: (String) -> Unit,
    onBio: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF091923))
            .border(1.dp, Color(0xFF174058), RoundedCornerShape(20.dp)).padding(12.dp)
    ) {
        EditField("Имя", displayName, onDisplayName)
        Spacer(Modifier.height(8.dp))
        EditField("Статус", statusText, onStatus)
        Spacer(Modifier.height(8.dp))
        EditField("О себе", bio, onBio)
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).background(NovaPalette.Accent)
                .clickable(enabled = !busy, onClick = onSave),
            contentAlignment = Alignment.Center
        ) {
            Text(if (busy) "Сохраняю…" else "Сохранить", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, color = NovaPalette.Muted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 3.dp, bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = NovaPalette.Text, fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF10202C))
                .border(1.dp, Color(0xFF203B4E), RoundedCornerShape(13.dp)).padding(horizontal = 12.dp),
            decorationBox = { inner -> Box(contentAlignment = Alignment.CenterStart) { inner() } }
        )
    }
}

@Composable
private fun ProfileInfoCard(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF081923))
            .border(1.dp, Color(0xFF15405A), RoundedCornerShape(20.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF0A3148))
                .border(1.dp, Color(0xFF0D5979), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NovaPalette.Accent2, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column {
            Text(title, color = NovaPalette.Text, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Text(value, color = Color(0xFFB1BFCD), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun ProfileDetailsCard(user: NovaUser) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF081923))
            .border(1.dp, Color(0xFF15405A), RoundedCornerShape(20.dp)).padding(vertical = 4.dp)
    ) {
        DetailRow(icon = Icons.Rounded.PersonOutline, label = "Имя пользователя", value = "@" + user.username, trailingIcon = Icons.Rounded.ContentCopy)

        if (user.birthDate.isNotBlank()) {
            Separator()
            DetailRow(icon = Icons.Rounded.Cake, label = "Дата рождения", value = user.birthDate)
        }

        Separator()
        DetailRow(icon = Icons.Rounded.Info, label = "NOVA ID", value = user.id.toString())
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    trailingIcon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NovaPalette.Accent2, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Color(0xFF74879B), fontSize = 9.sp)
            Text(value, color = NovaPalette.Text, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }
        trailingIcon?.let {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF092A3D)), contentAlignment = Alignment.Center) {
                Icon(it, contentDescription = null, tint = NovaPalette.Accent2, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun SettingsTab(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(36.dp).clip(RoundedCornerShape(13.dp))
            .background(if (active) Color(0xFF0B2A3B) else Color(0xFF0B1B27))
            .border(1.dp, if (active) Color(0xFF0D5979) else Color(0xFF173246), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (active) NovaPalette.Text else Color(0xFFA2B2C2), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF10202C))
            .border(1.dp, Color(0xFF203848), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = NovaPalette.Text, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Color(0xFF7E91A5), fontSize = 9.5.sp, modifier = Modifier.padding(top = 3.dp))
        }
        trailing()
    }
}

@Composable
private fun SettingsInfoPanel(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF0C1B27))
            .border(1.dp, Color(0xFF1A3346), RoundedCornerShape(20.dp)).padding(15.dp)
    ) {
        Text(title, color = NovaPalette.Text, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = NovaPalette.Muted, fontSize = 10.5.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun RoundAction(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF102330)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Color(0xFF879AAF),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun Separator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(1.dp).background(Color.White.copy(alpha = 0.045f))
    )
}

@Composable
fun BottomNav(
    active: RootTab,
    incomingCount: Int,
    onTab: (RootTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color(0xFF06121B))
            .border(0.5.dp, Color(0xFF153044))
            .padding(horizontal = 4.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavButton("Чаты", Icons.Rounded.ChatBubbleOutline, active == RootTab.CHATS, Modifier.weight(1f)) { onTab(RootTab.CHATS) }
        NavButton(if (incomingCount > 0) "Контакты · " + incomingCount else "Контакты", Icons.Rounded.PersonAdd, active == RootTab.CONTACTS, Modifier.weight(1f)) { onTab(RootTab.CONTACTS) }
        NavButton("Профиль", Icons.Rounded.PersonOutline, active == RootTab.PROFILE, Modifier.weight(1f)) { onTab(RootTab.PROFILE) }
        NavButton("Настройки", Icons.Rounded.Settings, active == RootTab.SETTINGS, Modifier.weight(1f)) { onTab(RootTab.SETTINGS) }
    }
}

@Composable
private fun NavButton(label: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(58.dp).clip(RoundedCornerShape(17.dp))
            .background(if (active) Color(0xFF082B40) else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (active) Color(0xFF17BFFF) else Color(0xFF8499AE), modifier = Modifier.size(23.dp))
        Text(label, color = if (active) Color(0xFF1ABFFF) else Color(0xFF8194A8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun NovaAvatar(user: NovaUser?, imageUrl: String?, size: Int) {
    val initials = user?.displayName
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.take(1).uppercase() }
        ?.takeIf { it.isNotBlank() }
        ?: "N"

    val gradient = when (user?.accent?.lowercase()) {
        "pink", "rose" -> listOf(Color(0xFFFF2E7A), Color(0xFFFF5D91))
        "violet", "purple" -> listOf(Color(0xFF7A5CFF), Color(0xFF4968FF))
        "cyan" -> listOf(Color(0xFF00C8FF), Color(0xFF1479FF))
        "green" -> listOf(Color(0xFF20C997), Color(0xFF0A8F7A))
        else -> listOf(Color(0xFF2D8CFF), Color(0xFF1A74F2))
    }

    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(model = imageUrl, contentDescription = user?.displayName, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.31f).sp)
        }
    }
}

@Composable
fun OnlineDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00D5AE))
            .border(2.dp, NovaPalette.Bg2, CircleShape)
    )
}

fun relativeTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""

    return runCatching {
        val local = Instant.parse(iso).atZone(ZoneId.systemDefault())
        val now = ZonedDateTime.now()

        if (local.toLocalDate() == now.toLocalDate()) {
            "%02d:%02d".format(local.hour, local.minute)
        } else {
            "%02d.%02d".format(local.dayOfMonth, local.monthValue)
        }
    }.getOrDefault("")
}
