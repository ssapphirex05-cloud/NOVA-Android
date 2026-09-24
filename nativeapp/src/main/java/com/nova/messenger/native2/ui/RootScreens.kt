package com.nova.messenger.native2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nova.messenger.native2.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ChatsScreen(state: NovaUiState, mediaUrl: (String?) -> String?, onOpen: (Conversation) -> Unit, onTab: (RootTab) -> Unit) {
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) state.conversations else state.conversations.filter {
        it.title.contains(search, true) || (it.peer?.username?.contains(search, true) == true)
    }

    Column(Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        RootHeader(state, mediaUrl)
        SearchField(search, { search = it }, "Поиск по чатам")

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            QuickCard("Чаты", filtered.size.toString(), Modifier.weight(1f))
            QuickCard("Новые", state.conversations.sumOf { it.unread }.toString(), Modifier.weight(1f))
            QuickCard("Онлайн", state.conversations.count { it.peer?.online == true }.toString(), Modifier.weight(1f))
        }

        Text("СООБЩЕНИЯ", color = NovaPalette.Muted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp)) {
            items(filtered, key = { it.id }) { c -> ConversationRow(c, mediaUrl, onOpen) }
        }
        BottomNav(RootTab.CHATS, state.friends.incomingCount, onTab)
    }
}

