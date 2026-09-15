package com.gravityfalls.codificador.ui.theme

import android.content.Context

/**
 * Persistencia da preferencia de tema via SharedPreferences (sem novas dependencias),
 * seguindo o mesmo padrao de HistoryStore.
 */
object ThemeStore {
    private const val PREFS = "gf_theme_prefs"
    private const val KEY = "theme_mode_v1"

    fun load(context: Context): ThemeMode {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, ThemeMode.SYSTEM.name)
            ThemeMode.fromName(raw)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun save(context: Context, mode: ThemeMode) {
        try {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, mode.name)
                .apply()
        } catch (_: Exception) {
            // Preferencia acessoria: nunca deve quebrar o app
        }
    }
}
