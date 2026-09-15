package com.gravityfalls.codificador.ui.theme

/**
 * Modo de tema com 3 estados.
 * SYSTEM = segue o celular (comportamento original, padrão).
 * LIGHT / DARK = forçam o visual, ignorando o sistema.
 */
enum class ThemeMode(val label: String) {
    SYSTEM("SISTEMA"),
    LIGHT("CLARO"),
    DARK("ESCURO");

    fun resolveDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromName(name: String?): ThemeMode =
            values().firstOrNull { it.name == name } ?: SYSTEM
    }
}
