package com.linguatranslate.app.core.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.Language.Companion.bcp47
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SpeechRecognitionEvent {
    data object ListeningStarted : SpeechRecognitionEvent()
    data class PartialResult(val text: String) : SpeechRecognitionEvent()
    data class FinalResult(val text: String) : SpeechRecognitionEvent()
    data class Error(val error: AppError) : SpeechRecognitionEvent()
    data object Done : SpeechRecognitionEvent()
}

/**
 * Wraps Android's on-device [SpeechRecognizer]. Exposed as a Flow so
 * ViewModels can collect partial/final results without holding a
 * reference to a listener with Android lifecycle concerns.
 *
 * NOTE: native SpeechRecognizer is NOT guaranteed to reliably support
 * multilingual auto-detection across all devices/OEMs. When
 * [language] is [Language.AUTO], we start listening using the
 * device's default locale and then run the recognized text through
 * LanguageDetectionService downstream - we do not claim the
 * recognizer itself detects the language.
 */
@Singleton
class SpeechToTextService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(language: Language): Flow<SpeechRecognitionEvent> = callbackFlow {
        if (!isAvailable()) {
            trySend(SpeechRecognitionEvent.Error(AppError.SpeechUnavailable))
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val localeTag = if (language == Language.AUTO) {
                java.util.Locale.getDefault().toLanguageTag()
            } else {
                language.bcp47()
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechRecognitionEvent.ListeningStarted)
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                trySend(SpeechRecognitionEvent.Error(AppError.SpeechUnavailable))
                close()
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) {
                    trySend(SpeechRecognitionEvent.FinalResult(text))
                } else {
                    trySend(SpeechRecognitionEvent.Error(AppError.SpeechUnavailable))
                }
                trySend(SpeechRecognitionEvent.Done)
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    trySend(SpeechRecognitionEvent.PartialResult(text))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(recognizerIntent)

        awaitClose {
            recognizer.stopListening()
            recognizer.destroy()
        }
    }
}
