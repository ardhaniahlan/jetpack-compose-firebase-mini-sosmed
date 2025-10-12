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
import org.apps.minisosmed.state.CommentUiState

class CommentViewModel(
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _commentsPerPost = mutableStateMapOf<String, List<CommentWithUser>>()
    val commentsPerPost: Map<String, List<CommentWithUser>> get() = _commentsPerPost

    private val _uiState = mutableStateOf(CommentUiState())
    val uiState: State<CommentUiState> = _uiState

    fun loadComments(postId: String) {
        viewModelScope.launch {
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = e.message)
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