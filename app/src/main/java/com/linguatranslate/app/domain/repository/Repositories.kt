package com.linguatranslate.app.domain.repository

import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.FavoriteEntry
import com.linguatranslate.app.domain.model.HistoryEntry
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language,
    ): AppResult<TranslationResult>

    suspend fun detectLanguage(text: String): AppResult<Language>
}

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun addEntry(result: TranslationResult)
    suspend fun deleteEntry(id: Long)
    suspend fun clearAll()
}

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<FavoriteEntry>>
    suspend fun addFavorite(result: TranslationResult)
    suspend fun removeFavorite(id: Long)
    suspend fun isFavorite(originalText: String, translatedText: String): Boolean
}
