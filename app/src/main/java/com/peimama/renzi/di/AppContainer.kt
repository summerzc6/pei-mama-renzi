package com.peimama.renzi.di

import android.content.Context
import com.peimama.renzi.audio.AudioManager
import com.peimama.renzi.audio.MockAudioManager
import com.peimama.renzi.audio.MockRecordingManager
import com.peimama.renzi.audio.RecordingManager
import com.peimama.renzi.data.repository.FakeLearningRepository
import com.peimama.renzi.data.repository.LearningRepository

class AppContainer(context: Context) {
    val audioManager: AudioManager = MockAudioManager(context)
    val recordingManager: RecordingManager = MockRecordingManager()

    // 首版使用 FakeRepository，确保无后端也可完整演示。
    val repository: LearningRepository = FakeLearningRepository(context)
}
