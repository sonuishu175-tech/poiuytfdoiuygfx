package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.FirestoreRepository
import com.example.model.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val aiRepository: AiRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var activeJob: Job? = null

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _activeChatId
        .flatMapLatest { chatId ->
            if (chatId.isNullOrBlank()) flowOf(emptyList())
            else firestoreRepository.observeMessages(currentUserId, chatId)
                .catch { error ->
                    if (error is CancellationException && error.cause == null) throw error
                    _errorMessage.value = error.localizedMessage ?: "Failed to load messages"
                    emit(emptyList())
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun selectChat(chatId: String?) {
        stopGeneration()
        _activeChatId.value = chatId
        _errorMessage.value = null
    }

    fun startNewChat() {
        stopGeneration()
        _activeChatId.value = null
        _streamingText.value = ""
        _errorMessage.value = null
    }

    fun sendMessage(prompt: String, languageCode: String, creditCost: Long = 1L) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() || _isGenerating.value) return

        activeJob = viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            _streamingText.value = ""
            var targetChatId = _activeChatId.value

            try {
                val creditResult = firestoreRepository.deductCredits(creditCost, "AI Chat")
                if (creditResult.isFailure) {
                    _errorMessage.value = creditResult.exceptionOrNull()?.message
                        ?: "Insufficient credits. Watch an ad or upgrade to continue."
                    return@launch
                }

                if (targetChatId == null) {
                    val created = firestoreRepository.createChat(
                        title = cleanPrompt.take(45),
                        toolId = "ai_chat"
                    )
                    targetChatId = created.getOrElse {
                        _errorMessage.value = it.localizedMessage ?: "Could not create chat"
                        return@launch
                    }
                    _activeChatId.value = targetChatId
                } else {
                    firestoreRepository.updateChatTitle(targetChatId, cleanPrompt.take(45))
                }

                val historyPairs = messages.value.map { it.role to it.content }
                firestoreRepository.addMessage(targetChatId, "user", cleanPrompt)

                val langHint = when (languageCode) {
                    "hi" -> "Respond naturally in Hindi (Devanagari script) unless the user asks for English."
                    else -> "Respond clearly and concisely. If the user writes in Hindi or Hinglish, reply appropriately."
                }
                val sysInstruction = "You are AI Assistant India, a helpful, accurate AI companion. $langHint"

                var latestResponse = ""
                aiRepository.streamText(
                    prompt = cleanPrompt,
                    systemInstruction = sysInstruction,
                    history = historyPairs
                ).collect { chunk ->
                    latestResponse = chunk
                    _streamingText.value = chunk
                }

                if (latestResponse.isNotBlank()) {
                    firestoreRepository.addMessage(targetChatId, "model", latestResponse)
                }
            } catch (e: CancellationException) {
                val partial = _streamingText.value
                if (partial.isNotBlank() && targetChatId != null) {
                    firestoreRepository.addMessage(targetChatId, "model", "$partial\n\n[Stopped]")
                }
                throw e
            } catch (e: Exception) {
                _errorMessage.value = aiRepository.formatFriendlyError(e)
            } finally {
                _streamingText.value = ""
                _isGenerating.value = false
            }
        }
    }

    fun regenerateLastAnswer(languageCode: String) {
        val lastUserMsg = messages.value.lastOrNull { it.role == "user" }?.content ?: return
        sendMessage(lastUserMsg, languageCode)
    }

    fun stopGeneration() {
        activeJob?.cancel()
        activeJob = null
        _isGenerating.value = false
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            val res = firestoreRepository.deleteChat(chatId)
            if (_activeChatId.value == chatId && res.isSuccess) {
                _activeChatId.value = null
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
