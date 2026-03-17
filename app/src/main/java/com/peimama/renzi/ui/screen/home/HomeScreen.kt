package com.peimama.renzi.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.data.model.LessonHighlight
import com.peimama.renzi.data.model.LessonStatus
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.components.SectionTitle
import com.peimama.renzi.ui.components.StatusChip
import com.peimama.renzi.ui.components.StatCard
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.HomeUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    uiState: StateFlow<HomeUiState>,
    contentPadding: PaddingValues,
    onOpenSceneList: () -> Unit,
    onOpenLessonList: (String) -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenReview: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val dashboard = state.dashboard

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
    ) {
        item {
            Text(
                text = "陪妈妈认字",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "会认、会读、会写、会在生活里用",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            )
        }

        item {
            WarmWelcomeCard(
                warmMessage = dashboard?.warmMessage ?: "今天也一起学一点。",
                continueLesson = state.continueLesson,
                onContinue = { state.continueLesson?.lessonId?.let(onOpenLesson) },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    title = "今日学习",
                    value = "${dashboard?.summary?.todayStats?.learnMinutes ?: 0} 分钟",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "今日新学",
                    value = "${dashboard?.summary?.todayStats?.newWordsCount ?: 0} 词",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            StatCard(
                title = "已学词数 / 连续学习",
                value = "${dashboard?.summary?.learnedWords ?: 0} / ${dashboard?.summary?.todayStats?.streakDays ?: 1} 天",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            SectionTitle(text = "场景入口")
        }

        items(state.scenes, key = { it.id }) { scene ->
            Card(
                onClick = { onOpenLessonList(scene.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scene.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = scene.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "进入",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        item {
            SectionTitle(text = "推荐课程")
        }

        items(state.recommendedLessons, key = { it.lessonId }) { lesson ->
            RecommendationCard(
                lesson = lesson,
                onStart = { onOpenLesson(lesson.lessonId) },
            )
        }

        if (!dashboard?.easyForgotWords.isNullOrEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "需要多看几次的字",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = dashboard?.easyForgotWords?.joinToString("、").orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }

        item {
            PrimaryActionButton(
                text = "查看全部场景",
                icon = Icons.Filled.Star,
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSceneList,
            )
        }

        item {
            PrimaryActionButton(
                text = "去复习",
                icon = Icons.Filled.Replay,
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenReview,
            )
        }
    }
}

@Composable
private fun WarmWelcomeCard(
    warmMessage: String,
    continueLesson: LessonHighlight?,
    onContinue: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = warmMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "今天先学 1 课就很好。",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (continueLesson != null) {
                Text(
                    text = "继续：${continueLesson.sceneName} · ${continueLesson.title}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                PrimaryActionButton(
                    text = "继续学习",
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onContinue,
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    lesson: LessonHighlight,
    onStart: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${lesson.sceneName} · ${lesson.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusChip(
                    text = lesson.status.label(),
                    background = lesson.status.color(),
                )
            }
            Text(
                text = "本课约 ${lesson.wordCount} 个词，建议 8-12 分钟",
                style = MaterialTheme.typography.bodyMedium,
            )
            PrimaryActionButton(
                text = if (lesson.status == LessonStatus.NOT_STARTED) "开始这课" else "继续这课",
                modifier = Modifier.fillMaxWidth(),
                onClick = onStart,
            )
        }
    }
}

private fun LessonStatus.label(): String {
    return when (this) {
        LessonStatus.NOT_STARTED -> "未开始"
        LessonStatus.IN_PROGRESS -> "学习中"
        LessonStatus.COMPLETED -> "已完成"
        LessonStatus.REVIEW_PENDING -> "待复习"
    }
}

private fun LessonStatus.color(): androidx.compose.ui.graphics.Color {
    return when (this) {
        LessonStatus.NOT_STARTED -> androidx.compose.ui.graphics.Color(0xFF8A8A8A)
        LessonStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFF2061C4)
        LessonStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
        LessonStatus.REVIEW_PENDING -> androidx.compose.ui.graphics.Color(0xFFF08A24)
    }
}
