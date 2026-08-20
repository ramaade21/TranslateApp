package com.linguatranslate.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "linguatranslate_settings")

enum class AppTheme { LIGHT, DARK, SYSTEM }

data class SettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val autoSpeakTranslation: Boolean = false,
    val speechRate: Float = 1.0f,
)

/**
 * Persists user preferences (requirement #18/#19). Uses DataStore
 * instead of SharedPreferences, per spec.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak_translation")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
    }

    val settings: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        SettingsState(
            theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.SYSTEM,
            autoSpeakTranslation = prefs[Keys.AUTO_SPEAK] ?: false,
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.0f,
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SPEAK] = enabled }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[Keys.SPEECH_RATE] = rate }
    }
}
