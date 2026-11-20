package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_assistant_prefs")

class PreferencesRepository(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val languageKey = stringPreferencesKey("language_code")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[darkModeKey] ?: true
    }

    val languageCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[languageKey] ?: "en"
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[languageKey] = code }
    }
}
