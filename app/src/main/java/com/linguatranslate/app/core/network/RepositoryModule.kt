package com.linguatranslate.app.core.network

import com.linguatranslate.app.data.repository.FavoriteRepositoryImpl
import com.linguatranslate.app.data.repository.HistoryRepositoryImpl
import com.linguatranslate.app.data.repository.TranslationRepositoryImpl
import com.linguatranslate.app.domain.repository.FavoriteRepository
import com.linguatranslate.app.domain.repository.HistoryRepository
import com.linguatranslate.app.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTranslationRepository(impl: TranslationRepositoryImpl): TranslationRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository
}
