package org.apps.minisosmed.entity

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)
