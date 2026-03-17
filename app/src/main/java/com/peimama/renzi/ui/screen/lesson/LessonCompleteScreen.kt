package com.peimama.renzi.ui.screen.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.ui.components.PrimaryActionButton

@Composable
fun LessonCompleteScreen(
    learnedWords: List<WordItemEntity>,
    onGoReview: () -> Unit,
    onGoHome: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(com.peimama.renzi.ui.theme.AppDimens.ItemGap),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(com.peimama.renzi.ui.theme.AppDimens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "今天学会了 ${learnedWords.size} 个词",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = learnedWords.joinToString("、") { it.text },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "你做得很好，每天进步一点点。",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        PrimaryActionButton(
            text = "去复习",
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoReview,
        )

        PrimaryActionButton(
            text = "回首页",
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoHome,
        )
    }
}