@Composable
private fun ConversationRow(c: Conversation, mediaUrl: (String?) -> String?, onOpen: (Conversation) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(17.dp)).clickable { onOpen(c) }.padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            NovaAvatar(c.peer ?: NovaUser(displayName = c.title), mediaUrl(c.peer?.avatarUrl), 58)
            if (c.peer?.online == true) OnlineDot(Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(c.title, color = NovaPalette.Text, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(c.lastMessage?.body ?: if (c.system) "Официальный канал NOVA" else "Начните диалог", color = NovaPalette.Muted, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(relativeTime(c.lastMessage?.createdAt), color = NovaPalette.Muted2, fontSize = 10.5.sp)
            if (c.unread > 0) {
                Spacer(Modifier.height(7.dp))
                Box(Modifier.height(21.dp).clip(RoundedCornerShape(999.dp)).background(NovaPalette.Accent).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                    Text(c.unread.coerceAtMost(99).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
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
    Column(Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        RootHeader(state, mediaUrl)
        SearchField(state.contactSearch, onSearch, "Найти по username")
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (state.friends.incoming.isNotEmpty()) {
                item { SectionTitle("ЗАПРОСЫ") }
                items(state.friends.incoming, key = { "in-" + it.id }) { req ->
                    ContactRow(req.user, mediaUrl, trailing = {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallIcon(Icons.Rounded.Check, NovaPalette.Good) { onRespond(req, "accept") }
                            SmallIcon(Icons.Rounded.Close, NovaPalette.Danger) { onRespond(req, "decline") }
                        }
                    })
                }
            }

            if (state.contactSearch.trim().length >= 2) {
                item { SectionTitle("ПОИСК") }
                items(state.searchUsers, key = { "search-" + it.id }) { user ->
                    ContactRow(
                        user,
                        mediaUrl,
                        onClick = {
                            if (user.relation == "friend" && user.conversationId > 0) {
                                onOpenFriend(user, user.conversationId)
                            }
                        },
                        trailing = {
                            when (user.relation) {
                                "friend" -> Text("В контактах", color = NovaPalette.Good, fontSize = 10.sp)
                                "outgoing" -> Text("Отправлено", color = NovaPalette.Muted, fontSize = 10.sp)
                                "incoming" -> Text("Входящий", color = NovaPalette.Accent2, fontSize = 10.sp)
                                else -> SmallIcon(Icons.Rounded.PersonAdd, NovaPalette.Accent2) { onRequest(user) }
                            }
                        }
                    )
                }
            } else {
                item { SectionTitle("КОНТАКТЫ") }
                items(state.friends.friends, key = { "friend-" + it.user.id }) { item ->
                    ContactRow(item.user, mediaUrl, onClick = { onOpenFriend(item.user, item.conversationId) }, trailing = {
                        if (item.user.online) Text("онлайн", color = NovaPalette.Good, fontSize = 10.sp)
                    })
                }
            }
        }
        BottomNav(RootTab.CONTACTS, state.friends.incomingCount, onTab)
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
    val user = state.user ?: return
    var displayName by remember(user.id, user.displayName) { mutableStateOf(user.displayName) }
    var statusText by remember(user.id, user.statusText) { mutableStateOf(user.statusText) }
    var bio by remember(user.id, user.bio) { mutableStateOf(user.bio) }

    Column(Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        RootHeader(state, mediaUrl)
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            NovaAvatar(user, mediaUrl(user.avatarUrl), 92)
            Spacer(Modifier.height(12.dp))
            Text("@" + user.username, color = NovaPalette.Accent2, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
            ProfileInput("Имя", displayName) { displayName = it }
            Spacer(Modifier.height(10.dp))
            ProfileInput("Статус", statusText) { statusText = it }
            Spacer(Modifier.height(10.dp))
            ProfileInput("О себе", bio) { bio = it }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF35A8F3), Color(0xFF2489DC))))
                    .clickable(enabled = !state.busy) { onSave(displayName, bio, statusText) },
                contentAlignment = Alignment.Center
            ) {
                Text(if (state.busy) "Сохраняю…" else "Сохранить профиль", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(15.dp)).background(NovaPalette.Panel2)
                    .border(1.dp, NovaPalette.Line, RoundedCornerShape(15.dp)).clickable(onClick = onLogout).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Logout, null, tint = NovaPalette.Danger)
                Spacer(Modifier.width(10.dp))
                Text("Выйти из аккаунта", color = NovaPalette.Danger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        BottomNav(RootTab.PROFILE, state.friends.incomingCount, onTab)
    }
}

@Composable
fun SettingsScreen(state: NovaUiState, onTab: (RootTab) -> Unit) {
    Column(Modifier.fillMaxSize().background(NovaPalette.Bg2)) {
        Row(Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Настройки", color = NovaPalette.Text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsCard("NOVA Native", "Версия 2.0 alpha")
            SettingsCard("Синхронизация", "Веб и Android используют общий сервер NOVA")
            SettingsCard("Уведомления", "FCM подключается через существующий backend")
        }
        BottomNav(RootTab.SETTINGS, state.friends.incomingCount, onTab)
    }
}

@Composable
private fun RootHeader(state: NovaUiState, mediaUrl: (String?) -> String?) {
    Row(
        Modifier.fillMaxWidth().height(70.dp).background(Color(0xDB090F17)).border(0.5.dp, NovaPalette.Line).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFF5BB8FF), NovaPalette.Accent, Color(0xFF3867E8)))),
            contentAlignment = Alignment.Center
        ) {
            Text("N", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Column(Modifier.padding(start = 10.dp)) {
            Text("NOVA", color = NovaPalette.Text, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text("Messenger", color = NovaPalette.Muted, fontSize = 9.5.sp)
        }
        Spacer(Modifier.weight(1f))
        NovaAvatar(state.user, mediaUrl(state.user?.avatarUrl), 39)
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(50.dp)
            .clip(RoundedCornerShape(17.dp)).background(NovaPalette.Panel2)
            .border(1.dp, NovaPalette.Line, RoundedCornerShape(17.dp)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, null, tint = NovaPalette.Muted, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = NovaPalette.Text, fontSize = 13.5.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, color = NovaPalette.Muted2, fontSize = 13.5.sp)
                    inner()
                }
            }
        )
    }
}

