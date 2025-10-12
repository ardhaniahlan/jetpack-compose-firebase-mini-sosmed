package org.apps.minisosmed.entity.relation

import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.User

data class PostWithUser(
    val post: Post,
    val user: User
)