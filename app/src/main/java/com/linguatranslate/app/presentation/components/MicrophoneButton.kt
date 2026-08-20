package com.linguatranslate.app.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.linguatranslate.app.R

/**
 * Microphone toggle button. Pulses gently while [isListening] is true.
 * Kept intentionally lightweight per spec ("jangan membuat animasi
 * terlalu berat").
 */
@Composable
fun MicrophoneButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic_scale",
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer(scaleX = if (isListening) scale else 1f, scaleY = if (isListening) scale else 1f),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
            contentDescription = stringResource(id = R.string.cd_microphone),
            tint = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
