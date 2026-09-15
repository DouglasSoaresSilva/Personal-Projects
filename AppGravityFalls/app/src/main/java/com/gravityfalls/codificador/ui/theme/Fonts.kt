package com.gravityfalls.codificador.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gravityfalls.codificador.R

/**
 * Fontes do universo Gravity Falls (arquivos locais em res/font).
 *
 * - Stanford: traco do Diario 3, usada nos titulos / cards / botoes no modo claro.
 * - Amatic SC: manuscrita condensada, usada no modo Bill e em detalhes pequenos
 *   (contadores, anotacoes de margem, datas) nos dois modos — muito mais legivel
 *   que a Stanford em tamanhos < 14.sp.
 * - CrtMono: monospace do sistema, preservada so para entrada/saida no modo
 *   escuro (legibilidade de terminal CRT).
 *
 * Se um .ttf falhar, o Compose faz fallback para a fonte padrao — o app nunca quebra.
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

/** Titulo grande estilo diario (modo claro). */
val JournalTitle: FontFamily get() = Stanford

/** Titulo estilo grimorio (modo escuro) — Amatic Bold e mais legivel que Stanford no preto. */
val BillTitle: FontFamily get() = Amatic

/** Detalhes pequenos legiveis nos dois modos (contadores, margem, datas). */
val HandSmall: FontFamily get() = Amatic
