package com.peimama.renzi.ui.screen.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.data.model.WordProgress
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.ReviewUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ReviewScreen(
    uiState: StateFlow<ReviewUiState>,
    onStartReview: (String) -> Unit,
    onBackHome: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val buckets = state.buckets

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习") },
                navigationIcon = {
                    IconButton(onClick = onBackHome) {
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
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "今天新学、容易忘、待复习",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (buckets == null) {
                item {
                    Text("正在准备复习内容...", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                item {
                    BucketCard(
                        title = "今天新学",
                        words = buckets.todayNew,
                        onStart = {
                            buckets.todayNew.firstOrNull()?.word?.lessonId?.let(onStartReview)
                        },
                    )
                }

                item {
                    BucketCard(
                        title = "容易忘",
                        words = buckets.easyForgot,
                        onStart = {
                            buckets.easyForgot.firstOrNull()?.word?.lessonId?.let(onStartReview)
                        },
                    )
                }

                item {
                    BucketCard(
                        title = "待复习",
                        words = buckets.pendingReview,
                        onStart = {
                            buckets.pendingReview.firstOrNull()?.word?.lessonId?.let(onStartReview)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BucketCard(
    title: String,
    words: List<WordProgress>,
    onStart: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "$title（${words.size}）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (words.isEmpty()) {
                Text(
                    text = "今天这一栏很棒，先休息一下",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    words.take(6).forEach { item ->
                        Text(
                            text = item.word.text,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
                PrimaryActionButton(
                    text = "开始复习",
                    icon = Icons.Filled.Replay,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onStart,
                )
            }
        }
    }
}


