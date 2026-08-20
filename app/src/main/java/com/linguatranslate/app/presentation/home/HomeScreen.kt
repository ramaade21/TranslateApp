package com.linguatranslate.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguatranslate.app.presentation.components.ErrorMessage
import com.linguatranslate.app.presentation.components.LanguageSelector
import com.linguatranslate.app.presentation.components.LanguageSwapButton
import com.linguatranslate.app.presentation.components.LoadingIndicator
import com.linguatranslate.app.presentation.components.TranslationInputCard
import com.linguatranslate.app.presentation.components.TranslationResultCard

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "LinguaTranslate", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Translate anything, anywhere",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(text = "FROM", style = MaterialTheme.typography.labelLarge)
        LanguageSelector(
            label = "Source language",
            selected = state.sourceLanguage,
            includeAuto = true,
            onSelected = viewModel::onSourceLanguageSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LanguageSwapButton(onClick = viewModel::onSwapLanguages)
        }

        Text(text = "TO", style = MaterialTheme.typography.labelLarge)
        LanguageSelector(
            label = "Target language",
            selected = state.targetLanguage,
            includeAuto = false,
            onSelected = viewModel::onTargetLanguageSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        TranslationInputCard(
            text = state.inputText,
            onTextChange = viewModel::onInputTextChange,
            isListening = state.isListening,
            onMicClick = viewModel::onMicClick,
        )

        Button(
            onClick = viewModel::translate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isTranslating,
        ) {
            Text(text = "Translate")
        }

        when {
            state.isTranslating -> LoadingIndicator(label = "Translating…")
            state.error != null -> ErrorMessage(message = state.error.orEmpty())
            state.translatedText.isNotBlank() -> TranslationResultCard(
                originalText = state.inputText,
                translatedText = state.translatedText,
                detectedLanguage = state.detectedLanguage,
                isFavorite = state.isFavorite,
                onSpeak = viewModel::onSpeakResult,
                onCopy = { clipboardManager.setText(AnnotatedString(state.translatedText)) },
                onToggleFavorite = viewModel::onToggleFavorite,
                onShare = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, state.translatedText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
            )
        }
    }
}
