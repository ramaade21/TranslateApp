package com.linguatranslate.app.domain.model

import java.util.Locale

/**
 * The set of languages LinguaTranslate supports. AUTO is only ever valid
 * as a *source* language selection - it is resolved to a concrete
 * language (via [LanguageDetectionService]) before being persisted or
 * sent to a target-language field.
 */
enum class Language(
    val apiCode: String,
    val displayName: String,
    val flagEmoji: String,
    val locale: Locale,
) {
    AUTO(apiCode = "auto", displayName = "Auto Detect", flagEmoji = "🌐", locale = Locale.getDefault()),
    ENGLISH(apiCode = "en", displayName = "English", flagEmoji = "🇬🇧", locale = Locale("en", "US")),
    INDONESIAN(apiCode = "id", displayName = "Indonesian", flagEmoji = "🇮🇩", locale = Locale("id", "ID")),
    JAPANESE(apiCode = "ja", displayName = "Japanese", flagEmoji = "🇯🇵", locale = Locale("ja", "JP"));

    /** Languages that can legally be a *target* (AUTO is excluded). */
    companion object {
        val translatableLanguages: List<Language> = listOf(ENGLISH, INDONESIAN, JAPANESE)

        fun fromApiCode(code: String): Language =
            entries.firstOrNull { it.apiCode == code } ?: ENGLISH

        /** BCP-47 tag used for SpeechRecognizer / TextToSpeech locale selection. */
        fun Language.bcp47(): String = when (this) {
            ENGLISH -> "en-US"
            INDONESIAN -> "id-ID"
            JAPANESE -> "ja-JP"
            AUTO -> Locale.getDefault().toLanguageTag()
        }
    }
}
