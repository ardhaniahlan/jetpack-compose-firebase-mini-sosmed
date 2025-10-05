package org.apps.minisosmed.repository

import android.net.Uri
import org.apps.minisosmed.entity.User

interface IUserRepository {

    suspend fun updateProfile(displayName: String?, bio: String?, photoUri: String?): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): Result<User>

}