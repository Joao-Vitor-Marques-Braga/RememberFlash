package com.rememberflash.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rememberflash.app.data.local.database.dao.ContestDao
import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.local.database.dao.EssayDao
import com.rememberflash.app.data.local.database.dao.FlashcardDao
import com.rememberflash.app.data.local.database.dao.QuestionDao
import com.rememberflash.app.data.local.database.dao.ScheduleDao
import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.data.local.database.entity.DailyGoalEntity
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import com.rememberflash.app.data.local.database.entity.EssayEntity
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import com.rememberflash.app.data.local.database.entity.ScheduleEntity

import com.rememberflash.app.data.local.database.entity.MockExamAttemptEntity
import com.rememberflash.app.data.local.database.dao.MockExamAttemptDao

@Database(
    entities = [
        ContestEntity::class,
        DisciplineEntity::class,
        FlashcardEntity::class,
        EssayEntity::class,
        QuestionEntity::class,
        ScheduleEntity::class,
        DailyGoalEntity::class,
        MockExamAttemptEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RememberFlashDatabase : RoomDatabase() {

    abstract fun contestDao(): ContestDao
    abstract fun disciplineDao(): DisciplineDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun essayDao(): EssayDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun questionDao(): QuestionDao
    abstract fun mockExamAttemptDao(): MockExamAttemptDao

    companion object {
        const val DATABASE_NAME = "remember_flash_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE disciplines ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contests ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE disciplines ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flashcards ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
