package com.alexander.paxmentis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * App-Theme: ruhige, gedämpfte Farben in vier Stimmungen, jeweils hell und dunkel.
 * Der Dunkelmodus folgt dem System.
 *
 * Rot (error) ist allein dem Notruf 112, Löschen und echten Fehlermeldungen vorbehalten –
 * keine der Stimmungen verwendet Rot als Akzent, damit Rot in einer Krise eindeutig bleibt.
 *
 * Bewusst dunkel gestaltete Screens (Nachtwache, Schlafhilfe, Social-Media-Stopp) setzen
 * eigene Farben und bleiben in beiden Modi dunkel.
 */
enum class ThemeMood(val label: String, val swatch: Color) {
    SAGE("Salbei", Color(0xFF4A6B57)),
    SEA("Meer", Color(0xFF2F6577)),
    DUSK("Abendrot", Color(0xFF8A5021)),
    LAVENDER("Lavendel", Color(0xFF65558F));

    companion object {
        val DEFAULT = SAGE
        fun fromStored(name: String?): ThemeMood = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** Die tonalen Werte einer Stimmung; daraus entstehen helles und dunkles Farbschema. */
private class Palette(
    val primary: Color, val onPrimary: Color, val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val tertiaryContainer: Color, val onTertiaryContainer: Color,
    val background: Color, val onSurface: Color, val surfaceVariant: Color, val onSurfaceVariant: Color,
    val outline: Color,
    /** surfaceContainerLowest … surfaceContainerHighest */
    val containers: List<Color>
)

private fun Palette.light(): ColorScheme = lightColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    secondary = secondary, onSecondary = Color.White,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = background, onBackground = onSurface,
    surface = background, onSurface = onSurface,
    surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
    surfaceTint = primary,
    outline = outline, outlineVariant = surfaceVariant,
    surfaceBright = background, surfaceDim = containers[4],
    surfaceContainerLowest = containers[0], surfaceContainerLow = containers[1],
    surfaceContainer = containers[2], surfaceContainerHigh = containers[3],
    surfaceContainerHighest = containers[4],
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
)

private fun Palette.dark(): ColorScheme = darkColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    secondary = secondary, onSecondary = secondaryContainer,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = tertiaryContainer,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = background, onBackground = onSurface,
    surface = background, onSurface = onSurface,
    surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
    surfaceTint = primary,
    outline = outline, outlineVariant = surfaceVariant,
    surfaceBright = containers[4], surfaceDim = background,
    surfaceContainerLowest = containers[0], surfaceContainerLow = containers[1],
    surfaceContainer = containers[2], surfaceContainerHigh = containers[3],
    surfaceContainerHighest = containers[4],
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
)

