package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreRepository
import com.example.data.PreferencesRepository
import com.example.model.AppConfig
import com.example.model.AppNotification
import com.example.model.ChatSession
import com.example.model.CreditTransaction
import com.example.model.UiState
import com.example.model.UserFileRecord
import com.example.model.UserProfile
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

class MainViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val preferencesRepository: PreferencesRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _actionBanner = MutableStateFlow<String?>(null)
    val actionBanner: StateFlow<String?> = _actionBanner.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        firestoreRepository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing user profile", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load profile"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val recentChats: StateFlow<List<ChatSession>> =
        firestoreRepository.observeChats(currentUserId)
            .catch { error ->
                unwrapCancelOrLog(error, "chats")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val creditTransactions: StateFlow<List<CreditTransaction>> =
        firestoreRepository.observeCreditTransactions(currentUserId)
            .catch { error ->
                unwrapCancelOrLog(error, "transactions")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<AppNotification>> =
        firestoreRepository.observeNotifications(currentUserId)
            .catch { error ->
                unwrapCancelOrLog(error, "notifications")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userFiles: StateFlow<List<UserFileRecord>> =
        firestoreRepository.observeUserFiles(currentUserId)
            .catch { error ->
                unwrapCancelOrLog(error, "userFiles")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val appConfig: StateFlow<AppConfig> =
        firestoreRepository.observeAppConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppConfig())

    init {
        viewModelScope.launch {
            firestoreRepository.ensureUserProfile()
        }
    }

    fun updateLanguage(code: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguageCode(code)
            firestoreRepository.updateProfilePreferences(language = code)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
            firestoreRepository.updateProfilePreferences(darkMode = enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            firestoreRepository.updateProfilePreferences(notificationsEnabled = enabled)
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            val res = firestoreRepository.updateProfilePreferences(name = name)
            _actionBanner.value = if (res.isSuccess) "Profile updated" else res.exceptionOrNull()?.message
        }
    }

    fun claimDailyFreeCredits() {
        viewModelScope.launch {
            val res = firestoreRepository.claimDailyFreeCredits()
            _actionBanner.value = if (res.isSuccess) {
                "Daily Free Credits refreshed to 20!"
            } else {
                res.exceptionOrNull()?.message ?: "Unable to claim daily credits"
            }
        }
    }

    fun onRewardedAdEarned() {
        viewModelScope.launch {
            val res = firestoreRepository.grantRewardedAdCredits()
            _actionBanner.value = if (res.isSuccess) {
                "+5 AI Credits added for watching ad!"
            } else {
                res.exceptionOrNull()?.message ?: "Could not add reward credits"
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            firestoreRepository.markNotificationRead(notificationId)
        }
    }

    fun saveFileSummary(fileName: String, storagePath: String, sizeBytes: Long, summary: String?) {
        viewModelScope.launch {
            firestoreRepository.saveUserFileRecord(fileName, storagePath, sizeBytes, summary)
        }
    }

    fun deleteFileRecord(fileId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteUserFileRecord(fileId)
        }
    }

    fun clearBanner() {
        _actionBanner.value = null
    }

    private fun unwrapCancelOrLog(error: Throwable, label: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing $label", firestoreError ?: error)
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
