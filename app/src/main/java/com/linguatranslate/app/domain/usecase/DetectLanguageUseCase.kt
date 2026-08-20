package com.linguatranslate.app.domain.usecase

import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.repository.TranslationRepository
import javax.inject.Inject

/**
 * Detects the language of a piece of text. Backed by the backend's
 * /api/detect-language endpoint via [TranslationRepository]; the
 * concrete provider used server-side can change without this class
 * (or anything calling it) needing to know.
 */
class DetectLanguageUseCase @Inject constructor(
    private val repository: TranslationRepository,
) {
    suspend operator fun invoke(text: String): AppResult<Language> {
        if (text.isBlank()) return AppResult.Failure(com.linguatranslate.app.core.common.AppError.EmptyText)
        return repository.detectLanguage(text)
    }
}
