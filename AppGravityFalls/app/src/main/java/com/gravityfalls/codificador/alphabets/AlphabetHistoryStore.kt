package com.gravityfalls.codificador.alphabets

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Um registro guardado no historico dos ALFABETOS.
 *
 * Separado de [com.gravityfalls.codificador.history.HistoryEntry] de proposito:
 * alfabetos nao sao cifras, entao os historicos nao se misturam.
 * O [input] e sempre texto normal (A-Z); a fonte so muda a aparencia.
 */
data class AlphabetHistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val alphabet: VisualAlphabet,
    val input: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val alphabetLabel: String get() = when (alphabet) {
        VisualAlphabet.AUTHOR -> "Autor"
        VisualAlphabet.BILL -> "Bill"
        VisualAlphabet.RUNES -> "Runas"
        VisualAlphabet.THERAPRISM -> "Theraprism"
    }
}

/**
 * Persistencia simples via SharedPreferences + org.json (mesmo padrao das cifras,
 * mas com PREFS e KEY proprios para manter os historicos separados).
 */
object AlphabetHistoryStore {
    private const val PREFS = "gf_alphabet_history_prefs"
    private const val KEY = "entries_alphabet_v1"
    const val MAX_ENTRIES = 50

    fun load(context: Context): List<AlphabetHistoryEntry> {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, null) ?: return emptyList()
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        AlphabetHistoryEntry(
                            id = o.optLong("id", System.currentTimeMillis()),
                            alphabet = VisualAlphabet.fromName(o.optString("alphabet", "AUTHOR")),
                            input = o.optString("input", ""),
                            timestamp = o.optLong("ts", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, entries: List<AlphabetHistoryEntry>) {
        try {
            val arr = JSONArray()
            entries.take(MAX_ENTRIES).forEach { e ->
                arr.put(
                    JSONObject()
                        .put("id", e.id)
                        .put("alphabet", e.alphabet.name)
                        .put("input", e.input)
                        .put("ts", e.timestamp)
                )
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, arr.toString())
                .apply()
        } catch (_: Exception) {
            // Historico e acessorio: nunca deve quebrar o app
        }
    }
}
