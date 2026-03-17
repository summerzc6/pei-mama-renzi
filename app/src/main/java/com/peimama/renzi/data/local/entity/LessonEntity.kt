package com.peimama.renzi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = SceneEntity::class,
            parentColumns = ["id"],
            childColumns = ["sceneId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sceneId")],
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val sceneId: String,
    val title: String,
    val description: String,
    val sortOrder: Int,
)
