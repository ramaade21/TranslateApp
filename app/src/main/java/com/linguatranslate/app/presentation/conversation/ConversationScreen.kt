package com.linguatranslate.app.presentation.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguatranslate.app.domain.model.ConversationSpeaker
import com.linguatranslate.app.presentation.components.ConversationBubble
import com.linguatranslate.app.presentation.components.ErrorMessage
import com.linguatranslate.app.presentation.components.LanguageSelector
import com.linguatranslate.app.presentation.components.MicrophoneButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(viewModel: ConversationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Conversation") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Person A", style = MaterialTheme.typography.labelLarge)
                    LanguageSelector(
                        label = "Person A language",
                        selected = state.personALanguage,
                        includeAuto = false,
                        onSelected = viewModel::onPersonALanguageSelected,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Person B", style = MaterialTheme.typography.labelLarge)
                    LanguageSelector(
                        label = "Person B language",
                        selected = state.personBLanguage,
                        includeAuto = false,
                        onSelected = viewModel::onPersonBLanguageSelected,
                    )
                }
            }

            Button(
                onClick = { if (state.isActive) viewModel.stopConversation() else viewModel.startConversation() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            ) {
                Text(if (state.isActive) "Stop Conversation" else "Start Conversation")
            }

            state.error?.let { ErrorMessage(message = it) }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                items(state.turns, key = { it.id }) { turn ->
                    ConversationBubble(
                        speaker = turn.speaker,
                        originalText = turn.originalText,
                        translatedText = turn.translatedText,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MicrophoneButton(
                    isListening = state.listeningSpeaker == ConversationSpeaker.PERSON_A,
                    onClick = { viewModel.onSpeakerMicClick(ConversationSpeaker.PERSON_A) },
                )
                MicrophoneButton(
                    isListening = state.listeningSpeaker == ConversationSpeaker.PERSON_B,
                    onClick = { viewModel.onSpeakerMicClick(ConversationSpeaker.PERSON_B) },
                )
            }
        }
    }
}
