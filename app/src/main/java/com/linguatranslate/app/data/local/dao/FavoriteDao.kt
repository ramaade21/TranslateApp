package com.linguatranslate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.linguatranslate.app.data.local.entity.FavoriteTranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_translations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteTranslationEntity>>

    @Insert
    suspend fun insert(entity: FavoriteTranslationEntity): Long

    @Query("DELETE FROM favorite_translations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite_translations " +
            "WHERE originalText = :originalText AND translatedText = :translatedText)"
    )
    suspend fun exists(originalText: String, translatedText: String): Boolean
}
