package com.linguatranslate.app.data.remote.api

import com.linguatranslate.app.data.remote.dto.DetectLanguageRequestDto
import com.linguatranslate.app.data.remote.dto.DetectLanguageResponseDto
import com.linguatranslate.app.data.remote.dto.TranslateRequestDto
import com.linguatranslate.app.data.remote.dto.TranslateResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Thin Retrofit definition of the LinguaTranslate backend contract
 * (see backend/src/routes). No business logic lives here - that's in
 * the repository implementation.
 */
interface TranslationApi {
    @POST("api/translate")
    suspend fun translate(@Body request: TranslateRequestDto): Response<TranslateResponseDto>

    @POST("api/detect-language")
    suspend fun detectLanguage(@Body request: DetectLanguageRequestDto): Response<DetectLanguageResponseDto>
}
