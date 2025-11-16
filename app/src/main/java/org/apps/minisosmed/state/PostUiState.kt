package org.apps.minisosmed.state

import android.net.Uri
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.entity.relation.PostWithUser

data class PostUiState(
    val description: String? = "",
    val photoUrl: Uri? = null,
    val postsWithUser: List<PostWithUser> = emptyList(),
    val mode: PostMode = PostMode.ADD,
    val postImage: String? = null,

    val userState: ViewState<User> = ViewState.Idle,
    val postsState: ViewState<List<PostWithUser>> = ViewState.Idle,
    val postOperation: ViewState<Unit> = ViewState.Idle,

    val isUiBlocked: Boolean = false
)
