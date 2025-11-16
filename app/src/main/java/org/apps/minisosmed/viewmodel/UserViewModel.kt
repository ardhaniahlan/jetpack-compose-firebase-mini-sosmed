package org.apps.minisosmed.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.UpdateUserUiState
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val imageRepository: ImageRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUserUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun getPostByUserId(userId: String){
        viewModelScope.launch {
            _uiState.update { it.copy(postsState = ViewState.Loading) }

            try {
                postRepository.getPostByUserId(userId).collect { posts ->
                    _uiState.update { it.copy(postsState = ViewState.Success(posts)) }
                }
            } catch (e: Exception){
                _uiState.update {
                    it.copy(postsState = ViewState.Error("Gagal memuat posts: ${e.message}"))
                }
            }
        }
    }

    fun getCurrentUserPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(postsState = ViewState.Loading) }
            try {
                val currentUser = userRepository.getCurrentUser().getOrThrow()
                currentUser.id?.let { userId ->
                    postRepository.getPostByUserId(userId).collect { posts ->
                        _uiState.update {
                            it.copy(postsState = ViewState.Success(posts))
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(postsState = ViewState.Error("Gagal memuat posts: ${e.message}"))
                }
            }
        }
    }

    fun searchUser(query: String, currentUser: String) {
        viewModelScope.launch {
            userRepository.searchUsersByName(query, currentUser)
                .collect { users ->
                    _uiState.update { it.copy(searchUser = ViewState.Success(users)) }
                }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(userState = ViewState.Loading) }

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(userState = ViewState.Success(user)) }
                    preFillForm(user)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(userState = ViewState.Error("Gagal memuat data user: ${e.message}")) }
                }
        }
    }

    fun preFillForm(user: User) {
        _uiState.update { currentState ->
            currentState.copy(
                displayName = user.displayName ?: "",
                bio = user.bio ?: "",
                photoUrl = user.photoUrl?.toUri(),
                selectedImageUri = null
            )
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userState = ViewState.Loading) }
            userRepository.getUserById(userId)
                .onSuccess { user ->
                    _uiState.update { it.copy(userState = ViewState.Success(user)) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(userState = ViewState.Error("Gagal mengambil user: ${e.message}")) }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateProfile(context: Context) {
        val currentUserState = _uiState.value

        val currentUser = when (val userState = currentUserState.userState) {
            is ViewState.Success -> userState.data
            else -> null
        }

        if (currentUser == null) return

        val hasChanges = currentUser.displayName != currentUserState.displayName ||
                currentUser.bio != currentUserState.bio ||
                currentUserState.photoUrl != null

        if (!hasChanges) return

        viewModelScope.launch {
            _uiState.update { it.copy(
                userState = ViewState.Loading,
                isUiBlocked = true
            ) }

            try {
                val photoBase64 = withContext(Dispatchers.IO) {
                    currentUserState.photoUrl?.let { uri ->
                        imageRepository.uriToBase64(context, uri)
                    }
                }

                val result = userRepository.updateProfile(
                    displayName = currentUserState.displayName,
                    bio = currentUserState.bio,
                    photoUri = photoBase64
                )

                result.onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            updateState = ViewState.Success(Unit),
                            userState = ViewState.Success(updatedUser),
                            selectedImageUri = null,
                            photoUrl = updatedUser.photoUrl?.toUri()
                        )
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar("Profil berhasil diupdate"))
                    _eventFlow.emit(UiEvent.Navigate)
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            updateState = ViewState.Error("Gagal update profil: ${e.message}"),
                            isUiBlocked = false
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(updateState = ViewState.Error("Terjadi Kesalahan: ${e.message}"))
                }
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

    fun onBioChange(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.update { it.copy(
            photoUrl = uri,
            selectedImageUri = uri
        ) }
    }

    fun unblockUi() {
        _uiState.update { it.copy(isUiBlocked = false) }
    }
}