package com.peimama.renzi.audio

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

class MockAudioManager(
    context: Context,
) : AudioManager, TextToSpeech.OnInitListener {

    private enum class EngineState {
        IDLE,
        INITIALIZING,
        READY,
        FAILED,
    }

    private val appContext = context.applicationContext
    private var state = EngineState.IDLE
    private var textToSpeech: TextToSpeech? = null
    private var pendingText: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun playWord(text: String, audioResName: String?): String {
        val spokenText = text.trim()
        if (spokenText.isBlank()) {
            return "当前词条暂无可播报内容"
        }

        val rawResId = resolveRawResource(audioResName)
        if (rawResId != null && playRaw(rawResId)) {
            return "正在播放发音"
        }

        if (state == EngineState.IDLE) {
            state = EngineState.INITIALIZING
            textToSpeech = TextToSpeech(appContext, this)
        }

        return when (state) {
            EngineState.READY -> speak(spokenText)
            EngineState.INITIALIZING -> {
                pendingText = spokenText
                "正在准备语音，马上播放"
            }

            EngineState.FAILED -> "手机语音引擎暂不可用，请检查系统语音设置"
            EngineState.IDLE -> "正在准备语音，请稍后再试"
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            state = EngineState.FAILED
            return
        }

        val tts = textToSpeech ?: run {
            state = EngineState.FAILED
            return
        }

        val localeCandidates = listOf(
            Locale.SIMPLIFIED_CHINESE,
            Locale.CHINESE,
            Locale.getDefault(),
            Locale.US,
        )

        var languageConfigured = false
        for (locale in localeCandidates) {
            val result = tts.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                languageConfigured = true
                break
            }
        }


        if (!languageConfigured) {
            // 某些机型缺少中文语音包，继续使用系统默认语言兜底。
        }

        tts.setPitch(1.0f)
        tts.setSpeechRate(0.85f)
        state = EngineState.READY

        pendingText?.let {
            pendingText = null
            speak(it)
        }
    }

    override fun stop() {
        releasePlayer()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        pendingText = null
        state = EngineState.IDLE
    }

    private fun speak(text: String): String {
        val tts = textToSpeech ?: return "语音引擎还未准备好"
        return if (
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "word_${System.currentTimeMillis()}",
            ) == TextToSpeech.SUCCESS
        ) {
            "正在播放：$text"
        } else {
            "语音播放失败，请稍后再试"
        }
    }

    private fun resolveRawResource(audioResName: String?): Int? {
        if (audioResName.isNullOrBlank()) return null
        val resourceId = appContext.resources.getIdentifier(audioResName, "raw", appContext.packageName)
        return resourceId.takeIf { it != 0 }
    }

    private fun playRaw(resourceId: Int): Boolean {
        releasePlayer()
        val player = MediaPlayer.create(appContext, resourceId) ?: return false
        mediaPlayer = player
        player.setOnCompletionListener {
            releasePlayer()
        }
        return runCatching {
            player.start()
            true
        }.getOrDefault(false)
    }

    private fun releasePlayer() {
        mediaPlayer?.runCatching {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }
}



