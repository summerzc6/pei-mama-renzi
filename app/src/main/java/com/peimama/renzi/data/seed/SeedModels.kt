package com.peimama.renzi.data.seed

import kotlinx.serialization.Serializable

@Serializable
data class SeedRoot(
    val scenes: List<SeedScene>,
)

@Serializable
data class SeedScene(
    val id: String,
    val name: String,
    val description: String,
    val sortOrder: Int,
    val lessons: List<SeedLesson>,
)

@Serializable
data class SeedLesson(
    val id: String,
    val title: String,
    val description: String,
    val sortOrder: Int,
    val words: List<SeedWord>,
)

@Serializable
data class SeedWord(
    val id: String,
    val text: String,
    val pinyin: String,
    val meaning: String,
    val exampleSentence: String,
    val difficulty: Int = 1,
)
