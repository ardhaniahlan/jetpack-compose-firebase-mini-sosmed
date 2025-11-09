package org.apps.minisosmed.viewmodel

import android.util.Log.e
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.apps.minisosmed.state.CommentUiState
import org.apps.minisosmed.state.ViewState
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState= _uiState.asStateFlow()

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentState = ViewState.Loading) }

            try {
                commentRepository.getCommentsByPost(postId).collect { comments ->
                    val userIds = comments.map { it.userId }.distinct()

                    val userMap = withContext(Dispatchers.IO) {
                        userIds.map { userId ->
                            async {
                                userId to userRepository.getUserById(userId).getOrNull()
                            }
                        }.awaitAll()
                            .filter { it.second != null }
                            .associate { it.first to it.second!! }
                    }

                    val enrichedComments = comments.mapNotNull { comment ->
                        val user = userMap[comment.userId]
                        user?.let { CommentWithUser(comment, it) }
                    }

                    _uiState.update {
                        it.copy(
                            comments = enrichedComments,
                            commentState = ViewState.Success(Unit)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(commentState = ViewState.Error("Gagal memuat komentar: ${e.message}"))
                }
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