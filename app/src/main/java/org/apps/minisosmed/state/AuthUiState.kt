package org.apps.minisosmed.state

data class AuthUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,

    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    override val isLoading: Boolean = false,
    override val success: String? = null,
    override val message: String? = null
) : UiState
