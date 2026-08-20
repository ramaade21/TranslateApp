package com.linguatranslate.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.linguatranslate.app.core.common.AppError
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.repository.HistoryRepository
import com.linguatranslate.app.domain.repository.TranslationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TranslateTextUseCaseTest {

    private lateinit var translationRepository: TranslationRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var useCase: TranslateTextUseCase

    @Before
    fun setUp() {
        translationRepository = mockk()
        historyRepository = mockk(relaxed = true)
        useCase = TranslateTextUseCase(translationRepository, historyRepository)
    }

    @Test
    fun `english to indonesian returns translated result and records history`() = runTest {
        val expected = TranslationResult(
            originalText = "Good morning",
            translatedText = "Selamat pagi",
            sourceLanguage = Language.ENGLISH,
            targetLanguage = Language.INDONESIAN,
        )
        coEvery {
            translationRepository.translate("Good morning", Language.ENGLISH, Language.INDONESIAN)
        } returns AppResult.Success(expected)

        val result = useCase("Good morning", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.translatedText).isEqualTo("Selamat pagi")
        coVerify { historyRepository.addEntry(expected) }
    }

    @Test
    fun `indonesian to english`() = runTest {
        val expected = TranslationResult("Selamat pagi", "Good morning", Language.INDONESIAN, Language.ENGLISH)
        coEvery {
            translationRepository.translate("Selamat pagi", Language.INDONESIAN, Language.ENGLISH)
        } returns AppResult.Success(expected)

        val result = useCase("Selamat pagi", Language.INDONESIAN, Language.ENGLISH)

        assertThat((result as AppResult.Success).data.translatedText).isEqualTo("Good morning")
    }

    @Test
    fun `japanese to indonesian`() = runTest {
        val expected = TranslationResult("おはよう", "Selamat pagi", Language.JAPANESE, Language.INDONESIAN)
        coEvery {
            translationRepository.translate("おはよう", Language.JAPANESE, Language.INDONESIAN)
        } returns AppResult.Success(expected)

        val result = useCase("おはよう", Language.JAPANESE, Language.INDONESIAN)

        assertThat((result as AppResult.Success).data.translatedText).isEqualTo("Selamat pagi")
    }

    @Test
    fun `indonesian to japanese`() = runTest {
        val expected = TranslationResult("Selamat pagi", "おはよう", Language.INDONESIAN, Language.JAPANESE)
        coEvery {
            translationRepository.translate("Selamat pagi", Language.INDONESIAN, Language.JAPANESE)
        } returns AppResult.Success(expected)

        val result = useCase("Selamat pagi", Language.INDONESIAN, Language.JAPANESE)

        assertThat((result as AppResult.Success).data.translatedText).isEqualTo("おはよう")
    }

    @Test
    fun `auto detects source language`() = runTest {
        val expected = TranslationResult(
            originalText = "Good morning",
            translatedText = "Selamat pagi",
            sourceLanguage = Language.AUTO,
            targetLanguage = Language.INDONESIAN,
            detectedLanguage = Language.ENGLISH,
        )
        coEvery {
            translationRepository.translate("Good morning", Language.AUTO, Language.INDONESIAN)
        } returns AppResult.Success(expected)

        val result = useCase("Good morning", Language.AUTO, Language.INDONESIAN)

        assertThat((result as AppResult.Success).data.detectedLanguage).isEqualTo(Language.ENGLISH)
    }

    @Test
    fun `empty input returns EmptyText error without calling repository`() = runTest {
        val result = useCase("   ", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
        assertThat((result as AppResult.Failure).error).isEqualTo(AppError.EmptyText)
        coVerify(exactly = 0) { historyRepository.addEntry(any()) }
    }

    @Test
    fun `same source and target language returns SameLanguage error`() = runTest {
        val result = useCase("Hello", Language.ENGLISH, Language.ENGLISH)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
        assertThat((result as AppResult.Failure).error).isEqualTo(AppError.SameLanguage)
    }

    @Test
    fun `provider failure is propagated as Failure`() = runTest {
        coEvery {
            translationRepository.translate("Hello", Language.ENGLISH, Language.INDONESIAN)
        } returns AppResult.Failure(AppError.ProviderUnavailable)

        val result = useCase("Hello", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
        assertThat((result as AppResult.Failure).error).isEqualTo(AppError.ProviderUnavailable)
    }

    @Test
    fun `network failure is propagated as Failure`() = runTest {
        coEvery {
            translationRepository.translate("Hello", Language.ENGLISH, Language.INDONESIAN)
        } returns AppResult.Failure(AppError.NoInternet)

        val result = useCase("Hello", Language.ENGLISH, Language.INDONESIAN)

        assertThat((result as AppResult.Failure).error).isEqualTo(AppError.NoInternet)
    }
}
