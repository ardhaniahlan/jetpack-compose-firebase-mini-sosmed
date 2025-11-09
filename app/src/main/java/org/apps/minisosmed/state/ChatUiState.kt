package org.apps.minisosmed.state

import org.apps.minisosmed.entity.Message
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.entity.relation.ChatWithUser

data class ChatUiState(
    // Untuk Chat List
    val chatsState: ViewState<List<ChatWithUser>> = ViewState.Idle,

    // Untuk Chat Room
    val messagesState: ViewState<List<Message>> = ViewState.Idle,
    val chatUserState: ViewState<User> = ViewState.Idle, // User yang diajak chat (bukan current user)

    // Untuk actions
    val sendMessageState: ViewState<Unit> = ViewState.Idle,
    val createChatState: ViewState<String> = ViewState.Idle // String adalah chatId
)