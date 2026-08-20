package com.linguatranslate.app.domain.model

/**
 * Result of a single translation operation, as consumed by the
 * presentation layer. Distinct from the network DTO - this is the
 * clean domain representation.
 */
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    /** Only populated when [sourceLanguage] selection was AUTO. */
    val detectedLanguage: Language? = null,
)

data class HistoryEntry(
    val id: Long,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val detectedLanguage: Language?,
    val createdAt: Long,
)

data class FavoriteEntry(
    val id: Long,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val createdAt: Long,
)

/** A single turn in Conversation Mode. */
data class ConversationTurn(
    val id: Long,
    val speaker: ConversationSpeaker,
    val originalText: String,
    val translatedText: String,
)

enum class ConversationSpeaker { PERSON_A, PERSON_B }
