package com.peimama.renzi.audio

data class RecordingClip(
    val id: String,
    val createdAt: Long,
)

interface RecordingManager {
    fun startRecording(): String
    fun stopRecording(): RecordingClip?
    fun playRecording(): String
    fun resetRecording(): String
}
