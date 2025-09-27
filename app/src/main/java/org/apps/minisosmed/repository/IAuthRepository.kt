package org.apps.minisosmed.repository

import org.apps.minisosmed.entity.User

interface IAuthRepository {
    suspend fun register(email: String, password: String, displayName: String): Result<User>
}