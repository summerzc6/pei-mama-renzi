package com.peimama.renzi.ui.screen.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.ui.components.PlaceholderImageBox
import com.peimama.renzi.ui.components.PrimaryActionButton

@Composable
fun FlashcardScreen(
    lessonTitle: String,
    word: WordItemEntity?,
    currentIndex: Int,
    total: Int,
    onPlayWord: () -> Unit,
    onPreviousWord: () -> Unit,
    onNextWord: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(com.peimama.renzi.ui.theme.AppDimens.ItemGap),
    ) {
        PlaceholderImageBox(
            label = "看图识字（占位图）",
            modifier = Modifier.fillMaxWidth(),
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(com.peimama.renzi.ui.theme.AppDimens.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = word?.text ?: "",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = word?.pinyin.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "场景分类：$lessonTitle",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "解释：${word?.meaning.orEmpty()}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "例句：${word?.exampleSentence.orEmpty()}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                PrimaryActionButton(
                    text = "播放发音",
                    icon = Icons.Filled.Campaign,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPlayWord,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryActionButton(
                text = "上一张",
                icon = Icons.Filled.ChevronLeft,
                modifier = Modifier.weight(1f),
                onClick = onPreviousWord,
            )
            PrimaryActionButton(
                text = "下一张",
                icon = Icons.Filled.ChevronRight,
                modifier = Modifier.weight(1f),
                onClick = onNextWord,
            )
        }

        Text(
            text = "第 ${currentIndex + 1} / ${total.coerceAtLeast(1)} 张",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}


