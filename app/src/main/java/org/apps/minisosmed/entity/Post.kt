package org.apps.minisosmed.entity

data class Post(
    val id: String? = "",
    val userId: String? = "",
    val description: String? = "",
    val photoUrl: String? = "",
    val createdAt: Long = 0L
)