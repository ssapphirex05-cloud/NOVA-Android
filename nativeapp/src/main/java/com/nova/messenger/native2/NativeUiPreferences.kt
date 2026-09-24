package com.nova.messenger.native2

import android.content.Context

class NativeUiPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("nova_native_ui", Context.MODE_PRIVATE)

    var compact: Boolean
        get() = prefs.getBoolean("compact", false)
        set(value) = prefs.edit().putBoolean("compact", value).apply()

    var bubbleSize: Int
        get() = prefs.getInt("bubble_size", 100).coerceIn(80, 130)
        set(value) = prefs.edit().putInt("bubble_size", value.coerceIn(80, 130)).apply()

    var bubbleTheme: String
        get() = normalizeBubbleTheme(prefs.getString("bubble_theme", "nova"))
        set(value) = prefs.edit().putString("bubble_theme", normalizeBubbleTheme(value)).apply()

    fun wallpaperId(conversationId: Long): String =
        prefs.getString("wallpaper_" + conversationId + "_id", "midnight-grid")
            ?.takeIf { it in wallpaperIds }
            ?: "midnight-grid"

    fun wallpaperDim(conversationId: Long): Int =
        prefs.getInt("wallpaper_" + conversationId + "_dim", 10).coerceIn(0, 42)

    fun wallpaperAtmosphere(conversationId: Long): String =
        prefs.getString("wallpaper_" + conversationId + "_atmosphere", "none")
            ?.takeIf { it in atmosphereIds }
            ?: "none"

    fun saveWallpaper(
        conversationId: Long,
        id: String,
        dim: Int,
        atmosphere: String
    ) {
        prefs.edit()
            .putString(
                "wallpaper_" + conversationId + "_id",
                id.takeIf { it in wallpaperIds } ?: "midnight-grid"
            )
            .putInt("wallpaper_" + conversationId + "_dim", dim.coerceIn(0, 42))
            .putString(
                "wallpaper_" + conversationId + "_atmosphere",
                atmosphere.takeIf { it in atmosphereIds } ?: "none"
            )
            .apply()
    }

    fun clearWallpaper(conversationId: Long) {
        prefs.edit()
            .remove("wallpaper_" + conversationId + "_id")
            .remove("wallpaper_" + conversationId + "_dim")
            .remove("wallpaper_" + conversationId + "_atmosphere")
            .apply()
    }

    companion object {
        val wallpaperIds = setOf(
            "midnight-grid",
            "signal-lines",
            "orbit-glow",
            "deep-tech",
            "clear"
        )

        val atmosphereIds = setOf(
            "none",
            "rain",
            "snow",
            "stars",
            "neon",
            "particles"
        )

        val bubbleThemes = setOf(
            "nova",
            "glass",
            "graphite",
            "violet",
            "sakura"
        )

        fun normalizeBubbleTheme(value: String?): String =
            value?.lowercase()?.takeIf { it in bubbleThemes } ?: "nova"
    }
}
