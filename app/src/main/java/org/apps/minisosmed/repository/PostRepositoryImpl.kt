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

            val postId = firestore.collection("posts").document().id
            val post = Post(
                id = postId,
                userId = userId,
                description = description,
                photoUrl = photoUri,
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

    override suspend fun deletePost(postId: String): Result<Unit> {
        val currentUser = firebaseAuth.currentUser ?: return Result.failure(Exception("User belum login"))

        return try {
            val postRef = firestore.collection("posts").document(postId)
            val snapshot = postRef.get().await()

            if (!snapshot.exists()){
                return Result.failure(Exception("Post tidak ditemukan"))
            }

            val postUserId = snapshot.getString("userId")
            if (postUserId != currentUser.uid){
                return Result.failure(Exception("Tidak punya izin untuk menghapus post ini"))
            }
            postRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun updatePost(
        postId: String,
        newDescription: String,
    ): Result<Unit> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .update("description", newDescription)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}