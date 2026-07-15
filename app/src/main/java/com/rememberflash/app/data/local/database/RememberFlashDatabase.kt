package com.rememberflash.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rememberflash.app.data.local.database.dao.ContestDao
import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.local.database.dao.EssayDao
import com.rememberflash.app.data.local.database.dao.FlashcardDao
import com.rememberflash.app.data.local.database.dao.ScheduleDao
import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.data.local.database.entity.DailyGoalEntity
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import com.rememberflash.app.data.local.database.entity.EssayEntity
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import com.rememberflash.app.data.local.database.entity.ScheduleEntity

@Database(
    entities = [
        ContestEntity::class,
        DisciplineEntity::class,
        FlashcardEntity::class,
        EssayEntity::class,
        QuestionEntity::class,
        ScheduleEntity::class,
        DailyGoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RememberFlashDatabase : RoomDatabase() {

    abstract fun contestDao(): ContestDao
    abstract fun disciplineDao(): DisciplineDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun essayDao(): EssayDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        const val DATABASE_NAME = "remember_flash_db"
    }
}
