package org.apps.minisosmed.entity.relation

import org.apps.minisosmed.entity.Comment
import org.apps.minisosmed.entity.User

data class CommentWithUser(
    val comment: Comment,
    val user: User
)