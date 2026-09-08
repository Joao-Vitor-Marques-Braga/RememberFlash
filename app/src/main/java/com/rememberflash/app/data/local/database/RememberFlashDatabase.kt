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

import com.rememberflash.app.data.local.database.dao.TopicDao
import com.rememberflash.app.data.local.database.entity.TopicEntity

@Database(
    entities = [
        ContestEntity::class,
        DisciplineEntity::class,
        TopicEntity::class,
        FlashcardEntity::class,
        EssayEntity::class,
        QuestionEntity::class,
        ScheduleEntity::class,
        DailyGoalEntity::class,
        MockExamAttemptEntity::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RememberFlashDatabase : RoomDatabase() {

    abstract fun contestDao(): ContestDao
    abstract fun disciplineDao(): DisciplineDao
    abstract fun topicDao(): TopicDao
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

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `discipline_id` INTEGER NOT NULL,
                        `contest_id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `is_completed` INTEGER NOT NULL DEFAULT 0,
                        `order_index` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL,
                        FOREIGN KEY(`discipline_id`) REFERENCES `disciplines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`contest_id`) REFERENCES `contests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_topics_discipline_id` ON `topics` (`discipline_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_topics_contest_id` ON `topics` (`contest_id`)")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `topic_id` INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_topic_id` ON `flashcards` (`topic_id`)")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `topic_id` INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_topic_id` ON `questions` (`topic_id`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `topics` ADD COLUMN `is_synced` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `is_synced` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
