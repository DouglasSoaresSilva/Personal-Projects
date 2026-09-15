package com.gravityfalls.codificador.ui

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gravityfalls.codificador.R
import com.gravityfalls.codificador.ciphers.CAESAR_MAX_SHIFT
import com.gravityfalls.codificador.ciphers.CAESAR_MIN_SHIFT
import com.gravityfalls.codificador.ciphers.CipherType
import com.gravityfalls.codificador.ciphers.caesarEncode
import com.gravityfalls.codificador.ciphers.cipherDescription
import com.gravityfalls.codificador.ciphers.cipherExampleInput
import com.gravityfalls.codificador.ciphers.runCipher
import com.gravityfalls.codificador.history.HistoryEntry
import com.gravityfalls.codificador.history.HistoryStore
import com.gravityfalls.codificador.ui.decor.FootnoteScribble
import com.gravityfalls.codificador.ui.decor.HandCircle
import com.gravityfalls.codificador.ui.decor.MarginDoodles
import com.gravityfalls.codificador.ui.decor.MiniBillIcon
import com.gravityfalls.codificador.ui.decor.StampText
import com.gravityfalls.codificador.ui.decor.TapePiece
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
                // ── Barra fina de pagina: n. de entrada + tema + menu ──
                JournalToolbar(
                    isDark = isDark,
                    themeMode = themeMode,
                    showMenu = showMenu,
                    onMenuChange = { showMenu = it },
                    onThemeModeChange = onThemeModeChange,
                    onAbout = { showAbout = true },
                    onHelp = { showHelp = true },
                    onExit = { activity?.finish() }
                )

                Spacer(Modifier.height(6.dp))

                BillHeader(isDark = isDark)

                Spacer(Modifier.height(8.dp))

                // Slogan — etiqueta costurada na pagina
                Box(
                    modifier = Modifier
                        .clip(if (isDark) BillCardShape else StampShape)
                        .background(if (isDark) Color(0xFF0A0F0A) else Color(0xFFFFFBEB))
                        .border(1.dp, scheme.error, if (isDark) BillCardShape else StampShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "▲ THIS IS NOT AN APP DOT COM ▲",
                        color = if (isDark) TerminalGreen else BrushRed,
                        fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isDark) "RECORD OF UNKNOWN CODES" else "DIARIO N. 3 // PAGINA DE CIFRAS",
                    color = scheme.onBackground.copy(alpha = 0.6f),
                    fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                if (isDark) {
                    Text(
                        "DO NOT TRUST THE TRIANGLE",
                        color = BloodRed.copy(alpha = 0.7f),
                        fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(10.dp))

                ThemeSelectorStamps(themeMode = themeMode, onChange = onThemeModeChange, isDark = isDark)

                Spacer(Modifier.height(10.dp))

                ModeSwitch(encodeMode, isDark = isDark) { encodeMode = it }

                Spacer(Modifier.height(10.dp))

                // ── Selecao de cifra em 3 fichas (layout mantido) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESCOLHA A CIFRA",
                        fontFamily = if (isDark) Amatic else Stanford,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isDark) 19.sp else 17.sp,
                        letterSpacing = 1.sp,
                        color = scheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "CLASSIFICACAO: CONFIDENCIAL",
                        fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = scheme.error.copy(alpha = 0.7f)
                    )
                }
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

                // ── Deslocamento do Cesar ──
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
                        .clip(if (isDark) BillCardShape else JournalCardShape)
                        .background(scheme.surfaceVariant)
                        .border(1.dp, scheme.outline, if (isDark) BillCardShape else JournalCardShape)
                        .padding(10.dp)
                ) {
                    Text(
                        cipherDescription(cipher, caesarShift),
                        color = scheme.onSurfaceVariant,
                        fontFamily = CrtMono, fontSize = 13.sp, lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Entrada (layout mantido: ENTRADA → SAIDA → botao → 4 acoes) ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    SectionLabel(if (encodeMode) "ENTRADA" else "ENTRADA CIFRADA", isDark)
                    Text(
                        "${input.length}/$MAX_INPUT", fontFamily = HandSmall, fontSize = 15.sp,
                        color = scheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(MAX_INPUT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .shadow(if (isDark) 6.dp else 2.dp, if (isDark) BillCardShape else JournalFieldShape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = scheme.onSurface,
                        unfocusedTextColor = scheme.onSurface,
                        cursorColor = if (isDark) BillGold else BrushRed,
                        focusedContainerColor = scheme.surface,
                        unfocusedContainerColor = scheme.surface,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outline
                    ),
                    shape = if (isDark) BillCardShape else JournalFieldShape,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = if (isDark) CrtMono else Stanford,
                        fontSize = if (isDark) 16.sp else 20.sp,
                        lineHeight = if (isDark) 23.sp else 25.sp
                    ),
                    placeholder = {
                        Text(
                            if (cipher == CipherType.A1Z26 && !encodeMode) "Ex: 23 5 12 3 15 13 5 / 20 15 …"
                            else "Digite o texto aqui...",
                            color = scheme.onSurface.copy(alpha = 0.45f),
                            fontFamily = if (isDark) CrtMono else HandSmall,
                            fontSize = if (isDark) 14.sp else 18.sp
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    SectionLabel("SAIDA", isDark)
                    Text(
                        if (output.isBlank()) "0/500" else "${output.length} chars",
                        fontFamily = HandSmall, fontSize = 15.sp,
                        color = scheme.onBackground.copy(alpha = 0.55f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .clip(if (isDark) BillCardShape else JournalFieldShape)
                        .background(scheme.secondaryContainer)
                        .border(1.dp, scheme.secondary, if (isDark) BillCardShape else JournalFieldShape)
                        .padding(12.dp)
                ) {
                    // linhas pautadas sutis no modo diario
                    if (!isDark) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val step = 26.dp.toPx()
                            var y = step
                            while (y < size.height) {
                                drawLine(
                                    Color(0xFF8D6E63).copy(alpha = 0.18f),
                                    Offset(0f, y), Offset(size.width, y), 1f
                                )
                                y += step
                            }
                        }
                    }
                    Text(
                        output.ifBlank { "Resultado aparecera aqui..." },
                        color = if (output.isBlank()) scheme.onSecondaryContainer.copy(alpha = 0.5f)
                        else scheme.onSecondaryContainer,
                        fontFamily = if (isDark) CrtMono else Stanford,
                        fontSize = if (isDark) 16.sp else 20.sp,
                        lineHeight = if (isDark) 23.sp else 25.sp
                    )
                }
                if (isDark) {
                    Text(
                        "DECODIFICACAO ENCONTRADA",
                        color = TerminalGreen.copy(alpha = 0.6f),
                        fontFamily = HandSmall, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )
                } else {
                    Text(
                        "— resultado —",
                        color = scheme.onBackground.copy(alpha = 0.45f),
                        fontFamily = HandSmall, fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Botao principal — pincelada / terminal ──
                if (isDark) {
                    Button(
                        onClick = {
                            val result = runCipher(cipher, encodeMode, input, caesarShift)
                            output = result
                            addToHistory(result)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, BillCardShape)
                            .border(1.dp, BloodRed, BillCardShape),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A0505),
                            contentColor = BloodRed
                        ),
                        shape = BillCardShape
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (encodeMode) "CODIFICAR  ▼" else "DECODIFICAR  ▼",
                            fontFamily = Amatic, fontWeight = FontWeight.Bold, fontSize = 21.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // pincelada: bloco vermelho com cantos vivos + leve rotacao
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rotate(-0.4f)
                            .clip(StampShape)
                            .background(BrushRed)
                            .clickable {
                                val result = runCipher(cipher, encodeMode, input, caesarShift)
                                output = result
                                addToHistory(result)
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (encodeMode) "CODIFICAR  ▼" else "DECODIFICAR  ▼",
                            fontFamily = Stanford, fontSize = 20.sp,
                            color = Color.White, letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
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

                Spacer(Modifier.height(14.dp))

                // ── Historico (resumo + ver tudo) ──
                HistorySection(
                    history = history,
                    isDark = isDark,
                    onRestore = { entry ->
                        cipher = entry.cipher
                        encodeMode = entry.encode
                        input = entry.input
                        if (entry.cipher == CipherType.CAESAR) caesarShift = entry.caesarShift
                        scope.launch { snackbar.showSnackbar("Entrada restaurada do historico") }
                    },
                    onDeleteOne = { id -> persist(history.filterNot { it.id == id }) },
                    onClearAllClick = { showClearAllDialog = true },
                    onSeeAll = { showFullHistory = true }
                )

                Spacer(Modifier.height(12.dp))

                MarginDoodles(
                    color = if (isDark) TerminalGreen else LeatherBrown,
                    alpha = if (isDark) 0.4f else 0.5f
                )
                Spacer(Modifier.height(4.dp))
                if (isDark) {
                    FootnoteScribble("THE CIPHER KNOWS — NAO CONFIE NO TRIANGULO", BloodRed)
                } else {
                    FootnoteScribble("O que voce nao ve... te ve.", BrushRed)
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
            title = {
                Text(
                    "LIMPAR TUDO?",
                    fontFamily = if (isDark) Amatic else Stanford,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isDark) 23.sp else 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "Tem certeza que deseja apagar todo o historico?",
                    fontFamily = HandSmall, fontSize = 18.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { persist(emptyList()); showClearAllDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = Color.White),
                    shape = if (isDark) BillCardShape else StampShape
                ) { Text("LIMPAR", fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 19.sp else 15.sp) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearAllDialog = false },
                    shape = if (isDark) BillCardShape else StampShape
                ) {
                    Text("CANCELAR", fontFamily = if (isDark) Amatic else Stanford, fontSize = if (isDark) 19.sp else 15.sp)
                }
            }
        )
    }

    if (showFullHistory) {
        AlertDialog(
            onDismissRequest = { showFullHistory = false },
            title = {
                Text(
                    "▤ HISTORICO // ${history.size}",
                    fontFamily = if (isDark) Amatic else Stanford,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isDark) 23.sp else 18.sp
                )
            },
            text = {
                if (history.isEmpty()) {
                    Text("Nenhuma conversao ainda…", fontFamily = HandSmall, fontSize = 18.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(history, key = { it.id }) { entry ->
                            HistoryCard(
                                entry = entry,
                                index = history.indexOf(entry),
                                isDark = isDark,
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
                    Text("FECHAR", fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 20.sp else 16.sp)
                }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Sobre o app", fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 25.sp else 20.sp) },
            text = {
                Text(
                    "Codificador Gravity Falls — as 3 cifras classicas da serie.\n\n• Cesar: deslocamento configuravel (padrao +3/-3)\n• Atbash: A↔Z simetrica\n• A1Z26: A=1…Z=26, / = espaco\n\nVisual: Livro do Bill (escuro) / Diario 3 (claro).",
                    fontFamily = HandSmall, fontSize = 18.sp, lineHeight = 23.sp
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("OK", fontFamily = HandSmall, fontSize = 18.sp) } }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Ajuda", fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 25.sp else 20.sp) },
            text = {
                Text(
                    "1. Escolha CODIFICAR ou DECODIFICAR.\n2. Escolha a cifra (Cesar, Atbash, A1Z26).\n3. No Cesar, ajuste o deslocamento com - / + (1-25).\n4. Digite e veja o resultado na hora.\n5. Toque no botao principal para gravar no historico.\n6. Toque num item do historico para reutilizar.",
                    fontFamily = HandSmall, fontSize = 18.sp, lineHeight = 23.sp
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("ENTENDI", fontFamily = HandSmall, fontSize = 18.sp) } }
        )
    }
}

// ── Barra fina de pagina (substitui o TopAppBar Material) ──

@Composable
fun JournalToolbar(
    isDark: Boolean,
    themeMode: ThemeMode,
    showMenu: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    onExit: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniBillIcon(isDark = isDark)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "ENTRADA N. 003",
                fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = scheme.onBackground.copy(alpha = 0.65f),
                letterSpacing = 1.sp
            )
            Text(
                "CIFRAS DESCONHECIDAS",
                fontFamily = if (isDark) Amatic else Stanford,
                fontWeight = FontWeight.Bold,
                fontSize = if (isDark) 18.sp else 15.sp,
                color = if (isDark) TerminalGreen else LeatherBrown,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        // ciclo rapido de tema: SISTEMA → ESCURO → CLARO
        IconButton(onClick = {
            onThemeModeChange(
                when (themeMode) {
                    ThemeMode.SYSTEM -> ThemeMode.DARK
                    ThemeMode.DARK -> ThemeMode.LIGHT
                    ThemeMode.LIGHT -> ThemeMode.SYSTEM
                }
            )
        }) {
            Icon(
                when (themeMode) {
                    ThemeMode.SYSTEM -> Icons.Filled.AutoMode
                    ThemeMode.LIGHT -> Icons.Filled.LightMode
                    ThemeMode.DARK -> Icons.Filled.DarkMode
                },
                contentDescription = "Trocar tema",
                tint = scheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )
        }
        Box {
            IconButton(onClick = { onMenuChange(true) }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Menu / configuracoes",
                    tint = scheme.onBackground.copy(alpha = 0.75f)
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuChange(false) }) {
                Text(
                    "TEMA", fontFamily = HandSmall, fontSize = 15.sp, fontWeight = FontWeight.Bold,
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
                                    fontFamily = HandSmall, fontSize = 17.sp
                                )
                                if (mode == themeMode) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Filled.Check, contentDescription = "Ativo", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        onClick = { onThemeModeChange(mode); onMenuChange(false) }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                            Text("Sobre o app", fontFamily = HandSmall, fontSize = 17.sp)
                        }
                    },
                    onClick = { onMenuChange(false); onAbout() }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.HelpOutline, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                            Text("Ajuda", fontFamily = HandSmall, fontSize = 17.sp)
                        }
                    },
                    onClick = { onMenuChange(false); onHelp() }
                )
                DropdownMenuItem(
                    text = { Text("Sair", fontFamily = HandSmall, fontSize = 17.sp) },
                    onClick = { onMenuChange(false); onExit() }
                )
            }
        }
    }
}

