package org.apps.minisosmed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.relation.ChatWithUser
import org.apps.minisosmed.repository.IChatRepository
import kotlin.jvm.java

class ChatViewModel(
    private val chatRepository: IChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages



    private val _chatsWithUser = MutableStateFlow<List<ChatWithUser>>(emptyList())
    val chatsWithUser: StateFlow<List<ChatWithUser>> = _chatsWithUser

    fun listenToChats(currentUserId: String) {
        viewModelScope.launch {
            chatRepository.listenToChats(currentUserId) { chatList ->
                _chatsWithUser.value = chatList
            }
        }
    }

    fun listenToMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect {
                _messages.value = it
            }
        }
    }

    suspend fun openOrCreateChat(targetUserId: String): String? {
        val result = chatRepository.openOrCreateChat(targetUserId)
        return result.getOrNull()
    }

    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(chatId,  text)
        }
    }
}
