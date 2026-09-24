package com.nova.messenger.native2.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NovaPalette {
    val Bg = Color(0xFF080C12)
    val Bg2 = Color(0xFF0A1019)
    val Panel = Color(0xFF0D141E)
    val PanelSolid = Color(0xFF0F1722)
    val Panel2 = Color(0xFF111B28)
    val Panel3 = Color(0xFF172334)
    val Panel4 = Color(0xFF1B2A3D)
    val Text = Color(0xFFF5F8FC)
    val Muted = Color(0xFF8795A8)
    val Muted2 = Color(0xFF66758A)
    val Accent = Color(0xFF3390EC)
    val Accent2 = Color(0xFF53B6FF)
    val Good = Color(0xFF28D59D)
    val Danger = Color(0xFFFF6B7D)
    val Line = Color.White.copy(alpha = 0.075f)
    val AccentSoft = Accent.copy(alpha = 0.13f)
}

private val NovaDarkScheme = darkColorScheme(
    primary = NovaPalette.Accent,
    secondary = NovaPalette.Accent2,
    background = NovaPalette.Bg,
    surface = NovaPalette.PanelSolid,
    onPrimary = Color.White,
    onBackground = NovaPalette.Text,
    onSurface = NovaPalette.Text,
    error = NovaPalette.Danger
)

@Composable
fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDarkScheme,
        typography = Typography(),
        content = content
    )
}
