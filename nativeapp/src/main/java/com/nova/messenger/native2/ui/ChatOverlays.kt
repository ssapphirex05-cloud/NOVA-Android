package com.nova.messenger.native2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.nova.messenger.native2.NovaUser

@Composable
fun WallpaperDialog(
    mediaUrl: (String?) -> String?,
    currentId: String,
    dim: Int,
    atmosphere: String,
    shared: Boolean,
    onApplyLocal: (String, Int, String) -> Unit,
    onPropose: (String, Int, String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val wallpapers = listOf(
        Triple("Midnight", "midnight-grid", "assets/chat-bg/midnight-grid.webp"),
        Triple("Signal", "signal-lines", "assets/chat-bg/signal-lines.webp"),
        Triple("Orbit", "orbit-glow", "assets/chat-bg/orbit-glow.webp"),
        Triple("Deep Tech", "deep-tech", "assets/chat-bg/deep-tech.webp")
    )
    val atmospheres = listOf(
        "none" to "Без эффекта",
        "rain" to "Дождь",
        "snow" to "Снег",
        "stars" to "Звёзды",
        "neon" to "Неон",
        "particles" to "Частицы"
    )

    var selectedId by remember(currentId) { mutableStateOf(currentId) }
    var selectedAtmosphere by remember(atmosphere) { mutableStateOf(atmosphere) }
    var dimValue by remember(dim) { mutableFloatStateOf(dim.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
                .padding(horizontal = 8.dp, vertical = 72.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(610.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xF5071C29))
                    .border(1.dp, Color(0xFF174B66), RoundedCornerShape(28.dp)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "CHAT WALLPAPER",
                                color = NovaPalette.Accent2,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.8.sp
                            )
                            Text(
                                "Обои чата",
                                color = NovaPalette.Text,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                            Text(
                                "Меняй фон прямо поверх переписки. По умолчанию изменения видишь только ты.",
                                color = NovaPalette.Muted,
                                fontSize = 10.3.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 7.dp, end = 12.dp)
                            )
                        }

                        CloseTile(onDismiss)
                    }
                }

                item {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallpapers.forEach { (name, id, path) ->
                            val active = selectedId == id
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (active) Color(0xFF0C2E42) else Color(0xFF081722))
                                    .border(
                                        1.dp,
                                        if (active) NovaPalette.Accent2.copy(alpha = 0.68f) else Color(0xFF173246),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        selectedId = id
                                        onApplyLocal(id, dimValue.toInt(), selectedAtmosphere)
                                    }
                                    .padding(7.dp)
                            ) {
                                Box {
                                    AsyncImage(
                                        model = mediaUrl(path),
                                        contentDescription = name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(86.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (active) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(7.dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xD6072738)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("✓", color = NovaPalette.Accent2, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                                Text(
                                    name,
                                    color = NovaPalette.Text,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(start = 3.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(17.dp))
                            .background(Color(0xFF061723))
                            .border(1.dp, Color(0xFF17384D), RoundedCornerShape(17.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                    ) {
                        Text("Затемнение", color = NovaPalette.Text, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Меняется сразу в текущем чате",
                            color = NovaPalette.Muted,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Slider(
                            value = dimValue,
                            onValueChange = {
                                dimValue = it
                                onApplyLocal(selectedId, it.toInt(), selectedAtmosphere)
                            },
                            valueRange = 0f..42f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFECEFF2),
                                activeTrackColor = Color(0xFF109DE2),
                                inactiveTrackColor = Color(0xFF302D37)
                            )
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(11.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .border(1.dp, Color(0xFF17455F), RoundedCornerShape(15.dp))
                            .clickable {
                                selectedId = "midnight-grid"
                                selectedAtmosphere = "none"
                                dimValue = 10f
                                onApplyLocal("midnight-grid", 10, "none")
                                onReset()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Сбросить", color = NovaPalette.Text, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF061723))
                            .border(1.dp, Color(0xFF17384D), RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        Text("Живая атмосфера", color = NovaPalette.Text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Эффект применяется только у тебя, пока собеседник не примет парные обои.",
                            color = NovaPalette.Muted,
                            fontSize = 9.2.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        atmospheres.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (id, label) ->
                                    val active = selectedAtmosphere == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(if (active) Color(0xFF0A3045) else Color(0xFF071824))
                                            .border(
                                                1.dp,
                                                if (active) Color(0xFF0EA8E8) else Color(0xFF18374A),
                                                RoundedCornerShape(13.dp)
                                            )
                                            .clickable {
                                                selectedAtmosphere = id
                                                onApplyLocal(selectedId, dimValue.toInt(), id)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (active) NovaPalette.Accent2 else Color(0xFF91A5B7),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(7.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF071A27))
                            .border(1.dp, Color(0xFF17435B), RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        Text("Парные обои", color = NovaPalette.Text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            if (shared) "В этом чате уже используются общие обои."
                            else "Отправь собеседнику обычное предложение прямо в переписке. Общий фон включится только после его согласия.",
                            color = NovaPalette.Muted,
                            fontSize = 9.1.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF108CCB), Color(0xFF076997))
                                    )
                                )
                                .border(1.dp, Color(0xFF1BB5F1), RoundedCornerShape(14.dp))
                                .clickable {
                                    onPropose(selectedId, dimValue.toInt(), selectedAtmosphere)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Предложить", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                item {
                    Text(
                        "Сейчас фон и атмосфера видны только тебе.",
                        color = Color(0xFF768B9F),
                        fontSize = 9.2.sp,
                        modifier = Modifier.padding(start = 10.dp, top = 13.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PeerProfileDialog(
    peer: NovaUser,
    mediaUrl: (String?) -> String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06141F))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF061722))
                            .border(0.5.dp, Color(0xFF102D3E))
                            .padding(start = 18.dp, end = 15.dp, top = 20.dp, bottom = 19.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "CONTACT",
                                color = NovaPalette.Accent2,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Профиль пользователя",
                                color = NovaPalette.Text,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        CloseTile(onDismiss)
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF174B67), Color(0xFF0A2837), Color(0xFF06141D))
                                )
                            )
                            .border(1.dp, Color(0xFF2B7899), RoundedCornerShape(24.dp))
                    ) {
                        val cover = mediaUrl(peer.coverUrl)
                        if (!cover.isNullOrBlank()) {
                            AsyncImage(
                                model = cover,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xE0061119))
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.align(Alignment.BottomStart).padding(15.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF07121A))
                                    .border(3.dp, Color(0xFF00C7FF), CircleShape)
                                    .padding(4.dp)
                            ) {
                                NovaAvatar(peer, mediaUrl(peer.avatarUrl), 84)
                            }

                            Spacer(Modifier.width(13.dp))

                            Column(Modifier.padding(bottom = 8.dp)) {
                                Text(peer.displayName, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                                Text(
                                    "@" + peer.username,
                                    color = NovaPalette.Accent2,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                                Text(
                                    if (peer.online) "●  в сети" else "●  не в сети",
                                    color = if (peer.online) NovaPalette.Good else NovaPalette.Muted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    ProfileStatusCard(peer)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContactAction("Написать", Icons.Rounded.Send, true, Modifier.weight(1f))
                        ContactAction("Позвонить", Icons.Rounded.Call, false, Modifier.weight(1f))
                    }
                }

                item {
                    InfoBlock(
                        icon = Icons.Rounded.PersonOutline,
                        eyebrow = "О СЕБЕ",
                        text = peer.bio.ifBlank { "Пользователь пока ничего не написал о себе." }
                    )
                }

                item {
                    InfoBlock(
                        icon = Icons.Rounded.CalendarMonth,
                        eyebrow = "ДАТА РОЖДЕНИЯ",
                        text = peer.birthDate.ifBlank { "Не указана" }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatusCard(peer: NovaUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Color(0xFF071A27))
            .border(1.dp, Color(0xFF17435B), RoundedCornerShape(19.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF07314A))
                .border(1.dp, Color(0xFF0B698D), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.CheckCircleOutline,
                contentDescription = null,
                tint = NovaPalette.Accent2,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(13.dp))
        Column {
            Text("СТАТУС", color = NovaPalette.Accent2, fontSize = 8.8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
            Text(
                if (peer.relation == "friend") "У вас в контактах" else "Профиль пользователя NOVA",
                color = NovaPalette.Text,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun ContactAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(17.dp))
            .then(
                if (primary) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF10C4F7), Color(0xFF078FDF))
                        )
                    )
                } else {
                    Modifier
                        .background(Color(0xFF081A26))
                        .border(1.dp, Color(0xFF17435B), RoundedCornerShape(17.dp))
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (primary) Color.White else Color(0xFFC4D6E2), modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = if (primary) Color.White else Color(0xFFC4D6E2), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun InfoBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    eyebrow: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Color(0xFF071A27))
            .border(1.dp, Color(0xFF17435B), RoundedCornerShape(19.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF07314A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NovaPalette.Accent2, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(eyebrow, color = NovaPalette.Accent2, fontSize = 8.8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
            Text(text, color = NovaPalette.Text, fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 7.dp))
        }
    }
}

@Composable
private fun CloseTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF0C1B28))
            .border(1.dp, Color(0xFF17384E), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Close,
            contentDescription = "Закрыть",
            tint = NovaPalette.Muted,
            modifier = Modifier.size(24.dp)
        )
    }
}
