package com.peimama.renzi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.peimama.renzi.data.local.entity.LearningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records")
    fun observeAll(): Flow<List<LearningRecordEntity>>

    @Query("SELECT * FROM learning_records WHERE wordId = :wordId LIMIT 1")
    suspend fun getByWordId(wordId: String): LearningRecordEntity?

    @Query("SELECT COUNT(*) FROM learning_records WHERE learned = 1")
    fun observeLearnedCount(): Flow<Int>

    @Query("SELECT * FROM learning_records WHERE lastLearnTime IS NOT NULL ORDER BY lastLearnTime DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<LearningRecordEntity>>

    @Upsert
    suspend fun upsert(record: LearningRecordEntity)
}
