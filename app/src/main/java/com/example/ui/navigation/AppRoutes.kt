package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Tools : AppRoute
    @Serializable data class Chat(val initialPrompt: String? = null, val chatId: String? = null) : AppRoute
    @Serializable data object Credits : AppRoute
    @Serializable data object Profile : AppRoute
    @Serializable data object Subscriptions : AppRoute
    @Serializable data object Notifications : AppRoute

    // Dedicated screens for all remaining AI features (Chat is Feature 1)
    @Serializable data object ImageAnalyzer : AppRoute
    @Serializable data object VoiceAssistant : AppRoute
    @Serializable data object Translator : AppRoute
    @Serializable data object ContentWriter : AppRoute
    @Serializable data object HomeworkHelper : AppRoute
    @Serializable data object EmailWriter : AppRoute
    @Serializable data object StoryGenerator : AppRoute
    @Serializable data object CodingAssistant : AppRoute
    @Serializable data object ResumeMaker : AppRoute
    @Serializable data object PdfAssistant : AppRoute
    @Serializable data object YouTubeCreator : AppRoute
    @Serializable data object BusinessAi : AppRoute
    @Serializable data object TextSummarizer : AppRoute
    @Serializable data object DailyPlanner : AppRoute
}
