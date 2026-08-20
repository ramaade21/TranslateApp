package com.linguatranslate.app.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.linguatranslate.app.data.local.dao.HistoryDao
import com.linguatranslate.app.data.local.entity.TranslationHistoryEntity
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HistoryRepositoryImplTest {

    private lateinit var dao: HistoryDao
    private lateinit var repository: HistoryRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = HistoryRepositoryImpl(dao)
    }

    @Test
    fun `observeHistory maps entities to domain models`() = runTest {
        every { dao.observeAll() } returns flowOf(
            listOf(
                TranslationHistoryEntity(
                    id = 1,
                    originalText = "Good morning",
                    translatedText = "Selamat pagi",
                    sourceLanguage = "en",
                    targetLanguage = "id",
                    detectedLanguage = null,
                    createdAt = 1000L,
                )
            )
        )

        repository.observeHistory().test {
            val entries = awaitItem()
            assertThat(entries).hasSize(1)
            assertThat(entries.first().translatedText).isEqualTo("Selamat pagi")
            assertThat(entries.first().sourceLanguage).isEqualTo(Language.ENGLISH)
            awaitComplete()
        }
    }

    @Test
    fun `addEntry persists mapped entity`() = runTest {
        val result = TranslationResult(
            originalText = "Good morning",
            translatedText = "Selamat pagi",
            sourceLanguage = Language.ENGLISH,
            targetLanguage = Language.INDONESIAN,
        )

        repository.addEntry(result)

        coVerify {
            dao.insert(
                match {
                    it.originalText == "Good morning" && it.translatedText == "Selamat pagi" &&
                        it.sourceLanguage == "en" && it.targetLanguage == "id"
                }
            )
        }
    }

    @Test
    fun `deleteEntry delegates to dao`() = runTest {
        repository.deleteEntry(5L)
        coVerify { dao.deleteById(5L) }
    }

    @Test
    fun `clearAll delegates to dao`() = runTest {
        repository.clearAll()
        coVerify { dao.clearAll() }
    }
}
