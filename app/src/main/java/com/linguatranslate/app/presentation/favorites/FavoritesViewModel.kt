package com.linguatranslate.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguatranslate.app.core.utils.TextToSpeechService
import com.linguatranslate.app.domain.model.FavoriteEntry
import com.linguatranslate.app.domain.usecase.ObserveFavoritesUseCase
import com.linguatranslate.app.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val textToSpeechService: TextToSpeechService,
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntry>> = observeFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(id: Long) {
        viewModelScope.launch { removeFavoriteUseCase(id) }
    }

    fun speak(entry: FavoriteEntry) {
        viewModelScope.launch { textToSpeechService.speak(entry.translatedText, entry.targetLanguage) }
    }
}
