package com.peimama.renzi.ui.screen.lesson

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peimama.renzi.ui.components.PrimaryActionButton

@Composable
fun WritingPracticeScreen(
    template: String,
) {
    var clearKey by remember { mutableIntStateOf(0) }

    Column(
        verticalArrangement = Arrangement.spacedBy(com.peimama.renzi.ui.theme.AppDimens.ItemGap),
    ) {
        Text(
            text = "看着淡色字，写一写",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "写完后点“下一步”",
            style = MaterialTheme.typography.bodyLarge,
        )

        HandwritingCanvas(
            template = template,
            clearSignal = clearKey,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        )

        PrimaryActionButton(
            text = "清空重写",
            icon = Icons.Filled.Clear,
            modifier = Modifier.fillMaxWidth(),
            onClick = { clearKey++ },
        )
    }
}

private data class StrokePath(
    val path: Path,
    var lastPoint: Offset,
)

@Composable
private fun HandwritingCanvas(
    template: String,
    clearSignal: Int,
    modifier: Modifier = Modifier,
) {
    val strokes = remember { mutableStateListOf<StrokePath>() }
    var activeStroke by remember { mutableStateOf<StrokePath?>(null) }
    var redrawTick by remember { mutableIntStateOf(0) }
    var dragPointCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(clearSignal) {
        strokes.clear()
        activeStroke = null
        dragPointCounter = 0
        redrawTick++
    }

    val templatePaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(70, 70, 70, 70)
            textSize = 220f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Box(
        modifier = modifier
            .background(color = Color(0xFFF8F8F8), shape = MaterialTheme.shapes.medium),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(clearSignal) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val stroke = StrokePath(
                                path = Path().apply { moveTo(offset.x, offset.y) },
                                lastPoint = offset,
                            )
                            strokes += stroke
                            activeStroke = stroke
                            dragPointCounter = 0
                            redrawTick++
                        },
                        onDrag = { change, _ ->
                            val stroke = activeStroke ?: return@detectDragGestures
                            val position = change.position
                            val last = stroke.lastPoint
                            val dx = position.x - last.x
                            val dy = position.y - last.y
                            if (dx * dx + dy * dy < 16f) {
                                change.consume()
                                return@detectDragGestures
                            }

                            val midX = (last.x + position.x) / 2f
                            val midY = (last.y + position.y) / 2f
                            stroke.path.quadraticBezierTo(last.x, last.y, midX, midY)
                            stroke.lastPoint = position
                            dragPointCounter++
                            if (dragPointCounter % 2 == 0) {
                                redrawTick++
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            activeStroke = null
                            redrawTick++
                        },
                        onDragCancel = {
                            activeStroke = null
                            redrawTick++
                        },
                    )
                },
        ) {
            redrawTick

            drawRect(Color.White)

            drawLine(
                color = Color(0xFFE4E4E4),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2f,
            )
            drawLine(
                color = Color(0xFFE4E4E4),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2f,
            )

            drawContext.canvas.nativeCanvas.drawText(
                template,
                size.width / 2f,
                size.height / 2f + templatePaint.textSize / 3f,
                templatePaint,
            )

            strokes.forEach { stroke ->
                drawPath(
                    path = stroke.path,
                    color = Color(0xFF202020),
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}
