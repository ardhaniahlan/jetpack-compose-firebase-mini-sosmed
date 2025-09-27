package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.User

class AuthRepositoryImpl (
    private val firebaseAuth: FirebaseAuth
) : IAuthRepository{
    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user

            if (firebaseUser != null){
                val profileUpdate = userProfileChangeRequest {
                    this.displayName = displayName
                }
                firebaseUser.updateProfile(profileUpdate).await()

                Result.success(firebaseUser.toUser())
            } else{
                Result.failure(Exception("User null"))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }


    fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email ?: "",
            displayName = displayName ?: "",
            photoUrl = photoUrl?.toString() ?: ""
        )
    }
}