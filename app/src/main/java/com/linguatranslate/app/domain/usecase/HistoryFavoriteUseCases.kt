package com.linguatranslate.app.domain.usecase

import com.linguatranslate.app.domain.model.FavoriteEntry
import com.linguatranslate.app.domain.model.HistoryEntry
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.repository.FavoriteRepository
import com.linguatranslate.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke(): Flow<List<HistoryEntry>> = repository.observeHistory()
}

class DeleteHistoryEntryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteEntry(id)
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke() = repository.clearAll()
}

class ObserveFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(): Flow<List<FavoriteEntry>> = repository.observeFavorites()
}

class AddFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(result: TranslationResult) = repository.addFavorite(result)
}

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(id: Long) = repository.removeFavorite(id)
}
