package org.apps.minisosmed.repository

import org.apps.minisosmed.entity.Post

interface IPostRepository {
    suspend fun createPost(description: String?, photoUri: String?): Result<Post>
    suspend fun getAllPost(): Result<List<Post>>
}