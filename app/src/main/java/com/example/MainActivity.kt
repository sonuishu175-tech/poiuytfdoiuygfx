package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocalAppStrings
import com.example.data.PreferencesRepository
import com.example.data.configureAppCheck
import com.example.data.stringsForLanguage
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AiAssistantIndiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase App Check BEFORE setContent / ViewModels / Firestore operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val preferencesRepository = remember { PreferencesRepository(applicationContext) }
            val isDarkMode by preferencesRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = true)
            val languageCode by preferencesRepository.languageCode.collectAsStateWithLifecycle(initialValue = "en")

            AiAssistantIndiaTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalAppStrings provides stringsForLanguage(languageCode)) {
                    AppNavigation(
                        preferencesRepository = preferencesRepository,
                        isDarkMode = isDarkMode,
                        languageCode = languageCode
                    )
                }
            }
        }
    }
}
