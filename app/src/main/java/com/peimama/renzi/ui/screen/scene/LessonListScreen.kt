package com.peimama.renzi.ui.screen.scene

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.data.model.LessonStatus
import com.peimama.renzi.data.model.LessonWithStatus
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.components.StatusChip
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.SceneLessonsUiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    uiState: StateFlow<SceneLessonsUiState>,
    onBack: () -> Unit,
    onPreviewLesson: (String) -> Unit,
    onStartLesson: (String) -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.scene?.name ?: "课程列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = state.scene?.description ?: "请选择一课开始",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            items(state.lessons, key = { it.lesson.id }) { item ->
                LessonCard(
                    item = item,
                    onPreview = { onPreviewLesson(item.lesson.id) },
                    onStart = { onStartLesson(item.lesson.id) },
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    item: LessonWithStatus,
    onPreview: () -> Unit,
    onStart: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val chip = lessonStatusChip(item.status)
                StatusChip(text = chip.first, background = chip.second)
            }

            Text(
                text = item.lesson.description,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "词数：${item.wordCount} · 已掌握：${item.masteredCount} · 预计${item.estimatedMinutes}分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            )

            if (item.focusWords.isNotEmpty()) {
                Text(
                    text = "重点字：${item.focusWords.joinToString("、")}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrimaryActionButton(
                    text = "先看字卡",
                    icon = Icons.Filled.Info,
                    modifier = Modifier.weight(1f),
                    onClick = onPreview,
                )
                PrimaryActionButton(
                    text = when (item.status) {
                        LessonStatus.NOT_STARTED -> "开始学习"
                        LessonStatus.IN_PROGRESS -> "继续学习"
                        LessonStatus.COMPLETED -> "再学一遍"
                        LessonStatus.REVIEW_PENDING -> "去复习"
                    },
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.weight(1f),
                    onClick = onStart,
                )
            }
        }
    }
}

private fun lessonStatusChip(status: LessonStatus): Pair<String, Color> {
    return when (status) {
        LessonStatus.NOT_STARTED -> "未开始" to Color(0xFF8A8A8A)
        LessonStatus.IN_PROGRESS -> "学习中" to Color(0xFF2061C4)
        LessonStatus.COMPLETED -> "已完成" to Color(0xFF2E7D32)
        LessonStatus.REVIEW_PENDING -> "待复习" to Color(0xFFF08A24)
    }
}


