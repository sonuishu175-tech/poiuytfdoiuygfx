package com.example.data

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String, val bcp47: String) {
    ENGLISH("en", "English", "English", "en-IN"),
    HINDI("hi", "हिन्दी", "Hindi", "hi-IN"),
    MARATHI("mr", "मराठी", "Marathi", "mr-IN"),
    TAMIL("ta", "தமிழ்", "Tamil", "ta-IN"),
    TELUGU("te", "తెలుగు", "Telugu", "te-IN"),
    BENGALI("bn", "বাংলা", "Bengali", "bn-IN"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "gu-IN");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

data class AppStrings(
    val appTitle: String,
    val tagline: String,
    val welcomeBack: String,
    val askAnythingHint: String,
    val quickAccessTools: String,
    val recentConversations: String,
    val popularTools: String,
    val dailyCreditsTitle: String,
    val watchAdForCredits: String,
    val upgradeToPremium: String,
    val navHome: String,
    val navTools: String,
    val navChat: String,
    val navCredits: String,
    val navProfile: String,
    val generate: String,
    val generating: String,
    val signInGoogle: String,
    val signInPhone: String,
    val logout: String,
    val deleteAccount: String,
    val darkMode: String,
    val notifications: String,
    val languageLabel: String
)

private val EnglishStrings = AppStrings(
    appTitle = "AI ASSISTANT INDIA",
    tagline = "Your Smart AI Partner",
    welcomeBack = "Welcome Back",
    askAnythingHint = "Ask Anything to AI...",
    quickAccessTools = "15 AI Power Tools",
    recentConversations = "Recent Conversations",
    popularTools = "Popular AI Tools",
    dailyCreditsTitle = "Daily Free Credits",
    watchAdForCredits = "Watch Ad (+5 Credits)",
    upgradeToPremium = "Go Premium — Ad-Free & 2,000 Credits",
    navHome = "Home",
    navTools = "AI Tools",
    navChat = "Chat",
    navCredits = "Credits",
    navProfile = "Profile",
    generate = "Generate with AI",
    generating = "Thinking with Gemini...",
    signInGoogle = "Continue with Google",
    signInPhone = "Login with Phone & OTP",
    logout = "Logout",
    deleteAccount = "Delete Account",
    darkMode = "Dark Mode",
    notifications = "Notifications",
    languageLabel = "Language"
)

private val HindiStrings = AppStrings(
    appTitle = "एआई असिस्टेंट इंडिया",
    tagline = "आपका स्मार्ट एआई साथी",
    welcomeBack = "वापसी पर स्वागत है",
    askAnythingHint = "एआई से कुछ भी पूछें...",
    quickAccessTools = "15 एआई पावर टूल्स",
    recentConversations = "हाल की बातचीत",
    popularTools = "लोकप्रिय एआई टूल्स",
    dailyCreditsTitle = "दैनिक मुफ्त क्रेडिट",
    watchAdForCredits = "विज्ञापन देखें (+5 क्रेडिट)",
    upgradeToPremium = "प्रीमियम लें — विज्ञापन-मुक्त और 2,000 क्रेडिट",
    navHome = "होम",
    navTools = "एआई टूल्स",
    navChat = "चैट",
    navCredits = "क्रेडिट",
    navProfile = "प्रोफ़ाइल",
    generate = "एआई से बनाएं",
    generating = "जेमिनी सोच रहा है...",
    signInGoogle = "Google के साथ जारी रखें",
    signInPhone = "फ़ोन और OTP से लॉगिन करें",
    logout = "लॉगआउट",
    deleteAccount = "खाता हटाएं",
    darkMode = "डार्क मोड",
    notifications = "सूचनाएं",
    languageLabel = "भाषा"
)

fun stringsForLanguage(code: String): AppStrings =
    if (code == "hi") HindiStrings else EnglishStrings

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
