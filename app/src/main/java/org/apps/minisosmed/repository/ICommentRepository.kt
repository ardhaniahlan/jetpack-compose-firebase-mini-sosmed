package org.apps.minisosmed.repository

import kotlinx.coroutines.flow.Flow
import org.apps.minisosmed.entity.Comment

interface ICommentRepository {
    suspend fun addComment(postId: String, text: String): Result<Comment>
    suspend fun getCommentsByPost(postId: String): Flow<List<Comment>>
}