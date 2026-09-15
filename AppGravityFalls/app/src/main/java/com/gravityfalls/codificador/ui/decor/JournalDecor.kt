package com.gravityfalls.codificador.ui.decor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gravityfalls.codificador.ui.theme.Amatic
import com.gravityfalls.codificador.ui.theme.StampRed
import com.gravityfalls.codificador.ui.theme.TapeBeige

/**
 * Peças de papelaria do Diário 3 + glifos do Livro do Bill.
 * Tudo aqui é decorativo e barato (Canvas estático, sem animação em loop).
 */

/** Pedaço de fita adesiva que prende fichas na página. */
@Composable
fun TapePiece(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 13.dp)
            .rotate(-4f)
            .background(TapeBeige.copy(alpha = 0.85f))
    )
}

/**
 * Círculo vermelho desenhado à mão (oval imperfeita, traço duplo).
 * Usado para marcar a seleção no modo Diário.
 */
@Composable
fun HandCircle(
    modifier: Modifier = Modifier,
    color: Color = StampRed
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // oval principal levemente torta
        drawOval(color, topLeft = Offset(w * 0.04f, h * 0.08f), size = Size(w * 0.92f, h * 0.84f), style = Stroke(width = 3.2f))
        // segundo traço deslocado (efeito caneta repassada)
        drawOval(
            color.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.07f, h * 0.12f),
            size = Size(w * 0.87f, h * 0.78f),
            style = Stroke(width = 1.6f)
        )
    }
}

/** Carimbo rotacionado tipo "CONFIDENCIAL / CLASSIFICAÇÃO". */
@Composable
fun StampText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = StampRed
) {
    Text(
        text = text,
        modifier = modifier.rotate(-5f),
        color = color.copy(alpha = 0.85f),
        fontFamily = Amatic,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center
    )
}

/**
 * Faixa de rabiscos de margem: pinheiros, olho, interrogação, triângulo.
 * Altura fixa pequena para não roubar atenção do núcleo funcional.
 */
@Composable
fun MarginDoodles(
    modifier: Modifier = Modifier,
    color: Color,
    alpha: Float = 0.5f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val c = color.copy(alpha = alpha)
        val h = size.height
        // pinheiro esquerdo (3 triângulos empilhados)
        fun pine(cx: Float) {
            val s = h * 0.32f
            for (i in 0..2) {
                val y = h * 0.12f + i * s * 0.62f
                drawPath(
                    Path().apply {
                        moveTo(cx, y); lineTo(cx - s * 0.55f, y + s); lineTo(cx + s * 0.55f, y + s); close()
                    },
                    c, style = Stroke(width = 1.6f)
                )
            }
            drawLine(c, Offset(cx, h * 0.72f), Offset(cx, h * 0.98f), 1.6f)
        }
        pine(size.width * 0.08f)
        pine(size.width * 0.92f)
        // olho central
        val ex = size.width * 0.5f
        drawOval(c, topLeft = Offset(ex - 14f, h * 0.3f), size = Size(28f, 14f), style = Stroke(width = 1.6f))
        drawCircle(c, radius = 3.4f, center = Offset(ex, h * 0.55f))
        // interrogações laterais
        drawCircle(c, radius = 1.6f, center = Offset(size.width * 0.3f, h * 0.78f))
        drawCircle(c, radius = 1.6f, center = Offset(size.width * 0.7f, h * 0.78f))
        // pequenos triângulos
        fun miniTri(cx: Float) {
            val s = 9f
            drawPath(
                Path().apply {
                    moveTo(cx, h * 0.25f); lineTo(cx - s, h * 0.25f + s * 1.4f); lineTo(cx + s, h * 0.25f + s * 1.4f); close()
                },
                c, style = Stroke(width = 1.4f)
            )
        }
        miniTri(size.width * 0.22f)
        miniTri(size.width * 0.78f)
    }
}

/** Linha de rodapé manuscrita — frase ambiental, alpha baixo. */
@Composable
fun FootnoteScribble(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = color.copy(alpha = 0.55f),
            fontFamily = Amatic,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
    }
}
