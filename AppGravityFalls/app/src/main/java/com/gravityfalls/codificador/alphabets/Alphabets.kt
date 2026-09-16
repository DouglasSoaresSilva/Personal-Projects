package com.gravityfalls.codificador.alphabets

import androidx.compose.ui.text.font.FontFamily
import com.gravityfalls.codificador.ui.theme.CipherFontA
import com.gravityfalls.codificador.ui.theme.CipherFontB
import com.gravityfalls.codificador.ui.theme.StrangeRunes
import com.gravityfalls.codificador.ui.theme.Theraprism

/**
 * Os 4 alfabetos visuais de Gravity Falls.
 *
 * IMPORTANTE: alfabetos NAO sao cifras. Nao ha relacao com Cesar, Atbash ou
 * A1Z26. O texto e sempre armazenado e processado como texto normal
 * (ex: "WELCOME" continua "WELCOME"); apenas a fonte troca a aparencia.
 */
enum class VisualAlphabet(
    val title: String,
    val subtitle: String,
    val description: String,
    val origin: String
) {
    AUTHOR(
        title = "ALFABETO DO AUTOR",
        subtitle = "Simbolos do Diario 3",
        description = "Escrita do Autor nos diarios. Cada letra vira um simbolo, mas o texto interno continua normal.",
        origin = "Diario 3 // CipherFontA"
    ),
    BILL(
        title = "ALFABETO DO BILL",
        subtitle = "Escrita triangular",
        description = "Simbolos usados pelo Bill. Aparencia diferente, mesmo texto por baixo.",
        origin = "Livro do Bill // CipherFontB"
    ),
    RUNES(
        title = "RUNAS ESTRANHAS",
        subtitle = "Runas da floresta",
        description = "Runas antigas da floresta e dos unicornios. So muda o visual das letras.",
        origin = "Floresta // Strange Runes"
    ),
    THERAPRISM(
        title = "THERAPRISM",
        subtitle = "Prisao interdimensional",
        description = "Escrita alienigena da prisao onde o Bill ficou preso. Sem cifra, so glifos.",
        origin = "Theraprism // sem glifo p/ Z"
    );

    companion object {
        fun fromName(name: String?): VisualAlphabet =
            values().firstOrNull { it.name == name } ?: AUTHOR
    }
}

/** Fonte de cada alfabeto. Centralizado aqui para a UI nao espalhar `when`. */
fun VisualAlphabet.font(): FontFamily = when (this) {
    VisualAlphabet.AUTHOR -> CipherFontA
    VisualAlphabet.BILL -> CipherFontB
    VisualAlphabet.RUNES -> StrangeRunes
    VisualAlphabet.THERAPRISM -> Theraprism
}

/** Texto de ajuda exibido abaixo da selecao de alfabeto. */
fun alphabetDescription(alphabet: VisualAlphabet): String =
    "${alphabet.description}\nEx: WELCOME continua WELCOME por dentro — na tela aparece em ${alphabet.subtitle.lowercase()}."

/** Exemplo padrao (mesmo das cifras, de proposito: mostra que nada muda por dentro). */
fun alphabetExampleInput(): String = "WELCOME TO GRAVITY FALLS"
