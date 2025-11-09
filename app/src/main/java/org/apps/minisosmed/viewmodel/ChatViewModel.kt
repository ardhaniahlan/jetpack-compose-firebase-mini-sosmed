package org.apps.minisosmed.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.entity.relation.ChatWithUser
import org.apps.minisosmed.repository.IChatRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.state.ChatUiState
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: IChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadChatUser(chatId: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(chatUserState = ViewState.Loading) }

            chatRepository.getOtherUserFromChat(chatId, currentUserId)
                .onSuccess { user ->
                    _uiState.update { it.copy(chatUserState = ViewState.Success(user)) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(chatUserState = ViewState.Error("Gagal memuat user: ${e.message}"))
                    }
                }
        }
    }

    fun loadChats(currentUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(chatsState = ViewState.Loading) }

            try {
                chatRepository.listenToChats(currentUserId).collect { chatList ->
                    _uiState.update {
                        it.copy(chatsState = ViewState.Success(chatList))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(chatsState = ViewState.Error("Gagal memuat chat: ${e.message}"))
                }
            }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(messagesState = ViewState.Loading) }

            try {
                chatRepository.getMessages(chatId).collect { messages ->
                    _uiState.update {
                        it.copy(messagesState = ViewState.Success(messages))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(messagesState = ViewState.Error("Gagal memuat pesan: ${e.message}"))
                }
            }
        }
    }

    fun openOrCreateChat(targetUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(createChatState = ViewState.Loading) }

            try {
                val result = chatRepository.openOrCreateChat(targetUserId)
                val chatId = result.getOrNull()

                if (chatId != null) {
                    _uiState.update { it.copy(createChatState = ViewState.Success(chatId)) }
                } else {
                    _uiState.update { it.copy(createChatState = ViewState.Error("Gagal membuat chat")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(createChatState = ViewState.Error(e.message ?: "Error")) }
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(sendMessageState = ViewState.Loading) }

            try {
                chatRepository.sendMessage(chatId, text)
                _uiState.update { it.copy(sendMessageState = ViewState.Success(Unit)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(sendMessageState = ViewState.Error("Gagal mengirim pesan: ${e.message}"))
                }
            }
        }
    }
}
