package com.peimama.renzi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.peimama.renzi.data.local.entity.WordItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordItemDao {
    @Query("SELECT * FROM word_items WHERE lessonId = :lessonId ORDER BY id")
    fun observeByLesson(lessonId: String): Flow<List<WordItemEntity>>

    @Query("SELECT * FROM word_items WHERE lessonId = :lessonId ORDER BY id")
    suspend fun getByLessonNow(lessonId: String): List<WordItemEntity>

    @Query("SELECT * FROM word_items")
    fun observeAll(): Flow<List<WordItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WordItemEntity>)
}
