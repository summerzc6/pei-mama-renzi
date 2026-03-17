package com.peimama.renzi.audio

interface AudioManager {
    fun playWord(text: String, audioResName: String? = null): String
    fun stop()
}
