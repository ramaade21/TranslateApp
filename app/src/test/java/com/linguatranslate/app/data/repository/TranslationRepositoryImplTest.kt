package com.linguatranslate.app.data.repository

import com.google.common.truth.Truth.assertThat
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.data.remote.api.TranslationApi
import com.linguatranslate.app.data.remote.dto.ApiErrorDto
import com.linguatranslate.app.data.remote.dto.DetectLanguageRequestDto
import com.linguatranslate.app.data.remote.dto.DetectLanguageResponseDto
import com.linguatranslate.app.data.remote.dto.TranslateRequestDto
import com.linguatranslate.app.data.remote.dto.TranslateResponseDto
import com.linguatranslate.app.data.remote.dto.TranslateResultDto
import com.linguatranslate.app.domain.model.Language
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class TranslationRepositoryImplTest {

    private lateinit var api: TranslationApi
    private lateinit var repository: TranslationRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        repository = TranslationRepositoryImpl(api)
    }

    @Test
    fun `successful translation maps to domain model`() = runTest {
        coEvery { api.translate(any()) } returns Response.success(
            TranslateResponseDto(
                success = true,
                data = TranslateResultDto(
                    originalText = "Good morning",
                    translatedText = "Selamat pagi",
                    sourceLanguage = "en",
                    targetLanguage = "id",
                ),
            )
        )

        val result = repository.translate("Good morning", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.translatedText).isEqualTo("Selamat pagi")
    }

    @Test
    fun `http error maps to Failure`() = runTest {
        val errorBody = """{"success":false,"error":{"code":"VALIDATION_ERROR","message":"Text must not be empty."}}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.translate(any()) } returns Response.error(400, errorBody)

        val result = repository.translate("", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
    }

    @Test
    fun `network exception maps to NoInternet`() = runTest {
        coEvery { api.translate(any()) } throws IOException("no network")

        val result = repository.translate("Hello", Language.ENGLISH, Language.INDONESIAN)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
    }

    @Test
    fun `detect language success`() = runTest {
        coEvery { api.detectLanguage(any()) } returns Response.success(
            DetectLanguageResponseDto(success = true, language = "ja")
        )

        val result = repository.detectLanguage("おはよう")

        assertThat((result as AppResult.Success).data).isEqualTo(Language.JAPANESE)
    }
}
