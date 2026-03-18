package com.peimama.renzi.ui.screen.lesson

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.peimama.renzi.data.local.entity.ExerciseEntity
import com.peimama.renzi.data.model.ExerciseType
import com.peimama.renzi.ui.components.PrimaryActionButton
import com.peimama.renzi.ui.theme.AppDimens
import com.peimama.renzi.ui.viewmodel.LearningStep
import com.peimama.renzi.ui.viewmodel.LessonFlowUiState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonFlowScreen(
    uiState: StateFlow<LessonFlowUiState>,
    messageFlow: SharedFlow<String>,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onNextWord: () -> Unit,
    onPreviousWord: () -> Unit,
    onPlayWord: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onReplayRecording: () -> Unit,
    onReRecord: () -> Unit,
    onChooseListenOption: (String) -> Unit,
    onChooseImageOption: (String) -> Unit,
    onChooseSceneOption: (String) -> Unit,
    onWriteComplete: () -> Unit,
    onGoReview: () -> Unit,
    onGoHome: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val latestContext by rememberUpdatedState(context)

    LaunchedEffect(messageFlow) {
        messageFlow.collectLatest { message ->
            Toast.makeText(latestContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    val stepProgress = (state.currentStep.ordinal + 1) / LearningStep.entries.size.toFloat()
    val imageExercise = exerciseFor(state, ExerciseType.IMAGE_CHOOSE)
    val sceneExercise = exerciseFor(state, ExerciseType.SCENE_JUDGE)
    val contentScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = state.lessonTitle.ifBlank { "课程学习" })
                        Text(text = state.currentStep.title)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        bottomBar = {
            if (state.currentStep != LearningStep.COMPLETE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.ScreenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrimaryActionButton(
                        text = "上一步",
                        modifier = Modifier.weight(1f),
                        onClick = onPreviousStep,
                    )
                    PrimaryActionButton(
                        text = "下一步",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (state.currentStep == LearningStep.WRITE_TRACE) {
                                onWriteComplete()
                            }
                            onNextStep()
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppDimens.ScreenPadding)
                .let { baseModifier ->
                    if (state.currentStep == LearningStep.WRITE_TRACE) {
                        baseModifier
                    } else {
                        baseModifier.verticalScroll(contentScrollState)
                    }
                },
        ) {
            LinearProgressIndicator(
                progress = { stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
            Spacer(modifier = Modifier.padding(bottom = 12.dp))

            when (state.currentStep) {
                LearningStep.LOOK_WORD -> FlashcardScreen(
                    lessonTitle = state.lessonTitle,
                    word = state.words.getOrNull(state.currentWordIndex),
                    currentIndex = state.currentWordIndex,
                    total = state.words.size,
                    onPlayWord = onPlayWord,
                    onPreviousWord = onPreviousWord,
                    onNextWord = onNextWord,
                )

                LearningStep.FOLLOW_READ -> ReadAlongScreen(
                    isRecording = state.isRecording,
                    hasRecording = state.hasRecording,
                    onPlayWord = onPlayWord,
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording,
                    onReplayRecording = onReplayRecording,
                    onReRecord = onReRecord,
                )

                LearningStep.LISTEN_CHOOSE -> AudioChoiceScreen(
                    prompt = promptFor(state, ExerciseType.LISTEN_CHOOSE),
                    options = optionsFor(state, ExerciseType.LISTEN_CHOOSE),
                    selectedOption = state.selectedOption,
                    feedback = state.feedback,
                    feedbackPositive = state.feedbackPositive,
                    onChoose = onChooseListenOption,
                )

                LearningStep.IMAGE_CHOOSE -> ImageChoiceScreen(
                    prompt = promptFor(state, ExerciseType.IMAGE_CHOOSE),
                    options = optionsFor(state, ExerciseType.IMAGE_CHOOSE),
                    selectedOption = state.selectedOption,
                    feedback = state.feedback,
                    feedbackPositive = state.feedbackPositive,
                    imageKey = imageExercise?.imageResName,
                    targetWord = imageExercise?.answer.orEmpty(),
                    onChoose = onChooseImageOption,
                )

                LearningStep.WRITE_TRACE -> WritingPracticeScreen(
                    template = state.words.getOrNull(state.currentWordIndex)?.text ?: "字",
                )

                LearningStep.SCENE_JUDGE -> SceneJudgeScreen(
                    prompt = promptFor(state, ExerciseType.SCENE_JUDGE),
                    options = optionsFor(state, ExerciseType.SCENE_JUDGE),
                    selectedOption = state.selectedOption,
                    feedback = state.feedback,
                    feedbackPositive = state.feedbackPositive,
                    imageKey = sceneExercise?.imageResName,
                    targetWord = sceneExercise?.answer.orEmpty(),
                    onChoose = onChooseSceneOption,
                )

                LearningStep.COMPLETE -> LessonCompleteScreen(
                    learnedWords = state.words,
                    onGoReview = onGoReview,
                    onGoHome = onGoHome,
                )
            }

            Spacer(modifier = Modifier.padding(bottom = 20.dp))
        }
    }
}

private fun exerciseFor(state: LessonFlowUiState, type: ExerciseType): ExerciseEntity? {
    return state.exercises.firstOrNull { it.type == type.name }
}

private fun promptFor(state: LessonFlowUiState, type: ExerciseType): String {
    return exerciseFor(state, type)?.prompt ?: "请选择"
}

private fun optionsFor(state: LessonFlowUiState, type: ExerciseType): List<String> {
    val raw = exerciseFor(state, type)?.options
    if (raw.isNullOrBlank()) {
        return state.words.take(3).map { it.text }
    }

    return runCatching { Json.decodeFromString<List<String>>(raw) }
        .getOrElse { state.words.take(3).map { it.text } }
}
