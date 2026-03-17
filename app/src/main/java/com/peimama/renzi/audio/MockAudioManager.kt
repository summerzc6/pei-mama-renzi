package com.peimama.renzi.audio

import android.content.Context

class MockAudioManager(
    private val context: Context,
) : AudioManager {

    override fun playWord(text: String, audioResName: String?): String {
        return if (audioResName.isNullOrBlank()) {
            "演示模式：播放发音“$text”"
        } else {
            "演示模式：播放占位音频 $audioResName"
        }
    }

    override fun stop() {
        // 当前版本使用 mock 播放，不需要额外释放。
    }
}
