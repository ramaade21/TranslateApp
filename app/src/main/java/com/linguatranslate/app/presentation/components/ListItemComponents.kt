package com.linguatranslate.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.linguatranslate.app.R
import com.linguatranslate.app.domain.model.ConversationSpeaker
import com.linguatranslate.app.domain.model.Language
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryItem(
    originalText: String,
    translatedText: String,
    sourceLanguage: Language,
    targetLanguage: Language,
    createdAt: Long,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = originalText, style = MaterialTheme.typography.bodyMedium)
            Text(text = translatedText, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${sourceLanguage.displayName} → ${targetLanguage.displayName} · ${formatDate(createdAt)}",
                style = MaterialTheme.typography.labelMedium,
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Default.VolumeUp, contentDescription = stringResource(id = R.string.cd_speaker))
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(id = R.string.cd_copy))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.cd_delete))
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(
    originalText: String,
    translatedText: String,
    sourceLanguage: Language,
    targetLanguage: Language,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = originalText, style = MaterialTheme.typography.bodyMedium)
            Text(text = translatedText, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${sourceLanguage.displayName} → ${targetLanguage.displayName}",
                style = MaterialTheme.typography.labelMedium,
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Default.VolumeUp, contentDescription = stringResource(id = R.string.cd_speaker))
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(id = R.string.cd_copy))
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.cd_delete))
                }
            }
        }
    }
}

@Composable
fun ConversationBubble(
    speaker: ConversationSpeaker,
    originalText: String,
    translatedText: String,
    modifier: Modifier = Modifier,
) {
    val isPersonA = speaker == ConversationSpeaker.PERSON_A
    val alignment = if (isPersonA) Alignment.Start else Alignment.End
    val bubbleColor = if (isPersonA) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = if (isPersonA) Alignment.CenterStart else Alignment.CenterEnd) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = originalText, style = MaterialTheme.typography.bodyMedium)
                Text(text = translatedText, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
