package com.example.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.FirestoreRepository
import com.example.model.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun runTextTool(
        featureName: String,
        creditCost: Long,
        prompt: String,
        systemInstruction: String = "You are AI Assistant India. Provide structured, accurate, and practical output.",
        useFastModel: Boolean = false
    ) {
        if (prompt.isBlank()) {
            _uiState.value = UiState.Error("Please enter your input before generating.")
            return
        }
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val creditRes = firestoreRepository.deductCredits(creditCost, featureName)
                if (creditRes.isFailure) {
                    _uiState.value = UiState.Error(
                        creditRes.exceptionOrNull()?.message
                            ?: "Insufficient AI credits. Watch a rewarded ad or upgrade your plan."
                    )
                    return@launch
                }

                var finalOutput = ""
                aiRepository.streamText(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    useFastModel = useFastModel
                ).collect { accumulated ->
                    finalOutput = accumulated
                    _uiState.value = UiState.Streaming(accumulated)
                }
                _uiState.value = UiState.Success(finalOutput)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(aiRepository.formatFriendlyError(e))
            }
        }
    }

    fun runVisionTool(
        featureName: String,
        creditCost: Long,
        prompt: String,
        images: List<Bitmap>,
        systemHint: String = ""
    ) {
        if (images.isEmpty()) {
            _uiState.value = UiState.Error("Please select or capture an image first.")
            return
        }
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val creditRes = firestoreRepository.deductCredits(creditCost, featureName)
                if (creditRes.isFailure) {
                    _uiState.value = UiState.Error(
                        creditRes.exceptionOrNull()?.message
                            ?: "Insufficient AI credits. Watch a rewarded ad or upgrade your plan."
                    )
                    return@launch
                }

                var finalOutput = ""
                aiRepository.streamMultimodal(
                    prompt = prompt.ifBlank { "Analyze this image in detail." },
                    images = images,
                    systemHint = systemHint
                ).collect { accumulated ->
                    finalOutput = accumulated
                    _uiState.value = UiState.Streaming(accumulated)
                }
                _uiState.value = UiState.Success(finalOutput)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(aiRepository.formatFriendlyError(e))
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        activeJob = null
        val current = _uiState.value
        if (current is UiState.Streaming && current.text.isNotBlank()) {
            _uiState.value = UiState.Success(current.text)
        } else if (current is UiState.Loading) {
            _uiState.value = UiState.Idle
        }
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = UiState.Idle
    }
}
