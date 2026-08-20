package com.linguatranslate.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linguatranslate.app.data.local.dao.FavoriteDao
import com.linguatranslate.app.data.local.dao.HistoryDao
import com.linguatranslate.app.data.local.entity.FavoriteTranslationEntity
import com.linguatranslate.app.data.local.entity.TranslationHistoryEntity

@Database(
    entities = [TranslationHistoryEntity::class, FavoriteTranslationEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "linguatranslate.db"
    }
}
