package com.alexander.paxmentis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Einziges App-Theme. Früher gab es zwei Funktionen namens PaxMentisTheme; aktiv war wegen
 * der Kotlin-Overload-Regeln nur die helle Variante – das Dark-Theme war toter Code.
 *
 * Dark Mode ist bewusst (noch) nicht aktiv: Mehrere Screens verwenden feste Farben
 * (z. B. Sicherer Ort, Schlafhilfe, Nachtwache). Vor dem Einschalten müssen diese auf
 * Theme-Farben umgestellt werden, sonst wird Text unlesbar.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3498DB),
    secondary = Color(0xFF6AB04C),
    background = Color(0xFFF0F4F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333)
)

@Composable
fun PaxMentisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
