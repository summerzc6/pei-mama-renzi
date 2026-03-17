package com.peimama.renzi.audio

class MockRecordingManager : RecordingManager {
    private var currentClip: RecordingClip? = null
    private var recordingStartedAt: Long? = null

    override fun startRecording(): String {
        recordingStartedAt = System.currentTimeMillis()
        return "开始录音（演示）"
    }

    override fun stopRecording(): RecordingClip? {
        val startedAt = recordingStartedAt ?: return currentClip
        val clip = RecordingClip(
            id = "mock_${startedAt}",
            createdAt = startedAt,
        )
        currentClip = clip
        recordingStartedAt = null
        return clip
    }

    override fun playRecording(): String {
        return if (currentClip == null) {
            "还没有录音，先点“开始录音”"
        } else {
            "回放录音（演示）"
        }
    }

    override fun resetRecording(): String {
        currentClip = null
        recordingStartedAt = null
        return "已重录"
    }
}
