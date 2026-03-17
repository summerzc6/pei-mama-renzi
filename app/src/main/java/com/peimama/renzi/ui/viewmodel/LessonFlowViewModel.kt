package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.audio.AudioManager
import com.peimama.renzi.audio.RecordingManager
import com.peimama.renzi.data.local.entity.ExerciseEntity
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.data.model.ExerciseType
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class LearningStep(val title: String) {
    LOOK_WORD("看图识字"),
    FOLLOW_READ("跟读练习"),
    LISTEN_CHOOSE("听音选字"),
    IMAGE_CHOOSE("看图选字"),
    WRITE_TRACE("写字描红"),
    SCENE_JUDGE("场景判断"),
    COMPLETE("完成啦"),
}

data class LessonFlowUiState(
    val lessonTitle: String = "",
    val words: List<WordItemEntity> = emptyList(),
    val exercises: List<ExerciseEntity> = emptyList(),
    val currentStep: LearningStep = LearningStep.LOOK_WORD,
    val currentWordIndex: Int = 0,
    val selectedOption: String? = null,
    val feedback: String? = null,
    val feedbackPositive: Boolean = true,
    val isRecording: Boolean = false,
    val hasRecording: Boolean = false,
    val listenPassed: Boolean = false,
    val imagePassed: Boolean = false,
    val scenePassed: Boolean = false,
    val completed: Boolean = false,
)

