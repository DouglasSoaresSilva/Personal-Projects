package com.gravityfalls.codificador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gravityfalls.codificador.ui.theme.*

/**
 * Pagina inicial: duas areas independentes — CIFRAS e ALFABETOS.
 *
 * Os alfabetos nao sao cifras (so trocam a aparencia via fonte), por isso
 * vivem numa aba separada, com historico e logica proprios.
 * O tema (Diario 3 / Livro do Bill) e compartilhado entre as abas.
 */
enum class HomeTab { CIPHER, ALPHABET }

@Composable
fun AppHome(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = themeMode.resolveDark(systemDark)
    var tab by remember { mutableStateOf(HomeTab.CIPHER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTabBar(selected = tab, isDark = isDark, onSelect = { tab = it })
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                HomeTab.CIPHER -> CipherScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )
                HomeTab.ALPHABET -> AlphabetScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )
            }
        }
    }
}

@Composable
fun HomeTabBar(selected: HomeTab, isDark: Boolean, onSelect: (HomeTab) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(BillCardShape)
                .background(Color(0xFF0A0F0A))
                .border(1.dp, BloodRed, BillCardShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mod = Modifier.weight(1f).height(44.dp)
            HomeTabButtonDark("△ CIFRAS", selected == HomeTab.CIPHER, mod) { onSelect(HomeTab.CIPHER) }
            HomeTabButtonDark("◉ ALFABETOS", selected == HomeTab.ALPHABET, mod) { onSelect(HomeTab.ALPHABET) }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3EAD3))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(JournalCardShape)
                .background(Color(0xFFFFFBEB))
                .border(1.dp, PencilGray, JournalCardShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mod = Modifier.weight(1f).height(44.dp)
            HomeTabButtonLight("CIFRAS", selected == HomeTab.CIPHER, mod) { onSelect(HomeTab.CIPHER) }
            HomeTabButtonLight("ALFABETOS", selected == HomeTab.ALPHABET, mod) { onSelect(HomeTab.ALPHABET) }
        }
    }
}

@Composable
fun HomeTabButtonDark(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    Box(
        modifier = mod
            .clip(BillCardShape)
            .background(if (active) BloodRed.copy(alpha = 0.22f) else Color.Transparent)
            .border(1.dp, if (active) BloodRed else Color.Transparent, BillCardShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (active) BillGold else BloodRed.copy(alpha = 0.75f),
            fontFamily = Amatic, fontWeight = FontWeight.Bold, fontSize = 18.sp
        )
    }
}

@Composable
fun HomeTabButtonLight(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    Box(
        modifier = mod
            .clip(StampShape)
            .background(if (active) BrushRed else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, color = if (active) Color.White else BrushRed.copy(alpha = 0.8f),
            fontFamily = Stanford, fontSize = 16.sp
        )
    }
}
