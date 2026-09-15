package com.gravityfalls.codificador.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gravityfalls.codificador.R

/**
 * Fontes do universo Gravity Falls (arquivos locais em res/font).
 *
 * - Stanford: traço do Diário 3, usada nos títulos / cards / botões no modo claro.
 * - Amatic SC: manuscrita condensada, usada no modo Bill e em detalhes pequenos
 *   (contadores, anotações de margem, datas) nos dois modos — muito mais legível
 *   que a Stanford em tamanhos < 14.sp.
 * - CrtMono: monospace do sistema, preservada só para entrada/saída no modo
 *   escuro (legibilidade de terminal CRT).
 *
 * Se um .ttf falhar, o Compose faz fallback para a fonte padrão — o app nunca quebra.
 */
val Stanford = FontFamily(
    Font(R.font.stanford_regular, FontWeight.Normal)
)

val Amatic = FontFamily(
    Font(R.font.amatic_sc_regular, FontWeight.Normal),
    Font(R.font.amatic_sc_bold, FontWeight.Bold)
)

val CrtMono = FontFamily.Monospace
val JournalSerif = FontFamily.Serif

/** Título grande estilo diário (modo claro). */
val JournalTitle: FontFamily get() = Stanford

/** Título estilo grimório (modo escuro) — Amatic Bold é mais legível que Stanford no preto. */
val BillTitle: FontFamily get() = Amatic

/** Detalhes pequenos legíveis nos dois modos (contadores, margem, datas). */
val HandSmall: FontFamily get() = Amatic
