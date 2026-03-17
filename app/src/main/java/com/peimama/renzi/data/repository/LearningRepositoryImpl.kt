package com.peimama.renzi.data.repository

import android.content.Context
import com.peimama.renzi.data.local.AppDatabase
import com.peimama.renzi.data.local.entity.DailyStatsEntity
import com.peimama.renzi.data.local.entity.ExerciseEntity
import com.peimama.renzi.data.local.entity.LearningRecordEntity
import com.peimama.renzi.data.local.entity.LessonEntity
import com.peimama.renzi.data.local.entity.LessonProgressEntity
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.data.model.ExerciseType
import com.peimama.renzi.data.model.FamilyDashboard
import com.peimama.renzi.data.model.HomeDashboard
import com.peimama.renzi.data.model.HomeSummary
import com.peimama.renzi.data.model.LessonHighlight
import com.peimama.renzi.data.model.LessonStatus
import com.peimama.renzi.data.model.LessonWithStatus
import com.peimama.renzi.data.model.NotebookBuckets
import com.peimama.renzi.data.model.RecentLearningItem
import com.peimama.renzi.data.model.ReviewBuckets
import com.peimama.renzi.data.model.WordProgress
import com.peimama.renzi.data.seed.SeedDataLoader
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LearningRepositoryImpl(
    context: Context,
    private val database: AppDatabase,
) : LearningRepository {

    private val sceneDao = database.sceneDao()
    private val lessonDao = database.lessonDao()
    private val wordItemDao = database.wordItemDao()
    private val exerciseDao = database.exerciseDao()
    private val learningRecordDao = database.learningRecordDao()
    private val dailyStatsDao = database.dailyStatsDao()
    private val lessonProgressDao = database.lessonProgressDao()
    private val seedLoader = SeedDataLoader(context)

    override suspend fun ensureSeedData() {
        if (sceneDao.count() > 0) {
            ensureTodayStatsRow()
            return
        }

        val seed = seedLoader.load()
        val scenes = seed.scenes.map {
            SceneEntity(
                id = it.id,
                name = it.name,
                description = it.description,
                sortOrder = it.sortOrder,
            )
        }

        val lessons = mutableListOf<LessonEntity>()
        val words = mutableListOf<WordItemEntity>()
        val exercises = mutableListOf<ExerciseEntity>()
        val progressList = mutableListOf<LessonProgressEntity>()
        val now = System.currentTimeMillis()

        seed.scenes.forEach { scene ->
            scene.lessons.forEach { lesson ->
                lessons += LessonEntity(
                    id = lesson.id,
                    sceneId = scene.id,
                    title = lesson.title,
                    description = lesson.description,
                    sortOrder = lesson.sortOrder,
                )

                val lessonWords = lesson.words.map { word ->
                    WordItemEntity(
                        id = word.id,
                        lessonId = lesson.id,
                        text = word.text,
                        pinyin = word.pinyin,
                        meaning = word.meaning,
                        exampleSentence = word.exampleSentence,
                        imageResName = "img_${word.id}",
                        audioResName = "audio_${word.id}",
                        difficulty = word.difficulty,
                    )
                }

                words += lessonWords
                exercises += buildExercises(lesson.id, lessonWords)
                progressList += LessonProgressEntity(
                    lessonId = lesson.id,
                    status = LessonStatus.NOT_STARTED.name,
                    updatedAt = now,
                )
            }
        }

        sceneDao.insertAll(scenes)
        lessonDao.insertAll(lessons)
        wordItemDao.insertAll(words)
        exerciseDao.insertAll(exercises)
        lessonProgressDao.insertAll(progressList)

        ensureTodayStatsRow()
    }

    override fun observeScenes(): Flow<List<SceneEntity>> = sceneDao.observeAll()

    override fun observeScene(sceneId: String): Flow<SceneEntity?> = sceneDao.observeById(sceneId)

    override fun observeHomeSummary(): Flow<HomeSummary> {
        val today = todayKey()
        return combine(
            dailyStatsDao.observeByDate(today),
            learningRecordDao.observeLearnedCount(),
        ) { stats, learnedCount ->
            val safeStats = stats ?: DailyStatsEntity(date = today, streakDays = 1)
            HomeSummary(todayStats = safeStats, learnedWords = learnedCount)
        }
    }

    override fun observeHomeDashboard(): Flow<HomeDashboard> {
        data class HomeBase(
            val summary: HomeSummary,
            val scenes: List<SceneEntity>,
            val lessons: List<LessonEntity>,
            val words: List<WordItemEntity>,
            val progressMap: Map<String, LessonProgressEntity>,
        )

        return combine(
            observeHomeSummary(),
            sceneDao.observeAll(),
            lessonDao.observeAll(),
            wordItemDao.observeAll(),
            lessonProgressDao.observeAll(),
        ) { summary, scenes, lessons, words, progressList ->
            HomeBase(
                summary = summary,
                scenes = scenes,
                lessons = lessons,
                words = words,
                progressMap = progressList.associateBy { it.lessonId },
            )
        }.combine(learningRecordDao.observeAll()) { base, records ->
            val sceneMap = base.scenes.associateBy { it.id }

            val lessonHighlights = base.lessons.map { lesson ->
                val lessonWords = base.words.filter { it.lessonId == lesson.id }
                LessonHighlight(
                    lessonId = lesson.id,
                    sceneId = lesson.sceneId,
                    sceneName = sceneMap[lesson.sceneId]?.name ?: "未分类",
                    title = lesson.title,
                    status = base.progressMap[lesson.id]?.status.toLessonStatus(),
                    wordCount = lessonWords.size,
                )
            }

            val continueLesson = lessonHighlights.firstOrNull { it.status == LessonStatus.IN_PROGRESS }
                ?: lessonHighlights.firstOrNull { it.status == LessonStatus.REVIEW_PENDING }

            val recommended = lessonHighlights
                .filter { it.status != LessonStatus.COMPLETED }
                .take(3)

            val easyForgotWords = records
                .filter { it.wrongCount >= 2 }
                .mapNotNull { record -> base.words.firstOrNull { it.id == record.wordId }?.text }
                .distinct()
                .take(6)

            HomeDashboard(
                summary = base.summary,
                warmMessage = warmMessageByMinutes(base.summary.todayStats.learnMinutes),
                continueLesson = continueLesson,
                recommendedLessons = recommended,
                quickScenes = base.scenes.take(5),
                easyForgotWords = easyForgotWords,
            )
        }
    }
    override fun observeLessonsWithStatus(sceneId: String): Flow<List<LessonWithStatus>> {
        return combine(
            lessonDao.observeByScene(sceneId),
            lessonProgressDao.observeByScene(sceneId),
            wordItemDao.observeAll(),
            learningRecordDao.observeAll(),
        ) { lessons, progress, words, records ->
            val statusMap = progress.associateBy { it.lessonId }
            val recordMap = records.associateBy { it.wordId }

            lessons.map { lesson ->
                val lessonWords = words.filter { it.lessonId == lesson.id }
                val masteredCount = lessonWords.count { recordMap[it.id]?.mastered == true }

                LessonWithStatus(
                    lesson = lesson,
                    status = statusMap[lesson.id]?.status.toLessonStatus(),
                    wordCount = lessonWords.size,
                    masteredCount = masteredCount,
                    focusWords = lessonWords.take(3).map { it.text },
                    estimatedMinutes = (lessonWords.size * 2 + 6).coerceAtLeast(8),
                )
            }
        }
    }

    override fun observeLesson(lessonId: String): Flow<LessonEntity?> = lessonDao.observeById(lessonId)

    override fun observeWordsByLesson(lessonId: String): Flow<List<WordItemEntity>> =
        wordItemDao.observeByLesson(lessonId)

    override fun observeExercisesByLesson(lessonId: String): Flow<List<ExerciseEntity>> =
        exerciseDao.observeByLesson(lessonId)

    override suspend fun markLessonInProgress(lessonId: String) {
        val now = System.currentTimeMillis()
        val current = lessonProgressDao.getByLessonId(lessonId)
        if (current?.status == LessonStatus.COMPLETED.name) return

        lessonProgressDao.upsert(
            LessonProgressEntity(
                lessonId = lessonId,
                status = LessonStatus.IN_PROGRESS.name,
                updatedAt = now,
            ),
        )
    }

    override suspend fun completeLesson(lessonId: String) {
        val words = wordItemDao.getByLessonNow(lessonId)
        val now = System.currentTimeMillis()

        words.forEach { word ->
            val current = learningRecordDao.getByWordId(word.id)
            val next = if (current == null) {
                LearningRecordEntity(
                    wordId = word.id,
                    learned = true,
                    mastered = true,
                    readCompleted = true,
                    writeCompleted = true,
                    lastLearnTime = now,
                    reviewLevel = 2,
                )
            } else {
                current.copy(
                    learned = true,
                    mastered = current.wrongCount < 2,
                    readCompleted = true,
                    writeCompleted = true,
                    lastLearnTime = now,
                    reviewLevel = (current.reviewLevel + 1).coerceAtMost(5),
                )
            }
            learningRecordDao.upsert(next)
        }

        lessonProgressDao.upsert(
            LessonProgressEntity(
                lessonId = lessonId,
                status = LessonStatus.COMPLETED.name,
                updatedAt = now,
            ),
        )

        updateTodayStats(newWordsDelta = words.size, minutesDelta = 15)
    }

    override suspend fun recordOptionAnswer(wordId: String?, isCorrect: Boolean) {
        if (wordId == null) return

        val now = System.currentTimeMillis()
        val current = learningRecordDao.getByWordId(wordId)

        val next = if (current == null) {
            if (isCorrect) {
                LearningRecordEntity(
                    wordId = wordId,
                    learned = true,
                    mastered = false,
                    readCompleted = true,
                    lastLearnTime = now,
                    reviewLevel = 1,
                )
            } else {
                LearningRecordEntity(
                    wordId = wordId,
                    wrongCount = 1,
                    lastLearnTime = now,
                )
            }
        } else if (isCorrect) {
            current.copy(
                learned = true,
                mastered = current.wrongCount <= 1,
                readCompleted = true,
                lastLearnTime = now,
                reviewLevel = (current.reviewLevel + 1).coerceAtMost(5),
            )
        } else {
            current.copy(
                learned = current.learned,
                mastered = false,
                wrongCount = current.wrongCount + 1,
                lastLearnTime = now,
            )
        }

        learningRecordDao.upsert(next)
        updateTodayStats(reviewWordsDelta = 1)
    }

    override suspend fun markWriteCompleted(wordId: String?) {
        if (wordId == null) return

        val now = System.currentTimeMillis()
        val current = learningRecordDao.getByWordId(wordId)
        val next = if (current == null) {
            LearningRecordEntity(
                wordId = wordId,
                learned = true,
                writeCompleted = true,
                lastLearnTime = now,
                reviewLevel = 1,
            )
        } else {
            current.copy(
                learned = true,
                writeCompleted = true,
                lastLearnTime = now,
                reviewLevel = (current.reviewLevel + 1).coerceAtMost(5),
            )
        }
        learningRecordDao.upsert(next)
    }

    override suspend fun toggleFavorite(wordId: String, favorite: Boolean) {
        val current = learningRecordDao.getByWordId(wordId)
        val next = if (current == null) {
            LearningRecordEntity(wordId = wordId, favorite = favorite)
        } else {
            current.copy(favorite = favorite)
        }
        learningRecordDao.upsert(next)
    }

    override fun observeReviewBuckets(): Flow<ReviewBuckets> {
        return combine(wordItemDao.observeAll(), learningRecordDao.observeAll()) { words, records ->
            val items = buildWordProgress(words, records)
            ReviewBuckets(
                todayNew = items.filter { item ->
                    val time = item.record?.lastLearnTime ?: return@filter false
                    time >= todayStartMillis()
                },
                easyForgot = items.filter { item ->
                    val record = item.record ?: return@filter false
                    record.wrongCount >= 2 || (record.learned && record.reviewLevel <= 1)
                },
                pendingReview = items.filter { item ->
                    val record = item.record ?: return@filter false
                    record.learned && !record.mastered
                },
            )
        }
    }

    override fun observeNotebookBuckets(): Flow<NotebookBuckets> {
        return combine(wordItemDao.observeAll(), learningRecordDao.observeAll()) { words, records ->
            val items = buildWordProgress(words, records)
            NotebookBuckets(
                mastered = items.filter { it.record?.mastered == true },
                easyForgot = items.filter {
                    val r = it.record ?: return@filter false
                    r.wrongCount >= 2 || (r.learned && r.reviewLevel <= 1)
                },
                cantWrite = items.filter {
                    val r = it.record ?: return@filter false
                    r.learned && !r.writeCompleted
                },
                favorites = items.filter { it.record?.favorite == true },
            )
        }
    }

    override fun observeFamilyDashboard(): Flow<FamilyDashboard> {
        val today = todayKey()
        return combine(
            dailyStatsDao.observeByDate(today),
            wordItemDao.observeAll(),
            learningRecordDao.observeAll(),
            lessonDao.observeAll(),
            sceneDao.observeAll(),
        ) { stats, words, records, lessons, scenes ->
            val wordMap = words.associateBy { it.id }
            val lessonMap = lessons.associateBy { it.id }
            val sceneMap = scenes.associateBy { it.id }
            val easyForgot = buildWordProgress(words, records).filter {
                val r = it.record ?: return@filter false
                r.wrongCount >= 2 || (r.learned && r.reviewLevel <= 1)
            }

            val recent = records
                .filter { it.lastLearnTime != null }
                .sortedByDescending { it.lastLearnTime }
                .take(10)
                .mapNotNull { record ->
                    val word = wordMap[record.wordId] ?: return@mapNotNull null
                    val lesson = lessonMap[word.lessonId]
                    val sceneName = sceneMap[lesson?.sceneId]?.name ?: "未分类"
                    RecentLearningItem(
                        wordText = word.text,
                        sceneName = sceneName,
                        time = record.lastLearnTime ?: 0,
                    )
                }

            FamilyDashboard(
                todayMinutes = stats?.learnMinutes ?: 0,
                todayNewWords = stats?.newWordsCount ?: 0,
                easyForgotWords = easyForgot,
                recentLearning = recent,
            )
        }
    }

    private fun buildExercises(
        lessonId: String,
        words: List<WordItemEntity>,
    ): List<ExerciseEntity> {
        if (words.isEmpty()) return emptyList()

        val options = words.take(3).map { it.text }
        val answerA = words.first().text
        val answerB = words.getOrElse(1) { words.first() }.text
        val answerC = words.last().text

        return listOf(
            ExerciseEntity(
                id = "ex_${lessonId}_listen",
                lessonId = lessonId,
                type = ExerciseType.LISTEN_CHOOSE.name,
                prompt = "请点出“$answerA”",
                answer = answerA,
                options = Json.encodeToString(options.shuffled()),
                audioResName = words.first().audioResName,
                sortOrder = 1,
            ),
            ExerciseEntity(
                id = "ex_${lessonId}_image",
                lessonId = lessonId,
                type = ExerciseType.IMAGE_CHOOSE.name,
                prompt = "看图，选出“$answerB”",
                answer = answerB,
                options = Json.encodeToString(options.shuffled()),
                imageResName = words.getOrElse(1) { words.first() }.imageResName,
                sortOrder = 2,
            ),
            ExerciseEntity(
                id = "ex_${lessonId}_scene",
                lessonId = lessonId,
                type = ExerciseType.SCENE_JUDGE.name,
                prompt = "这个地方应该找哪个字？",
                answer = answerC,
                options = Json.encodeToString(options.shuffled()),
                imageResName = words.last().imageResName,
                sortOrder = 3,
            ),
        )
    }

    private fun buildWordProgress(
        words: List<WordItemEntity>,
        records: List<LearningRecordEntity>,
    ): List<WordProgress> {
        val recordMap = records.associateBy { it.wordId }
        return words.map { word -> WordProgress(word = word, record = recordMap[word.id]) }
    }

    private suspend fun ensureTodayStatsRow() {
        val today = todayKey()
        if (dailyStatsDao.getByDate(today) != null) return

        val streak = calculateStreakForToday(today)
        dailyStatsDao.upsert(DailyStatsEntity(date = today, streakDays = streak))
    }

    private suspend fun updateTodayStats(
        newWordsDelta: Int = 0,
        reviewWordsDelta: Int = 0,
        minutesDelta: Int = 0,
    ) {
        val today = todayKey()
        val current = dailyStatsDao.getByDate(today)
            ?: DailyStatsEntity(date = today, streakDays = calculateStreakForToday(today))

        val next = current.copy(
            learnMinutes = (current.learnMinutes + minutesDelta).coerceAtLeast(0),
            newWordsCount = (current.newWordsCount + newWordsDelta).coerceAtLeast(0),
            reviewWordsCount = (current.reviewWordsCount + reviewWordsDelta).coerceAtLeast(0),
            streakDays = current.streakDays.coerceAtLeast(1),
        )
        dailyStatsDao.upsert(next)
    }

    private suspend fun calculateStreakForToday(today: String): Int {
        val latest = dailyStatsDao.getLatest() ?: return 1
        if (latest.date == today) return latest.streakDays.coerceAtLeast(1)

        return try {
            val latestDate = LocalDate.parse(latest.date)
            val todayDate = LocalDate.parse(today)
            if (latestDate.plusDays(1) == todayDate && latest.learnMinutes > 0) {
                (latest.streakDays + 1).coerceAtLeast(1)
            } else {
                1
            }
        } catch (_: Exception) {
            1
        }
    }

    private fun warmMessageByMinutes(minutes: Int): String {
        return when {
            minutes >= 20 -> "今天状态很棒，继续保持。"
            minutes >= 10 -> "今天已经学了不少，慢慢来就很好。"
            else -> "现在开始也不晚，我们一起练两分钟。"
        }
    }

    private fun String?.toLessonStatus(): LessonStatus {
        return runCatching { LessonStatus.valueOf(this ?: LessonStatus.NOT_STARTED.name) }
            .getOrDefault(LessonStatus.NOT_STARTED)
    }

    private fun todayStartMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun todayKey(): String = LocalDate.now().toString()
}



