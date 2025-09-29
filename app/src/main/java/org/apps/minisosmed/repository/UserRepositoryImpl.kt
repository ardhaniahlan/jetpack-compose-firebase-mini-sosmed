package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IUserRepository{
    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        photoUri: String?,
    ): Result<User> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return Result.failure(Exception("User belum login"))
        }

        return try {
            val userMap = mapOf(
                "displayName" to (displayName ?: currentUser.displayName),
                "email" to currentUser.email,
                "bio" to bio,
                "photoUrl" to photoUri
            )
            firestore.collection("users")
                .document(currentUser.uid)
                .set(userMap, SetOptions.merge())
                .await()

            val updatedUser = getCurrentUser()
            updatedUser?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Gagal mengambil data user terbaru"))
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get().await()

            if (document.exists()){
                document.toObject(User::class.java)
            } else {
                User(
                    id = currentUser.uid,
                    displayName = currentUser.displayName,
                    email = currentUser.email,
                    photoUrl = currentUser.photoUrl?.toString(),
                    bio = null
                )
            }
        } catch (e: Exception){
            null
        }
    }
}
