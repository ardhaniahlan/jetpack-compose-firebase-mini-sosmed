package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.Post

interface IPostRepository {
    suspend fun createPost(description: String?, photoUri: String?): Result<Post>
    suspend fun getAllPost(): Flow<List<Post>>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun updatePost(postId: String, newDescription: String): Result<Unit>
    suspend fun getPostById(postId: String): Result<Post>
    suspend fun getPostByUserId(userId: String): Flow<List<Post>>
}

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

    override suspend fun getAllPost(): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.toObjects(Post::class.java).orEmpty()
                trySend(posts)
            }

        awaitClose { listener.remove() }
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

    override suspend fun getPostById(postId: String): Result<Post> = try {
        val doc = firestore.collection("posts").document(postId).get().await()
        val post = doc.toObject(Post::class.java)!!.copy(id = doc.id)
        Result.success(post)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPostByUserId(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null){
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Post::class.java)?.copy(id = document.id)
                }.orEmpty().sortedByDescending { it.createdAt }

                trySend(posts)
            }
        awaitClose { listener.remove() }
    }


}