package com.gravityfalls.codificador.ui

import android.app.Activity
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SpaceBar
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
import com.gravityfalls.codificador.alphabets.AlphabetHistoryEntry
import com.gravityfalls.codificador.alphabets.AlphabetHistoryStore
import com.gravityfalls.codificador.alphabets.VisualAlphabet
import com.gravityfalls.codificador.alphabets.alphabetDescription
import com.gravityfalls.codificador.alphabets.alphabetExampleInput
import com.gravityfalls.codificador.alphabets.font
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

private const val ALPHABET_MAX_INPUT = 500
private val AZ_LETTERS = ('A'..'Z').toList()

/**
 * Tela ALFABETOS — independente da tela de cifras.
 *
 * Alfabetos NAO sao cifras: o texto e sempre armazenado como texto normal
 * (ex: "WELCOME" continua "WELCOME"); a fonte apenas troca a aparencia.
 * Nenhuma logica de Cesar / Atbash / A1Z26 e aplicada aqui.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlphabetScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    homeTab: HomeTab = HomeTab.ALPHABET,
    onHomeTabChange: (HomeTab) -> Unit = {}
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = themeMode.resolveDark(systemDark)
    val scheme = MaterialTheme.colorScheme

    var alphabet by remember { mutableStateOf(VisualAlphabet.AUTHOR) }
    var input by remember { mutableStateOf("") }
    var useVisualKeyboard by remember { mutableStateOf(true) }
    var showLegend by remember { mutableStateOf(true) }
    var history by remember { mutableStateOf<List<AlphabetHistoryEntry>>(emptyList()) }
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

    LaunchedEffect(Unit) { history = AlphabetHistoryStore.load(context) }

    fun persist(list: List<AlphabetHistoryEntry>) {
        history = list
        AlphabetHistoryStore.save(context, list)
    }

    fun registerHistory() {
        if (input.isBlank()) return
        val last = history.firstOrNull()
        if (last != null && last.input == input && last.alphabet == alphabet) return
        val entry = AlphabetHistoryEntry(alphabet = alphabet, input = input)
        persist((listOf(entry) + history).take(AlphabetHistoryStore.MAX_ENTRIES))
        scope.launch { snackbar.showSnackbar("Guardado no historico de alfabetos") }
    }

    fun copyNormal(text: String) {
        if (text.isBlank()) return
        clipboard.setText(AnnotatedString(text))
        scope.launch { snackbar.showSnackbar("Copiado como texto normal (a fonte so muda a aparencia)") }
    }

    fun typeLetter(letter: Char) {
        if (input.length >= ALPHABET_MAX_INPUT) return
        input += letter
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
                AlphabetToolbar(
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

                AlphabetHeader(isDark = isDark)

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(if (isDark) BillCardShape else StampShape)
                        .background(if (isDark) Color(0xFF0A0F0A) else Color(0xFFFFFBEB))
                        .border(1.dp, scheme.error, if (isDark) BillCardShape else StampShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "▲ ESTES NAO SAO CODIGOS SAO LETRAS ▲",
                        color = if (isDark) TerminalGreen else BrushRed,
                        fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isDark) "RECORD OF UNKNOWN ALPHABETS" else "DIARIO N. 3 // PAGINA DE ALFABETOS",
                    color = scheme.onBackground.copy(alpha = 0.6f),
                    fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                ThemeSelectorStamps(themeMode = themeMode, onChange = onThemeModeChange, isDark = isDark)

                Spacer(Modifier.height(10.dp))

                HomeTabBar(selected = homeTab, isDark = isDark, onSelect = onHomeTabChange)

                Spacer(Modifier.height(10.dp))

                AlphabetKeyboardSwitch(useVisual = useVisualKeyboard, isDark = isDark) {
                    useVisualKeyboard = it
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESCOLHA O ALFABETO",
                        fontFamily = if (isDark) Amatic else Stanford,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isDark) 19.sp else 17.sp,
                        letterSpacing = 1.sp,
                        color = scheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "4 SISTEMAS VISUAIS",
                        fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = scheme.error.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlphabetCard(
                            alphabet = VisualAlphabet.AUTHOR,
                            selected = alphabet == VisualAlphabet.AUTHOR,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { alphabet = VisualAlphabet.AUTHOR }
                        )
                        AlphabetCard(
                            alphabet = VisualAlphabet.BILL,
                            selected = alphabet == VisualAlphabet.BILL,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { alphabet = VisualAlphabet.BILL }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlphabetCard(
                            alphabet = VisualAlphabet.RUNES,
                            selected = alphabet == VisualAlphabet.RUNES,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { alphabet = VisualAlphabet.RUNES }
                        )
                        AlphabetCard(
                            alphabet = VisualAlphabet.THERAPRISM,
                            selected = alphabet == VisualAlphabet.THERAPRISM,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { alphabet = VisualAlphabet.THERAPRISM }
                        )
                    }
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
                        alphabetDescription(alphabet),
                        color = scheme.onSurfaceVariant,
                        fontFamily = CrtMono, fontSize = 13.sp, lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    SectionLabel("ESCREVA NORMAL", isDark)
                    Text(
                        "${input.length}/$ALPHABET_MAX_INPUT", fontFamily = HandSmall, fontSize = 15.sp,
                        color = scheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(ALPHABET_MAX_INPUT) },
                    readOnly = useVisualKeyboard,
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
                            if (useVisualKeyboard) "Use o teclado visual abaixo…"
                            else "Digite o texto aqui...",
                            color = scheme.onSurface.copy(alpha = 0.45f),
                            fontFamily = if (isDark) CrtMono else HandSmall,
                            fontSize = if (isDark) 14.sp else 18.sp
                        )
                    }
                )

                if (useVisualKeyboard) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionLabel("TECLADO ${alphabet.name} // 26 SIMBOLOS", isDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Legenda A–Z",
                            fontFamily = HandSmall, fontSize = 16.sp,
                            color = scheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = showLegend,
                            onCheckedChange = { showLegend = it }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AZ_LETTERS.forEach { letter ->
                            VisualKey(
                                letter = letter,
                                alphabet = alphabet,
                                showLegend = showLegend,
                                isDark = isDark,
                                onClick = { typeLetter(letter) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickButton("ESPACO", Icons.Filled.SpaceBar, Modifier.weight(1f), isDark) {
                            typeLetter(' ')
                        }
                        QuickButton("APAGAR", Icons.Filled.Backspace, Modifier.weight(1f), isDark) {
                            if (input.isNotEmpty()) input = input.dropLast(1)
                        }
                        QuickButton("LIMPAR", Icons.Filled.Delete, Modifier.weight(1f), isDark) { input = "" }
                    }
                    Text(
                        "Tocou no simbolo → entra a letra normal por dentro.",
                        color = scheme.onBackground.copy(alpha = 0.55f),
                        fontFamily = HandSmall, fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    SectionLabel("VISUAL EM ${alphabetLabelShort(alphabet)}", isDark)
                    Text(
                        if (input.isBlank()) "0/$ALPHABET_MAX_INPUT" else "${input.length} chars",
                        fontFamily = HandSmall, fontSize = 15.sp,
                        color = scheme.onBackground.copy(alpha = 0.55f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                        .clip(if (isDark) BillCardShape else JournalFieldShape)
                        .background(scheme.secondaryContainer)
                        .border(1.dp, scheme.secondary, if (isDark) BillCardShape else JournalFieldShape)
                        .padding(12.dp)
                ) {
                    if (!isDark) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val step = 34.dp.toPx()
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
                        input.ifBlank { "A previa aparece aqui…" },
                        color = if (input.isBlank()) scheme.onSecondaryContainer.copy(alpha = 0.5f)
                        else scheme.onSecondaryContainer,
                        fontFamily = alphabet.font(),
                        fontSize = 28.sp,
                        lineHeight = 36.sp
                    )
                }
                Text(
                    if (alphabet == VisualAlphabet.THERAPRISM)
                        "mesmo texto — so a aparencia muda • Theraprism sem glifo p/ Z"
                    else "mesmo texto — so a aparencia muda",
                    color = scheme.onBackground.copy(alpha = 0.45f),
                    fontFamily = HandSmall, fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    textAlign = TextAlign.End
                )

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    SectionLabel("TEXTO NORMAL (DECODIFICADO)", isDark)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(if (isDark) BillCardShape else JournalFieldShape)
                        .background(scheme.surface)
                        .border(1.dp, scheme.outline, if (isDark) BillCardShape else JournalFieldShape)
                        .padding(12.dp)
                ) {
                    Text(
                        input.ifBlank { "Aqui aparece o texto puro A–Z…" },
                        color = if (input.isBlank()) scheme.onSurface.copy(alpha = 0.5f)
                        else scheme.onSurface,
                        fontFamily = if (isDark) CrtMono else Stanford,
                        fontSize = if (isDark) 15.sp else 19.sp,
                        lineHeight = if (isDark) 22.sp else 24.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (isDark) {
                    Button(
                        onClick = { registerHistory() },
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
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "REGISTRAR NO HISTORICO  ▼",
                            fontFamily = Amatic, fontWeight = FontWeight.Bold, fontSize = 21.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rotate(-0.4f)
                            .clip(StampShape)
                            .background(BrushRed)
                            .clickable { registerHistory() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "REGISTRAR NO HISTORICO  ▼",
                            fontFamily = Stanford, fontSize = 20.sp,
                            color = Color.White, letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("EXEMPLO", Icons.Filled.Book, Modifier.weight(1f), isDark) {
                        input = alphabetExampleInput()
                    }
                    QuickButton("LIMPAR", Icons.Filled.Delete, Modifier.weight(1f), isDark) { input = "" }
                    QuickButton("COPIAR", Icons.Filled.ContentCopy, Modifier.weight(1f), isDark) {
                        copyNormal(input)
                    }
                }

                Spacer(Modifier.height(14.dp))

                AlphabetHistorySection(
                    history = history,
                    isDark = isDark,
                    onRestore = { entry ->
                        alphabet = entry.alphabet
                        input = entry.input
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
                    FootnoteScribble("THE LETTERS WATCH — NAO CONFIE NO TRIANGULO", BloodRed)
                } else {
                    FootnoteScribble("A mesma letra, outro olhar.", BrushRed)
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
                    "Tem certeza que deseja apagar todo o historico de alfabetos? (O das cifras fica intacto.)",
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
                    "▤ HISTORICO ALFABETOS // ${history.size}",
                    fontFamily = if (isDark) Amatic else Stanford,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isDark) 23.sp else 18.sp
                )
            },
            text = {
                if (history.isEmpty()) {
                    Text("Nenhum registro ainda…", fontFamily = HandSmall, fontSize = 18.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(history, key = { it.id }) { entry ->
                            AlphabetHistoryCard(
                                entry = entry,
                                index = history.indexOf(entry),
                                isDark = isDark,
                                onRestore = {
                                    alphabet = entry.alphabet; input = entry.input
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
            title = { Text("Sobre alfabetos", fontFamily = if (isDark) Amatic else Stanford, fontWeight = FontWeight.Bold, fontSize = if (isDark) 25.sp else 20.sp) },
            text = {
                Text(
                    "Os 4 alfabetos visuais de Gravity Falls.\n\n• Autor, Bill, Runas Estranhas e Theraprism.\n• Nao sao cifras: o texto continua normal por dentro.\n• A fonte so muda a aparencia na tela.\n• Historico separado do das cifras.",
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
                    "1. Escolha um dos 4 alfabetos.\n2. Digite normal ou use o teclado visual (26 simbolos).\n3. Tocou no simbolo = entra a letra normal.\n4. Veja a previa com a fonte especial.\n5. Confira o texto normal decodificado.\n6. Toque em REGISTRAR para guardar no historico.",
                    fontFamily = HandSmall, fontSize = 18.sp, lineHeight = 23.sp
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("ENTENDI", fontFamily = HandSmall, fontSize = 18.sp) } }
        )
    }
}

private fun alphabetLabelShort(alphabet: VisualAlphabet): String = when (alphabet) {
    VisualAlphabet.AUTHOR -> "AUTOR"
    VisualAlphabet.BILL -> "BILL"
    VisualAlphabet.RUNES -> "RUNAS"
    VisualAlphabet.THERAPRISM -> "THERAPRISM"
}

@Composable
fun AlphabetToolbar(
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
                "ENTRADA N. 004",
                fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = scheme.onBackground.copy(alpha = 0.65f),
                letterSpacing = 1.sp
            )
            Text(
                "ALFABETOS DESCONHECIDOS",
                fontFamily = if (isDark) Amatic else Stanford,
                fontWeight = FontWeight.Bold,
                fontSize = if (isDark) 18.sp else 15.sp,
                color = if (isDark) TerminalGreen else LeatherBrown,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
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
                            Text("Sobre alfabetos", fontFamily = HandSmall, fontSize = 17.sp)
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

@Composable
fun AlphabetHeader(isDark: Boolean) {
    val floatAnim = rememberInfiniteTransition(label = "billFloatAlpha")
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
                    "ALFABETOS DESCONHECIDOS",
                    modifier = Modifier.offset(y = 108.dp)
                )
            }
        }
        Spacer(Modifier.height(if (isDark) 6.dp else 22.dp))
        Text(
            "ALFABETOS",
            color = if (isDark) BillGold else LeatherBrown,
            fontFamily = if (isDark) Amatic else Stanford,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDark) 52.sp else 40.sp,
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
fun AlphabetKeyboardSwitch(useVisual: Boolean, isDark: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (isDark) {
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
            AlphabetModeButtonTerminal("⌨ NORMAL", !useVisual, mod, Icons.Filled.Keyboard) { onChange(false) }
            AlphabetModeButtonTerminal("◉ VISUAL", useVisual, mod, Icons.Filled.GridOn) { onChange(true) }
        }
    } else {
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
            AlphabetModeButtonJournal("NORMAL", !useVisual, mod) { onChange(false) }
            AlphabetModeButtonJournal("VISUAL", useVisual, mod) { onChange(true) }
        }
    }
}

@Composable
fun AlphabetModeButtonTerminal(
    text: String,
    active: Boolean,
    mod: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = mod
            .clip(BillCardShape)
            .background(if (active) BloodRed.copy(alpha = 0.22f) else Color.Transparent)
            .border(1.dp, if (active) BloodRed else Color.Transparent, BillCardShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(if (active) PhosphorGlow else Color.Gray.copy(alpha = 0.4f), radius = 4.dp.toPx())
            }
            Spacer(Modifier.width(6.dp))
            Icon(icon, contentDescription = null, tint = if (active) BillGold else BloodRed.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                color = if (active) BillGold else BloodRed.copy(alpha = 0.75f),
                fontFamily = Amatic, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
        }
    }
}

@Composable
fun AlphabetModeButtonJournal(text: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    Box(
        modifier = mod
            .clip(StampShape)
            .background(if (active) BrushRed else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, color = if (active) Color.White else BrushRed.copy(alpha = 0.8f),
            fontFamily = Stanford, fontSize = 16.sp
        )
    }
}

@Composable
fun AlphabetCard(
    alphabet: VisualAlphabet,
    selected: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = when {
        selected && isDark -> BloodRed
        selected -> BrushRed
        else -> scheme.outline
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
        if (!isDark) {
            TapePiece(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-16).dp))
        }
        if (isDark && selected) {
            Box(
                modifier = Modifier.matchParentSize()
                    .border(1.dp, BillGold.copy(alpha = 0.35f), shape)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (!isDark) {
                Text(
                    if (selected) "●" else "○",
                    color = if (selected) BrushRed else scheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp, fontFamily = HandSmall
                )
            }
            Text(
                "ABC",
                fontSize = 26.sp,
                fontFamily = alphabet.font(),
                color = when {
                    selected && isDark -> BillGold
                    selected -> BrushRed
                    else -> scheme.onSurface
                }
            )
            Spacer(Modifier.height(2.dp))
            Text(
                alphabet.title,
                color = if (selected && isDark) BillGold else if (selected) BrushRed else scheme.onSurface,
                fontFamily = if (isDark) Amatic else Stanford,
                fontWeight = FontWeight.Bold,
                fontSize = if (isDark) 18.sp else 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(
                alphabet.subtitle,
                color = scheme.onSurface.copy(alpha = 0.6f),
                fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                alphabet.origin,
                color = (if (isDark) TerminalGreen else GoldDim).copy(alpha = 0.9f),
                fontFamily = CrtMono, fontSize = 10.sp, textAlign = TextAlign.Center,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        if (selected && !isDark) {
            HandCircle(
                modifier = Modifier.matchParentSize().padding(2.dp),
                color = BrushRed.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun VisualKey(
    letter: Char,
    alphabet: VisualAlphabet,
    showLegend: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = if (isDark) BillCardShape else StampShape
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = if (showLegend) 62.dp else 52.dp)
            .clip(shape)
            .background(if (isDark) CardDarkElev else scheme.surface)
            .border(1.dp, if (isDark) TerminalGreen.copy(alpha = 0.5f) else PencilGray, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (letter == ' ') {
                Text("␣", fontSize = 20.sp, color = scheme.onSurface)
            } else {
                Text(
                    letter.toString(),
                    fontFamily = alphabet.font(),
                    fontSize = 24.sp,
                    color = if (isDark) BillGold else scheme.onSurface
                )
            }
            if (showLegend && letter != ' ') {
                Text(
                    letter.toString(),
                    fontFamily = HandSmall, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = scheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
fun AlphabetHistorySection(
    history: List<AlphabetHistoryEntry>,
    isDark: Boolean,
    onRestore: (AlphabetHistoryEntry) -> Unit,
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
                "HISTORICO ALFABETOS",
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
                "Nenhum registro ainda…\nToque em REGISTRAR NO HISTORICO para guardar aqui.\n(Separado do historico de cifras.)",
                color = scheme.onSurface.copy(alpha = 0.55f),
                fontFamily = HandSmall, fontSize = 17.sp, lineHeight = 22.sp
            )
        }
    } else {
        history.take(3).forEachIndexed { idx, entry ->
            AlphabetHistoryCard(entry = entry, index = idx, isDark = isDark, onRestore = { onRestore(entry) }, onDelete = { onDeleteOne(entry.id) })
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun AlphabetHistoryCard(entry: AlphabetHistoryEntry, index: Int = 0, isDark: Boolean = false, onRestore: () -> Unit, onDelete: () -> Unit) {
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
                    "REG #${(index + 1).toString().padStart(3, '0')} • ${entry.alphabetLabel}",
                    color = scheme.primary,
                    fontFamily = HandSmall,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                )
                Text(dateStr, color = scheme.onSurface.copy(alpha = 0.5f), fontFamily = HandSmall, fontSize = 15.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Apagar este registro", tint = scheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Text("TEXTO: ${entry.input}", color = scheme.onSurface, fontFamily = CrtMono, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                entry.input.ifBlank { "…" },
                color = scheme.secondary, fontFamily = entry.alphabet.font(), fontSize = 22.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                if (isDark) "▤ toque para reutilizar" else "✎ toque para reutilizar",
                color = scheme.onSurface.copy(alpha = 0.4f), fontFamily = HandSmall, fontSize = 14.sp
            )
        }
    }
}
