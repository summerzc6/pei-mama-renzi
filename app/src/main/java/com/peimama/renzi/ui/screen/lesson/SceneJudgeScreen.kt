package com.peimama.renzi.ui.screen.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.peimama.renzi.ui.components.FeedbackText
import com.peimama.renzi.ui.components.OptionChoiceCard
import com.peimama.renzi.ui.components.PlaceholderImageBox

@Composable
fun SceneJudgeScreen(
    prompt: String,
    options: List<String>,
    selectedOption: String?,
    feedback: String?,
    feedbackPositive: Boolean,
    onChoose: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(com.peimama.renzi.ui.theme.AppDimens.ItemGap),
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        PlaceholderImageBox(
            label = "场景图片占位",
            modifier = Modifier.fillMaxWidth(),
        )

        options.take(3).forEach { option ->
            OptionChoiceCard(
                text = option,
                selected = selectedOption == option,
                onClick = { onChoose(option) },
            )
        }

        if (!feedback.isNullOrBlank()) {
            FeedbackText(message = feedback, isPositive = feedbackPositive)
        }
    }
}
