package com.linguatranslate.app.data.repository

import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.core.network.NetworkErrorMapper
import com.linguatranslate.app.data.remote.api.TranslationApi
import com.linguatranslate.app.data.remote.dto.DetectLanguageRequestDto
import com.linguatranslate.app.data.remote.dto.TranslateRequestDto
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Real implementation backed entirely by the LinguaTranslate backend
 * API (see requirement #7/#8/#30 - no on-device dictionary, no
 * hardcoded word-for-word mapping; every translation is a live
 * network call).
 */
class TranslationRepositoryImpl @Inject constructor(
    private val api: TranslationApi,
) : TranslationRepository {

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language,
    ): AppResult<TranslationResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.translate(
                TranslateRequestDto(
                    text = text,
                    sourceLanguage = sourceLanguage.apiCode,
                    targetLanguage = targetLanguage.apiCode,
                )
            )

            if (!response.isSuccessful) {
                return@withContext AppResult.Failure(NetworkErrorMapper.fromHttpResponse(response))
            }

            val body = response.body()
            val data = body?.data
            if (body?.success != true || data == null) {
                return@withContext AppResult.Failure(AppError.InvalidResponse)
            }

            AppResult.Success(
                TranslationResult(
                    originalText = data.originalText,
                    translatedText = data.translatedText,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = Language.fromApiCode(data.targetLanguage),
                    detectedLanguage = data.detectedLanguage?.let { Language.fromApiCode(it) },
                )
            )
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.fromThrowable(t))
        }
    }

    override suspend fun detectLanguage(text: String): AppResult<Language> = withContext(Dispatchers.IO) {
        try {
            val response = api.detectLanguage(DetectLanguageRequestDto(text = text))

            if (!response.isSuccessful) {
                return@withContext AppResult.Failure(NetworkErrorMapper.fromHttpResponse(response))
            }

            val body = response.body()
            val languageCode = body?.language
            if (body?.success != true || languageCode == null) {
                return@withContext AppResult.Failure(AppError.InvalidResponse)
            }

            AppResult.Success(Language.fromApiCode(languageCode))
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.fromThrowable(t))
        }
    }
}
