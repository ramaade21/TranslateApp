package com.linguatranslate.app.data.repository

import com.linguatranslate.app.data.local.dao.FavoriteDao
import com.linguatranslate.app.data.local.dao.HistoryDao
import com.linguatranslate.app.data.local.entity.FavoriteTranslationEntity
import com.linguatranslate.app.data.local.entity.TranslationHistoryEntity
import com.linguatranslate.app.domain.model.FavoriteEntry
import com.linguatranslate.app.domain.model.HistoryEntry
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.repository.FavoriteRepository
import com.linguatranslate.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observeHistory(): Flow<List<HistoryEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(result: TranslationResult) {
        dao.insert(
            TranslationHistoryEntity(
                originalText = result.originalText,
                translatedText = result.translatedText,
                sourceLanguage = result.sourceLanguage.apiCode,
                targetLanguage = result.targetLanguage.apiCode,
                detectedLanguage = result.detectedLanguage?.apiCode,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    override suspend fun clearAll() = dao.clearAll()

    private fun TranslationHistoryEntity.toDomain() = HistoryEntry(
        id = id,
        originalText = originalText,
        translatedText = translatedText,
        sourceLanguage = Language.fromApiCode(sourceLanguage),
        targetLanguage = Language.fromApiCode(targetLanguage),
        detectedLanguage = detectedLanguage?.let { Language.fromApiCode(it) },
        createdAt = createdAt,
    )
}

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
) : FavoriteRepository {

    override fun observeFavorites(): Flow<List<FavoriteEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(result: TranslationResult) {
        dao.insert(
            FavoriteTranslationEntity(
                originalText = result.originalText,
                translatedText = result.translatedText,
                sourceLanguage = result.sourceLanguage.apiCode,
                targetLanguage = result.targetLanguage.apiCode,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun removeFavorite(id: Long) = dao.deleteById(id)

    override suspend fun isFavorite(originalText: String, translatedText: String): Boolean =
        dao.exists(originalText, translatedText)

    private fun FavoriteTranslationEntity.toDomain() = FavoriteEntry(
        id = id,
        originalText = originalText,
        translatedText = translatedText,
        sourceLanguage = Language.fromApiCode(sourceLanguage),
        targetLanguage = Language.fromApiCode(targetLanguage),
        createdAt = createdAt,
    )
}
