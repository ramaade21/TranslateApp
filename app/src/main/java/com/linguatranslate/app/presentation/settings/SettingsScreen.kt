package com.linguatranslate.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguatranslate.app.BuildConfig
import com.linguatranslate.app.data.repository.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingsSection(title = "Appearance") {
                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = settings.theme == theme,
                            onClick = { viewModel.setTheme(theme) },
                        )
                        Text(text = theme.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            SettingsSection(title = "Voice") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Auto Speak Translation")
                    Switch(
                        checked = settings.autoSpeakTranslation,
                        onCheckedChange = viewModel::setAutoSpeak,
                    )
                }
                Text("Speech Rate: ${String.format("%.1fx", settings.speechRate)}")
                Slider(
                    value = settings.speechRate,
                    onValueChange = viewModel::setSpeechRate,
                    valueRange = 0.5f..2.0f,
                )
            }

            SettingsSection(title = "About") {
                Text("Version ${BuildConfig.VERSION_NAME}")
                Text("Privacy Policy")
                Text("Open Source Licenses")
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        content()
    }
}
