package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

class AiRepository {
    companion object {
        private const val TAG = "AiRepository"
        const val DEFAULT_MODEL = "gemini-3.8-flash"
        const val FAST_MODEL = "gemini-3.5-flash-lite"
        private const val TIMEOUT_MS = 60_000L
    }

    private val defaultModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(DEFAULT_MODEL)
    }

    private val fastModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(FAST_MODEL)
    }

    private val plannerModel by lazy {
        val taskSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Actionable task or study block title"),
                "category" to Schema.string(description = "Category like Study, Work, Health, or Goal"),
                "reminderTime" to Schema.string(description = "Time label like 07:00 AM")
            )
        )
        val planSchema = Schema.obj(
            properties = mapOf(
                "tasks" to Schema.array(
                    items = taskSchema,
                    description = "Ordered list of daily tasks"
                ),
                "advice" to Schema.string(description = "Short motivational productivity tip")
            )
        )
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = planSchema
            }
        )
    }

    fun streamText(
        prompt: String,
        systemInstruction: String = "You are AI Assistant India, a helpful, accurate, culturally aware AI assistant supporting Hindi, English, and Indian languages.",
        history: List<Pair<String, String>> = emptyList(),
        useFastModel: Boolean = false
    ): Flow<String> = flow {
        val model = if (useFastModel) fastModel else defaultModel
        val fullContent = buildList {
            if (history.isEmpty()) {
                add(content("user") { text("$systemInstruction\n\n$prompt") })
            } else {
                add(content("user") { text(systemInstruction) })
                add(content("model") { text("Understood. I am AI Assistant India and will assist clearly and accurately.") })
                history.takeLast(12).forEach { (role, text) ->
                    add(content(role) { text(text) })
                }
                add(content("user") { text(prompt) })
            }
        }

        val accumulated = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            model.generateContentStream(fullContent).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI returned an empty response.")
        }
    }

    suspend fun generateText(
        prompt: String,
        useFastModel: Boolean = false
    ): String {
        val model = if (useFastModel) fastModel else defaultModel
        val response = withTimeout(TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        return response.text?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("AI returned an empty response.")
    }

    fun streamMultimodal(
        prompt: String,
        images: List<Bitmap>,
        systemHint: String = ""
    ): Flow<String> = flow {
        val scaledImages = images.map { scaleBitmapForAi(it) }
        val inputContent: Content = content {
            if (systemHint.isNotBlank()) text(systemHint)
            scaledImages.forEach { bitmap -> image(bitmap) }
            text(prompt)
        }
        val accumulated = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            defaultModel.generateContentStream(inputContent).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI vision model returned an empty response.")
        }
    }

    suspend fun generateDailyPlan(goalPrompt: String, language: String): GeneratedPlan {
        val prompt = buildString {
            append("Create a realistic daily timetable/task plan of 4 to 6 tasks for: $goalPrompt. ")
            append(if (language == "hi") "Write task titles and advice in Hindi." else "Write in clear English.")
        }
        val response = withTimeout(TIMEOUT_MS) {
            plannerModel.generateContent(prompt)
        }
        val jsonText = response.text ?: throw IllegalStateException("Empty planner response")
        val json = JSONObject(jsonText)
        val tasksJson: JSONArray = json.optJSONArray("tasks") ?: JSONArray()
        val tasks = (0 until tasksJson.length()).map { idx ->
            val obj = tasksJson.getJSONObject(idx)
            GeneratedTaskItem(
                title = obj.optString("title", "Focus Task ${idx + 1}"),
                category = obj.optString("category", "Work"),
                reminderTime = obj.optString("reminderTime", "09:00 AM")
            )
        }
        val advice = json.optString("advice", "Stay consistent and take short breaks.")
        return GeneratedPlan(tasks = tasks, advice = advice)
    }

    private fun scaleBitmapForAi(bitmap: Bitmap, maxDim: Int = 1280): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        val targetW = (w * ratio).toInt().coerceAtLeast(1)
        val targetH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    fun formatFriendlyError(e: Throwable): String {
        Log.e(TAG, "AI error", e)
        return when (e) {
            is TimeoutCancellationException -> "Request timed out after 60 seconds. Please check your connection and try again."
            is InsufficientCreditsException -> e.message ?: "Insufficient AI credits. Watch an ad or upgrade to continue."
            else -> e.localizedMessage ?: "AI service encountered an error. Please try again."
        }
    }
}

data class GeneratedTaskItem(
    val title: String,
    val category: String,
    val reminderTime: String
)

data class GeneratedPlan(
    val tasks: List<GeneratedTaskItem>,
    val advice: String
)
