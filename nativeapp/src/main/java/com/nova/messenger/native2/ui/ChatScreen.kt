package com.nova.messenger.native2.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.messenger.native2.NovaMessage
import com.nova.messenger.native2.NovaUiState
import com.nova.messenger.native2.NovaUser
import com.nova.messenger.native2.RootTab

@Composable
fun ChatScreen(
    state: NovaUiState,
    mediaUrl: (String?) -> String?,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onAttachment: (Uri) -> Unit,
    onEditMessage: (NovaMessage, String) -> Unit,
    onDeleteMessage: (NovaMessage) -> Unit,
    onReactMessage: (NovaMessage, String) -> Unit,
    onDraftChanged: (Boolean) -> Unit,
    onNavigateRoot: (RootTab) -> Unit,
    wallpaperId: (Long) -> String,
    wallpaperDim: (Long) -> Int,
    wallpaperAtmosphere: (Long) -> String,
    onProposeWallpaper: (String, Int, String) -> Unit,
    onResetWallpaper: () -> Unit,
    onRespondWallpaper: (NovaMessage, String) -> Unit,
    onRemoveFriend: (NovaUser) -> Unit
) {
    val conversation = state.selectedConversation ?: return
    val currentUser = state.user ?: return

    var draft by remember(conversation.id) { mutableStateOf("") }
    var showEmoji by remember(conversation.id) { mutableStateOf(false) }
    var selectedMessage by remember(conversation.id) { mutableStateOf<NovaMessage?>(null) }
    var editingMessage by remember(conversation.id) { mutableStateOf<NovaMessage?>(null) }
    var showMenu by remember(conversation.id) { mutableStateOf(false) }
    var showWallpaper by remember(conversation.id) { mutableStateOf(false) }
    var showPeerProfile by remember(conversation.id) { mutableStateOf(false) }
    var localWallpaperId by remember(conversation.id) {
        mutableStateOf(wallpaperId(conversation.id))
    }
    var localWallpaperDim by remember(conversation.id) {
        mutableStateOf(wallpaperDim(conversation.id))
    }
    var localAtmosphere by remember(conversation.id) {
        mutableStateOf(wallpaperAtmosphere(conversation.id))
    }
    val sharedWallpaper = conversation.sharedWallpaper
    val effectiveWallpaperId = sharedWallpaper?.id ?: localWallpaperId
    val effectiveWallpaperDim = sharedWallpaper?.dim ?: localWallpaperDim
    val effectiveAtmosphere = sharedWallpaper?.atmosphere ?: localAtmosphere
    val wallpaperPath = when (effectiveWallpaperId) {
        "clear" -> ""
        "custom" -> sharedWallpaper?.url.orEmpty()
        else -> "assets/chat-bg/" + effectiveWallpaperId + ".webp"
    }
    val listState = rememberLazyListState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onAttachment(uri)
    }

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaPalette.Bg2)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xB80B121B))
                .border(0.5.dp, NovaPalette.Line)
                .padding(start = 7.dp, end = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIcon(Icons.Rounded.ArrowBack, "Назад", onBack)

            Box {
                NovaAvatar(conversation.peer, mediaUrl(conversation.peer?.avatarUrl), 42)
                if (conversation.peer?.online == true) {
                    OnlineDot(Modifier.align(Alignment.BottomEnd))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 9.dp)
            ) {
                Text(
                    conversation.title,
                    color = NovaPalette.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    chatSubtitle(state),
                    color = if (state.typingUsers.isNotEmpty()) NovaPalette.Accent2 else NovaPalette.Muted,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HeaderIcon(Icons.Rounded.Search, "Поиск") { }
            HeaderIcon(Icons.Rounded.Call, "Звонок") { }
            Box {
                HeaderIcon(Icons.Rounded.MoreVert, "Меню") { showMenu = true }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .width(286.dp)
                        .background(Color(0xFF0A1A25))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Профиль собеседника",
                                color = NovaPalette.Text,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            showMenu = false
                            showPeerProfile = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Поиск в переписке", color = NovaPalette.Text, fontSize = 14.sp) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Обои чата", color = NovaPalette.Text, fontSize = 14.sp) },
                        onClick = {
                            showMenu = false
                            showWallpaper = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Обновить диалог", color = NovaPalette.Text, fontSize = 14.sp) },
                        onClick = { showMenu = false }
                    )
                    if (conversation.peer != null && conversation.peer.relation == "friend") {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Удалить из контактов",
                                    color = Color(0xFFFFA94D),
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                showMenu = false
                                onRemoveFriend(conversation.peer)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Заблокировать пользователя",
                                color = Color(0xFFFF5C83),
                                fontSize = 14.sp
                            )
                        },
                        onClick = { showMenu = false }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF07121D))
        ) {
            if (wallpaperPath.isNotBlank()) {
                AsyncImage(
                    model = mediaUrl(wallpaperPath),
                    contentDescription = "Обои чата",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 1f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = (effectiveWallpaperDim.coerceIn(0, 42) / 100f)
                            )
                        )
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        mine = message.sender.id == currentUser.id,
                        mediaUrl = mediaUrl,
                        selected = selectedMessage?.id == message.id,
                        bubbleSize = state.bubbleSize,
                        bubbleTheme = state.bubbleTheme,
                        onWallpaperResponse = { action ->
                            onRespondWallpaper(message, action)
                        },
                        onClick = {
                            selectedMessage = if (selectedMessage?.id == message.id) null else message
                        }
                    )
                }
            }
        }

        selectedMessage?.let { message ->
            MessageActionBar(
                message = message,
                mine = message.sender.id == currentUser.id,
                onReact = { emoji ->
                    onReactMessage(message, emoji)
                    selectedMessage = null
                },
                onEdit = {
                    editingMessage = message
                    draft = message.body
                    onDraftChanged(draft.isNotBlank())
                    selectedMessage = null
                },
                onDelete = {
                    onDeleteMessage(message)
                    selectedMessage = null
                },
                onClose = { selectedMessage = null }
            )
        }

        if (state.typingUsers.isNotEmpty()) {
            Text(
                chatSubtitle(state),
                color = NovaPalette.Accent2,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 12.dp)
            )
        }

        editingMessage?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaPalette.Panel2)
                    .border(1.dp, NovaPalette.Line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Edit, null, tint = NovaPalette.Accent2, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Редактирование", color = NovaPalette.Accent2, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(message.body, color = NovaPalette.Muted, fontSize = 9.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Rounded.Close,
                    "Отмена",
                    tint = NovaPalette.Muted,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            editingMessage = null
                            draft = ""
                            onDraftChanged(false)
                        }
                        .padding(5.dp)
                )
            }
        }

        if (showEmoji) {
            EmojiStrip(
                onEmoji = {
                    draft += it
                    onDraftChanged(true)
                },
                onClose = { showEmoji = false }
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
            ComposerIcon(Icons.Rounded.AttachFile, "Вложение") {
                filePicker.launch("*/*")
            }

            BasicTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    onDraftChanged(it.isNotBlank())
                },
                textStyle = TextStyle(color = Color(0xFFEFF7FF), fontSize = 13.sp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                maxLines = 4,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (draft.isEmpty()) {
                            Text(
                                if (state.busy) "Загрузка…" else "Сообщение...",
                                color = Color(0xFF72869A),
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                }
            )

            ComposerIcon(Icons.Rounded.SentimentSatisfiedAlt, "Эмодзи") {
                showEmoji = !showEmoji
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (draft.isBlank()) {
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0FA7E9), Color(0xFF0C7CC6))
                                )
                            )
                        } else {
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2F9CE5), Color(0xFF2485D1))
                                )
                            )
                        }
                    )
                    .clickable(enabled = draft.isNotBlank() && !state.busy) {
                        val messageBeingEdited = editingMessage
                        val text = draft
                        draft = ""
                        onDraftChanged(false)

                        if (messageBeingEdited != null) {
                            onEditMessage(messageBeingEdited, text)
                            editingMessage = null
                        } else {
                            onSend(text)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (draft.isBlank()) Icons.Rounded.Mic else Icons.Rounded.Send,
                    if (draft.isBlank()) "Голосовое" else "Отправить",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        BottomNav(
            active = RootTab.CHATS,
            incomingCount = state.friends.incomingCount,
            onTab = onNavigateRoot
        )
    }

    if (showWallpaper) {
        WallpaperDialog(
            mediaUrl = mediaUrl,
            currentId = effectiveWallpaperId,
            dim = effectiveWallpaperDim,
            atmosphere = effectiveAtmosphere,
            shared = sharedWallpaper != null,
            onApplyLocal = { id, nextDim, nextAtmosphere ->
                localWallpaperId = id
                localWallpaperDim = nextDim
                localAtmosphere = nextAtmosphere
            },
            onPropose = { id, nextDim, nextAtmosphere ->
                localWallpaperId = id
                localWallpaperDim = nextDim
                localAtmosphere = nextAtmosphere
                onProposeWallpaper(id, nextDim, nextAtmosphere)
            },
            onReset = {
                localWallpaperId = "midnight-grid"
                localWallpaperDim = 10
                localAtmosphere = "none"
                onResetWallpaper()
            },
            onDismiss = { showWallpaper = false }
        )
    }

    if (showPeerProfile) {
        PeerProfileDialog(
            peer = conversation.peer ?: NovaUser(displayName = conversation.title),
            messages = state.messages,
            mediaUrl = mediaUrl,
            onDismiss = { showPeerProfile = false }
        )
    }
}

