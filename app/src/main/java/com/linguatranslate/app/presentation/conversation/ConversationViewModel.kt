package com.linguatranslate.app.presentation.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.core.utils.SpeechRecognitionEvent
import com.linguatranslate.app.core.utils.SpeechToTextService
import com.linguatranslate.app.core.utils.TextToSpeechService
import com.linguatranslate.app.domain.model.ConversationSpeaker
import com.linguatranslate.app.domain.model.ConversationTurn
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val personALanguage: Language = Language.ENGLISH,
    val personBLanguage: Language = Language.INDONESIAN,
    val turns: List<ConversationTurn> = emptyList(),
    val listeningSpeaker: ConversationSpeaker? = null,
    val isActive: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    private val speechToTextService: SpeechToTextService,
    private val textToSpeechService: TextToSpeechService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var nextTurnId = 0L

    fun onPersonALanguageSelected(language: Language) {
        _uiState.value = _uiState.value.copy(personALanguage = language)
    }

    fun onPersonBLanguageSelected(language: Language) {
        _uiState.value = _uiState.value.copy(personBLanguage = language)
    }

    fun startConversation() {
        _uiState.value = _uiState.value.copy(isActive = true, error = null)
    }

    fun stopConversation() {
        _uiState.value = _uiState.value.copy(isActive = false, listeningSpeaker = null)
    }

    fun onSpeakerMicClick(speaker: ConversationSpeaker) {
        val state = _uiState.value
        if (!state.isActive || state.listeningSpeaker != null) return

        val speakerLanguage = if (speaker == ConversationSpeaker.PERSON_A) state.personALanguage else state.personBLanguage
        val targetLanguage = if (speaker == ConversationSpeaker.PERSON_A) state.personBLanguage else state.personALanguage

        viewModelScope.launch {
            speechToTextService.listen(speakerLanguage)
                .onEach { event ->
                    when (event) {
                        is SpeechRecognitionEvent.ListeningStarted ->
                            _uiState.value = _uiState.value.copy(listeningSpeaker = speaker, error = null)

                        is SpeechRecognitionEvent.FinalResult ->
                            handleRecognizedSpeech(speaker, event.text, targetLanguage)

                        is SpeechRecognitionEvent.Error -> {
                            _uiState.value = _uiState.value.copy(listeningSpeaker = null, error = event.error.message)
                        }

                        is SpeechRecognitionEvent.Done ->
                            _uiState.value = _uiState.value.copy(listeningSpeaker = null)

                        is SpeechRecognitionEvent.PartialResult -> Unit
                    }
                }
                .catch {
                    _uiState.value = _uiState.value.copy(listeningSpeaker = null, error = "Unable to recognize speech.")
                }
                .collect()
        }
    }

    private suspend fun handleRecognizedSpeech(
        speaker: ConversationSpeaker,
        text: String,
        targetLanguage: Language,
    ) {
        val sourceLanguage = if (speaker == ConversationSpeaker.PERSON_A) {
            _uiState.value.personALanguage
        } else {
            _uiState.value.personBLanguage
        }

        when (val result = translateTextUseCase(text, sourceLanguage, targetLanguage, recordHistory = false)) {
            is AppResult.Success -> {
                val turn = ConversationTurn(
                    id = nextTurnId++,
                    speaker = speaker,
                    originalText = result.data.originalText,
                    translatedText = result.data.translatedText,
                )
                _uiState.value = _uiState.value.copy(turns = _uiState.value.turns + turn)
                textToSpeechService.speak(result.data.translatedText, targetLanguage)
            }
            is AppResult.Failure -> {
                _uiState.value = _uiState.value.copy(error = result.error.message)
            }
        }
    }
}
