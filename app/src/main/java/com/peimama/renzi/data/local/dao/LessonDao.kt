package com.peimama.renzi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.peimama.renzi.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE sceneId = :sceneId ORDER BY sortOrder")
    fun observeByScene(sceneId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    fun observeById(lessonId: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons")
    fun observeAll(): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LessonEntity>)
}
