package com.peimama.renzi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
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
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val type: String,
    val prompt: String,
    val answer: String,
    val options: String,
    val imageResName: String? = null,
    val audioResName: String? = null,
    val sortOrder: Int,
)
