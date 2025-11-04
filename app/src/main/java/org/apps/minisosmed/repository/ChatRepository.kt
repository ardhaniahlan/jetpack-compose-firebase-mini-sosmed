package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.relation.ChatWithUser
import kotlin.jvm.java
import org.apps.minisosmed.entity.User


interface IChatRepository {
    suspend fun openOrCreateChat(targetUserId: String): Result<String>
    fun getUserChats(userId: String): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, text: String): Result<Unit>
    suspend fun listenToChats(
        currentUserId: String,
        onChatsChanged: (List<ChatWithUser>) -> Unit
    )
    suspend fun getOtherUserFromChat(chatId: String, currentUserId: String): Result<User>
}

class ChatRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IChatRepository {

    private val chatsRef = firestore.collection("chats")

    override suspend fun getOtherUserFromChat(chatId: String, currentUserId: String): Result<User> {
        return try {
            val chatDoc = chatsRef.document(chatId).get().await()
            val participants = chatDoc.get("participants") as? List<*>
                ?: return Result.failure(Exception("Participants not found"))

            val otherUserId = participants.firstOrNull { it != currentUserId }
                ?: return Result.failure(Exception("Other user not found"))

            val userRef = firestore.collection("users").document(otherUserId.toString())
            val userDoc = userRef.get().await()

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("User data invalid"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun openOrCreateChat(targetUserId: String): Result<String> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User belum login"))

        return try {
            val chatId = generateChatId(currentUserId, targetUserId)
            val chatRef = chatsRef.document(chatId)

            val snapshot = chatRef.get().await()
            if (!snapshot.exists()) {
                val chat = Chat(
                    id = chatId,
                    participants = listOf(currentUserId, targetUserId),
                    lastMessage = "",
                    updatedAt = System.currentTimeMillis()
                )
                chatRef.set(chat).await()
            }

            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(chatId: String, text: String): Result<Unit> {
        val senderId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val messageId = chatsRef.document(chatId)
                .collection("messages")
                .document().id

            val message = Message(
                id = messageId,
                senderId = senderId,
                text = text,
                createdAt = System.currentTimeMillis()
            )

            firestore.runBatch { batch ->
                val chatRef = chatsRef.document(chatId)
                batch.set(chatRef.collection("messages").document(messageId), message)
                batch.update(chatRef, mapOf(
                    "lastMessage" to text,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatsRef.document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = chatsRef
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun listenToChats(
        currentUserId: String,
        onChatsChanged: (List<ChatWithUser>) -> Unit
    ) {
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                CoroutineScope(Dispatchers.IO).launch {
                    val chatList = snapshot?.documents?.mapNotNull { doc ->
                        val chat = doc.toObject(Chat::class.java)?.copy(id = doc.id)
                        chat?.let {
                            val otherUserId = it.participants.firstOrNull { id -> id != currentUserId }

                            if (otherUserId.isNullOrBlank()) {
                                return@mapNotNull null
                            }

                            val userDoc = firestore.collection("users")
                                .document(otherUserId)
                                .get()
                                .await()

                            val otherUser = userDoc.toObject(User::class.java)
                            if (otherUser != null) ChatWithUser(it, otherUser) else null
                        }
                    } ?: emptyList()

                    onChatsChanged(chatList)
                }
            }
    }


    private fun generateChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
