package org.apps.minisosmed.viewmodel

import android.util.Log
import android.util.Log.e
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.state.AuthUiState
import org.apps.minisosmed.repository.IAuthRepository
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: IAuthRepository) : ViewModel(){

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _eventFlow.emit(UiEvent.Navigate)
        }
    }

    fun login(){
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(authState = ViewState.Loading) }

            val result = authRepository.login(current.email, current.password)
            result.onSuccess { user ->
                _uiState.update { it.copy(authState = ViewState.Success(user)) }
                _eventFlow.emit(UiEvent.ShowSnackbar("Login berhasil"))
                _eventFlow.emit(UiEvent.Navigate)
            }.onFailure { e ->
                _uiState.update { it.copy(authState = ViewState.Error(e.message ?: "Login gagal")) }
            }
        }
    }

    fun register (){
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(authState = ViewState.Loading) }
            val result = authRepository.register(current.email, current.password, current.displayName)
            result.onSuccess { user ->
                _uiState.update { it.copy(authState = ViewState.Success(user)) }
                _eventFlow.emit(UiEvent.ShowSnackbar("Register berhasil"))
                _eventFlow.emit(UiEvent.Navigate)
            }.onFailure { e ->
                _uiState.update { it.copy(authState = ViewState.Error(e.message ?: "Register gagal")) }
            }
        }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.update {
            it.copy(
                displayName = newDisplayName,
                displayNameError = if (newDisplayName.isNotEmpty() && newDisplayName.length < 3) {
                    "Minimal 3 digit dan tidak boleh Kosong"
                } else null
            )
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = if (newEmail.isNotEmpty() && !isValidGmail(newEmail)) {
                    "Email tidak valid"
                } else null
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = if (newPassword.isNotEmpty() && newPassword.length < 6) {
                    "Password minimal 6 karakter"
                } else null
            )
        }
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.update{
            it.copy(
                confirmPassword = newConfirm,
                confirmPasswordError = if (newConfirm.isNotEmpty() && newConfirm != _uiState.value.password) {
                    "Password tidak sama"
                } else null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(
                passwordVisible = !it.passwordVisible
            )
        }
    }

    fun isValidGmail(input: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@gmail\\.com$").matches(input)
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                displayName = "",
                email = "",
                password = "",
                confirmPassword = ""
            )
        }
    }

    fun resetAuthState() {
        _uiState.update { it.copy(authState = ViewState.Idle) }
    }
}
