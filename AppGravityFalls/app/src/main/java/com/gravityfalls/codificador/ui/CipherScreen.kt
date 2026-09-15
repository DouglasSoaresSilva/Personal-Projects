package com.gravityfalls.codificador.ui

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gravityfalls.codificador.ciphers.CAESAR_MAX_SHIFT
import com.gravityfalls.codificador.ciphers.CAESAR_MIN_SHIFT
import com.gravityfalls.codificador.ciphers.CipherType
import com.gravityfalls.codificador.ciphers.caesarEncode
import com.gravityfalls.codificador.ciphers.cipherDescription
import com.gravityfalls.codificador.ciphers.cipherExampleInput
import com.gravityfalls.codificador.ciphers.runCipher
import com.gravityfalls.codificador.history.HistoryEntry
import com.gravityfalls.codificador.history.HistoryStore
import com.gravityfalls.codificador.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private const val MAX_INPUT = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = themeMode.resolveDark(systemDark)
    val scheme = MaterialTheme.colorScheme

    var cipher by remember { mutableStateOf(CipherType.CAESAR) }
    var encodeMode by remember { mutableStateOf(true) }
    var caesarShift by remember { mutableStateOf(3) }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showFullHistory by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    val scroll = rememberScrollState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { history = HistoryStore.load(context) }

    fun persist(list: List<HistoryEntry>) {
        history = list
        HistoryStore.save(context, list)
    }

    fun addToHistory(result: String) {
        if (input.isBlank() || result.isBlank()) return
        val last = history.firstOrNull()
        if (last != null && last.input == input && last.output == result &&
            last.cipher == cipher && last.encode == encodeMode && last.caesarShift == caesarShift
        ) return
        val entry = HistoryEntry(
            cipher = cipher,
            encode = encodeMode,
            input = input,
            output = result,
            caesarShift = if (cipher == CipherType.CAESAR) caesarShift else 3
        )
        persist((listOf(entry) + history).take(HistoryStore.MAX_ENTRIES))
    }

    LaunchedEffect(input, cipher, encodeMode, caesarShift) {
        output = if (input.isBlank()) "" else runCipher(cipher, encodeMode, input, caesarShift)
    }

    fun copyResult(text: String) {
        if (text.isBlank()) return
        clipboard.setText(AnnotatedString(text))
        scope.launch { snackbar.showSnackbar("Copiado! O Bill viu isso… 👁") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiniBillIcon(isDark = isDark)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                if (isDark) "MODO ESCURO — LIVRO DO BILL" else "MODO CLARO — DIÁRIO 3",
                                fontFamily = if (isDark) CrtMono else JournalSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = if (isDark) TerminalGreen else LeatherBrown,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                when (themeMode) {
                                    ThemeMode.SYSTEM -> "Tema: sistema (auto)"
                                    ThemeMode.LIGHT -> "Tema: claro (fixo)"
                                    ThemeMode.DARK -> "Tema: escuro (fixo)"
                                },
                                fontFamily = CrtMono, fontSize = 10.sp,
                                color = scheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu / configurações")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        Text(
                            "TEMA", fontFamily = CrtMono, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = scheme.primary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        ThemeMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            when (mode) {
                                                ThemeMode.SYSTEM -> Icons.Filled.AutoMode
                                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                                ThemeMode.DARK -> Icons.Filled.DarkMode
                                            },
                                            contentDescription = null, modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            when (mode) {
                                                ThemeMode.SYSTEM -> "Sistema (auto)"
                                                ThemeMode.LIGHT -> "Claro"
                                                ThemeMode.DARK -> "Escuro"
                                            },
                                            fontFamily = CrtMono, fontSize = 13.sp
                                        )
                                        if (mode == themeMode) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(Icons.Filled.Check, contentDescription = "Ativo", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = { onThemeModeChange(mode); showMenu = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                Text("Sobre o app", fontFamily = CrtMono, fontSize = 13.sp)
                            } },
                            onClick = { showMenu = false; showAbout = true }
                        )
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.HelpOutline, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                Text("Ajuda", fontFamily = CrtMono, fontSize = 13.sp)
                            } },
                            onClick = { showMenu = false; showHelp = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Sair", fontFamily = CrtMono, fontSize = 13.sp) },
                            onClick = { showMenu = false; activity?.finish() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF050705) else Color(0xFFE4D3A8),
                    titleContentColor = scheme.onBackground,
                    actionIconContentColor = scheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = scheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    if (isDark) Brush.verticalGradient(listOf(Color.Black, Color(0xFF070B07), Color.Black))
                    else Brush.verticalGradient(listOf(Color(0xFFF3EAD3), Color(0xFFE9D9B4), Color(0xFFF3EAD3)))
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BillHeader(isDark = isDark)

                Spacer(Modifier.height(10.dp))

                // Slogan pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isDark) Color(0xFF0A0F0A) else Color(0xFFFFFBEB))
                        .border(1.dp, scheme.error, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "▲ THIS IS NOT AN APP DOT COM ▲",
                        color = if (isDark) TerminalGreen else BrushRed,
                        fontFamily = CrtMono, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isDark) "McGUCKET LABS // TERMINAL DE CIFRAS" else "DIÁRIO Nº 3 // PÁGINA DE CIFRAS",
                    color = scheme.onBackground.copy(alpha = 0.6f),
                    fontFamily = CrtMono, fontSize = 11.sp, textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // ── Seletor de tema 3-estados (botão pedido: desativa o seguir-sistema) ──
                ThemeSelectorBar(themeMode = themeMode, onChange = onThemeModeChange, isDark = isDark)

                Spacer(Modifier.height(12.dp))

                ModeSwitch(encodeMode, isDark = isDark) { encodeMode = it }

                Spacer(Modifier.height(12.dp))

                // ── Seleção de cifra em 3 cards horizontais (mockup) ──
                Text(
                    "ESCOLHA A CIFRA", fontFamily = CrtMono, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                    color = scheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CipherType.values().forEach { type ->
                        CipherGridCard(
                            type = type,
                            selected = type == cipher,
                            isDark = isDark,
                            caesarShift = caesarShift,
                            modifier = Modifier.weight(1f),
                            onClick = { cipher = type }
                        )
                    }
                }

                // ── Campo extra: deslocamento do César (+N / −N) ──
                if (cipher == CipherType.CAESAR) {
                    Spacer(Modifier.height(8.dp))
                    CaesarShiftStepper(
                        shift = caesarShift,
                        isDark = isDark,
                        onChange = { caesarShift = it.coerceIn(CAESAR_MIN_SHIFT, CAESAR_MAX_SHIFT) }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.surfaceVariant)
                        .border(1.dp, scheme.outline, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        cipherDescription(cipher, caesarShift),
                        color = scheme.onSurfaceVariant,
                        fontFamily = CrtMono, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Entrada ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionLabel(if (encodeMode) "> TEXTO DE ENTRADA_" else "> TEXTO CIFRADO_", isDark)
                    Text(
                        "${input.length}/$MAX_INPUT", fontFamily = CrtMono, fontSize = 10.sp,
                        color = scheme.onBackground.copy(alpha = 0.55f)
                    )
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(MAX_INPUT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                        .shadow(if (isDark) 6.dp else 2.dp, RoundedCornerShape(10.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = scheme.onSurface,
                        unfocusedTextColor = scheme.onSurface,
                        cursorColor = scheme.secondary,
                        focusedContainerColor = scheme.surface,
                        unfocusedContainerColor = scheme.surface,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = LocalTextStyle.current.copy(fontFamily = CrtMono, fontSize = 15.sp),
                    placeholder = {
                        Text(
                            if (cipher == CipherType.A1Z26 && !encodeMode) "Ex: 23 5 12 3 15 13 5 / 20 15 …"
                            else "Digite algo… ele está observando 👁",
                            color = scheme.onSurface.copy(alpha = 0.45f),
                            fontFamily = CrtMono, fontSize = 13.sp
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("EXEMPLO", Icons.Filled.Book, Modifier.weight(1f), isDark) {
                        input = cipherExampleInput(cipher, encodeMode, caesarShift)
                    }
                    QuickButton("LIMPAR", Icons.Filled.Delete, Modifier.weight(1f), isDark) { input = "" }
                    QuickButton("TROCAR", Icons.Filled.SwapHoriz, Modifier.weight(1f), isDark) {
                        if (output.isNotBlank()) { input = output; encodeMode = !encodeMode }
                        else encodeMode = !encodeMode
                    }
                    QuickButton("COPIAR", Icons.Filled.ContentCopy, Modifier.weight(1f), isDark) {
                        copyResult(output)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Botão principal ──
                Button(
                    onClick = {
                        val result = runCipher(cipher, encodeMode, input, caesarShift)
                        output = result
                        addToHistory(result)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) BloodRed else BrushRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (encodeMode) "CODIFICAR  ▼" else "DESCODIFICAR  ▼",
                        fontFamily = CrtMono, fontWeight = FontWeight.Black, fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Resultado ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionLabel("< RESULTADO_", isDark)
                    if (output.isNotBlank()) {
                        Text(
                            "${output.length} chars", fontFamily = CrtMono, fontSize = 10.sp,
                            color = scheme.onBackground.copy(alpha = 0.55f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.secondaryContainer)
                        .border(1.dp, scheme.secondary, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        output.ifBlank { "A saída aparecerá aqui…" },
                        color = if (output.isBlank()) scheme.onSecondaryContainer.copy(alpha = 0.5f)
                        else scheme.onSecondaryContainer,
                        fontFamily = CrtMono, fontSize = 15.sp, lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Histórico (resumo + ver tudo) ──
                HistorySection(
                    history = history,
                    isDark = isDark,
                    onRestore = { entry ->
                        cipher = entry.cipher
                        encodeMode = entry.encode
                        input = entry.input
                        if (entry.cipher == CipherType.CAESAR) caesarShift = entry.caesarShift
                        scope.launch { snackbar.showSnackbar("Entrada restaurada do histórico") }
                    },
                    onDeleteOne = { id -> persist(history.filterNot { it.id == id }) },
                    onClearAllClick = { showClearAllDialog = true },
                    onSeeAll = { showFullHistory = true }
                )

                Spacer(Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.error.copy(alpha = if (isDark) 0.12f else 0.1f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = null
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "NÃO CONFIE NO TRIÂNGULO\nRealidade é uma ilusão, o universo é um holograma.",
                            color = scheme.error, fontFamily = CrtMono, fontSize = 10.sp,
                            textAlign = TextAlign.Center, lineHeight = 15.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Warning, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            if (isDark) ScanlinesOverlay(Modifier.matchParentSize())
            else ParchmentGrainOverlay(Modifier.matchParentSize())
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = { Text("△", fontSize = 28.sp, color = MaterialTheme.colorScheme.error) },
            title = { Text("LIMPAR TUDO?", fontFamily = CrtMono, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = { Text("Tem certeza que deseja apagar todo o histórico?", fontFamily = CrtMono, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = { persist(emptyList()); showClearAllDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("LIMPAR", fontFamily = CrtMono, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllDialog = false }, shape = RoundedCornerShape(8.dp)) {
                    Text("CANCELAR", fontFamily = CrtMono)
                }
            }
        )
    }

    if (showFullHistory) {
        AlertDialog(
            onDismissRequest = { showFullHistory = false },
            title = { Text("▤ HISTÓRICO // ${history.size}", fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                if (history.isEmpty()) {
                    Text("Nenhuma conversão ainda…", fontFamily = CrtMono, fontSize = 12.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(history, key = { it.id }) { entry ->
                            HistoryCard(
                                entry = entry,
                                onRestore = {
                                    cipher = entry.cipher; encodeMode = entry.encode; input = entry.input
                                    if (entry.cipher == CipherType.CAESAR) caesarShift = entry.caesarShift
                                    showFullHistory = false
                                },
                                onDelete = { persist(history.filterNot { h -> h.id == entry.id }) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFullHistory = false }) {
                    Text("FECHAR", fontFamily = CrtMono, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Sobre o app", fontFamily = CrtMono, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Codificador Gravity Falls — as 3 cifras clássicas da série.\n\n• César: deslocamento configurável (padrão +3/−3)\n• Atbash: A↔Z simétrica\n• A1Z26: A=1…Z=26, / = espaço\n\nVisual: Livro do Bill (escuro) / Diário 3 (claro). Tema segue o sistema por padrão, mas pode ser fixado em claro/escuro.",
                    fontFamily = CrtMono, fontSize = 12.sp, lineHeight = 17.sp
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("OK", fontFamily = CrtMono) } }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Ajuda", fontFamily = CrtMono, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "1. Escolha CODIFICAR ou DESCODIFICAR.\n2. Escolha a cifra (César, Atbash, A1Z26).\n3. No César, ajuste o deslocamento com − / + (1–25).\n4. Digite — a conversão é automática.\n5. Toque no botão principal para gravar no histórico.\n6. Toque num item do histórico para reutilizar.",
                    fontFamily = CrtMono, fontSize = 12.sp, lineHeight = 18.sp
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("ENTENDI", fontFamily = CrtMono) } }
        )
    }
}

// ── Seletor de tema ─────────────────────────────────────────

@Composable
fun ThemeSelectorBar(themeMode: ThemeMode, onChange: (ThemeMode) -> Unit, isDark: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surface)
            .border(1.dp, scheme.outline, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AutoMode, null, tint = scheme.primary, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "TEMA DO APP", fontFamily = CrtMono, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = scheme.primary
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (themeMode == ThemeMode.SYSTEM) "segue o sistema" else "fixo — sistema desativado",
                fontFamily = CrtMono, fontSize = 10.sp, color = scheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeChip("◉ SISTEMA", ThemeMode.SYSTEM, themeMode == ThemeMode.SYSTEM, Modifier.weight(1f)) { onChange(it) }
            ThemeChip("☾ ESCURO", ThemeMode.DARK, themeMode == ThemeMode.DARK, Modifier.weight(1f)) { onChange(it) }
            ThemeChip("☀ CLARO", ThemeMode.LIGHT, themeMode == ThemeMode.LIGHT, Modifier.weight(1f)) { onChange(it) }
        }
    }
}

@Composable
fun ThemeChip(text: String, mode: ThemeMode, active: Boolean, mod: Modifier, onClick: (ThemeMode) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) scheme.primary else scheme.surfaceVariant, label = "chip")
    val fg by animateColorAsState(if (active) scheme.onPrimary else scheme.onSurfaceVariant, label = "chipfg")
    Box(
        modifier = mod
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (active) scheme.primary else scheme.outline, RoundedCornerShape(8.dp))
            .clickable { onClick(mode) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

// ── Histórico ─────────────────────────────────────────────

@Composable
fun HistorySection(
    history: List<HistoryEntry>,
    isDark: Boolean,
    onRestore: (HistoryEntry) -> Unit,
    onDeleteOne: (Long) -> Unit,
    onClearAllClick: () -> Unit,
    onSeeAll: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.History, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("HISTÓRICO", fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = scheme.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearAllClick) {
                    Text("LIMPAR TUDO", fontFamily = CrtMono, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.error)
                }
            }
            TextButton(onClick = onSeeAll) {
                Text("Ver tudo >", fontFamily = CrtMono, fontSize = 11.sp, color = scheme.primary)
            }
        }
    }
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(scheme.surface)
                .border(1.dp, scheme.outline, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Text(
                "Nenhuma conversão ainda…\nToque em CODIFICAR / DESCODIFICAR para gravar aqui.",
                color = scheme.onSurface.copy(alpha = 0.55f),
                fontFamily = CrtMono, fontSize = 12.sp, lineHeight = 17.sp
            )
        }
    } else {
        history.take(3).forEach { entry ->
            HistoryCard(entry = entry, onRestore = { onRestore(entry) }, onDelete = { onDeleteOne(entry.id) })
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun HistoryCard(entry: HistoryEntry, onRestore: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dateStr = remember(entry.timestamp) {
        try { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(entry.timestamp)) }
        catch (_: Exception) { "" }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.surface)
            .border(1.dp, scheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onRestore)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${entry.cipherLabel} • ${entry.modeLabel}",
                    color = scheme.primary, fontFamily = CrtMono,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                )
                Text(dateStr, color = scheme.onSurface.copy(alpha = 0.5f), fontFamily = CrtMono, fontSize = 10.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Apagar esta conversão", tint = scheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Text("IN: ${entry.input}", color = scheme.onSurface, fontFamily = CrtMono, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("OUT: ${entry.output}", color = scheme.secondary, fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("toque para reutilizar ↑", color = scheme.onSurface.copy(alpha = 0.4f), fontFamily = CrtMono, fontSize = 10.sp)
        }
    }
}

// ── Componentes visuais ───────────────────────────────────

@Composable
fun MiniBillIcon(isDark: Boolean) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width; val h = size.height
        val tri = Path().apply { moveTo(w / 2f, 2f); lineTo(w - 2f, h - 2f); lineTo(2f, h - 2f); close() }
        drawPath(tri, if (isDark) BillGold else Color(0xFF3E2723))
        drawCircle(Color.White, radius = w * 0.2f, center = Offset(w / 2f, h * 0.62f))
        drawCircle(Color.Black, radius = w * 0.09f, center = Offset(w / 2f, h * 0.62f))
    }
}

@Composable
fun BillHeader(isDark: Boolean) {
    val scheme = MaterialTheme.colorScheme
    // Piscada do olho a cada ~4s
    val blink = rememberInfiniteTransition(label = "blink")
    val eyeOpen by blink.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Restart), label = "eye"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(104.dp)) {
            val w = size.width; val h = size.height
            // glow externo
            val glowColor = if (isDark) BillGold.copy(alpha = 0.25f) else Color(0xFF8D6E63).copy(alpha = 0.25f)
            val triBig = Path().apply { moveTo(w / 2f, 0f); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(triBig, glowColor)
            val tri = Path().apply { moveTo(w / 2f, 8f); lineTo(w - 8f, h - 8f); lineTo(8f, h - 8f); close() }
            drawPath(tri, if (isDark) BillGold else Color(0xFFFFFBEB))
            // contorno
            drawPath(tri, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
            // tijolos / textura simples
            drawLine(Color.Black.copy(alpha = 0.25f), Offset(w * 0.3f, h * 0.45f), Offset(w * 0.7f, h * 0.45f), 2f)
            // olho (com escala vertical simulando piscada via eyeOpen — simplificado: sempre aberto)
            val eyeR = w * 0.20f * eyeOpen.coerceAtLeast(0.35f)
            drawOval(Color.White, topLeft = Offset(w / 2f - w * 0.2f, h * 0.62f - eyeR), size = Size(w * 0.4f, eyeR * 2))
            drawCircle(Color.Black, radius = w * 0.09f, center = Offset(w / 2f, h * 0.62f))
            // cartola
            drawRect(Color.Black, topLeft = Offset(w * 0.32f, 0f), size = Size(w * 0.36f, h * 0.15f))
            drawRect(Color.Black, topLeft = Offset(w * 0.24f, h * 0.12f), size = Size(w * 0.52f, h * 0.05f))
        }
        Text(
            "CODIFICADOR DE\nGRAVITY FALLS",
            color = if (isDark) BillGold else LeatherBrown,
            fontFamily = if (isDark) CrtMono else JournalSerif,
            fontWeight = FontWeight.Black, fontSize = 24.sp,
            textAlign = TextAlign.Center, lineHeight = 27.sp,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = if (isDark) TerminalGreen.copy(alpha = 0.6f) else Color.Transparent,
                    offset = Offset(0f, 0f), blurRadius = 12f
                )
            )
        )
    }
}

@Composable
fun ModeSwitch(encode: Boolean, isDark: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) Color(0xFF0A0F0A) else Color(0xFFFFFBEB))
            .border(1.dp, scheme.error, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val mod = Modifier.weight(1f).height(44.dp)
        ModeButton("◉ CODIFICAR", encode, mod) { onChange(true) }
        ModeButton("◎ DESCODIFICAR", !encode, mod) { onChange(false) }
    }
}

@Composable
fun ModeButton(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) scheme.error else Color.Transparent, label = "mode")
    Box(
        modifier = mod
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, color = if (active) Color.White else scheme.error,
            fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 13.sp
        )
    }
}

@Composable
fun CipherGridCard(
    type: CipherType, selected: Boolean, isDark: Boolean,
    caesarShift: Int, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = if (selected) scheme.primary else scheme.outline
    val (icon, sub) = when (type) {
        CipherType.CAESAR -> "△" to "+$caesarShift / −$caesarShift"
        CipherType.ATBASH -> "👁" to "A↔Z"
        CipherType.A1Z26 -> "?" to "A=1…Z=26"
    }
    val title = when (type) {
        CipherType.CAESAR -> "CÉSAR"
        CipherType.ATBASH -> "ATBASH"
        CipherType.A1Z26 -> "A1Z26"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) scheme.surfaceVariant else scheme.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (selected) "●" else "○",
                    color = if (selected) scheme.primary else scheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 9.sp, fontFamily = CrtMono
                )
                Spacer(Modifier.width(4.dp))
                Text(icon, fontSize = 22.sp, color = if (selected) scheme.primary else scheme.onSurface)
            }
            Spacer(Modifier.height(4.dp))
            Text(title, color = if (selected) scheme.primary else scheme.onSurface, fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text(sub, color = scheme.onSurface.copy(alpha = 0.55f), fontFamily = CrtMono, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CaesarShiftStepper(shift: Int, isDark: Boolean, onChange: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.surface)
            .border(1.dp, scheme.primary, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("DESLOCAMENTO DE CÉSAR", fontFamily = CrtMono, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.primary)
            Text(
                "Codifica +$shift • Descodifica −$shift  (1–25)",
                fontFamily = CrtMono, fontSize = 11.sp, color = scheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "WELCOME → ${caesarEncode("WELCOME", shift)}",
                fontFamily = CrtMono, fontSize = 11.sp, color = scheme.secondary, fontWeight = FontWeight.Bold
            )
        }
        IconButton(
            onClick = { onChange(shift - 1) }, enabled = shift > CAESAR_MIN_SHIFT,
            modifier = Modifier.size(40.dp)
        ) { Icon(Icons.Filled.Remove, contentDescription = "Diminuir deslocamento") }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(scheme.primary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("$shift", color = scheme.onPrimary, fontFamily = CrtMono, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        IconButton(
            onClick = { onChange(shift + 1) }, enabled = shift < CAESAR_MAX_SHIFT,
            modifier = Modifier.size(40.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Aumentar deslocamento") }
    }
}

@Composable
fun SectionLabel(text: String, isDark: Boolean) {
    Text(
        text, color = MaterialTheme.colorScheme.primary,
        fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
    )
}

@Composable
fun QuickButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    mod: Modifier = Modifier,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick, modifier = mod, shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary),
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outline),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(2.dp))
            Text(text, fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun ScanlinesOverlay(mod: Modifier) {
    Canvas(modifier = mod) {
        val step = 8.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(Color.White.copy(alpha = 0.03f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
        drawRect(color = Color.Black.copy(alpha = 0.12f))
        // vinheta
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxOf(size.width, size.height) * 0.7f
            )
        )
    }
}

@Composable
fun ParchmentGrainOverlay(mod: Modifier) {
    Canvas(modifier = mod) {
        // manchas de papel + bordas queimadas sutis
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF5D4037).copy(alpha = 0.18f),
                    Color.Transparent, Color.Transparent,
                    Color(0xFF5D4037).copy(alpha = 0.18f)
                )
            )
        )
        val step = 28.dp.toPx()
        var y = step
        while (y < size.height) {
            drawLine(Color(0xFF8D6E63).copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }
    }
}
