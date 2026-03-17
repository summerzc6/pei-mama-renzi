package com.peimama.renzi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.peimama.renzi.data.local.dao.DailyStatsDao
import com.peimama.renzi.data.local.dao.ExerciseDao
import com.peimama.renzi.data.local.dao.LearningRecordDao
import com.peimama.renzi.data.local.dao.LessonDao
import com.peimama.renzi.data.local.dao.LessonProgressDao
import com.peimama.renzi.data.local.dao.SceneDao
import com.peimama.renzi.data.local.dao.WordItemDao
import com.peimama.renzi.data.local.entity.DailyStatsEntity
import com.peimama.renzi.data.local.entity.ExerciseEntity
import com.peimama.renzi.data.local.entity.LearningRecordEntity
import com.peimama.renzi.data.local.entity.LessonEntity
import com.peimama.renzi.data.local.entity.LessonProgressEntity
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.local.entity.WordItemEntity

@Database(
    entities = [
        SceneEntity::class,
        LessonEntity::class,
        WordItemEntity::class,
        ExerciseEntity::class,
        LearningRecordEntity::class,
        DailyStatsEntity::class,
        LessonProgressEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sceneDao(): SceneDao
    abstract fun lessonDao(): LessonDao
    abstract fun wordItemDao(): WordItemDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun learningRecordDao(): LearningRecordDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun lessonProgressDao(): LessonProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mama_read.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
