package com.peimama.renzi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.peimama.renzi.data.local.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {
    @Query(
        """
        SELECT lp.*
        FROM lesson_progress lp
        INNER JOIN lessons l ON l.id = lp.lessonId
        WHERE l.sceneId = :sceneId
        ORDER BY l.sortOrder
        """,
    )
    fun observeByScene(sceneId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress")
    fun observeAll(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getByLessonId(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LessonProgressEntity>)

    @Upsert
    suspend fun upsert(progress: LessonProgressEntity)
}
