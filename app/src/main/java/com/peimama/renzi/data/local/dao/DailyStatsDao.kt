package com.peimama.renzi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.peimama.renzi.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): DailyStatsEntity?

    @Upsert
    suspend fun upsert(stats: DailyStatsEntity)
}
