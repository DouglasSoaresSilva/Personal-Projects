package com.gravityfalls.codificador.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Cores compartilhadas (identidade Bill / Gravity Falls) ──
val BlackVoid = Color(0xFF000000)
val CrtBlack = Color(0xFF050705)
val PanelDark = Color(0xFF0A0F0A)
val TerminalGreen = Color(0xFF33FF66)
val TerminalDim = Color(0xFF1A8C3A)
val PhosphorGlow = Color(0xFF00FF41)
val BloodRed = Color(0xFFFF1A1A)
val ButtonRed = Color(0xFFB30000)
val BillGold = Color(0xFFFFD90A)
val Parchment = Color(0xFFE8DCC0)
val Scanline = Color(0xFF101410)

// ── Modo escuro: terminal CRT do thisisnotawebsitedotcom.com ──
private val DarkCrtScheme = darkColorScheme(
    primary = TerminalGreen,
    onPrimary = Color.Black,
    secondary = BillGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A1A08),
    onSecondaryContainer = BillGold,
    background = BlackVoid,
    onBackground = TerminalGreen,
    surface = PanelDark,
    onSurface = TerminalGreen,
    surfaceVariant = Color(0xFF0D1A0D),
    onSurfaceVariant = Parchment,
    outline = TerminalDim,
    error = BloodRed,
    onError = Color.White
)

// ── Modo claro: Diário do Dipper / pergaminho ──
// Fundo papel, texto marrom-escuro, destaques em vermelho-journal e dourado-queimado.
private val LightJournalScheme = lightColorScheme(
    primary = Color(0xFF6D1A1A),
    onPrimary = Color.White,
    secondary = Color(0xFF7A6200),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5B8),
    onSecondaryContainer = Color(0xFF3E2F00),
    background = Color(0xFFF3EAD3),
    onBackground = Color(0xFF3E2723),
    surface = Color(0xFFFFFBEB),
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFEFE0B8),
    onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFF8D6E63),
    error = Color(0xFFB00020),
    onError = Color.White
)

// CrtMono / Stanford / Amatic / JournalSerif moram em Fonts.kt (evita duplicação).

// ── Cores extras para o redesign imersivo ──
val ParchmentDeep = Color(0xFFD9C69A)
val LeatherBrown = Color(0xFF4E342E)
val BrushRed = Color(0xFFA31A1A)
val CardDarkElev = Color(0xFF0E150E)
val GoldDim = Color(0xFF8C7300)
val InkBrown = Color(0xFF3E2723)
val PencilGray = Color(0xFF8D6E63)
val TapeBeige = Color(0xFFE8DCC0)
val StampRed = Color(0xFFA31A1A)

// ── Formas: cantos ligeiramente irregulares (papel) / terminal (bill) ──
/** Ficha de investigação presa na página — cada canto com raio diferente. */
val JournalCardShape = RoundedCornerShape(3.dp, 14.dp, 4.dp, 12.dp)
/** Campo de diário — quase reto, com leve tremor. */
val JournalFieldShape = RoundedCornerShape(4.dp, 10.dp, 3.dp, 11.dp)
/** Terminal ocultista — reto e severo. */
val BillCardShape = RoundedCornerShape(4.dp)
/** Etiqueta / carimbo. */
val StampShape = RoundedCornerShape(2.dp)

/**
 * Tema com 3 estados:
 * - SYSTEM (padrão): segue o celular via isSystemInDarkTheme()
 * - LIGHT / DARK: forçam o visual, ignorando o sistema.
 * A preferência é persistida em ThemeStore (SharedPreferences).
 */
@Composable
fun GravityFallsTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dark = mode.resolveDark(systemDark)
    GravityFallsTheme(darkTheme = dark, content = content)
}

@Composable
fun GravityFallsTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkCrtScheme else LightJournalScheme,
        content = content
    )
}
