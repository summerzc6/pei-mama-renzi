package com.peimama.renzi.data.model

import com.peimama.renzi.data.local.entity.DailyStatsEntity
import com.peimama.renzi.data.local.entity.LearningRecordEntity
import com.peimama.renzi.data.local.entity.LessonEntity
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.local.entity.WordItemEntity

enum class LessonStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    REVIEW_PENDING,
}

enum class ExerciseType {
    LISTEN_CHOOSE,
    IMAGE_CHOOSE,
    SCENE_JUDGE,
}

data class HomeSummary(
    val todayStats: DailyStatsEntity,
    val learnedWords: Int,
)

data class LessonHighlight(
    val lessonId: String,
    val sceneId: String,
    val sceneName: String,
    val title: String,
    val status: LessonStatus,
    val wordCount: Int,
)

data class HomeDashboard(
    val summary: HomeSummary,
    val warmMessage: String,
    val continueLesson: LessonHighlight?,
    val recommendedLessons: List<LessonHighlight>,
    val quickScenes: List<SceneEntity>,
    val easyForgotWords: List<String>,
)

data class LessonWithStatus(
    val lesson: LessonEntity,
    val status: LessonStatus,
    val wordCount: Int = 0,
    val masteredCount: Int = 0,
    val focusWords: List<String> = emptyList(),
    val estimatedMinutes: Int = 10,
)

data class WordProgress(
    val word: WordItemEntity,
    val record: LearningRecordEntity?,
)

data class ReviewBuckets(
    val todayNew: List<WordProgress>,
    val easyForgot: List<WordProgress>,
    val pendingReview: List<WordProgress>,
)

data class NotebookBuckets(
    val mastered: List<WordProgress>,
    val easyForgot: List<WordProgress>,
    val cantWrite: List<WordProgress>,
    val favorites: List<WordProgress>,
)

data class RecentLearningItem(
    val wordText: String,
    val sceneName: String,
    val time: Long,
)

data class FamilyDashboard(
    val todayMinutes: Int,
    val todayNewWords: Int,
    val easyForgotWords: List<WordProgress>,
    val recentLearning: List<RecentLearningItem>,
)

data class SceneEntry(
    val scene: SceneEntity,
)
