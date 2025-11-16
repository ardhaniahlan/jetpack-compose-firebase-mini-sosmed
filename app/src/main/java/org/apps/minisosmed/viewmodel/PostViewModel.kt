package org.apps.minisosmed.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.entity.relation.PostWithUser
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.PostUiState
import androidx.core.net.toUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val imageRepository: ImageRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadCurrentUser()
    }

    fun loadPostById(postId: String) {
        _uiState.update { it.copy(mode = PostMode.EDIT) }
        viewModelScope.launch {
            val result = postRepository.getPostById(postId)
            result.onSuccess { post ->
                startEditPost(post)
            } .onFailure {
                //
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(userState = ViewState.Success(user))
                    }
                    loadPost()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(userState = ViewState.Error("Gagal memuat user: ${e.message}"))
                    }
                }
        }

    }

    fun loadPost() {
        viewModelScope.launch {
            _uiState.update { it.copy(postsState = ViewState.Loading) }

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

                    _uiState.update {
                        it.copy(postsState = ViewState.Success(enrichedPosts))
                    }
                }
            } catch (e: Exception){
                _uiState.update {
                    it.copy(postsState = ViewState.Error("Terjadi kesalahan: ${e.message}"))
                }
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

        _uiState.update {
            it.copy(
                mode = PostMode.EDIT,
                postImage = post.id,
                description = post.description ?: "",
                photoUrl = photoUriForUi
            )
        }
    }

    fun updatePost() {
        val current = _uiState.value
        val postId = current.postImage ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(
                postOperation = ViewState.Loading,
                isUiBlocked = true
            ) }

            try {
                val result = postRepository.updatePost(
                    postId = postId,
                    newDescription = current.description ?: ""
                )

                result.onSuccess {
                    val updatedPosts = _uiState.value.postsWithUser.map {
                        if (it.post.id == postId)
                            it.copy(post = it.post.copy(description = current.description))
                        else it
                    }

                    _uiState.update {
                        it.copy(
                            postsState = ViewState.Success(updatedPosts),
                            mode = PostMode.EDIT,
                            postImage = null,
                            postOperation = ViewState.Success(Unit)
                        )
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar("Post berhasil diupdate"))
                    _eventFlow.emit(UiEvent.Navigate)
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            postOperation = ViewState.Error("Gagal memperbarui post: $it"),
                            isUiBlocked = false
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        postOperation = ViewState.Error("Terjadi kesalahan: ${e.message}"),
                        isUiBlocked = false
                    )
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(postOperation = ViewState.Loading) }

            postRepository.deletePost(postId)
                .onSuccess {
                    val currentPosts = when (val state = uiState.value.postsState) {
                        is ViewState.Success -> state.data
                        else -> emptyList()
                    }

                    val updatedPosts = currentPosts.filterNot { it.post.id == postId }
                    _uiState.update {
                        it.copy(
                            postsState = ViewState.Success(updatedPosts),
                            postOperation = ViewState.Success(Unit)
                        )
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar("Post berhasil dihapus"))
                }
                .onFailure {
                    _uiState.update {
                        it.copy(postOperation = ViewState.Error("Gagal menghapus post: $it"))
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(postOperation = ViewState.Loading, isUiBlocked = true) }

            try {
                val photoBase64 = withContext(Dispatchers.IO) {
                    uiState.value.photoUrl?.let { uri ->
                        imageRepository.uriToBase64(context, uri)
                    }
                }

                postRepository.createPost(
                    description = uiState.value.description,
                    photoUri = photoBase64
                ).onSuccess { newPost ->
                    val currentPosts = when (val state = uiState.value.postsState) {
                        is ViewState.Success -> state.data
                        else -> emptyList()
                    }

                    val currentUser = when (val state = uiState.value.userState) {
                        is ViewState.Success -> state.data
                        else -> null
                    }

                    currentUser?.let { user ->
                        val updatedPosts = currentPosts + PostWithUser(newPost, user)
                        _uiState.update {
                            it.copy(
                                postsState = ViewState.Success(updatedPosts),
                                mode = PostMode.ADD,
                                postImage = null,
                                postOperation = ViewState.Success(Unit)
                            )
                        }
                    }

                    _eventFlow.emit(UiEvent.ShowSnackbar("Post berhasil dibuat"))
                    _eventFlow.emit(UiEvent.Navigate)
                }.onFailure {
                    _uiState.update {
                        it.copy(postOperation = ViewState.Error("Gagal membuat post: $it"), isUiBlocked = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(postOperation = ViewState.Error("Terjadi kesalahan: ${e.message}"), isUiBlocked = false)
                }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.update { it.copy(photoUrl = uri) }
    }

    fun resetPostState() {
        _uiState.update {
            it.copy(postOperation = ViewState.Idle)
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                description = "",
                photoUrl = null,
                mode = PostMode.ADD,
                postImage = null
            )
        }
    }

    fun unblockUi() {
        _uiState.update { it.copy(isUiBlocked = false) }
    }
}