package org.apps.minisosmed.state

import android.net.Uri
import org.apps.minisosmed.entity.Post

data class PostUiState(
    val description: String? = "",
    val photoUrl: Uri? = null,
    val posts: List<Post> = emptyList(),
    override val isLoading: Boolean = false,
    override val success: String? = null,
    override val message: String? = null
): UiState
