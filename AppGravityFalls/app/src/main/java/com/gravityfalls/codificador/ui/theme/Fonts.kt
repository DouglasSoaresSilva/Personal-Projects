package com.gravityfalls.codificador.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gravityfalls.codificador.R

/**
 * Fontes do universo Gravity Falls (arquivos locais em res/font).
 *
 * - Stanford: traco do Diario 3, usada APENAS nos titulos grandes
 *   ("CIFRADOR" / "ALFABETOS") no modo claro — fora do titulo ela
 *   prejudica a legibilidade e foi trocada pela fonte do modo escuro.
 * - Amatic SC: manuscrita condensada, usada em toda a interface
 *   (cards, botoes, etiquetas, contadores, margem) nos dois modos —
 *   muito mais legivel que a Stanford em tamanhos pequenos.
 * - CrtMono: monospace do sistema, usada na entrada/saida de texto
 *   nos dois modos (legibilidade de terminal CRT).
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

/**
 * Alfabetos visuais de Gravity Falls (apenas aparencia, sem cifra).
 * O texto interno continua normal (A-Z); a fonte troca os glifos.
 * Arquivos em res/font; fallback automatico se um .ttf falhar.
 */
val CipherFontA = FontFamily(
    Font(R.font.cipher_font_a, FontWeight.Normal)
)

val CipherFontB = FontFamily(
    Font(R.font.cipher_font_b, FontWeight.Normal)
)

val StrangeRunes = FontFamily(
    Font(R.font.strange_runes, FontWeight.Normal)
)

val Theraprism = FontFamily(
    Font(R.font.theraprism, FontWeight.Normal)
)

/** Titulo grande estilo diario (modo claro) — unico uso da Stanford. */
val JournalTitle: FontFamily get() = Stanford

/** Titulo estilo grimorio (modo escuro) — Amatic Bold e mais legivel que Stanford no preto. */
val BillTitle: FontFamily get() = Amatic

/** Detalhes pequenos legiveis nos dois modos (contadores, margem, datas). */
val HandSmall: FontFamily get() = Amatic