@Composable
private fun MessageBubble(
    message: NovaMessage,
    mine: Boolean,
    mediaUrl: (String?) -> String?,
    selected: Boolean,
    bubbleSize: Int,
    bubbleTheme: String,
    onWallpaperResponse: (String) -> Unit,
    onClick: () -> Unit
) {
    val scale = (bubbleSize.coerceIn(80, 130) / 100f)
    val radius = (18f * scale).dp
    val tail = (6f * scale).dp
    val shape = if (mine) {
        RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = radius, bottomEnd = tail)
    } else {
        RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = tail, bottomEnd = radius)
    }
    val incomingBrush = when (bubbleTheme) {
        "glass" -> Brush.verticalGradient(listOf(Color(0x8620394C), Color(0x75102637)))
        "graphite" -> Brush.verticalGradient(listOf(Color(0xFF303943), Color(0xFF252D35)))
        "violet" -> Brush.verticalGradient(listOf(Color(0xFF302C4D), Color(0xFF24253D)))
        "sakura" -> Brush.verticalGradient(listOf(Color(0xFF3A2933), Color(0xFF30222B)))
        else -> Brush.verticalGradient(listOf(Color(0xC21B2F40), Color(0xB811212F)))
    }
    val outgoingBrush = when (bubbleTheme) {
        "glass" -> Brush.verticalGradient(listOf(Color(0xA82799DC), Color(0x941167A5)))
        "graphite" -> Brush.verticalGradient(listOf(Color(0xFF334B5B), Color(0xFF263A48)))
        "violet" -> Brush.linearGradient(listOf(Color(0xFF7658D9), Color(0xFF4B75DF)))
        "sakura" -> Brush.linearGradient(listOf(Color(0xFFDE6A9A), Color(0xFFB84C7E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF268FD4), Color(0xFF196FB1)))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!mine) {
            NovaAvatar(
                user = NovaUser(
                    id = message.sender.id,
                    username = message.sender.username,
                    displayName = message.sender.displayName,
                    avatarUrl = message.sender.avatarUrl
                ),
                imageUrl = mediaUrl(message.sender.avatarUrl),
                size = 30
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
                        if (mine) {
                            Modifier.background(outgoingBrush)
                        } else {
                            Modifier
                                .background(incomingBrush)
                                .border(1.dp, NovaPalette.Line, shape)
                        }
                    )
                    .then(
                        if (selected) Modifier.border(1.5.dp, NovaPalette.Accent2, shape)
                        else Modifier
                    )
                    .clickable(onClick = onClick)
                    .padding(horizontal = (12f * scale).dp, vertical = (9f * scale).dp)
            ) {
                if (!mine && message.sender.displayName.isNotBlank()) {
                    Text(
                        message.sender.displayName,
                        color = NovaPalette.Accent2,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                message.replyTo?.let { reply ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color.Black.copy(alpha = 0.10f))
                            .border(2.dp, NovaPalette.Accent2.copy(alpha = 0.65f), RoundedCornerShape(7.dp))
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Text(reply.senderName, color = NovaPalette.Accent2, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(reply.body, color = NovaPalette.Text.copy(alpha = 0.82f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(5.dp))
                }

                when {
                    message.deletedAt != null -> Text(
                        "Сообщение удалено",
                        color = if (mine) Color.White.copy(alpha = 0.68f) else NovaPalette.Muted,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp
                    )

                    message.body.isNotBlank() -> Text(
                        message.body,
                        color = if (mine) Color.White else NovaPalette.Text,
                        fontSize = (14.35f * scale).sp,
                        lineHeight = (19.5f * scale).sp
                    )
                }

                message.wallpaperRequest?.let { request ->
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            "Парные обои · " + request.id,
                            color = NovaPalette.Accent2,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (request.atmosphere == "none") "Без эффекта" else "Атмосфера: " + request.atmosphere,
                            color = if (mine) Color.White.copy(alpha = 0.7f) else NovaPalette.Muted,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        if (request.status == "pending" && !mine) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                WallpaperRequestButton("Принять", true) {
                                    onWallpaperResponse("accept")
                                }
                                WallpaperRequestButton("Отклонить", false) {
                                    onWallpaperResponse("decline")
                                }
                            }
                        } else {
                            Text(
                                when (request.status) {
                                    "accepted" -> "Принято"
                                    "declined" -> "Отклонено"
                                    "canceled" -> "Отменено"
                                    else -> "Ожидает ответа"
                                },
                                color = NovaPalette.Muted,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                message.attachment?.let { attachment ->
                    Spacer(Modifier.height(if (message.body.isNotBlank()) 7.dp else 0.dp))
                    AttachmentCard(
                        attachmentName = attachment.name,
                        attachmentType = attachment.type,
                        voice = attachment.voice,
                        durationMs = attachment.durationMs,
                        mine = mine
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
                            color = if (mine) Color.White.copy(alpha = 0.62f) else NovaPalette.Muted2,
                            fontSize = 8.5.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        relativeTime(message.createdAt),
                        color = if (mine) Color.White.copy(alpha = 0.64f) else NovaPalette.Muted2,
                        fontSize = 8.8.sp
                    )
                    if (mine) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (message.readByPeer) "✓✓" else "✓",
                            color = if (message.readByPeer) Color(0xFFDDF5FF) else Color.White.copy(alpha = 0.62f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.take(5).forEach { reaction ->
                        Text(
                            reaction.emoji + " " + reaction.count,
                            color = NovaPalette.Text,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(NovaPalette.Panel2)
                                .border(1.dp, NovaPalette.Line, RoundedCornerShape(999.dp))
                                .clickable(onClick = onClick)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageActionBar(
    message: NovaMessage,
    mine: Boolean,
    onReact: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF10F1823))
            .border(1.dp, NovaPalette.Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        listOf("👍", "❤️", "😂", "🔥", "👀").forEach { emoji ->
            Text(
                emoji,
                fontSize = 18.sp,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onReact(emoji) }
                    .padding(5.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        if (mine && message.deletedAt == null) {
            ActionIcon(Icons.Rounded.Edit, "Изменить", NovaPalette.Accent2, onEdit)
            ActionIcon(Icons.Rounded.DeleteOutline, "Удалить", NovaPalette.Danger, onDelete)
        }

        ActionIcon(Icons.Rounded.Close, "Закрыть", NovaPalette.Muted, onClose)
    }
}

@Composable
private fun ActionIcon(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NovaPalette.Panel3)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AttachmentCard(
    attachmentName: String,
    attachmentType: String,
    voice: Boolean,
    durationMs: Long,
    mine: Boolean
) {
    if (voice) {
        Row(
            modifier = Modifier
                .widthIn(min = 218.dp, max = 340.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = if (mine) 0.16f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, "Воспроизвести", tint = if (mine) Color.White else NovaPalette.Accent2)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(24) { index ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height((5 + (index % 5) * 3).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (mine) Color.White.copy(alpha = 0.55f) else NovaPalette.Accent2.copy(alpha = 0.55f))
                        )
                    }
                }
                Text(
                    formatDuration(durationMs),
                    color = if (mine) Color.White.copy(alpha = 0.72f) else NovaPalette.Muted,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("1×", color = if (mine) Color.White.copy(alpha = 0.82f) else NovaPalette.Text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        Row(
            modifier = Modifier
                .widthIn(min = 180.dp, max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.08f))
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Description, null, tint = if (mine) Color.White else NovaPalette.Accent2, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    attachmentName.ifBlank { "Вложение" },
                    color = if (mine) Color.White else NovaPalette.Text,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    attachmentType.ifBlank { "Файл" },
                    color = if (mine) Color.White.copy(alpha = 0.62f) else NovaPalette.Muted,
                    fontSize = 8.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmojiStrip(
    onEmoji: (String) -> Unit,
    onClose: () -> Unit
) {
    val emojis = listOf(
        "😀", "😁", "😂", "🤣", "😊", "😍", "🥰", "😎",
        "😭", "😡", "🤔", "🤯", "👍", "❤️", "🔥", "🎉"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF6061320))
            .border(1.dp, Color(0x4776A4DC), RoundedCornerShape(18.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Смайлики и эмоции", color = NovaPalette.Text, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.Close,
                "Закрыть",
                tint = NovaPalette.Muted,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onClose)
                    .padding(5.dp)
            )
        }

        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            emojis.chunked(8).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { emoji ->
                        Text(
                            emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onEmoji(emoji) }
                                .padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = NovaPalette.Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ComposerIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = Color(0xFF91A6BA), modifier = Modifier.size(20.dp))
    }
}

private fun chatSubtitle(state: NovaUiState): String {
    val typing = state.typingUsers.firstOrNull()
    if (typing != null) {
        return when (typing.mode) {
            "voice-recording" -> typing.displayName + " записывает голосовое…"
            "voice-sending" -> typing.displayName + " отправляет голосовое…"
            else -> typing.displayName + " печатает…"
        }
    }

    val peer = state.selectedConversation?.peer
    return when {
        state.selectedConversation?.system == true -> "официальный канал"
        peer?.online == true -> "онлайн"
        else -> "был(а) недавно"
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000L).coerceAtLeast(0L)
    return "%d:%02d".format(seconds / 60L, seconds % 60L)
}


@Composable
private fun WallpaperRequestButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .then(
                if (primary) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF12BFF3), Color(0xFF078EDE))
                        )
                    )
                } else {
                    Modifier
                        .background(Color(0xFF0A1A26))
                        .border(1.dp, Color(0xFF244357), RoundedCornerShape(11.dp))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (primary) Color.White else NovaPalette.Text,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
