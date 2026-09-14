package com.gravityfalls.codificador.ciphers

/**
 * Lógica das 3 cifras clássicas de Gravity Falls.
 *
 * - César: deslocamento fixo de 3 (como na série).
 *   Codificar = +3 (A -> D), Descodificar = -3 (D -> A).
 *   Ex: "WELCOME TO GRAVITY FALLS" <-> "ZHOFRPH WR JUDYLWB IDOOV"
 * - Atbash: alfabeto invertido (A <-> Z, B <-> Y...). Simétrica.
 * - A1Z26: A=1, B=2 ... Z=26.
 */

enum class CipherType(val title: String, val symbol: String) {
    CAESAR("CIFRA DE CÉSAR", "△"),
    ATBASH("CIFRA ATBASH", "👁"),
    A1Z26("CIFRA A1Z26", "?")
}

private const val CAESAR_SHIFT = 3

fun caesarShiftChar(c: Char, shift: Int): Char {
    if (c in 'A'..'Z') {
        val base = 'A'.code
        return ((c.code - base + shift).mod(26) + base).toChar()
    }
    if (c in 'a'..'z') {
        val base = 'a'.code
        return ((c.code - base + shift).mod(26) + base).toChar()
    }
    return c
}

/** Codificar César: anda 3 para a FRENTE (WELCOME -> ZHOFRPH) */
fun caesarEncode(input: String, shift: Int = CAESAR_SHIFT): String =
    input.map { caesarShiftChar(it, shift) }.joinToString("")

/** Descodificar César: anda 3 para TRÁS (ZHOFRPH -> WELCOME) */
fun caesarDecode(input: String, shift: Int = CAESAR_SHIFT): String =
    input.map { caesarShiftChar(it, -shift) }.joinToString("")

fun atbashChar(c: Char): Char {
    if (c in 'A'..'Z') return ('Z'.code - (c.code - 'A'.code)).toChar()
    if (c in 'a'..'z') return ('z'.code - (c.code - 'a'.code)).toChar()
    return c
}

/** Atbash é simétrica: codificar == descodificar */
fun atbash(input: String): String = input.map(::atbashChar).joinToString("")

/**
 * A1Z26 — Codificar:
 * Cada letra vira o seu número. Letras da mesma palavra separadas por espaço,
 * palavras separadas por " / ". Ex: "WELCOME" -> "23 5 12 3 15 13 5"
 */
fun a1z26Encode(input: String): String {
    val result = StringBuilder()
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    // Normaliza espaços múltiplos
    val words = trimmed.split(Regex("\\s+"))
    words.forEachIndexed { wi, word ->
        if (wi > 0) result.append(" / ")
        var firstLetter = true
        for (c in word) {
            val up = c.uppercaseChar()
            if (up in 'A'..'Z') {
                if (!firstLetter) result.append(' ')
                result.append(up.code - 'A'.code + 1)
                firstLetter = false
            } else {
                // Mantém pontuação/dígitos como estão, separados por espaço
                if (!firstLetter) result.append(' ')
                result.append(c)
                firstLetter = false
            }
        }
    }
    return result.toString()
}

/**
 * A1Z26 — Descodificar:
 * Aceita números 1-26 separados por espaço, vírgula ou hífen.
 * "/" ou " / " vira espaço (separador de palavra).
 * Tokens inválidos são mantidos como "�" com aviso, ou preservados.
 * Ex: "23 5 12 3 15 13 5" -> "WELCOME"
 */
fun a1z26Decode(input: String): String {
    if (input.isBlank()) return ""
    // Troca vírgulas e hífens por espaço para tolerância
    var normalized = input.replace(",", " ").replace("-", " ").trim()
    // Garante que "/" seja um token isolado
    normalized = normalized.replace("/", " / ")
    val tokens = normalized.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val out = StringBuilder()
    for (token in tokens) {
        when {
            token == "/" -> out.append(' ')
            token.toIntOrNull() != null -> {
                val n = token.toInt()
                if (n in 1..26) out.append(('A'.code + n - 1).toChar())
                else out.append("[$token?]")
            }
            // Tolerância: token tipo "23," já tratado; se for letra solta, mantém
            else -> out.append(token)
        }
    }
    return out.toString()
}

/** Ponto de entrada único usado pela UI */
fun runCipher(type: CipherType, encode: Boolean, input: String): String {
    return when (type) {
        CipherType.CAESAR -> if (encode) caesarEncode(input) else caesarDecode(input)
        CipherType.ATBASH -> atbash(input) // simétrica
        CipherType.A1Z26 -> if (encode) a1z26Encode(input) else a1z26Decode(input)
    }
}

fun cipherDescription(type: CipherType): String = when (type) {
    CipherType.CAESAR ->
        "Cada letra é deslocada 3 posições. D vira A, E vira B…\nEx: ZHOFRPH WR JUDYLWB IDOOV → WELCOME TO GRAVITY FALLS"
    CipherType.ATBASH ->
        "Alfabeto totalmente invertido. A vira Z, B vira Y, C vira X…\nCodificar e descodificar são a mesma operação."
    CipherType.A1Z26 ->
        "Cada letra vira o seu número: A=1, B=2 … Z=26.\nEx: WELCOME → 23 5 12 3 15 13 5  ( / = espaço )"
}

fun cipherExampleInput(type: CipherType, encode: Boolean): String = when (type) {
    CipherType.CAESAR -> if (encode) "WELCOME TO GRAVITY FALLS" else "ZHOFRPH WR JUDYLWB IDOOV"
    CipherType.ATBASH -> if (encode) "WELCOME TO GRAVITY FALLS" else "DVOXLNV GL TIZERMGB UZOOQ"
    CipherType.A1Z26 -> if (encode) "WELCOME TO GRAVITY FALLS"
    else "23 5 12 3 15 13 5 / 20 15 / 7 18 1 22 9 20 25 / 6 1 12 12 19"
}
