package org.apps.minisosmed.repository

import kotlinx.coroutines.flow.Flow
import org.apps.minisosmed.entity.Post

interface IPostRepository {
    suspend fun createPost(description: String?, photoUri: String?): Result<Post>
    suspend fun getAllPost(): Flow<List<Post>>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun updatePost(postId: String, newDescription: String): Result<Unit>
}