package com.linguatranslate.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranslateRequestDto(
    val text: String,
    val sourceLanguage: String,
    val targetLanguage: String,
)

@Serializable
data class TranslateResponseDto(
    val success: Boolean,
    val data: TranslateResultDto? = null,
    val error: ApiErrorDto? = null,
)

@Serializable
data class TranslateResultDto(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val detectedLanguage: String? = null,
)

@Serializable
data class DetectLanguageRequestDto(
    val text: String,
)

@Serializable
data class DetectLanguageResponseDto(
    val success: Boolean,
    val language: String? = null,
    val error: ApiErrorDto? = null,
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
)
