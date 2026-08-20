package com.linguatranslate.app.presentation.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linguatranslate.app.R

@Composable
fun LanguageSwapButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier, shape = CircleShape) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = stringResource(id = R.string.cd_swap_languages),
        )
    }
}

// Local alias to keep imports tidy where stringResource is used from a
// non-Activity Composable context.
@Composable
private fun stringResource(id: Int) = androidx.compose.ui.res.stringResource(id)
