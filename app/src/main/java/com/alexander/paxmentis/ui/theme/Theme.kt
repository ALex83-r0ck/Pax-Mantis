package com.alexander.paxmentis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.data.PaxMentisDatabase

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A90E2), // Beruhigendes Blau
    secondary = Color(0xFF81C784), // Sanftes Grün
    background = Color(0xFFF5F7FA), // Heller, beruhigender Hintergrund
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF2C3E50), // Dunkler Text für Kontrast
    onSurface = Color(0xFF2C3E50)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6AB0F3), // Helleres Blau für Dark Mode
    secondary = Color(0xFF66BB6A), // Dunkleres Grün
    background = Color(0xFF2C3E50), // Dunkler Hintergrund
    surface = Color(0xFF34495E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1)
)

private val AppTypography = androidx.compose.material3.Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun PaxMentisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}