package org.apps.minisosmed.state

import android.net.Uri
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.relation.PostWithUser

data class PostUiState(
    val description: String? = "",
    val photoUrl: Uri? = null,
    val postsWithUser: List<PostWithUser> = emptyList(),
    val mode: PostMode = PostMode.ADD,
    val postBeingEditedId: String? = null,
    override val isLoading: Boolean = false,
    override val success: String? = null,
    override val message: String? = null
): UiState
