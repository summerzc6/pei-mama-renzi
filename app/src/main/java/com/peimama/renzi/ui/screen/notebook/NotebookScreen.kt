package com.peimama.renzi.ui.screen.notebook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.data.model.WordProgress
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.NotebookUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NotebookScreen(
    uiState: StateFlow<NotebookUiState>,
    contentPadding: PaddingValues,
    onToggleFavorite: (String, Boolean) -> Unit,
    onOpenLesson: (String) -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val buckets = state.buckets
    var tabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("已学会", "容易忘", "不会写", "收藏")

    val currentWords = when (tabIndex) {
        0 -> buckets?.mastered.orEmpty()
        1 -> buckets?.easyForgot.orEmpty()
        2 -> buckets?.cantWrite.orEmpty()
        else -> buckets?.favorites.orEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "我的字本",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, label ->
                    AssistChip(
                        onClick = { tabIndex = index },
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (tabIndex == index) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                    )
                }
            }
        }

        if (currentWords.isEmpty()) {
            item {
                Text(
                    text = "这一栏暂时还没有词，继续学习就会出现。",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(currentWords, key = { it.word.id }) { item ->
                WordBookCard(
                    item = item,
                    onToggleFavorite = onToggleFavorite,
                    onOpenLesson = onOpenLesson,
                )
            }
        }
    }
}

@Composable
private fun WordBookCard(
    item: WordProgress,
    onToggleFavorite: (String, Boolean) -> Unit,
    onOpenLesson: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.word.text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = {
                    val next = item.record?.favorite != true
                    onToggleFavorite(item.word.id, next)
                }) {
                    Icon(
                        imageVector = if (item.record?.favorite == true) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = "收藏",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(text = "解释：${item.word.meaning}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "示例：${item.word.exampleSentence}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "场景课：${item.word.lessonId}", style = MaterialTheme.typography.bodyMedium)

            PrimaryActionButton(
                text = "去练这一课",
                icon = Icons.Filled.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenLesson(item.word.lessonId) },
            )
        }
    }
}
