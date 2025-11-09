package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

interface IUserRepository {
    suspend fun updateProfile(displayName: String?, bio: String?, photoUri: String?): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun getAllUsers(): Result<List<User>>
    fun searchUsersByName(query: String, currentUser: String): Flow<List<User>>
}

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

            val updatedResult = getCurrentUser()
            updatedResult.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(Exception("Gagal mengambil data user terbaru: ${it.message}")) }
            )
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        val currentUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("User belum login"))

        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)?.copy(id = document.id)
                ?: User(
                    id = currentUser.uid,
                    displayName = currentUser.displayName,
                    email = currentUser.email,
                    photoUrl = currentUser.photoUrl?.toString(),
                    bio = null
                )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) Result.success(user)
            else Result.failure(Exception("User tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchUsersByName(query: String, currentUser: String): Flow<List<User>> = callbackFlow {
        if (query.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("users")
            .orderBy("displayName")
            .startAt(query)
            .endAt("$query\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(User::class.java)?.copy(id = doc.id) }
                    ?.filter { it.id != currentUser }
                    ?: emptyList()

                trySend(users)
            }

        awaitClose { listenerRegistration.remove() }
    }


}
