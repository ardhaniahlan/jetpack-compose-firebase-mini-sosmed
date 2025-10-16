package org.apps.minisosmed.state

import android.net.Uri

data class UpdateUserUiState(
    val displayName: String? = "",
    val bio: String? = "",
    val photoUrl: Uri? = null,
    val displayNameError: String? = null)
