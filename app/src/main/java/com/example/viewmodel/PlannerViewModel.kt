package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.FirestoreRepository
import com.example.model.DailyTask
import com.example.model.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlannerViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val aiRepository: AiRepository,
    private val currentUserId: String
) : ViewModel() {

    val tasksState: StateFlow<UiState<List<DailyTask>>> =
        firestoreRepository.observeTasks(currentUserId)
            .map<List<DailyTask>, UiState<List<DailyTask>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w("PlannerVM", "Error observing tasks", root)
                emit(UiState.Error(root.message ?: "Failed to load tasks"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun addTask(title: String, category: String, reminderTime: String) {
        viewModelScope.launch {
            val res = firestoreRepository.addTask(title, category, reminderTime)
            if (res.isFailure) {
                _error.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to save task"
            }
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            firestoreRepository.toggleTask(taskId, completed)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteTask(taskId)
        }
    }

    fun generateAiPlan(goalPrompt: String, languageCode: String) {
        if (goalPrompt.isBlank() || _isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val creditRes = firestoreRepository.deductCredits(1L, "Daily Planner AI")
                if (creditRes.isFailure) {
                    _error.value = creditRes.exceptionOrNull()?.message
                        ?: "Insufficient credits for AI Planner"
                    return@launch
                }
                val plan = aiRepository.generateDailyPlan(goalPrompt, languageCode)
                _aiAdvice.value = plan.advice
                plan.tasks.forEach { item ->
                    firestoreRepository.addTask(item.title, item.category, item.reminderTime)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = aiRepository.formatFriendlyError(e)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
