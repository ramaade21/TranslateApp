package com.linguatranslate.app.core.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wraps Android's [TextToSpeech]. Handles the async engine-init
 * lifecycle and gracefully reports when a requested language's voice
 * data isn't installed on the device, instead of crashing
 * (requirement #11).
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private suspend fun ensureInitialized(): Boolean {
        if (isInitialized) return true
        return suspendCancellableCoroutine { cont ->
            tts = TextToSpeech(context) { status ->
                isInitialized = status == TextToSpeech.SUCCESS
                if (cont.isActive) cont.resume(isInitialized)
            }
        }
    }

    suspend fun speak(text: String, language: Language): AppResult<Unit> {
        if (!ensureInitialized()) {
            return AppResult.Failure(AppError.Unknown("Text-to-speech engine failed to initialize."))
        }
        val engine = tts ?: return AppResult.Failure(AppError.Unknown("Text-to-speech unavailable."))

        val locale = language.locale
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return AppResult.Failure(AppError.TtsUnavailable(language.displayName))
        }

        engine.stop()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "linguatranslate_utterance")
        return AppResult.Success(Unit)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
