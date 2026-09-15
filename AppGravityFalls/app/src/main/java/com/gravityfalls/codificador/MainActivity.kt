package com.gravityfalls.codificador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gravityfalls.codificador.ui.CipherScreen
import com.gravityfalls.codificador.ui.theme.GravityFallsTheme
import com.gravityfalls.codificador.ui.theme.ThemeMode
import com.gravityfalls.codificador.ui.theme.ThemeStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Carrega sincronamente antes do setContent para evitar flash de tema errado
        val initialMode = ThemeStore.load(this)
        setContent {
            var themeMode by remember { mutableStateOf(initialMode) }
            val systemDark = isSystemInDarkTheme()
            GravityFallsTheme(mode = themeMode, systemDark = systemDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CipherScreen(
                        themeMode = themeMode,
                        onThemeModeChange = { newMode ->
                            themeMode = newMode
                            ThemeStore.save(applicationContext, newMode)
                        }
                    )
                }
            }
        }
    }
}
