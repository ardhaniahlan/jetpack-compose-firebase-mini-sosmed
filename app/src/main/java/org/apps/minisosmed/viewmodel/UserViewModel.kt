package org.apps.minisosmed.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log.e
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils.result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.UpdateUserUiState
import java.util.Base64

class UserViewModel (
    private  val userRepository: IUserRepository,
    private val imageRepository: ImageRepository
): ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user

    private val _uiState = mutableStateOf(UpdateUserUiState())
    val uiState : State<UpdateUserUiState> = _uiState

    init {
        refreshUser()
    }

    fun refreshUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUser = userRepository.getCurrentUser()
                _user.value = currentUser
                currentUser?.let {
                    _uiState.value = UpdateUserUiState(
                        displayName = it.displayName ?: "",
                        bio = it.bio ?: "",
                        photoUrl = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal memuat data user: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateProfile(context: Context) {
        val currentUser = _user.value
        if (currentUser == null) return

        val currentState = _uiState.value

        val hasChanges = currentUser.displayName != currentState.displayName ||
                currentUser.bio != currentState.bio ||
                currentState.photoUrl != null

        if (!hasChanges) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            try {
                val photoBase64 = withContext(Dispatchers.IO) {
                    currentState.photoUrl?.let { uri ->
                        imageRepository.uriToBase64(context, uri)
                    }
                }

                val result = userRepository.updateProfile(
                    displayName = currentState.displayName,
                    bio = currentState.bio,
                    photoUri = photoBase64
                )

                result.onSuccess { updatedUser ->
                    _user.value = updatedUser
                    _uiState.value = UpdateUserUiState(
                        displayName = updatedUser.displayName,
                        bio = updatedUser.bio,
                        photoUrl = null,
                        success = "Profil berhasil diupdate",
                        isLoading = false
                    )

                }.onFailure { error ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        message = "Gagal update profil: ${error.message}"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    message = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.value = _uiState.value.copy(
            displayName = newDisplayName,
            displayNameError = if (newDisplayName.isNotEmpty() && newDisplayName.length < 3) {
                "Minimal 3 digit dan tidak boleh Kosong"
            } else null
        )
    }

    fun onBioChange(value: String) {
        _uiState.value = _uiState.value.copy(bio = value)
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(photoUrl = uri)
    }
}