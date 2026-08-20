package com.linguatranslate.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.repository.TranslationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DetectLanguageUseCaseTest {

    private lateinit var repository: TranslationRepository
    private lateinit var useCase: DetectLanguageUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = DetectLanguageUseCase(repository)
    }

    @Test
    fun `detects english`() = runTest {
        coEvery { repository.detectLanguage("Good morning") } returns AppResult.Success(Language.ENGLISH)
        val result = useCase("Good morning")
        assertThat((result as AppResult.Success).data).isEqualTo(Language.ENGLISH)
    }

    @Test
    fun `detects indonesian`() = runTest {
        coEvery { repository.detectLanguage("Selamat pagi") } returns AppResult.Success(Language.INDONESIAN)
        val result = useCase("Selamat pagi")
        assertThat((result as AppResult.Success).data).isEqualTo(Language.INDONESIAN)
    }

    @Test
    fun `detects japanese`() = runTest {
        coEvery { repository.detectLanguage("おはようございます") } returns AppResult.Success(Language.JAPANESE)
        val result = useCase("おはようございます")
        assertThat((result as AppResult.Success).data).isEqualTo(Language.JAPANESE)
    }

    @Test
    fun `empty text returns EmptyText error`() = runTest {
        val result = useCase("")
        assertThat((result as AppResult.Failure).error).isEqualTo(AppError.EmptyText)
    }
}
