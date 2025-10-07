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
import org.apps.minisosmed.entity.PostWithUser
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.PostUiState

class PostViewModel(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val imageRepository: ImageRepository
): ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user

    private val _uiState = mutableStateOf(PostUiState())
    val uiState: State<PostUiState> = _uiState

    fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { _user.value = it }
                .onFailure { _uiState.value = _uiState.value.copy(message = "Gagal memuat user: ${it.message}") }
        }

    }


    fun loadPost() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            postRepository.getAllPost()
                .onSuccess { posts ->
                    val enrichedPosts = posts.mapNotNull { post ->
                        val userResult = userRepository.getUserById(post.userId ?: return@mapNotNull null)
                        userResult.getOrNull()?.let { user ->
                            PostWithUser(post, user)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        postsWithUser = enrichedPosts,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Terjadi kesalahan: ${it.message}"
                    )
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            if (result.isSuccess) {
                val updatedList = _uiState.value.postsWithUser.filterNot { it.post.id == postId }
                _uiState.value = _uiState.value.copy(
                    postsWithUser = updatedList,
                    success = "Post berhasil dihapus"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal menghapus post: ${result.exceptionOrNull()?.message}"
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

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            success = null
        )
    }
}