package org.apps.minisosmed.entity.relation

import org.apps.minisosmed.entity.Chat
import org.apps.minisosmed.entity.User

data class ChatWithUser(
    val chat: Chat,
    val otherUser: User
)

