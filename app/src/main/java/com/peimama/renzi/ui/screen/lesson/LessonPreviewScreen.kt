package com.peimama.renzi.ui.screen.lesson

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.LessonPreviewUiState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPreviewScreen(
    uiState: StateFlow<LessonPreviewUiState>,
    messageFlow: SharedFlow<String>,
    onBack: () -> Unit,
    onNextWord: () -> Unit,
    onPreviousWord: () -> Unit,
    onPlayWord: () -> Unit,
    onStartLesson: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val latestContext by rememberUpdatedState(context)

    LaunchedEffect(messageFlow) {
        messageFlow.collectLatest { message ->
            Toast.makeText(latestContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程预览") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            Text(
                text = "先看字卡，再开始练习",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )

            FlashcardScreen(
                lessonTitle = state.lessonTitle,
                word = state.words.getOrNull(state.currentWordIndex),
                currentIndex = state.currentWordIndex,
                total = state.words.size,
                onPlayWord = onPlayWord,
                onPreviousWord = onPreviousWord,
                onNextWord = onNextWord,
            )

            PrimaryActionButton(
                text = "开始本课练习",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onStartLesson()
                },
            )
        }
    }
}


