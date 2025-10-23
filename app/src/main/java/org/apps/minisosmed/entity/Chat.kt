package org.apps.minisosmed.entity

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

