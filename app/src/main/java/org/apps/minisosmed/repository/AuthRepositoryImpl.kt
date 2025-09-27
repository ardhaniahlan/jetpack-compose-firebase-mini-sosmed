package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email sudah terdaftar"))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendaftar, coba lagi"))
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<User> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user?.toUser()
            if (firebaseUser != null) Result.success(firebaseUser) else Result.failure(Exception("User null"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Email tidak terdaftar"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Password salah"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan, coba lagi"))
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