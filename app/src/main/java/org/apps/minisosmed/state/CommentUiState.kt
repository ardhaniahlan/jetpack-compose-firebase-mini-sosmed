package org.apps.minisosmed.state

import org.apps.minisosmed.entity.relation.CommentWithUser

data class CommentUiState(
    val comments: List<CommentWithUser> = emptyList(),

    override val isLoading: Boolean = false,
    override val success: String? = null,
    override val message: String? = null
): UiState
