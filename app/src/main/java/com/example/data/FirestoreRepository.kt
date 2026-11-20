package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AppConfig
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.model.CreditTransaction
import com.example.model.DailyTask
import com.example.model.UserFileRecord
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    suspend fun ensureUserProfile(preferredLanguage: String = "en", darkMode: Boolean = true): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val path = "users/${user.uid}"
        return try {
            val docRef = db.collection("users").document(user.uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val safeName = (user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.phoneNumber?.takeIf { it.isNotBlank() }
                    ?: "AI Explorer").take(100)
                val payload = buildMap<String, Any> {
                    put("uid", user.uid)
                    put("name", safeName)
                    user.email?.takeIf { it.isNotBlank() }?.let { put("email", it.take(200)) }
                    user.phoneNumber?.takeIf { it.isNotBlank() }?.let { put("phone", it.take(30)) }
                    user.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.let { put("photoUrl", it.take(1000)) }
                    put("language", preferredLanguage.takeIf { it in listOf("en", "hi") } ?: "en")
                    put("credits", 20L)
                    put("membership", "free")
                    put("darkMode", darkMode)
                    put("notificationsEnabled", true)
                    put("lastDailyCreditDate", todayDateString())
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(payload).await()
                logCreditTransaction(user.uid, 20L, "daily_free", "Welcome Daily Free Credits")
                createWelcomeNotification(user.uid)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private suspend fun createWelcomeNotification(uid: String) {
        val ref = db.collection("notifications").document()
        val payload = mapOf(
            "uid" to uid,
            "title" to "Welcome to AI Assistant India",
            "message" to "20 Daily Free AI Credits added to your account. Explore all 15 AI tools!",
            "read" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )
        runCatching { ref.set(payload).await() }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeAppConfig(): Flow<AppConfig> = flow {
        val path = "appConfig/global"
        emitAll(
            db.collection("appConfig").document("global")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(AppConfig::class.java) ?: AppConfig()
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(AppConfig())
                }
        )
    }

    suspend fun updateProfilePreferences(
        name: String? = null,
        language: String? = null,
        darkMode: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            name?.trim()?.takeIf { it.isNotBlank() }?.let { updates["name"] = it.take(100) }
            language?.takeIf { it in listOf("en", "hi", "mr", "ta", "te", "bn", "gu") }?.let { updates["language"] = it }
            darkMode?.let { updates["darkMode"] = it }
            notificationsEnabled?.let { updates["notificationsEnabled"] = it }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deductCredits(amount: Long, featureName: String): Result<Long> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(amount in 1L..10L) { "Credit deduction must be between 1 and 10" }
            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val newBalance = db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentCredits = snap.getLong("credits") ?: 0L
                if (currentCredits < amount) {
                    throw InsufficientCreditsException("Need $amount credits, available: $currentCredits")
                }
                val updated = currentCredits - amount
                tx.update(
                    userRef,
                    mapOf(
                        "credits" to updated,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                updated
            }.await()
            logCreditTransaction(uid, -amount, "ai_usage", featureName.take(100))
            Result.success(newBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: InsufficientCreditsException) {
            Result.failure(e)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun grantRewardedAdCredits(): Result<Long> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val newBalance = db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentCredits = snap.getLong("credits") ?: 0L
                val updated = currentCredits + 5L
                tx.update(
                    userRef,
                    mapOf(
                        "credits" to updated,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                updated
            }.await()
            logCreditTransaction(uid, 5L, "reward_ad", "Rewarded Ad Bonus")
            Result.success(newBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun claimDailyFreeCredits(): Result<Long> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val today = todayDateString()
            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val newBalance = db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentCredits = snap.getLong("credits") ?: 0L
                val lastClaim = snap.getString("lastDailyCreditDate") ?: ""
                if (lastClaim == today) {
                    throw IllegalStateException("Today's daily free credits already claimed.")
                }
                if (currentCredits >= 20L) {
                    throw IllegalStateException("Your balance is already 20 or higher.")
                }
                tx.update(
                    userRef,
                    mapOf(
                        "credits" to 20L,
                        "lastDailyCreditDate" to today,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                20L
            }.await()
            logCreditTransaction(uid, 20L, "daily_free", "Daily Free Refresh")
            Result.success(newBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun logCreditTransaction(uid: String, amount: Long, type: String, feature: String) {
        val txRef = db.collection("creditTransactions").document()
        val payload = mapOf(
            "uid" to uid,
            "amount" to amount,
            "type" to type,
            "feature" to feature,
            "status" to "completed",
            "timestamp" to FieldValue.serverTimestamp()
        )
        runCatching { txRef.set(payload).await() }
    }

    fun observeCreditTransactions(uid: String): Flow<List<CreditTransaction>> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "creditTransactions"
        emitAll(
            db.collection(path)
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { it.toObjects(CreditTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeChats(uid: String): Flow<List<ChatSession>> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "chats"
        emitAll(
            db.collection(path)
                .whereEqualTo("uid", uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { it.toObjects(ChatSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createChat(title: String, toolId: String = "ai_chat"): Result<String> {
        var path = "chats"
        return try {
            val uid = requireUserId()
            val ref = db.collection("chats").document()
            path = ref.path
            val payload = mapOf(
                "uid" to uid,
                "title" to title.trim().ifBlank { "New Conversation" }.take(160),
                "toolId" to toolId.take(60),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateChatTitle(chatId: String, title: String): Result<Unit> {
        var path = "chats/$chatId"
        return try {
            requireUserId()
            require(chatId.isNotBlank()) { "chatId must not be blank" }
            val ref = db.collection("chats").document(chatId)
            path = ref.path
            ref.update(
                mapOf(
                    "title" to title.trim().ifBlank { "AI Chat" }.take(160),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        var path = "chats/$chatId"
        return try {
            val uid = requireUserId()
            require(chatId.isNotBlank()) { "chatId must not be blank" }
            val msgs = db.collection("chats").document(chatId).collection("messages")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            for (doc in msgs.documents) {
                doc.reference.delete().await()
            }
            db.collection("chats").document(chatId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeMessages(uid: String, chatId: String): Flow<List<ChatMessage>> = flow {
        require(uid.isNotBlank() && chatId.isNotBlank()) { "uid and chatId must not be blank" }
        val path = "chats/$chatId/messages"
        emitAll(
            db.collection("chats").document(chatId).collection("messages")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(100)
                .snapshots()
                .map { it.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addMessage(chatId: String, role: String, content: String): Result<Unit> {
        var path = "chats/$chatId/messages"
        return try {
            val uid = requireUserId()
            require(chatId.isNotBlank()) { "chatId must not be blank" }
            require(role in listOf("user", "model")) { "Invalid role" }
            val msgRef = db.collection("chats").document(chatId).collection("messages").document()
            path = msgRef.path
            val payload = mapOf(
                "chatId" to chatId,
                "uid" to uid,
                "role" to role,
                "content" to content.take(50000),
                "timestamp" to FieldValue.serverTimestamp()
            )
            msgRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeTasks(uid: String): Flow<List<DailyTask>> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "dailyTasks"
        emitAll(
            db.collection(path)
                .whereEqualTo("uid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(DailyTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addTask(title: String, category: String, reminderTime: String): Result<Unit> {
        var path = "dailyTasks"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(200)
            require(cleanTitle.isNotBlank()) { "Task title cannot be empty" }
            val ref = db.collection("dailyTasks").document()
            path = ref.path
            val payload = mapOf(
                "uid" to uid,
                "title" to cleanTitle,
                "category" to category.trim().ifBlank { "Work" }.take(60),
                "completed" to false,
                "reminderTime" to reminderTime.trim().take(40),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleTask(taskId: String, completed: Boolean): Result<Unit> {
        var path = "dailyTasks/$taskId"
        return try {
            requireUserId()
            require(taskId.isNotBlank())
            db.collection("dailyTasks").document(taskId).update(
                mapOf(
                    "completed" to completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        var path = "dailyTasks/$taskId"
        return try {
            requireUserId()
            require(taskId.isNotBlank())
            db.collection("dailyTasks").document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeUserFiles(uid: String): Flow<List<UserFileRecord>> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "userFiles"
        emitAll(
            db.collection(path)
                .whereEqualTo("uid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(UserFileRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun saveUserFileRecord(
        fileName: String,
        storagePath: String,
        sizeBytes: Long,
        summary: String?
    ): Result<String> {
        var path = "userFiles"
        return try {
            val uid = requireUserId()
            val ref = db.collection("userFiles").document()
            path = ref.path
            val payload = buildMap<String, Any> {
                put("uid", uid)
                put("fileName", fileName.take(200))
                put("storagePath", storagePath.take(500))
                put("sizeBytes", sizeBytes.coerceIn(0L, 20_971_520L))
                summary?.takeIf { it.isNotBlank() }?.let { put("summary", it.take(20000)) }
                put("createdAt", FieldValue.serverTimestamp())
            }
            ref.set(payload).await()
            Result.success(ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserFileRecord(fileId: String): Result<Unit> {
        var path = "userFiles/$fileId"
        return try {
            requireUserId()
            require(fileId.isNotBlank())
            db.collection("userFiles").document(fileId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeNotifications(uid: String): Flow<List<AppNotification>> = flow {
        require(uid.isNotBlank()) { "User ID must not be blank" }
        val path = "notifications"
        emitAll(
            db.collection(path)
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { it.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        var path = "notifications/$notificationId"
        return try {
            requireUserId()
            db.collection("notifications").document(notificationId).update("read", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserRecord(uid: String) {
        runCatching {
            db.collection("users").document(uid).delete().await()
        }
    }

    private fun unwrapAndLog(error: Throwable, operationType: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}

class InsufficientCreditsException(message: String) : Exception(message)
