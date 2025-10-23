package org.apps.minisosmed.repository

import kotlinx.coroutines.flow.Flow
import org.apps.minisosmed.entity.User

interface IUserRepository {

    suspend fun updateProfile(displayName: String?, bio: String?, photoUri: String?): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun getAllUsers(): Result<List<User>>
    fun searchUsersByName(query: String, currentUser: String): Flow<List<User>>


}