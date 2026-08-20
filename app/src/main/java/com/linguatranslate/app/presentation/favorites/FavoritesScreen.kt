package com.linguatranslate.app.presentation.favorites

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguatranslate.app.presentation.components.EmptyState
import com.linguatranslate.app.presentation.components.FavoriteItem

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Favorites") }) }) { padding ->
        if (favorites.isEmpty()) {
            EmptyState(message = "No favorites yet.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favorites, key = { it.id }) { entry ->
                    FavoriteItem(
                        originalText = entry.originalText,
                        translatedText = entry.translatedText,
                        sourceLanguage = entry.sourceLanguage,
                        targetLanguage = entry.targetLanguage,
                        onCopy = { clipboardManager.setText(AnnotatedString(entry.translatedText)) },
                        onSpeak = { viewModel.speak(entry) },
                        onShare = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, entry.translatedText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        onRemove = { viewModel.remove(entry.id) },
                    )
                }
            }
        }
    }
}
