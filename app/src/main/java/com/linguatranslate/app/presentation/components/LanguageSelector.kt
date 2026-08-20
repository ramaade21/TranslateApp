package com.linguatranslate.app.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.linguatranslate.app.domain.model.Language

/**
 * Dropdown selector for choosing a language. [includeAuto] controls
 * whether "Auto Detect" appears in the list (only valid for source).
 */
@Composable
fun LanguageSelector(
    label: String,
    selected: Language,
    includeAuto: Boolean,
    onSelected: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = if (includeAuto) {
        listOf(Language.AUTO) + Language.translatableLanguages
    } else {
        Language.translatableLanguages
    }

    OutlinedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "${selected.flagEmoji}  ${selected.displayName}", style = MaterialTheme.typography.bodyLarge)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = label)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { language ->
                DropdownMenuItem(
                    text = { Text("${language.flagEmoji}  ${language.displayName}") },
                    onClick = {
                        onSelected(language)
                        expanded = false
                    },
                )
            }
        }
    }
}