private fun palettes(mood: ThemeMood): Pair<Palette, Palette> = when (mood) {
    ThemeMood.SAGE -> Palette(
        Color(0xFF4A6B57), Color.White, Color(0xFFCDE8D3), Color(0xFF0B2014),
        Color(0xFF5F6B63), Color(0xFFDCE5DD), Color(0xFF19201B),
        Color(0xFF3F6374), Color(0xFFC3E8FB), Color(0xFF001F29),
        Color(0xFFF7F8F3), Color(0xFF1A1C19), Color(0xFFDCE5DB), Color(0xFF414942), Color(0xFF717971),
        listOf(Color(0xFFFFFFFF), Color(0xFFF1F3EE), Color(0xFFEBEEE8), Color(0xFFE5E8E2), Color(0xFFE0E3DD))
    ) to Palette(
        Color(0xFFB1CFB8), Color(0xFF1D3526), Color(0xFF334C3C), Color(0xFFCDE8D3),
        Color(0xFFC0C9C1), Color(0xFF414B44), Color(0xFFDCE5DD),
        Color(0xFFA7CCDF), Color(0xFF264B5B), Color(0xFFC3E8FB),
        Color(0xFF111411), Color(0xFFE1E3DE), Color(0xFF414942), Color(0xFFC0C9C0), Color(0xFF8A938A),
        listOf(Color(0xFF0C0F0C), Color(0xFF1A1C19), Color(0xFF1E201D), Color(0xFF282B27), Color(0xFF333532))
    )
    ThemeMood.SEA -> Palette(
        Color(0xFF2F6577), Color.White, Color(0xFFB9EAFF), Color(0xFF001F28),
        Color(0xFF4C6169), Color(0xFFCFE6F0), Color(0xFF071E25),
        Color(0xFF5B5B7E), Color(0xFFE1DFFF), Color(0xFF181837),
        Color(0xFFF6FAFC), Color(0xFF171C1F), Color(0xFFDBE4E8), Color(0xFF3F484C), Color(0xFF6F787C),
        listOf(Color(0xFFFFFFFF), Color(0xFFF0F4F7), Color(0xFFEAEEF1), Color(0xFFE4E9EB), Color(0xFFDFE3E6))
    ) to Palette(
        Color(0xFF9BCFE3), Color(0xFF003545), Color(0xFF0E4D5E), Color(0xFFB9EAFF),
        Color(0xFFB3CAD3), Color(0xFF344A51), Color(0xFFCFE6F0),
        Color(0xFFC4C3EA), Color(0xFF434465), Color(0xFFE1DFFF),
        Color(0xFF0F1416), Color(0xFFDEE3E6), Color(0xFF3F484C), Color(0xFFBFC8CC), Color(0xFF899296),
        listOf(Color(0xFF0A0F11), Color(0xFF171C1F), Color(0xFF1B2023), Color(0xFF252B2D), Color(0xFF303638))
    )
    ThemeMood.DUSK -> Palette(
        Color(0xFF8A5021), Color.White, Color(0xFFFFDCC5), Color(0xFF301400),
        Color(0xFF755846), Color(0xFFFFDCC5), Color(0xFF2B1709),
        Color(0xFF606135), Color(0xFFE6E6AE), Color(0xFF1C1D00),
        Color(0xFFFFF8F5), Color(0xFF221A15), Color(0xFFF3DFD3), Color(0xFF52443B), Color(0xFF85746A),
        listOf(Color(0xFFFFFFFF), Color(0xFFFCF1EB), Color(0xFFF6EBE5), Color(0xFFF1E5DF), Color(0xFFEBE0DA))
    ) to Palette(
        Color(0xFFFFB783), Color(0xFF4F2500), Color(0xFF6D390B), Color(0xFFFFDCC5),
        Color(0xFFE5BFA9), Color(0xFF5B4130), Color(0xFFFFDCC5),
        Color(0xFFCACA94), Color(0xFF48491F), Color(0xFFE6E6AE),
        Color(0xFF19120D), Color(0xFFEFE0D8), Color(0xFF52443B), Color(0xFFD7C3B7), Color(0xFFA08D82),
        listOf(Color(0xFF140D08), Color(0xFF221A15), Color(0xFF261E19), Color(0xFF312823), Color(0xFF3C332D))
    )
    ThemeMood.LAVENDER -> Palette(
        Color(0xFF65558F), Color.White, Color(0xFFE9DDFF), Color(0xFF201047),
        Color(0xFF625B71), Color(0xFFE8DEF8), Color(0xFF1E192B),
        Color(0xFF7D5260), Color(0xFFFFD8E4), Color(0xFF31111D),
        Color(0xFFFDF8FD), Color(0xFF1C1B1F), Color(0xFFE7E0EB), Color(0xFF49454E), Color(0xFF7A757F),
        listOf(Color(0xFFFFFFFF), Color(0xFFF7F2F8), Color(0xFFF1ECF2), Color(0xFFEBE6EC), Color(0xFFE6E1E6))
    ) to Palette(
        Color(0xFFCFBDFE), Color(0xFF36275D), Color(0xFF4D3D75), Color(0xFFE9DDFF),
        Color(0xFFCBC2DB), Color(0xFF4A4458), Color(0xFFE8DEF8),
        Color(0xFFEFB8C8), Color(0xFF633B48), Color(0xFFFFD8E4),
        Color(0xFF141218), Color(0xFFE6E1E6), Color(0xFF49454E), Color(0xFFCAC4CF), Color(0xFF948F99),
        listOf(Color(0xFF0F0D13), Color(0xFF1C1B1F), Color(0xFF211F26), Color(0xFF2B2930), Color(0xFF36343B))
    )
}

fun colorSchemeFor(mood: ThemeMood, dark: Boolean): ColorScheme =
    palettes(mood).let { (light, night) -> if (dark) night.dark() else light.light() }

// Weiche, große Rundungen wirken ruhiger als harte Kanten.
private val PaxShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val PaxTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
        // Zeilenhöhen bleiben bewusst in sp: Textfelder blenden ihr Label zwischen bodyLarge und
        // bodySmall über, und Compose kann em und sp nicht mischen (IllegalArgumentException).
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun PaxMentisTheme(
    mood: ThemeMood = ThemeMood.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(mood, darkTheme),
        typography = PaxTypography,
        shapes = PaxShapes,
        content = content
    )
}
