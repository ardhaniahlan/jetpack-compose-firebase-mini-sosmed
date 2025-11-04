package org.apps.minisosmed.viewmodel

import android.util.Log
import android.util.Log.e
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.state.AuthUiState
import org.apps.minisosmed.repository.IAuthRepository
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: IAuthRepository) : ViewModel(){

    private val _uiState = mutableStateOf(AuthUiState())
    val uiState: State<AuthUiState> = _uiState

    private val _authState = MutableStateFlow<ViewState<User>>(ViewState.Idle)
    val authState = _authState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = ViewState.Idle
            _uiState.value = AuthUiState()
        }
    }

    fun login(){
        val current = _uiState.value
        viewModelScope.launch {
            _authState.value = ViewState.Loading

            val result = authRepository.login(current.email, current.password)
            result.onSuccess { user ->
                _authState.value = ViewState.Success(user)
            }.onFailure { e ->
                _authState.value = ViewState.Error(e.message ?: "Login gagal")
            }
        }
    }

    fun register (){
        val current = _uiState.value
        viewModelScope.launch {
            _authState.value = ViewState.Loading
            val result = authRepository.register(current.email, current.password, current.displayName)
            result.onSuccess { user ->
                _authState.value = ViewState.Success(user)
            }.onFailure { e ->
                _authState.value = ViewState.Error(e.message ?: "Register gagal")
            }
        }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.value = _uiState.value.copy(
            displayName = newDisplayName,
            displayNameError = if (newDisplayName.isNotEmpty() && newDisplayName.length < 3){
                "Minimal 3 digit dan tidak boleh Kosong"
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
            passwordError = if (newPassword.isNotEmpty() && newPassword.length < 6) {
                "Password minimal 6 karakter"
            } else null
        )
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = newConfirm,
            confirmPasswordError = if (newConfirm.isNotEmpty() && newConfirm != _uiState.value.password) {
                "Password tidak sama"
            } else null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun isValidGmail(input: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@gmail\\.com$").matches(input)
    }

    fun clearForm() {
        _uiState.value = AuthUiState()
    }

    fun resetAuthState() {
        _authState.value = ViewState.Idle
    }
}
