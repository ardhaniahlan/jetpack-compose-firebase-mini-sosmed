package org.apps.minisosmed.entity

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)



