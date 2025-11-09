package org.apps.minisosmed.state

import android.net.Uri
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.User

data class UpdateUserUiState(
    val displayName: String? = "",
    val bio: String? = "",
    val photoUrl: Uri? = null,
    val displayNameError: String? = null,
    val selectedImageUri: Uri? = null,

    val userState: ViewState<User> = ViewState.Idle,
    val updateState: ViewState<Unit> = ViewState.Idle,
    val searchUser: ViewState<List<User>> = ViewState.Idle,
    val postsState: ViewState<List<Post>> = ViewState.Idle
)
