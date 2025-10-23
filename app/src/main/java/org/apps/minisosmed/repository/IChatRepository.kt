package org.apps.minisosmed.repository

import kotlinx.coroutines.flow.Flow
import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.relation.ChatWithUser

interface IChatRepository {
    suspend fun openOrCreateChat(targetUserId: String): Result<String>
    fun getUserChats(userId: String): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, text: String): Result<Unit>
    suspend fun listenToChats(
        currentUserId: String,
        onChatsChanged: (List<ChatWithUser>) -> Unit
    )
}
