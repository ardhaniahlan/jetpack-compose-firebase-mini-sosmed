package org.apps.minisosmed.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.apps.minisosmed.entity.relation.CommentWithUser
import org.apps.minisosmed.repository.ICommentRepository
import org.apps.minisosmed.repository.IUserRepository
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.apps.minisosmed.state.CommentUiState
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _commentsPerPost = mutableStateMapOf<String, List<CommentWithUser>>()
    val commentsPerPost: Map<String, List<CommentWithUser>> get() = _commentsPerPost

    private val _commentState = MutableStateFlow<ViewState<Unit>>(ViewState.Idle)
    val commentState: StateFlow<ViewState<Unit>> = _commentState

    private val _uiState = mutableStateOf(CommentUiState())
    val uiState: State<CommentUiState> = _uiState

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _commentState.value = ViewState.Loading
            try {
                commentRepository.getCommentsByPost(postId).collect { comments ->
                    val usersResult = userRepository.getAllUsers()

                    val users = usersResult.getOrDefault(emptyList())
                    val userMap = users.associateBy { it.id }

                    val enriched = comments.mapNotNull { comment ->
                        val user = userMap[comment.userId]
                        user?.let { CommentWithUser(comment, it) }
                    }

                    _commentsPerPost[postId] = enriched
                    _uiState.value = _uiState.value.copy(comments = enriched)
                    _commentState.value = ViewState.Success(Unit)
                }
            } catch (e: Exception) {
                _commentState.value = ViewState.Error("Gagal memuat comment: ${e.message}")
            }
        }
    }


    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            if (text.isBlank()) return@launch
            commentRepository.addComment(postId, text)
        }
    }
}