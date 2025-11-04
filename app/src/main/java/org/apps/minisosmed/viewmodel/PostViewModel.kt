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
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.relation.PostWithUser
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.PostUiState
import androidx.core.net.toUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val imageRepository: ImageRepository,
): ViewModel() {

    private val _user = MutableStateFlow<ViewState<User?>>(ViewState.Idle)
    val user = _user.asStateFlow()

    private val _postState = MutableStateFlow<ViewState<Unit>>(ViewState.Idle)
    val postState = _postState.asStateFlow()

    private val _uiState = mutableStateOf(PostUiState())
    val uiState: State<PostUiState> = _uiState

    fun loadPostById(postId: String) {
        viewModelScope.launch {
            val result = postRepository.getPostById(postId)
            result.onSuccess { post ->
                startEditPost(post)
                _postState.value = ViewState.Idle
            } .onFailure {
                _postState.value = ViewState.Error("Post tidak ditemukan")
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _user.value = ViewState.Success(user)
                }
                .onFailure { e ->
                    _user.value = ViewState.Error("Gagal memuat user: ${e.message}")
                }
        }

    }

    fun loadPost() {
        viewModelScope.launch {
            _user.value = ViewState.Loading

            try {
                val usersResult = userRepository.getAllUsers()
                val users = usersResult.getOrDefault(emptyList())
                val userMap = users.associateBy { it.id }

                postRepository.getAllPost().collect { posts ->
                    val enrichedPosts = posts.mapNotNull { post ->
                        val user = userMap[post.userId]
                        user?.let {
                            PostWithUser(post, it)
                        }
                    }

                    _uiState.value = _uiState.value.copy(postsWithUser = enrichedPosts)
                }
            } catch (e: Exception){
                _user.value = ViewState.Error("Terjadi kesalahan: ${e.message}")
            }

        }
    }

    fun startEditPost(post: Post) {
        val photoUriForUi: Uri? = post.photoUrl?.let { photoStr ->
            val normalized = if (photoStr.startsWith("data:")) {
                photoStr
            } else {
                "data:image/jpeg;base64,$photoStr"
            }
            normalized.toUri()
        }

        _uiState.value = _uiState.value.copy(
            mode = PostMode.EDIT,
            postBeingEditedId = post.id,
            description = post.description ?: "",
            photoUrl = photoUriForUi
        )
    }

    fun updatePost() {
        val current = _uiState.value
        val postId = current.postBeingEditedId ?: return

        viewModelScope.launch {
            _postState.value = ViewState.Loading
            try {
                val result = postRepository.updatePost(
                    postId = postId,
                    newDescription = current.description ?: ""
                )

                result.onSuccess {
                    val updatedList = _uiState.value.postsWithUser.map {
                        if (it.post.id == postId)
                            it.copy(post = it.post.copy(description = current.description))
                        else it
                    }

                    _uiState.value = _uiState.value.copy(
                        mode = PostMode.EDIT,
                        postBeingEditedId = null,
                        postsWithUser = updatedList
                    )

                    _postState.value = ViewState.Success(Unit)
                }.onFailure {
                    _postState.value = ViewState.Error("Gagal memperbarui post: ${it.message}")
                }

            } catch (e: Exception) {
                _postState.value = ViewState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }


    fun deletePost(postId: String) {
        viewModelScope.launch {
            _postState.value = ViewState.Loading

            val result = postRepository.deletePost(postId)
            if (result.isSuccess) {
                val updatedList = _uiState.value.postsWithUser.filterNot { it.post.id == postId }
                _uiState.value = _uiState.value.copy(postsWithUser = updatedList)
                _postState.value = ViewState.Success(Unit)
            } else {
                _postState.value = ViewState.Error(
                    "Gagal menghapus post: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(context: Context){
        val current = _uiState.value
        viewModelScope.launch {
            _postState.value = ViewState.Loading

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

                createPost.onSuccess { newPost ->
                    val user = (_user.value as? ViewState.Success)?.data
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(
                            postsWithUser = _uiState.value.postsWithUser + PostWithUser(newPost, user),
                            description = "",
                            photoUrl = null
                        )
                    }
                    _postState.value = ViewState.Success(Unit)
                }.onFailure {
                    _postState.value = ViewState.Error("Gagal membuat post: ${it.message}")
                }
            } catch (e: Exception){
                _postState.value = ViewState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(photoUrl = uri)
    }

    fun resetPostState() {
        _postState.value = ViewState.Idle
    }

    fun clearForm() {
        _uiState.value = PostUiState()
    }
}