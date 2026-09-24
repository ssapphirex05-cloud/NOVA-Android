package com.nova.messenger.native2.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.nova.messenger.native2.NovaUser

@Composable
fun WallpaperDialog(
    mediaUrl: (String?) -> String?,
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val wallpapers = listOf(
        "Midnight" to "assets/chat-bg/midnight-grid.webp",
        "Signal" to "assets/chat-bg/signal-lines.webp",
        "Orbit" to "assets/chat-bg/orbit-glow.webp",
        "Deep Tech" to "assets/chat-bg/deep-tech.webp"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF5071C29))
                .border(1.dp, Color(0xFF174B66), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "Меняй фон прямо поверх переписки.",
                        color = NovaPalette.Muted,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0C1B28))
                        .border(1.dp, Color(0xFF17384E), RoundedCornerShape(14.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Закрыть",
                        tint = NovaPalette.Muted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            wallpapers.forEach { (name, path) ->
                val active = current == path

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (active) Color(0xFF0C2E42) else Color(0xFF0A1722))
                        .border(
                            1.dp,
                            if (active) NovaPalette.Accent2.copy(alpha = 0.6f)
                            else Color(0xFF173246),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelect(path) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = mediaUrl(path),
                        contentDescription = name,
                        modifier = Modifier
                            .width(104.dp)
                            .height(58.dp)
                            .clip(RoundedCornerShape(11.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        name,
                        color = NovaPalette.Text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )

                    if (active) {
                        Text(
                            "✓",
                            color = NovaPalette.Accent2,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF17405A), RoundedCornerShape(14.dp))
                    .clickable { onSelect("") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Сбросить",
                    color = NovaPalette.Text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
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
    Dialog(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF071620))
                .border(1.dp, Color(0xFF17445D), RoundedCornerShape(24.dp)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "CONTACT",
                            color = NovaPalette.Accent2,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            "Профиль пользователя",
                            color = NovaPalette.Text,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C1B28))
                            .border(1.dp, Color(0xFF17384E), RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Закрыть",
                            tint = NovaPalette.Muted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(245.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF174B67),
                                    Color(0xFF0A2837),
                                    Color(0xFF06141D)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF2B7899), RoundedCornerShape(22.dp))
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
                            .height(110.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xD0051018)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF07121A))
                                .border(3.dp, Color(0xFF00C7FF), CircleShape)
                                .padding(4.dp)
                        ) {
                            NovaAvatar(
                                peer,
                                mediaUrl(peer.avatarUrl),
                                78
                            )
                        }

                        Spacer(Modifier.width(13.dp))

                        Column(Modifier.padding(bottom = 7.dp)) {
                            Text(
                                peer.displayName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF13C0F3),
                                        Color(0xFF078FE0)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Написать",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B1C28))
                            .border(1.dp, Color(0xFF17435A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Позвонить",
                            color = NovaPalette.Text,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A1B27))
                        .border(1.dp, Color(0xFF163C52), RoundedCornerShape(18.dp))
                        .padding(15.dp)
                ) {
                    Text(
                        "О СЕБЕ",
                        color = NovaPalette.Accent2,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        peer.bio.ifBlank {
                            "Пользователь пока ничего не написал о себе."
                        },
                        color = NovaPalette.Text,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
