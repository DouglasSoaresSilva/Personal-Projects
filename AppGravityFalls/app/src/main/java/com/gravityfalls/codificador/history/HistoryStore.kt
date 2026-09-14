package com.gravityfalls.codificador.history

import android.content.Context
import com.gravityfalls.codificador.ciphers.CipherType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Uma conversão guardada no histórico.
 */
data class HistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val cipher: CipherType,
    val encode: Boolean,
    val input: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val modeLabel: String get() = if (encode) "CODIFICAR" else "DESCODIFICAR"
}

/**
 * Persistência simples via SharedPreferences + org.json (sem novas dependências).
 * Guarda no máximo [MAX_ENTRIES] itens (os mais recentes primeiro).
 */
object HistoryStore {
    private const val PREFS = "gf_history_prefs"
    private const val KEY = "entries_v1"
    const val MAX_ENTRIES = 50

    fun load(context: Context): List<HistoryEntry> {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, null) ?: return emptyList()
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cipher = runCatching {
                        CipherType.valueOf(o.optString("cipher", "CAESAR"))
                    }.getOrDefault(CipherType.CAESAR)
                    add(
                        HistoryEntry(
                            id = o.optLong("id", System.currentTimeMillis()),
                            cipher = cipher,
                            encode = o.optBoolean("encode", true),
                            input = o.optString("input", ""),
                            output = o.optString("output", ""),
                            timestamp = o.optLong("ts", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, entries: List<HistoryEntry>) {
        try {
            val arr = JSONArray()
            entries.take(MAX_ENTRIES).forEach { e ->
                arr.put(
                    JSONObject()
                        .put("id", e.id)
                        .put("cipher", e.cipher.name)
                        .put("encode", e.encode)
                        .put("input", e.input)
                        .put("output", e.output)
                        .put("ts", e.timestamp)
                )
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, arr.toString())
                .apply()
        } catch (_: Exception) {
            // Histórico é acessório: nunca deve quebrar o app
        }
    }
}