class LessonFlowViewModel(
    private val lessonId: String,
    private val repository: LearningRepository,
    private val audioManager: AudioManager,
    private val recordingManager: RecordingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonFlowUiState())
    val uiState: StateFlow<LessonFlowUiState> = _uiState.asStateFlow()

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    private var lessonCompletedHandled = false

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            combine(
                repository.observeLesson(lessonId),
                repository.observeWordsByLesson(lessonId),
                repository.observeExercisesByLesson(lessonId),
            ) { lesson, words, exercises ->
                val current = _uiState.value
                current.copy(
                    lessonTitle = lesson?.title ?: "课程学习",
                    words = words,
                    exercises = exercises,
                )
            }.collect {
                _uiState.value = it
            }
        }

        viewModelScope.launch {
            repository.markLessonInProgress(lessonId)
        }
    }

    fun previousWord() {
        val state = _uiState.value
        if (state.words.isEmpty()) return

        _uiState.value = state.copy(
            currentWordIndex = (state.currentWordIndex - 1).coerceAtLeast(0),
            feedback = null,
        )
    }

    fun nextWord() {
        val state = _uiState.value
        if (state.words.isEmpty()) return

        _uiState.value = state.copy(
            currentWordIndex = (state.currentWordIndex + 1).coerceAtMost(state.words.lastIndex),
            feedback = null,
        )
    }

    fun playCurrentWordPronunciation() {
        val word = currentWord() ?: return
        viewModelScope.launch {
            _messageFlow.emit(audioManager.playWord(word.text, word.audioResName))
        }
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true)
        viewModelScope.launch {
            _messageFlow.emit(recordingManager.startRecording())
        }
    }

    fun stopRecording() {
        val clip = recordingManager.stopRecording()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            hasRecording = clip != null,
        )
        viewModelScope.launch {
            _messageFlow.emit(if (clip == null) "请先开始录音" else "录音完成")
        }
    }

    fun replayRecording() {
        viewModelScope.launch {
            _messageFlow.emit(recordingManager.playRecording())
        }
    }

    fun reRecord() {
        _uiState.value = _uiState.value.copy(
            hasRecording = false,
            isRecording = false,
        )
        viewModelScope.launch {
            _messageFlow.emit(recordingManager.resetRecording())
        }
    }

    fun chooseOption(type: ExerciseType, option: String) {
        val exercise = exerciseByType(type)
        if (exercise == null) return

        val isCorrect = option == exercise.answer
        val feedback = if (isCorrect) "对了，真棒" else "没关系，再试一次"
        val wordId = _uiState.value.words.firstOrNull { it.text == exercise.answer }?.id

        viewModelScope.launch {
            repository.recordOptionAnswer(wordId = wordId, isCorrect = isCorrect)
        }

        val state = _uiState.value
        _uiState.value = state.copy(
            selectedOption = option,
            feedback = feedback,
            feedbackPositive = isCorrect,
            listenPassed = if (type == ExerciseType.LISTEN_CHOOSE) isCorrect else state.listenPassed,
            imagePassed = if (type == ExerciseType.IMAGE_CHOOSE) isCorrect else state.imagePassed,
            scenePassed = if (type == ExerciseType.SCENE_JUDGE) isCorrect else state.scenePassed,
        )
    }

    fun markWriteCompleted() {
        val wordId = currentWord()?.id
        viewModelScope.launch {
            repository.markWriteCompleted(wordId)
            _messageFlow.emit("写得真好，继续下一步")
        }
    }

    fun nextStep() {
        val state = _uiState.value
        when (state.currentStep) {
            LearningStep.LOOK_WORD -> _uiState.value = state.copy(currentStep = LearningStep.FOLLOW_READ)
            LearningStep.FOLLOW_READ -> _uiState.value = state.copy(currentStep = LearningStep.LISTEN_CHOOSE)
            LearningStep.LISTEN_CHOOSE -> {
                if (!state.listenPassed) {
                    notifyShort("先完成这题，再继续")
                    return
                }
                _uiState.value = state.copy(
                    currentStep = LearningStep.IMAGE_CHOOSE,
                    selectedOption = null,
                    feedback = null,
                )
            }

            LearningStep.IMAGE_CHOOSE -> {
                if (!state.imagePassed) {
                    notifyShort("先完成这题，再继续")
                    return
                }
                _uiState.value = state.copy(
                    currentStep = LearningStep.WRITE_TRACE,
                    selectedOption = null,
                    feedback = null,
                )
            }

            LearningStep.WRITE_TRACE -> _uiState.value = state.copy(currentStep = LearningStep.SCENE_JUDGE)
            LearningStep.SCENE_JUDGE -> {
                if (!state.scenePassed) {
                    notifyShort("先完成这题，再继续")
                    return
                }
                _uiState.value = state.copy(currentStep = LearningStep.COMPLETE, completed = true)
                completeLessonIfNeeded()
            }

            LearningStep.COMPLETE -> Unit
        }
    }

    fun previousStep() {
        val step = _uiState.value.currentStep
        val previous = when (step) {
            LearningStep.LOOK_WORD -> LearningStep.LOOK_WORD
            LearningStep.FOLLOW_READ -> LearningStep.LOOK_WORD
            LearningStep.LISTEN_CHOOSE -> LearningStep.FOLLOW_READ
            LearningStep.IMAGE_CHOOSE -> LearningStep.LISTEN_CHOOSE
            LearningStep.WRITE_TRACE -> LearningStep.IMAGE_CHOOSE
            LearningStep.SCENE_JUDGE -> LearningStep.WRITE_TRACE
            LearningStep.COMPLETE -> LearningStep.SCENE_JUDGE
        }
        _uiState.value = _uiState.value.copy(currentStep = previous)
    }

    fun optionsFor(type: ExerciseType): List<String> {
        val exercise = exerciseByType(type) ?: return emptyList()
        return runCatching { Json.decodeFromString<List<String>>(exercise.options) }
            .getOrElse { emptyList() }
            .ifEmpty { _uiState.value.words.take(3).map { it.text } }
    }

    fun promptFor(type: ExerciseType): String {
        return exerciseByType(type)?.prompt ?: "请选择正确答案"
    }

    fun answerFor(type: ExerciseType): String? {
        return exerciseByType(type)?.answer
    }

    private fun currentWord(): WordItemEntity? {
        val state = _uiState.value
        if (state.words.isEmpty()) return null
        return state.words.getOrNull(state.currentWordIndex)
    }

    private fun exerciseByType(type: ExerciseType): ExerciseEntity? {
        return _uiState.value.exercises.firstOrNull { it.type == type.name }
    }

    private fun completeLessonIfNeeded() {
        if (lessonCompletedHandled) return
        lessonCompletedHandled = true
        viewModelScope.launch {
            repository.completeLesson(lessonId)
            _messageFlow.emit("本课完成，继续保持")
        }
    }

    private fun notifyShort(message: String) {
        viewModelScope.launch { _messageFlow.emit(message) }
    }

    companion object {
        fun factory(
            lessonId: String,
            repository: LearningRepository,
            audioManager: AudioManager,
            recordingManager: RecordingManager,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LessonFlowViewModel(
                    lessonId = lessonId,
                    repository = repository,
                    audioManager = audioManager,
                    recordingManager = recordingManager,
                )
            }
        }
    }
}
