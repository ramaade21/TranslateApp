package com.linguatranslate.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.core.utils.SpeechRecognitionEvent
import com.linguatranslate.app.core.utils.SpeechToTextService
import com.linguatranslate.app.core.utils.TextToSpeechService
import com.linguatranslate.app.data.repository.SettingsRepository
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.usecase.AddFavoriteUseCase
import com.linguatranslate.app.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val sourceLanguage: Language = Language.AUTO,
    val targetLanguage: Language = Language.INDONESIAN,
    val inputText: String = "",
    val detectedLanguage: Language? = null,
    val translatedText: String = "",
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val isFavorite: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val speechToTextService: SpeechToTextService,
    private val textToSpeechService: TextToSpeechService,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, error = null)
    }

    fun onSourceLanguageSelected(language: Language) {
        _uiState.value = _uiState.value.copy(sourceLanguage = language)
    }

    fun onTargetLanguageSelected(language: Language) {
        val current = _uiState.value
        if (language == current.sourceLanguage) {
            _uiState.value = current.copy(error = "Source and target languages must be different.")
            return
        }
        _uiState.value = current.copy(targetLanguage = language)
    }

    fun onSwapLanguages() {
        val current = _uiState.value
        val effectiveSource = current.detectedLanguage ?: current.sourceLanguage
        if (effectiveSource == Language.AUTO) return // nothing meaningful to swap yet

        _uiState.value = current.copy(
            sourceLanguage = current.targetLanguage,
            targetLanguage = effectiveSource,
            inputText = current.translatedText,
            translatedText = current.inputText,
            detectedLanguage = null,
        )
    }

    fun translate() {
        val state = _uiState.value
        if (state.inputText.isBlank()) {
            _uiState.value = state.copy(error = "Please enter some text to translate.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true, error = null)

            when (val result = translateTextUseCase(state.inputText, state.sourceLanguage, state.targetLanguage)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTranslating = false,
                        translatedText = result.data.translatedText,
                        detectedLanguage = result.data.detectedLanguage,
                        isFavorite = false,
                    )
                    maybeAutoSpeak(result.data.translatedText, result.data.targetLanguage)
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isTranslating = false, error = result.error.message)
                }
            }
        }
    }

    fun onMicClick() {
        val state = _uiState.value
        if (state.isListening) return

        viewModelScope.launch {
            speechToTextService.listen(state.sourceLanguage)
                .onEach { event ->
                    when (event) {
                        is SpeechRecognitionEvent.ListeningStarted ->
                            _uiState.value = _uiState.value.copy(isListening = true, error = null)
                        is SpeechRecognitionEvent.PartialResult ->
                            _uiState.value = _uiState.value.copy(inputText = event.text)
                        is SpeechRecognitionEvent.FinalResult -> {
                            _uiState.value = _uiState.value.copy(inputText = event.text, isListening = false)
                            translate()
                        }
                        is SpeechRecognitionEvent.Error ->
                            _uiState.value = _uiState.value.copy(isListening = false, error = event.error.message)
                        is SpeechRecognitionEvent.Done ->
                            _uiState.value = _uiState.value.copy(isListening = false)
                    }
                }
                .catch { _uiState.value = _uiState.value.copy(isListening = false, error = "Unable to recognize speech.") }
                .collect()
        }
    }

    private suspend fun maybeAutoSpeak(text: String, language: Language) {
        val state = settingsRepository.settings.first()
        if (state.autoSpeakTranslation) {
            textToSpeechService.speak(text, language)
        }
    }

    fun onSpeakResult() {
        val state = _uiState.value
        if (state.translatedText.isBlank()) return
        viewModelScope.launch {
            val language = state.targetLanguage
            when (val result = textToSpeechService.speak(state.translatedText, language)) {
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error.message)
                else -> Unit
            }
        }
    }

    fun onToggleFavorite() {
        val state = _uiState.value
        if (state.translatedText.isBlank()) return
        viewModelScope.launch {
            addFavoriteUseCase(
                com.linguatranslate.app.domain.model.TranslationResult(
                    originalText = state.inputText,
                    translatedText = state.translatedText,
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    detectedLanguage = state.detectedLanguage,
                )
            )
            _uiState.value = _uiState.value.copy(isFavorite = true)
        }
    }

    override fun onCleared() {
        textToSpeechService.shutdown()
        super.onCleared()
    }
}
