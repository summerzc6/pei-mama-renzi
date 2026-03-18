package com.peimama.renzi.data.repository

import android.content.Context
import android.util.Log
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
import com.peimama.renzi.data.seed.SeedLesson
import com.peimama.renzi.data.seed.SeedRoot
import com.peimama.renzi.data.seed.SeedScene
import com.peimama.renzi.data.seed.SeedWord
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "FakeLearningRepo"

class FakeLearningRepository(
    context: Context,
) : LearningRepository {

    private data class HomeBase(
        val summary: HomeSummary,
        val scenes: List<SceneEntity>,
        val lessons: List<LessonEntity>,
        val words: List<WordItemEntity>,
        val progressMap: Map<String, LessonProgressEntity>,
    )

    private val seed = runCatching {
        SeedDataLoader(context).load()
    }.getOrElse { error ->
        Log.e(TAG, "Failed to load seed data. Falling back to built-in seed.", error)
        fallbackSeed()
    }

    private val scenes = seed.scenes
        .map {
            SceneEntity(
                id = it.id,
                name = it.name,
                description = it.description,
                sortOrder = it.sortOrder,
            )
        }
        .sortedBy { it.sortOrder }

    private val lessons = seed.scenes
        .flatMap { scene ->
            scene.lessons.map { lesson ->
                LessonEntity(
                    id = lesson.id,
                    sceneId = scene.id,
                    title = lesson.title,
                    description = lesson.description,
                    sortOrder = lesson.sortOrder,
                )
            }
        }
        .sortedWith(compareBy({ it.sceneId }, { it.sortOrder }))

    private val words = seed.scenes
        .flatMap { scene ->
            scene.lessons.flatMap { lesson ->
                lesson.words.map { word ->
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
            }
        }

    private val exercises = seed.scenes
        .flatMap { scene ->
            scene.lessons.flatMap { lesson ->
                val lessonWords = words.filter { it.lessonId == lesson.id }
                buildExercises(lesson.id, lessonWords)
            }
        }

    private val scenesFlow = MutableStateFlow(scenes)
    private val lessonsFlow = MutableStateFlow(lessons)
    private val wordsFlow = MutableStateFlow(words)
    private val exercisesFlow = MutableStateFlow(exercises)
    private val recordsFlow = MutableStateFlow(initialRecords())
    private val progressFlow = MutableStateFlow(initialLessonProgress())
    private val dailyStatsFlow = MutableStateFlow(initialDailyStats())

    override suspend fun ensureSeedData() = Unit

    override fun observeScenes(): Flow<List<SceneEntity>> = scenesFlow

    override fun observeScene(sceneId: String): Flow<SceneEntity?> =
        scenesFlow.map { list -> list.firstOrNull { it.id == sceneId } }

    override fun observeHomeSummary(): Flow<HomeSummary> {
        return combine(
            dailyStatsFlow.map { it[todayKey()] ?: DailyStatsEntity(date = todayKey(), streakDays = 1) },
            recordsFlow.map { items -> items.count { it.learned } },
        ) { stats, learnedCount ->
            HomeSummary(todayStats = stats, learnedWords = learnedCount)
        }
    }

    override fun observeHomeDashboard(): Flow<HomeDashboard> {
        return combine(
            observeHomeSummary(),
            scenesFlow,
            lessonsFlow,
            wordsFlow,
            progressFlow,
        ) { summary, scenesList, lessonsList, wordsList, progressMap ->
            HomeBase(
                summary = summary,
                scenes = scenesList,
                lessons = lessonsList,
                words = wordsList,
                progressMap = progressMap,
            )
        }.combine(recordsFlow) { base, recordsList ->
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

            val continueLesson = lessonHighlights
                .firstOrNull { it.status == LessonStatus.IN_PROGRESS }
                ?: lessonHighlights.firstOrNull { it.status == LessonStatus.REVIEW_PENDING }

            val recommendedLessons = lessonHighlights
                .filter { it.status != LessonStatus.COMPLETED }
                .sortedBy { it.lessonId }
                .take(3)

            val easyForgotWords = recordsList
                .filter { it.wrongCount >= 2 }
                .mapNotNull { record -> base.words.firstOrNull { it.id == record.wordId }?.text }
                .distinct()
                .take(6)

            HomeDashboard(
                summary = base.summary,
                warmMessage = warmMessageByMinutes(base.summary.todayStats.learnMinutes),
                continueLesson = continueLesson,
                recommendedLessons = recommendedLessons,
                quickScenes = base.scenes.take(5),
                easyForgotWords = easyForgotWords,
            )
        }
    }

    override fun observeLessonsWithStatus(sceneId: String): Flow<List<LessonWithStatus>> {
        return combine(lessonsFlow, progressFlow, wordsFlow, recordsFlow) { allLessons, progressMap, wordsList, recordsList ->
            val recordMap = recordsList.associateBy { it.wordId }
            allLessons
                .filter { it.sceneId == sceneId }
                .sortedBy { it.sortOrder }
                .map { lesson ->
                    val lessonWords = wordsList.filter { it.lessonId == lesson.id }
                    val masteredCount = lessonWords.count { word -> recordMap[word.id]?.mastered == true }
                    LessonWithStatus(
                        lesson = lesson,
                        status = progressMap[lesson.id]?.status.toLessonStatus(),
                        wordCount = lessonWords.size,
                        masteredCount = masteredCount,
                        focusWords = lessonWords.take(3).map { it.text },
                        estimatedMinutes = (lessonWords.size * 2 + 6).coerceAtLeast(8),
                    )
                }
        }
    }

    override fun observeLesson(lessonId: String): Flow<LessonEntity?> =
        lessonsFlow.map { items -> items.firstOrNull { it.id == lessonId } }

    override fun observeWordsByLesson(lessonId: String): Flow<List<WordItemEntity>> =
        wordsFlow.map { items -> items.filter { it.lessonId == lessonId } }

    override fun observeExercisesByLesson(lessonId: String): Flow<List<ExerciseEntity>> =
        exercisesFlow.map { items -> items.filter { it.lessonId == lessonId }.sortedBy { it.sortOrder } }

    override suspend fun markLessonInProgress(lessonId: String) {
        progressFlow.update { map ->
            val current = map[lessonId]
            if (current?.status == LessonStatus.COMPLETED.name) map
            else {
                map + (
                    lessonId to LessonProgressEntity(
                        lessonId = lessonId,
                        status = LessonStatus.IN_PROGRESS.name,
                        updatedAt = System.currentTimeMillis(),
                    )
                    )
            }
        }
    }

    override suspend fun completeLesson(lessonId: String) {
        val lessonWords = wordsFlow.value.filter { it.lessonId == lessonId }
        val now = System.currentTimeMillis()

        recordsFlow.update { currentList ->
            val map = currentList.associateBy { it.wordId }.toMutableMap()
            lessonWords.forEach { word ->
                val existing = map[word.id]
                map[word.id] = if (existing == null) {
                    LearningRecordEntity(
                        wordId = word.id,
                        learned = true,
                        mastered = true,
                        writeCompleted = true,
                        readCompleted = true,
                        lastLearnTime = now,
                        lastReviewTime = now,
                        reviewLevel = 2,
                    )
                } else {
                    existing.copy(
                        learned = true,
                        mastered = existing.wrongCount < 2,
                        writeCompleted = true,
                        readCompleted = true,
                        lastLearnTime = now,
                        lastReviewTime = now,
                        reviewLevel = (existing.reviewLevel + 1).coerceAtMost(5),
                    )
                }
            }
            map.values.toList()
        }

        progressFlow.update { map ->
            map + (
                lessonId to LessonProgressEntity(
                    lessonId = lessonId,
                    status = LessonStatus.COMPLETED.name,
                    updatedAt = now,
                )
                )
        }

        updateTodayStats(learnMinutesDelta = 15, newWordsDelta = lessonWords.size)
    }

    override suspend fun recordOptionAnswer(wordId: String?, isCorrect: Boolean) {
        if (wordId == null) return

        val now = System.currentTimeMillis()
        recordsFlow.update { currentList ->
            val map = currentList.associateBy { it.wordId }.toMutableMap()
            val existing = map[wordId]
            val next = if (existing == null) {
                if (isCorrect) {
                    LearningRecordEntity(
                        wordId = wordId,
                        learned = true,
                        mastered = false,
                        readCompleted = true,
                        lastLearnTime = now,
                        lastReviewTime = now,
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
                existing.copy(
                    learned = true,
                    mastered = existing.wrongCount <= 1,
                    readCompleted = true,
                    lastLearnTime = now,
                    lastReviewTime = now,
                    reviewLevel = (existing.reviewLevel + 1).coerceAtMost(5),
                )
            } else {
                existing.copy(
                    mastered = false,
                    wrongCount = existing.wrongCount + 1,
                    lastLearnTime = now,
                )
            }
            map[wordId] = next
            map.values.toList()
        }

        updateTodayStats(reviewWordsDelta = 1)
    }

    override suspend fun markWriteCompleted(wordId: String?) {
        if (wordId == null) return

        val now = System.currentTimeMillis()
        recordsFlow.update { currentList ->
            val map = currentList.associateBy { it.wordId }.toMutableMap()
            val existing = map[wordId]
            map[wordId] = if (existing == null) {
                LearningRecordEntity(
                    wordId = wordId,
                    learned = true,
                    writeCompleted = true,
                    lastLearnTime = now,
                    lastReviewTime = now,
                    reviewLevel = 1,
                )
            } else {
                existing.copy(
                    learned = true,
                    writeCompleted = true,
                    lastLearnTime = now,
                    lastReviewTime = now,
                    reviewLevel = (existing.reviewLevel + 1).coerceAtMost(5),
                )
            }
            map.values.toList()
        }
    }

    override suspend fun toggleFavorite(wordId: String, favorite: Boolean) {
        recordsFlow.update { currentList ->
            val map = currentList.associateBy { it.wordId }.toMutableMap()
            val existing = map[wordId]
            map[wordId] = if (existing == null) {
                LearningRecordEntity(wordId = wordId, favorite = favorite)
            } else {
                existing.copy(favorite = favorite)
            }
            map.values.toList()
        }
    }

    override fun observeReviewBuckets(): Flow<ReviewBuckets> {
        return combine(wordsFlow, recordsFlow) { wordsList, recordsList ->
            val items = buildWordProgress(wordsList, recordsList)
            ReviewBuckets(
                todayNew = items.filter {
                    val time = it.record?.lastLearnTime ?: return@filter false
                    time >= todayStartMillis()
                },
                easyForgot = items.filter {
                    val record = it.record ?: return@filter false
                    record.wrongCount >= 2 || (record.learned && record.reviewLevel <= 1)
                },
                pendingReview = items.filter {
                    val record = it.record ?: return@filter false
                    record.learned && !record.mastered
                },
            )
        }
    }

    override fun observeNotebookBuckets(): Flow<NotebookBuckets> {
        return combine(wordsFlow, recordsFlow) { wordsList, recordsList ->
            val items = buildWordProgress(wordsList, recordsList)
            NotebookBuckets(
                mastered = items.filter { it.record?.mastered == true },
                easyForgot = items.filter {
                    val record = it.record ?: return@filter false
                    record.wrongCount >= 2 || (record.learned && record.reviewLevel <= 1)
                },
                cantWrite = items.filter {
                    val record = it.record ?: return@filter false
                    record.learned && !record.writeCompleted
                },
                favorites = items.filter { it.record?.favorite == true },
            )
        }
    }

    override fun observeFamilyDashboard(): Flow<FamilyDashboard> {
        return combine(
            dailyStatsFlow,
            wordsFlow,
            recordsFlow,
            lessonsFlow,
            scenesFlow,
        ) { statsMap, wordsList, recordsList, lessonsList, scenesList ->
            val todayStats = statsMap[todayKey()] ?: DailyStatsEntity(date = todayKey(), streakDays = 1)
            val wordMap = wordsList.associateBy { it.id }
            val lessonMap = lessonsList.associateBy { it.id }
            val sceneMap = scenesList.associateBy { it.id }

            val easyForgot = buildWordProgress(wordsList, recordsList).filter {
                val record = it.record ?: return@filter false
                record.wrongCount >= 2 || (record.learned && record.reviewLevel <= 1)
            }

            val recent = recordsList
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
                todayMinutes = todayStats.learnMinutes,
                todayNewWords = todayStats.newWordsCount,
                easyForgotWords = easyForgot,
                recentLearning = recent,
            )
        }
    }

    private fun buildWordProgress(
        wordsList: List<WordItemEntity>,
        recordsList: List<LearningRecordEntity>,
    ): List<WordProgress> {
        val recordMap = recordsList.associateBy { it.wordId }
        return wordsList.map { word -> WordProgress(word = word, record = recordMap[word.id]) }
    }

    private suspend fun updateTodayStats(
        learnMinutesDelta: Int = 0,
        newWordsDelta: Int = 0,
        reviewWordsDelta: Int = 0,
    ) {
        dailyStatsFlow.update { map ->
            val today = todayKey()
            val current = map[today] ?: DailyStatsEntity(date = today, streakDays = 1)
            map + (
                today to current.copy(
                    learnMinutes = (current.learnMinutes + learnMinutesDelta).coerceAtLeast(0),
                    newWordsCount = (current.newWordsCount + newWordsDelta).coerceAtLeast(0),
                    reviewWordsCount = (current.reviewWordsCount + reviewWordsDelta).coerceAtLeast(0),
                    streakDays = current.streakDays.coerceAtLeast(1),
                )
                )
        }
    }

    private fun initialDailyStats(): Map<String, DailyStatsEntity> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1).toString()
        val twoDaysAgo = today.minusDays(2).toString()

        return mapOf(
            twoDaysAgo to DailyStatsEntity(
                date = twoDaysAgo,
                learnMinutes = 18,
                newWordsCount = 6,
                reviewWordsCount = 4,
                streakDays = 4,
            ),
            yesterday to DailyStatsEntity(
                date = yesterday,
                learnMinutes = 22,
                newWordsCount = 7,
                reviewWordsCount = 5,
                streakDays = 5,
            ),
            today.toString() to DailyStatsEntity(
                date = today.toString(),
                learnMinutes = 16,
                newWordsCount = 5,
                reviewWordsCount = 3,
                streakDays = 6,
            ),
        )
    }

    private fun initialLessonProgress(): Map<String, LessonProgressEntity> {
        val now = System.currentTimeMillis()
        val default = lessons.associate { lesson ->
            lesson.id to LessonProgressEntity(
                lessonId = lesson.id,
                status = LessonStatus.NOT_STARTED.name,
                updatedAt = now,
            )
        }.toMutableMap()

        default["home_l1"] = LessonProgressEntity("home_l1", LessonStatus.COMPLETED.name, now)
        default["market_l1"] = LessonProgressEntity("market_l1", LessonStatus.IN_PROGRESS.name, now)
        default["traffic_l1"] = LessonProgressEntity("traffic_l1", LessonStatus.REVIEW_PENDING.name, now)

        return default
    }

    private fun initialRecords(): List<LearningRecordEntity> {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L

        return listOf(
            LearningRecordEntity(
                wordId = "w_home_mi",
                learned = true,
                mastered = true,
                favorite = true,
                writeCompleted = true,
                readCompleted = true,
                lastLearnTime = now - oneHour * 8,
                lastReviewTime = now - oneHour * 4,
                reviewLevel = 3,
            ),
            LearningRecordEntity(
                wordId = "w_home_mian",
                learned = true,
                mastered = true,
                writeCompleted = true,
                readCompleted = true,
                lastLearnTime = now - oneHour * 7,
                lastReviewTime = now - oneHour * 3,
                reviewLevel = 3,
            ),
            LearningRecordEntity(
                wordId = "w_home_you",
                learned = true,
                mastered = false,
                wrongCount = 2,
                writeCompleted = true,
                readCompleted = true,
                lastLearnTime = now - oneHour * 2,
                lastReviewTime = now - oneHour,
                reviewLevel = 1,
            ),
            LearningRecordEntity(
                wordId = "w_home_yan",
                learned = true,
                mastered = true,
                writeCompleted = true,
                readCompleted = true,
                lastLearnTime = now - oneHour * 6,
                lastReviewTime = now - oneHour * 2,
                reviewLevel = 2,
            ),
            LearningRecordEntity(
                wordId = "w_market_cai",
                learned = true,
                mastered = false,
                wrongCount = 1,
                writeCompleted = false,
                readCompleted = true,
                lastLearnTime = now - oneHour,
                reviewLevel = 1,
            ),
            LearningRecordEntity(
                wordId = "w_market_rou",
                learned = true,
                mastered = false,
                wrongCount = 2,
                writeCompleted = false,
                readCompleted = true,
                lastLearnTime = now - oneHour / 2,
                reviewLevel = 1,
            ),
        )
    }

    private fun buildExercises(
        lessonId: String,
        lessonWords: List<WordItemEntity>,
    ): List<ExerciseEntity> {
        if (lessonWords.isEmpty()) return emptyList()

        val options = lessonWords.take(3).map { it.text }
        val answerA = lessonWords.first().text
        val answerB = lessonWords.getOrElse(1) { lessonWords.first() }.text
        val answerC = lessonWords.last().text

        return listOf(
            ExerciseEntity(
                id = "ex_${lessonId}_listen",
                lessonId = lessonId,
                type = ExerciseType.LISTEN_CHOOSE.name,
                prompt = "请点出“$answerA”",
                answer = answerA,
                options = Json.encodeToString(options.shuffled()),
                audioResName = lessonWords.first().audioResName,
                sortOrder = 1,
            ),
            ExerciseEntity(
                id = "ex_${lessonId}_image",
                lessonId = lessonId,
                type = ExerciseType.IMAGE_CHOOSE.name,
                prompt = "看图，选出“$answerB”",
                answer = answerB,
                options = Json.encodeToString(options.shuffled()),
                imageResName = lessonWords.getOrElse(1) { lessonWords.first() }.imageResName,
                sortOrder = 2,
            ),
            ExerciseEntity(
                id = "ex_${lessonId}_scene",
                lessonId = lessonId,
                type = ExerciseType.SCENE_JUDGE.name,
                prompt = "这个地方应该找哪个字？",
                answer = answerC,
                options = Json.encodeToString(options.shuffled()),
                imageResName = lessonWords.last().imageResName,
                sortOrder = 3,
            ),
        )
    }

    private fun fallbackSeed(): SeedRoot {
        return SeedRoot(
            scenes = listOf(
                SeedScene(
                    id = "home",
                    name = "家里识字",
                    description = "厨房和家庭常见字",
                    sortOrder = 1,
                    lessons = listOf(
                        SeedLesson(
                            id = "home_l1",
                            title = "第1课：米、面、油",
                            description = "先学会厨房常见字",
                            sortOrder = 1,
                            words = listOf(
                                SeedWord("w_home_mi", "米", "mi", "大米", "家里有米。", 1),
                                SeedWord("w_home_mian", "面", "mian", "面条和面粉", "我买了面。", 1),
                                SeedWord("w_home_you", "油", "you", "食用油", "炒菜要放油。", 1),
                                SeedWord("w_home_yan", "盐", "yan", "调味盐", "菜里加一点盐。", 1),
                            ),
                        ),
                    ),
                ),
                SeedScene(
                    id = "market",
                    name = "买菜识字",
                    description = "买菜购物常见字",
                    sortOrder = 2,
                    lessons = listOf(
                        SeedLesson(
                            id = "market_l1",
                            title = "第1课：菜、肉、钱",
                            description = "先学会买菜常见字",
                            sortOrder = 1,
                            words = listOf(
                                SeedWord("w_market_cai", "菜", "cai", "蔬菜", "今天买点菜。", 1),
                                SeedWord("w_market_rou", "肉", "rou", "肉类", "这块肉很新鲜。", 1),
                                SeedWord("w_market_qian", "钱", "qian", "钱款", "先准备好钱。", 1),
                            ),
                        ),
                    ),
                ),
                SeedScene(
                    id = "traffic",
                    name = "外出识字",
                    description = "出行交通常见字",
                    sortOrder = 3,
                    lessons = listOf(
                        SeedLesson(
                            id = "traffic_l1",
                            title = "第1课：路、站、车",
                            description = "先学会交通常见字",
                            sortOrder = 1,
                            words = listOf(
                                SeedWord("w_traffic_lu", "路", "lu", "道路", "这条路向前走。", 1),
                                SeedWord("w_traffic_zhan", "站", "zhan", "车站", "在这一站下车。", 1),
                                SeedWord("w_traffic_che", "车", "che", "车辆", "这辆车去市区。", 1),
                            ),
                        ),
                    ),
                ),
            ),
        )
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

