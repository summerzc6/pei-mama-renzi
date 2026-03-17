package com.peimama.renzi.ui.screen.lesson

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    var clearKey by remember { mutableStateOf(0) }

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

@Composable
private fun HandwritingCanvas(
    template: String,
    clearSignal: Int,
    modifier: Modifier = Modifier,
) {
    val strokes = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    LaunchedEffect(clearSignal) {
        strokes.clear()
        currentPath = null
    }

    val templatePaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(80, 50, 50, 50)
            textSize = 220f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Box(
        modifier = modifier
            .background(color = Color.White, shape = MaterialTheme.shapes.medium),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color(0xFFF6F6F6), shape = MaterialTheme.shapes.medium)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val path = Path().apply { moveTo(offset.x, offset.y) }
                            strokes += path
                            currentPath = path
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = { currentPath = null },
                        onDragCancel = { currentPath = null },
                    )
                },
        ) {
            drawContext.canvas.nativeCanvas.drawText(
                template,
                size.width / 2f,
                size.height / 2f + templatePaint.textSize / 3f,
                templatePaint,
            )

            strokes.forEach { path ->
                drawPath(
                    path = path,
                    color = Color(0xFF222222),
                    style = Stroke(
                        width = 14f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}
