package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeColor(
    val id: String,
    val displayName: String,
    val primary: Color,
    val primaryVariant: Color,
    val glowColor: Color
) {
    BLUE(
        id = "blue",
        displayName = "Azul Eléctrico",
        primary = Color(0xFF2AABEE),
        primaryVariant = Color(0xFF1565C0),
        glowColor = Color(0x662AABEE)
    ),
    RED(
        id = "red",
        displayName = "Rojo Cine",
        primary = Color(0xFFE50914),
        primaryVariant = Color(0xFF991B1B),
        glowColor = Color(0x66E50914)
    ),
    EMERALD(
        id = "emerald",
        displayName = "Verde Esmeralda",
        primary = Color(0xFF10B981),
        primaryVariant = Color(0xFF047857),
        glowColor = Color(0x6610B981)
    ),
    PURPLE(
        id = "purple",
        displayName = "Violeta Neón",
        primary = Color(0xFF8B5CF6),
        primaryVariant = Color(0xFF6D28D9),
        glowColor = Color(0x668B5CF6)
    ),
    AMBER(
        id = "amber",
        displayName = "Ámbar Dorado",
        primary = Color(0xFFF59E0B),
        primaryVariant = Color(0xFFB45309),
        glowColor = Color(0x66F59E0B)
    ),
    CYAN(
        id = "cyan",
        displayName = "Cian Océano",
        primary = Color(0xFF06B6D4),
        primaryVariant = Color(0xFF0E7490),
        glowColor = Color(0x6606B6D4)
    );

    companion object {
        fun fromId(id: String?): AppThemeColor {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: BLUE
        }
    }
}
