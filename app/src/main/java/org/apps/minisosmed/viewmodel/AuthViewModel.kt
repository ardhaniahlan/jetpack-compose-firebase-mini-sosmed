package org.apps.minisosmed.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.apps.minisosmed.state.AuthUiState
import org.apps.minisosmed.repository.IAuthRepository

class AuthViewModel(private val repository: IAuthRepository) : ViewModel(){

    private val _uiState = mutableStateOf(AuthUiState())
    val uiState: State<AuthUiState> = _uiState

    fun register (){
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.register(current.email, current.password, current.displayName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = "Register berhasil"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = it.message ?: "Register Error"
                )
            }
        }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.value = _uiState.value.copy(
            displayName = newDisplayName,
            displayNameError = if (newDisplayName.isBlank() || newDisplayName.length < 3){
                "Minimal 3 Digit dan tidak boleh Kosong"
            } else null
        )
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            emailError = if (newEmail.isNotEmpty() && !isValidGmail(newEmail)) {
                "Email tidak valid"
            } else null
        )
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            passwordError = if (newPassword.length < 6) {
                "Password minimal 6 karakter"
            } else null
        )
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = newConfirm,
            confirmPasswordError = if (newConfirm != _uiState.value.password) {
                "Password tidak sama"
            } else null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }
}

fun isValidGmail(input: String): Boolean {
    return Regex("^[A-Za-z0-9+_.-]+@gmail\\.com$").matches(input)
}