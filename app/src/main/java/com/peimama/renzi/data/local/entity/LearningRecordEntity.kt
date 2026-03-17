package com.peimama.renzi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_records",
    indices = [Index(value = ["wordId"], unique = true)],
)
data class LearningRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: String,
    val learned: Boolean = false,
    val mastered: Boolean = false,
    val favorite: Boolean = false,
    val wrongCount: Int = 0,
    val writeCompleted: Boolean = false,
    val readCompleted: Boolean = false,
    val lastLearnTime: Long? = null,
    val lastReviewTime: Long? = null,
    val reviewLevel: Int = 0,
)
