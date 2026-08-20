package com.linguatranslate.app.domain.usecase

import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.repository.HistoryRepository
import com.linguatranslate.app.domain.repository.TranslationRepository
import javax.inject.Inject

/**
 * Translates text end to end: validates input, calls the translation
 * service, and (on success) records the result in history. This is
 * the single entry point the Home and Conversation screens use so
 * validation rules live in exactly one place.
 */
class TranslateTextUseCase @Inject constructor(
    private val translationRepository: TranslationRepository,
    private val historyRepository: HistoryRepository,
) {
    suspend operator fun invoke(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language,
        recordHistory: Boolean = true,
    ): AppResult<TranslationResult> {
        if (text.isBlank()) return AppResult.Failure(AppError.EmptyText)

        if (sourceLanguage != Language.AUTO && sourceLanguage == targetLanguage) {
            return AppResult.Failure(AppError.SameLanguage)
        }

        val result = translationRepository.translate(text, sourceLanguage, targetLanguage)
        if (result is AppResult.Success && recordHistory) {
            historyRepository.addEntry(result.data)
        }
        return result
    }
}
