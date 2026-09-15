package com.gravityfalls.codificador.ui.decor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gravityfalls.codificador.ui.theme.BillGold

/**
 * Triângulo do Bill reutilizável.
 *
 * @param handDrawn true = traço de caneta investigativa (Diário 3, contorno duplo
 *                  irregular marrom). false = grimório dourado com glow (Livro do Bill).
 * @param eyeOpen 0f..1f — altura vertical do olho (piscada). 1f = aberto.
 * @param glitchOffset deslocamento horizontal sutil para o glitch do modo Bill.
 */
@Composable
fun BillTriangle(
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    handDrawn: Boolean = false,
    eyeOpen: Float = 1f,
    glitchOffset: Float = 0f,
    gold: Color = BillGold,
    ink: Color = Color(0xFF3E2723)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val ox = glitchOffset * w * 0.02f

        fun triPath(inset: Float): Path = Path().apply {
            moveTo(w / 2f + ox, inset)
            lineTo(w - inset + ox, h - inset)
            lineTo(inset + ox, h - inset)
            close()
        }

        if (!handDrawn) {
            // glow externo dourado
            drawPath(triPath(0f), gold.copy(alpha = 0.22f))
            // raios geométricos discretos
            val cx = w / 2f + ox
            drawLine(
                gold.copy(alpha = 0.5f),
                Offset(cx, -h * 0.02f), Offset(cx, h * 0.08f), 2f
            )
            drawLine(
                gold.copy(alpha = 0.35f),
                Offset(w * 0.12f, h * 0.96f), Offset(w * 0.30f, h * 0.96f), 2f
            )
            drawLine(
                gold.copy(alpha = 0.35f),
                Offset(w * 0.70f, h * 0.96f), Offset(w * 0.88f, h * 0.96f), 2f
            )
        }

        // corpo
        val body = triPath(if (handDrawn) 7f else 8f)
        drawPath(body, if (handDrawn) Color(0xFFFFFBEB) else gold)
        // contorno — duplo e trêmulo no modo diário
        drawPath(body, Color.Black, style = Stroke(width = if (handDrawn) 3f else 4f))
        if (handDrawn) {
            drawPath(triPath(10f), ink.copy(alpha = 0.55f), style = Stroke(width = 1.4f))
            // textura de tijolos / anotação
            drawLine(
                ink.copy(alpha = 0.3f),
                Offset(w * 0.32f, h * 0.46f), Offset(w * 0.68f, h * 0.46f), 1.6f
            )
        } else {
            drawLine(
                Color.Black.copy(alpha = 0.28f),
                Offset(w * 0.3f + ox, h * 0.45f), Offset(w * 0.7f + ox, h * 0.45f), 2f
            )
        }

        // olho (altura animável para a piscada)
        val open = eyeOpen.coerceIn(0.12f, 1f)
        val eyeR = w * 0.20f * open
        val eyeCx = w / 2f + ox
        val eyeCy = h * 0.66f
        drawOval(
            Color.White,
            topLeft = Offset(eyeCx - w * 0.2f, eyeCy - eyeR),
            size = Size(w * 0.4f, eyeR * 2f)
        )
        drawOval(
            Color.Black,
            topLeft = Offset(eyeCx - w * 0.2f, eyeCy - eyeR),
            size = Size(w * 0.4f, eyeR * 2f),
            style = Stroke(width = 2f)
        )
        drawCircle(Color.Black, radius = w * 0.085f, center = Offset(eyeCx, eyeCy))
        drawCircle(Color.White, radius = w * 0.022f, center = Offset(eyeCx + w * 0.03f, eyeCy - w * 0.03f))

        // cartola
        drawRect(Color.Black, topLeft = Offset(w * 0.32f + ox, 0f), size = Size(w * 0.36f, h * 0.15f))
        drawRect(
            Color.Black,
            topLeft = Offset(w * 0.24f + ox, h * 0.12f),
            size = Size(w * 0.52f, h * 0.05f)
        )
        if (!handDrawn) {
            drawRect(
                gold.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.32f + ox, h * 0.09f),
                size = Size(w * 0.36f, h * 0.02f)
            )
        }
    }
}

/** Ícone pequeno para a barra de título / histórico. */
@Composable
fun MiniBillIcon(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val tri = Path().apply {
            moveTo(w / 2f, 2f); lineTo(w - 2f, h - 2f); lineTo(2f, h - 2f); close()
        }
        drawPath(tri, if (isDark) BillGold else Color(0xFF3E2723))
        drawCircle(Color.White, radius = w * 0.2f, center = Offset(w / 2f, h * 0.62f))
        drawCircle(Color.Black, radius = w * 0.09f, center = Offset(w / 2f, h * 0.62f))
    }
}