// ── Seletor de tema em carimbos ──

@Composable
fun ThemeSelectorBar(themeMode: ThemeMode, onChange: (ThemeMode) -> Unit, isDark: Boolean) {
    ThemeSelectorStamps(themeMode, onChange, isDark)
}

@Composable
fun ThemeSelectorStamps(themeMode: ThemeMode, onChange: (ThemeMode) -> Unit, isDark: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(if (isDark) BillCardShape else JournalCardShape)
            .background(scheme.surface)
            .border(1.dp, scheme.outline, if (isDark) BillCardShape else JournalCardShape)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Refresh, null, tint = scheme.primary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "TEMA DO APP",
                fontFamily = if (isDark) Amatic else Stanford,
                fontSize = if (isDark) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = scheme.primary
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (themeMode == ThemeMode.SYSTEM) "segue o sistema" else "fixo — sistema desativado",
                fontFamily = HandSmall, fontSize = 15.sp, color = scheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeChip("◉ SISTEMA", ThemeMode.SYSTEM, themeMode == ThemeMode.SYSTEM, Modifier.weight(1f), isDark) { onChange(it) }
            ThemeChip("☾ ESCURO", ThemeMode.DARK, themeMode == ThemeMode.DARK, Modifier.weight(1f), isDark) { onChange(it) }
            ThemeChip("☀ CLARO", ThemeMode.LIGHT, themeMode == ThemeMode.LIGHT, Modifier.weight(1f), isDark) { onChange(it) }
        }
    }
}