@Composable
private fun QuickCard(title: String, value: String, modifier: Modifier) {
    Column(
        modifier.height(70.dp).clip(RoundedCornerShape(16.dp)).background(NovaPalette.Panel2).border(1.dp, NovaPalette.Line, RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = NovaPalette.Text, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(value, color = NovaPalette.Muted, fontSize = 9.sp)
    }
}

@Composable
fun BottomNav(active: RootTab, incomingCount: Int, onTab: (RootTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(70.dp).background(Color(0xF20A1018)).border(0.5.dp, NovaPalette.Line),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavButton("Чаты", Icons.Rounded.ChatBubbleOutline, active == RootTab.CHATS, Modifier.weight(1f)) { onTab(RootTab.CHATS) }
        NavButton(if (incomingCount > 0) "Контакты · " + incomingCount else "Контакты", Icons.Rounded.PersonOutline, active == RootTab.CONTACTS, Modifier.weight(1f)) { onTab(RootTab.CONTACTS) }
        NavButton("Профиль", Icons.Rounded.PersonOutline, active == RootTab.PROFILE, Modifier.weight(1f)) { onTab(RootTab.PROFILE) }
        NavButton("Настройки", Icons.Rounded.Settings, active == RootTab.SETTINGS, Modifier.weight(1f)) { onTab(RootTab.SETTINGS) }
    }
}

@Composable
private fun NavButton(label: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(58.dp).clip(RoundedCornerShape(12.dp)).background(if (active) NovaPalette.AccentSoft else Color.Transparent).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, label, tint = if (active) NovaPalette.Accent2 else NovaPalette.Muted, modifier = Modifier.size(21.dp))
        Text(label, color = if (active) NovaPalette.Accent2 else NovaPalette.Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NovaAvatar(user: NovaUser?, imageUrl: String?, size: Int) {
    val initials = user?.displayName?.split(" ")?.filter { it.isNotBlank() }?.take(2)?.joinToString("") { it.take(1).uppercase() }?.takeIf { it.isNotBlank() } ?: "N"
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF5D7CFF), Color(0xFF2E8ADF)))),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) AsyncImage(model = imageUrl, contentDescription = user?.displayName, modifier = Modifier.fillMaxSize())
        else Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.30f).sp)
    }
}

@Composable
fun OnlineDot(modifier: Modifier = Modifier) {
    Box(modifier.size(9.dp).clip(CircleShape).background(NovaPalette.Good).border(2.dp, NovaPalette.PanelSolid, CircleShape))
}

@Composable
private fun ContactRow(user: NovaUser, mediaUrl: (String?) -> String?, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(16.dp)).background(NovaPalette.Panel2.copy(alpha = 0.36f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            NovaAvatar(user, mediaUrl(user.avatarUrl), 46)
            if (user.online) OnlineDot(Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(user.displayName, color = NovaPalette.Text, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("@" + user.username, color = NovaPalette.Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
        }
        trailing()
    }
}

@Composable
private fun SmallIcon(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(NovaPalette.Panel3).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = NovaPalette.Muted, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp))
}

@Composable
private fun ProfileInput(label: String, value: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = NovaPalette.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = NovaPalette.Text, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(15.dp)).background(NovaPalette.Panel2)
                .border(1.dp, NovaPalette.Line, RoundedCornerShape(15.dp)).padding(horizontal = 14.dp),
            decorationBox = { inner -> Box(contentAlignment = Alignment.CenterStart) { inner() } }
        )
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(NovaPalette.Panel2)
            .border(1.dp, NovaPalette.Line, RoundedCornerShape(16.dp)).padding(15.dp)
    ) {
        Text(title, color = NovaPalette.Text, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = NovaPalette.Muted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

fun relativeTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val local = Instant.parse(iso).atZone(ZoneId.systemDefault())
        val now = ZonedDateTime.now()
        if (local.toLocalDate() == now.toLocalDate()) "%02d:%02d".format(local.hour, local.minute)
        else "%02d.%02d".format(local.dayOfMonth, local.monthValue)
    }.getOrDefault("")
}
