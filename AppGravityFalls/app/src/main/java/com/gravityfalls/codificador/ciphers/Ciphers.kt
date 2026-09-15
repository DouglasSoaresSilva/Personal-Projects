package com.gravityfalls.codificador.ciphers

/**
 * Logica das 3 cifras classicas de Gravity Falls.
 *
 * - Cesar: deslocamento configuravel (padrao 3, como na serie).
 *   Codificar = +N (A -> D p/ N=3), Decodificar = -N.
 *   Ex: "WELCOME TO GRAVITY FALLS" <-> "ZHOFRPH WR JUDYLWB IDOOV" (N=3)
 * - Atbash: alfabeto invertido (A <-> Z, B <-> Y...). Simetrica.
 * - A1Z26: A=1, B=2 ... Z=26.
 */

enum class CipherType(val title: String, val symbol: String) {
    CAESAR("CIFRA DE CESAR", "△"),
    ATBASH("CIFRA ATBASH", "👁"),
    A1Z26("CIFRA A1Z26", "?")
}

private const val CAESAR_SHIFT = 3
const val CAESAR_DEFAULT_SHIFT = 3
const val CAESAR_MIN_SHIFT = 1
const val CAESAR_MAX_SHIFT = 25

fun caesarLabel(shift: Int): String = "(+$shift / −$shift)"

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

/** Codificar Cesar: anda 3 para a FRENTE (WELCOME -> ZHOFRPH) */
fun caesarEncode(input: String, shift: Int = CAESAR_SHIFT): String =
    input.map { caesarShiftChar(it, shift) }.joinToString("")

/** Decodificar Cesar: anda 3 para TRAS (ZHOFRPH -> WELCOME) */
fun caesarDecode(input: String, shift: Int = CAESAR_SHIFT): String =
    input.map { caesarShiftChar(it, -shift) }.joinToString("")

fun atbashChar(c: Char): Char {
    if (c in 'A'..'Z') return ('Z'.code - (c.code - 'A'.code)).toChar()
    if (c in 'a'..'z') return ('z'.code - (c.code - 'a'.code)).toChar()
    return c
}

/** Atbash e simetrica: codificar == decodificar */
fun atbash(input: String): String = input.map(::atbashChar).joinToString("")

/**
 * A1Z26 — Codificar:
 * Cada letra vira o seu numero. Letras da mesma palavra separadas por espaco,
 * palavras separadas por " / ". Ex: "WELCOME" -> "23 5 12 3 15 13 5"
 */
fun a1z26Encode(input: String): String {
    val result = StringBuilder()
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    // Normaliza espacos multiplos
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
                // Mantem pontuacao/digitos como estao, separados por espaco
                if (!firstLetter) result.append(' ')
                result.append(c)
                firstLetter = false
            }
        }
    }
    return result.toString()
}

/**
 * A1Z26 — Decodificar:
 * Aceita numeros 1-26 separados por espaco, virgula ou hifen.
 * "/" ou " / " vira espaco (separador de palavra).
 * Tokens invalidos sao mantidos como estao.
 * Ex: "23 5 12 3 15 13 5" -> "WELCOME"
 */
fun a1z26Decode(input: String): String {
    if (input.isBlank()) return ""
    // Troca virgulas e hifens por espaco para tolerancia
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
            // Tolerancia: token tipo "23," ja tratado; se for letra solta, mantem
            else -> out.append(token)
        }
    }
    return out.toString()
}

/** Ponto de entrada unico usado pela UI — com deslocamento configuravel para Cesar */
fun runCipher(type: CipherType, encode: Boolean, input: String, caesarShift: Int = CAESAR_DEFAULT_SHIFT): String {
    val shift = caesarShift.coerceIn(CAESAR_MIN_SHIFT, CAESAR_MAX_SHIFT)
    return when (type) {
        CipherType.CAESAR -> if (encode) caesarEncode(input, shift) else caesarDecode(input, shift)
        CipherType.ATBASH -> atbash(input) // simetrica
        CipherType.A1Z26 -> if (encode) a1z26Encode(input) else a1z26Decode(input)
    }
}

fun cipherDescription(type: CipherType, caesarShift: Int = CAESAR_DEFAULT_SHIFT): String = when (type) {
    CipherType.CAESAR ->
        "Cada letra muda $caesarShift posicoes no alfabeto. Codificar anda +$caesarShift, decodificar volta −$caesarShift.\nEx: WELCOME → ${caesarEncode("WELCOME", caesarShift.coerceIn(CAESAR_MIN_SHIFT, CAESAR_MAX_SHIFT))}"
    CipherType.ATBASH ->
        "Alfabeto totalmente invertido. A vira Z, B vira Y, C vira X…\nCodificar e decodificar fazem a mesma coisa."
    CipherType.A1Z26 ->
        "Cada letra vira o seu numero: A=1, B=2 … Z=26.\nEx: WELCOME → 23 5 12 3 15 13 5  ( / = espaco )"
}

fun cipherExampleInput(type: CipherType, encode: Boolean, caesarShift: Int = CAESAR_DEFAULT_SHIFT): String = when (type) {
    CipherType.CAESAR -> if (encode) "WELCOME TO GRAVITY FALLS"
    else caesarEncode("WELCOME TO GRAVITY FALLS", caesarShift.coerceIn(CAESAR_MIN_SHIFT, CAESAR_MAX_SHIFT))
    CipherType.ATBASH -> if (encode) "WELCOME TO GRAVITY FALLS" else "DVOXLNV GL TIZERMGB UZOOQ"
    CipherType.A1Z26 -> if (encode) "WELCOME TO GRAVITY FALLS"
    else "23 5 12 3 15 13 5 / 20 15 / 7 18 1 22 9 20 25 / 6 1 12 12 19"
}
