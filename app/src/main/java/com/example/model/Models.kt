package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val name: String = "User",
    val email: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val language: String = "en",
    val credits: Long = 20L,
    val membership: String = "free",
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val lastDailyCreditDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatSession(
    @DocumentId val chatId: String = "",
    val uid: String = "",
    val title: String = "New Chat",
    val toolId: String = "ai_chat",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val messageId: String = "",
    val chatId: String = "",
    val uid: String = "",
    val role: String = "user",
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CreditTransaction(
    @DocumentId val transactionId: String = "",
    val uid: String = "",
    val amount: Long = 0L,
    val type: String = "ai_usage",
    val feature: String = "AI Chat",
    val status: String = "completed",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SubscriptionRecord(
    @DocumentId val id: String = "",
    val uid: String = "",
    val planId: String = "free",
    val status: String = "active",
    val expiryDate: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserFileRecord(
    @DocumentId val fileId: String = "",
    val uid: String = "",
    val fileName: String = "",
    val storagePath: String = "",
    val sizeBytes: Long = 0L,
    val summary: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DailyTask(
    @DocumentId val taskId: String = "",
    val uid: String = "",
    val title: String = "",
    val category: String = "Work",
    val completed: Boolean = false,
    val reminderTime: String = "09:00 AM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val notificationId: String = "",
    val uid: String = "",
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    @ServerTimestamp val timestamp: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppConfig(
    val activeModel: String = "gemini-3.8-flash",
    val maintenanceMode: Boolean = false,
    val chatCreditCost: Long = 1L,
    val visionCreditCost: Long = 2L,
    val documentCreditCost: Long = 2L
)

enum class AiToolType(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val subtitleEn: String,
    val subtitleHi: String,
    val creditCost: Long,
    val category: String,
    val popular: Boolean = false
) {
    AI_CHAT("ai_chat", "AI Chat", "एआई चैट", "Smart multilingual chat", "स्मार्ट बहुभाषी चैट", 1L, "Core", true),
    IMAGE_ANALYZER("image_analyzer", "Image Analyzer", "इमेज विश्लेषक", "Vision & photo Q&A", "फोटो और दृश्य प्रश्नोत्तर", 2L, "Vision", true),
    VOICE_ASSISTANT("voice_assistant", "Voice Assistant", "वॉइस असिस्टेंट", "Hindi & English voice AI", "हिंदी और अंग्रेजी वॉइस एआई", 1L, "Core", true),
    TRANSLATOR("translator", "AI Translator", "एआई अनुवादक", "50+ languages & speech", "50+ भाषाएं और उच्चारण", 1L, "Language", true),
    CONTENT_WRITER("content_writer", "Content Writer", "कंटेंट राइटर", "Blogs, captions & ads", "ब्लॉग, कैप्शन और पोस्ट", 1L, "Creator", true),
    HOMEWORK_HELPER("homework_helper", "Homework Helper", "होमवर्क हेल्पर", "Math, Science & NEET", "गणित, विज्ञान और NEET", 2L, "Study", true),
    EMAIL_WRITER("email_writer", "Email & Letters", "ईमेल और पत्र", "Leave, job & govt letters", "छुट्टी, नौकरी और आवेदन", 1L, "Writing", false),
    STORY_GENERATOR("story_generator", "Story Generator", "कहानी लेखक", "Hindi & English stories", "रोचक कहानियां और स्क्रिप्ट", 1L, "Creator", false),
    CODING_ASSISTANT("coding_assistant", "Coding Assistant", "कोडिंग असिस्टेंट", "Kotlin, Python, Java & C++", "कोड लिखें और डीबग करें", 2L, "Tech", true),
    RESUME_MAKER("resume_maker", "Resume Maker", "रिज्यूमे मेकर", "AI CV & PDF export", "प्रोफेशनल रिज्यूमे और PDF", 2L, "Career", false),
    PDF_ASSISTANT("pdf_assistant", "PDF Assistant", "पीडीएफ सहायक", "Summarize & ask PDFs", "पीडीएफ सारांश और प्रश्न", 2L, "Productivity", true),
    YOUTUBE_CREATOR("youtube_creator", "YouTube Creator", "यूट्यूब क्रिएटर", "Titles, scripts & shorts", "टाइटल, स्क्रिप्ट और शॉर्ट्स", 1L, "Creator", false),
    BUSINESS_AI("business_ai", "Business AI", "बिजनेस एआई", "Startup ideas & plans", "बिजनेस आइडिया और प्लान", 1L, "Business", false),
    TEXT_SUMMARIZER("text_summarizer", "Text Summarizer", "टेक्स्ट सारांश", "Summarize & fix grammar", "सारांश और व्याकरण सुधार", 1L, "Writing", false),
    DAILY_PLANNER("daily_planner", "Daily Planner", "दैनिक प्लानर", "AI schedule & reminders", "दैनिक कार्य और टाइमटेबल", 1L, "Productivity", false)
}

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Streaming(val text: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
