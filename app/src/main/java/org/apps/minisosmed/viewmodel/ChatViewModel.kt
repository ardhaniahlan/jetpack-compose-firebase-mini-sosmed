package org.apps.minisosmed.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.entity.relation.ChatWithUser
import org.apps.minisosmed.repository.IChatRepository
import org.apps.minisosmed.repository.IUserRepository
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: IChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatsWithUser = MutableStateFlow<List<ChatWithUser>>(emptyList())
    val chatsWithUser = _chatsWithUser.asStateFlow()

    private val _chatUser = MutableStateFlow<User?>(null)
    val chatUser = _chatUser.asStateFlow()

    fun loadChatUser(chatId: String, currentUserId: String) {
        viewModelScope.launch {
            val result = chatRepository.getOtherUserFromChat(chatId, currentUserId)
            result.onSuccess {
                _chatUser.value = result.getOrNull()
            }
        }
    }

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
