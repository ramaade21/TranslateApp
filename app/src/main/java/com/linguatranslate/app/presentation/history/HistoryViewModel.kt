package com.linguatranslate.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguatranslate.app.core.utils.TextToSpeechService
import com.linguatranslate.app.domain.model.HistoryEntry
import com.linguatranslate.app.domain.usecase.ClearHistoryUseCase
import com.linguatranslate.app.domain.usecase.DeleteHistoryEntryUseCase
import com.linguatranslate.app.domain.usecase.ObserveHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeHistoryUseCase: ObserveHistoryUseCase,
    private val deleteHistoryEntryUseCase: DeleteHistoryEntryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val textToSpeechService: TextToSpeechService,
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> = observeHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { deleteHistoryEntryUseCase(id) }
    }

    fun clearAll() {
        viewModelScope.launch { clearHistoryUseCase() }
    }

    fun speak(entry: HistoryEntry) {
        viewModelScope.launch { textToSpeechService.speak(entry.translatedText, entry.targetLanguage) }
    }
}
