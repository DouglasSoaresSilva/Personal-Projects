package com.gravityfalls.codificador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gravityfalls.codificador.ciphers.*
import com.gravityfalls.codificador.history.HistoryEntry
import com.gravityfalls.codificador.history.HistoryStore
import com.gravityfalls.codificador.ui.theme.*
import java.util.Date

@Composable
fun CipherScreen() {
    val scheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var cipher by remember { mutableStateOf(CipherType.CAESAR) }
    var encodeMode by remember { mutableStateOf(true) } // true = codificar
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scroll = rememberScrollState()

    // Carrega o histórico persistido ao abrir
    LaunchedEffect(Unit) {
        history = HistoryStore.load(context)
    }

    fun persist(list: List<HistoryEntry>) {
        history = list
        HistoryStore.save(context, list)
    }

    fun addToHistory(result: String) {
        if (input.isBlank() || result.isBlank()) return
        // Evita duplicar exatamente a última conversão seguida
        val last = history.firstOrNull()
        if (last != null && last.input == input && last.output == result &&
            last.cipher == cipher && last.encode == encodeMode
        ) return
        val entry = HistoryEntry(
            cipher = cipher,
            encode = encodeMode,
            input = input,
            output = result
        )
        persist((listOf(entry) + history).take(HistoryStore.MAX_ENTRIES))
    }

    // Recalcula automaticamente ao digitar / trocar de cifra
    // (o histórico só é gravado no botão CODIFICAR/DESCODIFICAR, para não poluir)
    LaunchedEffect(input, cipher, encodeMode) {
        output = if (input.isBlank()) "" else runCipher(cipher, encodeMode, input)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── CABEÇALHO ──────────────────────────────────
            BillHeader()

            Spacer(Modifier.height(12.dp))

            Text(
                "△ THIS IS NOT AN APP DOT COM △",
                color = scheme.error,
                fontFamily = CrtMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                if (isDark) "McGUCKET LABS // TERMINAL DE CIFRAS"
                else "DIÁRIO Nº 3 // PÁGINA DE CIFRAS",
                color = scheme.onBackground.copy(alpha = 0.65f),
                fontFamily = CrtMono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // ── MODO: CODIFICAR / DESCODIFICAR ─────────────
            ModeSwitch(encodeMode) { encodeMode = it }

            Spacer(Modifier.height(12.dp))

            // ── SELETOR DE CIFRA ───────────────────────────
            CipherType.values().forEach { type ->
                CipherCard(
                    type = type,
                    selected = type == cipher,
                    onClick = { cipher = type }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Descrição da cifra ativa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, scheme.outline, RoundedCornerShape(4.dp))
                    .background(scheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    cipherDescription(cipher),
                    color = scheme.onSurfaceVariant,
                    fontFamily = CrtMono,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── ENTRADA ────────────────────────────────────
            CrtLabel(if (encodeMode) "> INPUT // TEXTO CLARO_" else "> INPUT // TEXTO CIFRADO_")
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .border(1.dp, scheme.primary, RoundedCornerShape(4.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = scheme.onSurface,
                    unfocusedTextColor = scheme.onSurface,
                    cursorColor = scheme.secondary,
                    focusedContainerColor = scheme.surface,
                    unfocusedContainerColor = scheme.surface,
                    focusedBorderColor = scheme.primary,
                    unfocusedBorderColor = scheme.outline
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = CrtMono, fontSize = 15.sp
                ),
                placeholder = {
                    Text(
                        if (cipher == CipherType.A1Z26 && !encodeMode)
                            "Ex: 23 5 12 3 15 13 5 / 20 15 …"
                        else "Digite aqui… ele está observando 👁",
                        color = scheme.onSurface.copy(alpha = 0.45f),
                        fontFamily = CrtMono, fontSize = 13.sp
                    )
                }
            )

            // Botões de ação rápida
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CrtButton("EXEMPLO", Modifier.weight(1f)) {
                    input = cipherExampleInput(cipher, encodeMode)
                }
                CrtButton("LIMPAR", Modifier.weight(1f), icon = Icons.Filled.Delete) {
                    input = ""
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CrtButton(
                    if (encodeMode) "CODIFICAR ▼" else "DESCODIFICAR ▼",
                    Modifier.weight(1f),
                    icon = Icons.Filled.PlayArrow,
                    highlight = true
                ) {
                    val result = runCipher(cipher, encodeMode, input)
                    output = result
                    addToHistory(result)
                }
                CrtButton("TROCAR ⇄", Modifier.weight(1f), icon = Icons.Filled.Refresh) {
                    if (output.isNotBlank()) {
                        input = output
                        encodeMode = !encodeMode
                    } else {
                        encodeMode = !encodeMode
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── SAÍDA ──────────────────────────────────────
            CrtLabel("< OUTPUT // RESULTADO_")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .border(1.dp, scheme.secondary, RoundedCornerShape(4.dp))
                    .background(scheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    output.ifBlank { "— aguardando input… —" },
                    color = if (output.isBlank())
                        scheme.onSecondaryContainer.copy(alpha = 0.5f)
                    else scheme.onSecondaryContainer,
                    fontFamily = CrtMono,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            if (output.isNotBlank()) {
                Button(
                    onClick = { clipboard.setText(AnnotatedString(output)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed, contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("COPIAR RESULTADO", fontFamily = CrtMono, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── HISTÓRICO ──────────────────────────────────
            HistorySection(
                history = history,
                onRestore = { entry ->
                    cipher = entry.cipher
                    encodeMode = entry.encode
                    input = entry.input
                },
                onDeleteOne = { id ->
                    persist(history.filterNot { it.id == id })
                },
                onClearAllClick = { showClearAllDialog = true }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "⚠ NÃO CONFIE NO TRIÂNGULO ⚠\nREALIDADE É UMA ILUSÃO, O UNIVERSO É UM HOLOGRAMA.",
                color = scheme.error.copy(alpha = 0.8f),
                fontFamily = CrtMono,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
            Spacer(Modifier.height(32.dp))
        }

        // Scanlines CRT apenas no modo escuro (no modo claro/Diário não faz sentido)
        if (isDark) {
            ScanlinesOverlay(Modifier.matchParentSize())
        }
    }

    // Diálogo de confirmação: apagar TODO o histórico
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text("Apagar todo o histórico?", fontFamily = CrtMono, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "O Bill vai adorar ver você queimando as provas… Esta ação não pode ser desfeita.",
                    fontFamily = CrtMono, fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    persist(emptyList())
                    showClearAllDialog = false
                }) {
                    Text("APAGAR TUDO", fontFamily = CrtMono, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("CANCELAR", fontFamily = CrtMono)
                }
            }
        )
    }
}

// ── Histórico ─────────────────────────────────────────────

@Composable
fun HistorySection(
    history: List<HistoryEntry>,
    onRestore: (HistoryEntry) -> Unit,
    onDeleteOne: (Long) -> Unit,
    onClearAllClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CrtLabel("▤ HISTÓRICO // ${history.size}")
        if (history.isNotEmpty()) {
            TextButton(onClick = onClearAllClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Limpar todo o histórico",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("LIMPAR TUDO", fontFamily = CrtMono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, scheme.outline, RoundedCornerShape(4.dp))
                .background(scheme.surface)
                .padding(12.dp)
        ) {
            Text(
                "Nenhuma conversão ainda…\nToque em CODIFICAR / DESCODIFICAR para gravar aqui.",
                color = scheme.onSurface.copy(alpha = 0.55f),
                fontFamily = CrtMono,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    } else {
        history.forEach { entry ->
            HistoryCard(
                entry = entry,
                onRestore = { onRestore(entry) },
                onDelete = { onDeleteOne(entry.id) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun HistoryCard(
    entry: HistoryEntry,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dateStr = remember(entry.timestamp) {
        // Formato simples e seguro em qualquer locale
        try {
            java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                .format(Date(entry.timestamp))
        } catch (_: Exception) {
            ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, scheme.outline, RoundedCornerShape(4.dp))
            .background(scheme.surface)
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
                    "${entry.cipher.title} • ${entry.modeLabel}",
                    color = scheme.primary,
                    fontFamily = CrtMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    dateStr,
                    color = scheme.onSurface.copy(alpha = 0.5f),
                    fontFamily = CrtMono,
                    fontSize = 10.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Apagar esta conversão",
                        tint = scheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                "IN: ${entry.input}",
                color = scheme.onSurface,
                fontFamily = CrtMono,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "OUT: ${entry.output}",
                color = scheme.secondary,
                fontFamily = CrtMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "toque para reutilizar ↑",
                color = scheme.onSurface.copy(alpha = 0.4f),
                fontFamily = CrtMono,
                fontSize = 10.sp
            )
        }
    }
}

// ── Componentes ──────────────────────────────────────────

@Composable
fun BillHeader() {
    val scheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(84.dp)) {
            val w = size.width
            val h = size.height
            val tri = Path().apply {
                moveTo(w / 2f, 4f)
                lineTo(w - 4f, h - 4f)
                lineTo(4f, h - 4f)
                close()
            }
            drawPath(tri, BillGold)
            drawPath(tri, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
            drawCircle(Color.White, radius = w * 0.20f, center = Offset(w / 2f, h * 0.62f))
            drawCircle(Color.Black, radius = w * 0.09f, center = Offset(w / 2f, h * 0.62f))
            drawRect(Color.Black, topLeft = Offset(w * 0.32f, 0f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.14f))
        }
        Text(
            "CODIFICADOR DE\nGRAVITY FALLS",
            color = scheme.primary,
            fontFamily = CrtMono,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

@Composable
fun ModeSwitch(encode: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, scheme.error, RoundedCornerShape(4.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val mod = Modifier.weight(1f).height(44.dp)
        ModeButton("◉ CODIFICAR", encode, scheme.error, mod) { onChange(true) }
        ModeButton("◎ DESCODIFICAR", !encode, scheme.error, mod) { onChange(false) }
    }
}

@Composable
fun ModeButton(text: String, active: Boolean, color: Color, mod: Modifier, onClick: () -> Unit) {
    Box(
        modifier = mod
            .background(if (active) color else Color.Transparent, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (active) Color.White else color,
            fontFamily = CrtMono,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CipherCard(type: CipherType, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = if (selected) scheme.primary else scheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(if (selected) scheme.surfaceVariant else scheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (selected) "[●]" else "[○]",
                color = if (selected) scheme.primary else scheme.onSurface.copy(alpha = 0.5f),
                fontFamily = CrtMono,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "${type.symbol}  ${type.title}",
                    color = if (selected) scheme.primary else scheme.onSurface,
                    fontFamily = CrtMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    when (type) {
                        CipherType.CAESAR -> "deslocamento −3 / +3"
                        CipherType.ATBASH -> "A↔Z • simétrica"
                        CipherType.A1Z26 -> "A=1 … Z=26"
                    },
                    color = scheme.onSurface.copy(alpha = 0.55f),
                    fontFamily = CrtMono, fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CrtLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = CrtMono,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
    )
}

@Composable
fun CrtButton(
    text: String,
    mod: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = mod,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlight) scheme.primary else scheme.surface,
            contentColor = if (highlight) scheme.onPrimary else scheme.primary
        ),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (highlight) scheme.primary else scheme.outline
        ),
        contentPadding = PaddingValues(10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontFamily = CrtMono, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun ScanlinesOverlay(mod: Modifier) {
    Canvas(modifier = mod) {
        val step = 8.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                Color.White.copy(alpha = 0.025f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
        drawRect(
            color = Color.Black.copy(alpha = 0.15f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height)
        )
    }
}
