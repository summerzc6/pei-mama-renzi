package com.peimama.renzi.data.repository

import com.peimama.renzi.data.local.entity.ExerciseEntity
import com.peimama.renzi.data.local.entity.LessonEntity
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.data.model.FamilyDashboard
import com.peimama.renzi.data.model.HomeDashboard
import com.peimama.renzi.data.model.HomeSummary
import com.peimama.renzi.data.model.LessonWithStatus
import com.peimama.renzi.data.model.NotebookBuckets
import com.peimama.renzi.data.model.ReviewBuckets
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    suspend fun ensureSeedData()

    fun observeScenes(): Flow<List<SceneEntity>>
    fun observeScene(sceneId: String): Flow<SceneEntity?>
    fun observeHomeSummary(): Flow<HomeSummary>
    fun observeHomeDashboard(): Flow<HomeDashboard>
    fun observeLessonsWithStatus(sceneId: String): Flow<List<LessonWithStatus>>

    fun observeLesson(lessonId: String): Flow<LessonEntity?>
    fun observeWordsByLesson(lessonId: String): Flow<List<WordItemEntity>>
    fun observeExercisesByLesson(lessonId: String): Flow<List<ExerciseEntity>>

    suspend fun markLessonInProgress(lessonId: String)
    suspend fun completeLesson(lessonId: String)
    suspend fun recordOptionAnswer(wordId: String?, isCorrect: Boolean)
    suspend fun markWriteCompleted(wordId: String?)
    suspend fun toggleFavorite(wordId: String, favorite: Boolean)

    fun observeReviewBuckets(): Flow<ReviewBuckets>
    fun observeNotebookBuckets(): Flow<NotebookBuckets>
    fun observeFamilyDashboard(): Flow<FamilyDashboard>
}
