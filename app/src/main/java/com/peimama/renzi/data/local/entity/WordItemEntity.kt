package com.peimama.renzi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_items",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("lessonId")],
)
data class WordItemEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val text: String,
    val pinyin: String,
    val meaning: String,
    val exampleSentence: String,
    val imageResName: String? = null,
    val audioResName: String? = null,
    val difficulty: Int = 1,
)
