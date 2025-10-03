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
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.AuthUiState
import org.apps.minisosmed.state.PostUiState

class PostViewModel(
    private val postRepository: IPostRepository,
    private val imageRepository: ImageRepository
): ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user

    private val _uiState = mutableStateOf(PostUiState())
    val uiState: State<PostUiState> = _uiState

    fun loadPost(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            postRepository.getAllPost()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        posts = it,
                        isLoading = false
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Terjadi kesalahan: ${it.message}"
                    )
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(context: Context){
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val photoBase64 = withContext(Dispatchers.IO) {
                    current.photoUrl?.let { uri ->
                        imageRepository.uriToBase64(context, uri)
                    }
                }

                val createPost = postRepository.createPost(
                    description = current.description,
                    photoUri = photoBase64
                )

                createPost.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = "Post berhasil",
                        posts = _uiState.value.posts + it,
                        description = "",
                        photoUrl = null
                    )
                }.onFailure {
                    _uiState.value = PostUiState(
                        isLoading = false,
                        message = it.message
                    )
                }
            } catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(photoUrl = uri)
    }

    fun clearForm() {
        _uiState.value = _uiState.value.copy(
            description = "",
            photoUrl = null,
            message = null
        )
    }

    fun resetUiState() {
        _uiState.value = PostUiState()
    }

}