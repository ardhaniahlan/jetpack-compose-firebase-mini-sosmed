package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.Post

class PostRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IPostRepository {
    override suspend fun createPost(
        description: String?,
        photoUri: String?,
    ): Result<Post> {
        return try {
            val user  = firebaseAuth.currentUser ?: return Result.failure(Exception("User Belum Login"))
            val userId = user.uid

            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("User profile tidak ditemukan"))
            }

            val displayName = userDoc.getString("displayName") ?: "Anonymous"
            val photoProfile = userDoc.getString("photoUrl")

            val postId = firestore.collection("posts").document().id
            val post = Post(
                id = postId,
                userId = user.uid,
                description = description,
                photoUrl = photoUri,
                displayName = displayName,
                photoProfile = photoProfile,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("posts").document(postId).set(post).await()
            Result.success(post)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getAllPost(): Result<List<Post>> {
        return try {
            val snapshot = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val post = snapshot.toObjects(Post::class.java)
            Result.success(post)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

}