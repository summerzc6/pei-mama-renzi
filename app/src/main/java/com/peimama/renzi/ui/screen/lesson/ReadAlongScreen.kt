package com.peimama.renzi.ui.screen.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peimama.renzi.ui.components.PrimaryActionButton

@Composable
fun ReadAlongScreen(
    isRecording: Boolean,
    hasRecording: Boolean,
    onPlayWord: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onReplayRecording: () -> Unit,
    onReRecord: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(com.peimama.renzi.ui.theme.AppDimens.ItemGap),
    ) {
        Text(
            text = "先听标准发音，再跟着读",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        PrimaryActionButton(
            text = "播放标准发音",
            icon = Icons.Filled.PlayArrow,
            modifier = Modifier.fillMaxWidth(),
            onClick = onPlayWord,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryActionButton(
                text = if (isRecording) "录音中" else "开始录音",
                icon = Icons.Filled.KeyboardVoice,
                modifier = Modifier.weight(1f),
                onClick = onStartRecording,
            )
            PrimaryActionButton(
                text = "结束录音",
                icon = Icons.Filled.Clear,
                modifier = Modifier.weight(1f),
                onClick = onStopRecording,
            )
        }

        PrimaryActionButton(
            text = "回放录音",
            icon = Icons.Filled.GraphicEq,
            modifier = Modifier.fillMaxWidth(),
            onClick = onReplayRecording,
        )

        PrimaryActionButton(
            text = "重录",
            modifier = Modifier.fillMaxWidth(),
            onClick = onReRecord,
        )

        Text(
            text = if (hasRecording) "已录音，可以继续" else "录一遍会更好",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

