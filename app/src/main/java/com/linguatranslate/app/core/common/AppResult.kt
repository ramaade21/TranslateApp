package com.linguatranslate.app.core.common

/**
 * A generic success/failure wrapper used throughout the domain and
 * data layers instead of throwing raw exceptions across boundaries.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

/**
 * User-facing error taxonomy. Every error the app can surface maps to
 * one of these so the UI layer can show a friendly, translated message
 * instead of a stack trace (see requirement #21 - Error Handling).
 */
sealed class AppError(open val message: String) {
    data object NoInternet : AppError("No internet connection.")
    data object Timeout : AppError("The request took too long. Please try again.")
    data class Http(val code: Int, override val message: String) : AppError(message)
    data object InvalidResponse : AppError("Received an unexpected response from the server.")
    data object SpeechUnavailable : AppError("Unable to recognize speech.")
    data object MicPermissionDenied : AppError("Microphone permission is required to use voice input.")
    data object EmptyText : AppError("Please enter some text to translate.")
    data object SameLanguage : AppError("Source and target languages must be different.")
    data object UnsupportedLanguage : AppError("This language is not supported yet.")
    data object ProviderUnavailable : AppError("Translation service is temporarily unavailable.")
    data class TtsUnavailable(val languageName: String) :
        AppError("$languageName voice is not installed on this device.")
    data class Unknown(override val message: String) : AppError(message)
}
