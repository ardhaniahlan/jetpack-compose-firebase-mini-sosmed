package org.apps.minisosmed.state

import org.apps.minisosmed.entity.relation.CommentWithUser

data class CommentUiState(
    val comments: List<CommentWithUser> = emptyList(),
    val commentState: ViewState<Unit> = ViewState.Idle,
)
