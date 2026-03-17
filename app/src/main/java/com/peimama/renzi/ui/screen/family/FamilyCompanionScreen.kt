package com.peimama.renzi.ui.screen.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.ui.components.StatCard
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.FamilyUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FamilyCompanionScreen(
    uiState: StateFlow<FamilyUiState>,
    contentPadding: PaddingValues,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val dashboard = state.dashboard

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "家属陪学",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    title = "今日学习时长",
                    value = "${dashboard?.todayMinutes ?: 0} 分钟",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "今日新学词",
                    value = "${dashboard?.todayNewWords ?: 0}",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "容易忘的词",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    val forgetWords = dashboard?.easyForgotWords.orEmpty()
                    if (forgetWords.isEmpty()) {
                        Text("今天状态不错，暂时没有高风险词。", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text(
                            text = forgetWords.take(12).joinToString("、") { it.word.text },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "最近学习记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        val records = dashboard?.recentLearning.orEmpty()
        if (records.isEmpty()) {
            item {
                Text(
                    text = "今天还没有学习记录",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(records) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${item.wordText}（${item.sceneName}）",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = formatTime(item.time),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(time: Long): String {
    if (time <= 0L) return "--:--"
    return DateTimeFormatter.ofPattern("MM-dd HH:mm")
        .format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
}