@Composable
fun ThemeChip(text: String, mode: ThemeMode, active: Boolean, mod: Modifier, isDark: Boolean, onClick: (ThemeMode) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) scheme.primary else scheme.surfaceVariant, label = "chip")
    val fg by animateColorAsState(if (active) scheme.onPrimary else scheme.onSurfaceVariant, label = "chipfg")
    Box(
        modifier = mod
            .clip(if (isDark) BillCardShape else StampShape)
            .background(bg)
            .border(1.dp, if (active) scheme.primary else scheme.outline, if (isDark) BillCardShape else StampShape)
            .clickable { onClick(mode) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = fg,
            fontFamily = if (isDark) Amatic else Stanford,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDark) 17.sp else 13.sp
        )
    }
}

// ── Historico ──

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
            Text(
                "HISTORICO",
                fontFamily = if (isDark) Amatic else Stanford,
                fontSize = if (isDark) 20.sp else 15.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = scheme.primary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearAllClick) {
                    Text(
                        "limpar tudo ⌫",
                        fontFamily = HandSmall, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = scheme.error
                    )
                }
            }
            TextButton(onClick = onSeeAll) {
                Text("Ver tudo >", fontFamily = HandSmall, fontSize = 16.sp, color = scheme.primary)
            }
        }
    }
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(if (isDark) BillCardShape else JournalCardShape)
                .background(scheme.surface)
                .border(1.dp, scheme.outline, if (isDark) BillCardShape else JournalCardShape)
                .padding(12.dp)
        ) {
            Text(
                "Nenhuma conversao ainda…\nToque em CODIFICAR / DECODIFICAR para gravar aqui.",
                color = scheme.onSurface.copy(alpha = 0.55f),
                fontFamily = HandSmall, fontSize = 17.sp, lineHeight = 22.sp
            )
        }
    } else {
        history.take(3).forEachIndexed { idx, entry ->
            HistoryCard(entry = entry, index = idx, isDark = isDark, onRestore = { onRestore(entry) }, onDelete = { onDeleteOne(entry.id) })
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun HistoryCard(entry: HistoryEntry, index: Int = 0, isDark: Boolean = false, onRestore: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dateStr = remember(entry.timestamp) {
        try { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(entry.timestamp)) }
        catch (_: Exception) { "" }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isDark) Modifier.rotate(if (index % 2 == 0) -0.3f else 0.3f) else Modifier)
            .clip(if (isDark) BillCardShape else JournalCardShape)
            .background(if (isDark) CardDarkElev else scheme.surface)
            .border(1.dp, if (isDark) BloodRed.copy(alpha = 0.45f) else scheme.outline, if (isDark) BillCardShape else JournalCardShape)
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
                    "REG #${(index + 1).toString().padStart(3, '0')} • ${entry.cipherLabel} • ${entry.modeLabel}",
                    color = scheme.primary,
                    fontFamily = HandSmall,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                )
                Text(dateStr, color = scheme.onSurface.copy(alpha = 0.5f), fontFamily = HandSmall, fontSize = 15.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Apagar esta conversao", tint = scheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Text("IN: ${entry.input}", color = scheme.onSurface, fontFamily = CrtMono, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("OUT: ${entry.output}", color = scheme.secondary, fontFamily = CrtMono, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                if (isDark) "▤ toque para reutilizar" else "✎ toque para reutilizar",
                color = scheme.onSurface.copy(alpha = 0.4f), fontFamily = HandSmall, fontSize = 14.sp
            )
        }
    }
}

