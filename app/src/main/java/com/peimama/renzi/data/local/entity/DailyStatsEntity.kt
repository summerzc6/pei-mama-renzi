package com.peimama.renzi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String,
    val learnMinutes: Int = 0,
    val newWordsCount: Int = 0,
    val reviewWordsCount: Int = 0,
    val streakDays: Int = 0,
)
