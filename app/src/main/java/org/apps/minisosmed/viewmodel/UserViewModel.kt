package org.apps.minisosmed.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.UpdateUserUiState
import org.apps.minisosmed.state.ViewState

class UserViewModel (
    private  val userRepository: IUserRepository,
    private val imageRepository: ImageRepository
): ViewModel() {
    private val _user = MutableStateFlow<ViewState<User?>>(ViewState.Idle)
    val user : StateFlow<ViewState<User?>> = _user

    private val _updateState = MutableStateFlow<ViewState<Unit>>(ViewState.Idle)
    val updateState: StateFlow<ViewState<Unit>> = _updateState

    private val _uiState = mutableStateOf(UpdateUserUiState())
    val uiState : State<UpdateUserUiState> = _uiState

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    fun searchUser(query: String, currentUser: String) {
        viewModelScope.launch {
            userRepository.searchUsersByName(query, currentUser)
                .collect { users ->
                    _searchResults.value = users
                }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            _user.value = ViewState.Loading

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _user.value = ViewState.Success(user)
                }
                .onFailure { e ->
                    _user.value = ViewState.Error("Gagal memuat data user: ${e.message}")
                }
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _user.value = ViewState.Loading
            val result = userRepository.getUserById(userId)
            _user.value = result.fold(
                onSuccess = { ViewState.Success(it) },

                onFailure = { ViewState.Error(it.message ?: "Gagal mengambil user") }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateProfile(context: Context) {
        val currentUserState = _user.value
        if (currentUserState !is ViewState.Success) return

        val currentUser= currentUserState.data
        val currentUiState = _uiState.value

        val hasChanges = currentUser?.displayName != currentUiState.displayName ||
                currentUser?.bio != currentUiState.bio ||
                currentUiState.photoUrl != null

        if (!hasChanges) return

        viewModelScope.launch {
            _updateState.value = ViewState.Loading

            try {
                val photoBase64 = withContext(Dispatchers.IO) {
                    currentUiState.photoUrl?.let { uri ->
                        imageRepository.uriToBase64(context, uri)
                    }
                }

                val result = userRepository.updateProfile(
                    displayName = currentUiState.displayName,
                    bio = currentUiState.bio,
                    photoUri = photoBase64
                )

                result.onSuccess { updatedUser ->
                    _updateState.value = ViewState.Success(Unit)
                    refreshUser()
                }.onFailure { error ->
                    _updateState.value = ViewState.Error("Gagal update profil: ${error.message}")
                }

            } catch (e: Exception) {
                _updateState.value = ViewState.Error("Terjadi kesalahan: ${e.message}")
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

    fun resetUpdateState(){
        _updateState.value = ViewState.Idle
    }
}