// ── Componentes visuais ──

@Composable
fun BillHeader(isDark: Boolean) {
    // Flutuacao lenta e sutil do Bill (PNG 500px em nodpi, exibido a ~120.dp)
    val floatAnim = rememberInfiniteTransition(label = "billFloat")
    val floatPhase by floatAnim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "float"
    )
    val driftY = ((floatPhase - 0.5f) * 12f).dp
    val tilt = (floatPhase - 0.5f) * 2f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            Image(
                painter = painterResource(
                    if (isDark) R.drawable.bill_floating_dark_mode
                    else R.drawable.bill_floating_white_mode
                ),
                contentDescription = "Bill flutuando",
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = driftY)
                    .rotate(tilt)
            )
            if (!isDark) {
                StampText(
                    "CIFRAS DESCONHECIDAS",
                    modifier = Modifier.offset(y = 108.dp)
                )
            }
        }
        Spacer(Modifier.height(if (isDark) 6.dp else 22.dp))
        Text(
            "CIFRADOR",
            color = if (isDark) BillGold else LeatherBrown,
            fontFamily = if (isDark) Amatic else Stanford,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDark) 52.sp else 44.sp,
            textAlign = TextAlign.Center, lineHeight = 46.sp,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = if (isDark) TerminalGreen.copy(alpha = 0.6f) else Color.Transparent,
                    offset = Offset(0f, 0f), blurRadius = 12f
                )
            )
        )
        if (!isDark) {
            Text(
                "gravity falls",
                color = LeatherBrown.copy(alpha = 0.7f),
                fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModeSwitch(encode: Boolean, isDark: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (isDark) {
        // Terminal ocultista com bordas vermelhas + LEDs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BillCardShape)
                .background(Color(0xFF0A0F0A))
                .border(1.dp, BloodRed, BillCardShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mod = Modifier.weight(1f).height(46.dp)
            ModeButtonTerminal("◉ CODIFICAR", encode, mod) { onChange(true) }
            ModeButtonTerminal("◎ DECODIFICAR", !encode, mod) { onChange(false) }
        }
    } else {
        // Duas etiquetas manuscritas; selecao = circulo de caneta vermelha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(JournalCardShape)
                .background(Color(0xFFFFFBEB))
                .border(1.dp, PencilGray, JournalCardShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mod = Modifier.weight(1f).height(46.dp)
            ModeButtonJournal("CODIFICAR", encode, mod) { onChange(true) }
            ModeButtonJournal("DECODIFICAR", !encode, mod) { onChange(false) }
        }
    }
}

@Composable
fun ModeButtonTerminal(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) BloodRed.copy(alpha = 0.22f) else Color.Transparent, label = "mode")
    val border = if (active) BloodRed else Color.Transparent
    Box(
        modifier = mod
            .clip(BillCardShape)
            .background(bg)
            .border(1.dp, border, BillCardShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // LED indicador
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(if (active) PhosphorGlow else Color.Gray.copy(alpha = 0.4f), radius = 4.dp.toPx())
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                color = if (active) BillGold else BloodRed.copy(alpha = 0.75f),
                fontFamily = Amatic, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ModeButtonJournal(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    Box(
        modifier = mod
            .clip(StampShape)
            .background(if (active) BrushRed else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!active) {
            // etiqueta inativa: so texto carimbado
            Text(
                text, color = BrushRed.copy(alpha = 0.8f),
                fontFamily = Stanford, fontSize = 16.sp
            )
        } else {
            Text(
                text, color = Color.White,
                fontFamily = Stanford, fontSize = 16.sp
            )
        }
    }
}

// Mantido para compatibilidade (caso algum preview use)
@Composable
fun ModeButton(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) scheme.error else Color.Transparent, label = "mode")
    Box(
        modifier = mod
            .clip(StampShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, color = if (active) Color.White else scheme.error,
            fontFamily = Stanford, fontSize = 15.sp
        )
    }
}

@Composable
fun CipherGridCard(
    type: CipherType, selected: Boolean, isDark: Boolean,
    caesarShift: Int, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = when {
        selected && isDark -> BloodRed
        selected -> BrushRed
        else -> scheme.outline
    }
    val (icon, sub) = when (type) {
        CipherType.CAESAR -> "△" to "+$caesarShift / −$caesarShift"
        CipherType.ATBASH -> "◉" to "A↔Z"
        CipherType.A1Z26 -> "?" to "A=1…Z=26"
    }
    val title = when (type) {
        CipherType.CAESAR -> "CESAR"
        CipherType.ATBASH -> "ATBASH"
        CipherType.A1Z26 -> "A1Z26"
    }
    val detail = when (type) {
        CipherType.CAESAR -> "A→${caesarEncode("A", caesarShift)}"
        CipherType.ATBASH -> "A↔Z"
        CipherType.A1Z26 -> "A=1 B=2"
    }
    val shape = if (isDark) BillCardShape else JournalCardShape
    Box(
        modifier = modifier
            .then(if (!isDark && selected) Modifier.rotate(-1f) else Modifier)
            .clip(shape)
            .background(
                when {
                    selected && isDark -> CardDarkElev
                    selected -> Color(0xFFFFFBEB)
                    else -> scheme.surface
                }
            )
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(top = 12.dp, bottom = 10.dp, start = 8.dp, end = 8.dp)
    ) {
        // fita adesiva no modo diario
        if (!isDark) {
            TapePiece(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-16).dp))
        }
        // brilho de selecao no modo bill
        if (isDark && selected) {
            Box(
                modifier = Modifier.matchParentSize()
                    .border(1.dp, BillGold.copy(alpha = 0.35f), shape)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDark) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(
                            if (selected) PhosphorGlow else Color.Gray.copy(alpha = 0.4f),
                            radius = 4.dp.toPx()
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                } else {
                    Text(
                        if (selected) "●" else "○",
                        color = if (selected) BrushRed else scheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp, fontFamily = HandSmall
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    icon,
                    fontSize = 24.sp,
                    fontFamily = if (isDark) Amatic else Stanford,
                    color = when {
                        selected && isDark -> BillGold
                        selected -> BrushRed
                        else -> scheme.onSurface
                    }
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                title,
                color = if (selected && isDark) BillGold else if (selected) BrushRed else scheme.onSurface,
                fontFamily = if (isDark) Amatic else Stanford,
                fontWeight = FontWeight.Bold,
                fontSize = if (isDark) 21.sp else 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                sub,
                color = scheme.onSurface.copy(alpha = 0.6f),
                fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                detail,
                color = (if (isDark) TerminalGreen else GoldDim).copy(alpha = 0.9f),
                fontFamily = CrtMono, fontSize = 12.sp, textAlign = TextAlign.Center
            )
        }
        // circulo de caneta marcando a ficha ativa no diario
        if (selected && !isDark) {
            HandCircle(
                modifier = Modifier.matchParentSize().padding(2.dp),
                color = BrushRed.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CaesarShiftStepper(shift: Int, isDark: Boolean, onChange: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = if (isDark) BillCardShape else JournalCardShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.surface)
            .border(1.dp, if (isDark) BloodRed.copy(alpha = 0.6f) else BrushRed.copy(alpha = 0.6f), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "DESLOCAMENTO DE CESAR",
                fontFamily = if (isDark) Amatic else Stanford,
                fontSize = if (isDark) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold, color = scheme.primary
            )
            Text(
                "Codifica +$shift • Decodifica −$shift  (1–25)",
                fontFamily = HandSmall, fontSize = 15.sp, color = scheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "WELCOME → ${caesarEncode("WELCOME", shift)}",
                fontFamily = CrtMono, fontSize = 12.sp, color = scheme.secondary, fontWeight = FontWeight.Bold
            )
        }
        IconButton(
            onClick = { onChange(shift - 1) }, enabled = shift > CAESAR_MIN_SHIFT,
            modifier = Modifier.size(40.dp)
        ) { Icon(Icons.Filled.Remove, contentDescription = "Diminuir deslocamento") }
        Box(
            modifier = Modifier
                .clip(if (isDark) BillCardShape else StampShape)
                .background(if (isDark) BloodRed else BrushRed)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("$shift", color = Color.White, fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 21.sp else 17.sp)
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
        fontFamily = if (isDark) Amatic else Stanford,
        fontSize = if (isDark) 20.sp else 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
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
        onClick = onClick, modifier = mod,
        shape = if (isDark) BillCardShape else StampShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) TerminalGreen else LeatherBrown),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) TerminalGreen.copy(alpha = 0.5f) else PencilGray),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(2.dp))
            Text(
                text,
                fontFamily = if (isDark) Amatic else Stanford,
                fontWeight = FontWeight.Bold,
                fontSize = if (isDark) 16.sp else 12.sp,
                maxLines = 1
            )
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
