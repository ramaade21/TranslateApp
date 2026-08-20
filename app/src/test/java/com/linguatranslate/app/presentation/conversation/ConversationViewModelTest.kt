package com.linguatranslate.app.presentation.conversation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.linguatranslate.app.core.common.AppResult
import com.linguatranslate.app.core.utils.SpeechRecognitionEvent
import com.linguatranslate.app.core.utils.SpeechToTextService
import com.linguatranslate.app.core.utils.TextToSpeechService
import com.linguatranslate.app.domain.model.ConversationSpeaker
import com.linguatranslate.app.domain.model.Language
import com.linguatranslate.app.domain.model.TranslationResult
import com.linguatranslate.app.domain.usecase.TranslateTextUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var translateTextUseCase: TranslateTextUseCase
    private lateinit var speechToTextService: SpeechToTextService
    private lateinit var textToSpeechService: TextToSpeechService
    private lateinit var viewModel: ConversationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        translateTextUseCase = mockk()
        speechToTextService = mockk()
        textToSpeechService = mockk(relaxed = true)
        viewModel = ConversationViewModel(translateTextUseCase, speechToTextService, textToSpeechService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starting conversation sets isActive true`() = runTest {
        viewModel.startConversation()
        assertThat(viewModel.uiState.value.isActive).isTrue()
    }

    @Test
    fun `stopping conversation clears listening speaker`() = runTest {
        viewModel.startConversation()
        viewModel.stopConversation()
        assertThat(viewModel.uiState.value.isActive).isFalse()
        assertThat(viewModel.uiState.value.listeningSpeaker).isNull()
    }

    @Test
    fun `person A speech is translated to person B language and appended as a turn`() = runTest {
        viewModel.startConversation()

        coEvery { speechToTextService.listen(Language.ENGLISH) } returns flowOf(
            SpeechRecognitionEvent.ListeningStarted,
            SpeechRecognitionEvent.FinalResult("Where are you going?"),
            SpeechRecognitionEvent.Done,
        )
        coEvery {
            translateTextUseCase("Where are you going?", Language.ENGLISH, Language.INDONESIAN, recordHistory = false)
        } returns AppResult.Success(
            TranslationResult(
                originalText = "Where are you going?",
                translatedText = "Kamu mau pergi ke mana?",
                sourceLanguage = Language.ENGLISH,
                targetLanguage = Language.INDONESIAN,
            )
        )

        viewModel.onSpeakerMicClick(ConversationSpeaker.PERSON_A)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.turns).hasSize(1)
        assertThat(state.turns.first().translatedText).isEqualTo("Kamu mau pergi ke mana?")
        assertThat(state.turns.first().speaker).isEqualTo(ConversationSpeaker.PERSON_A)
    }

    @Test
    fun `mic click is ignored when conversation is not active`() = runTest {
        viewModel.onSpeakerMicClick(ConversationSpeaker.PERSON_A)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.turns).isEmpty()
    }
}
