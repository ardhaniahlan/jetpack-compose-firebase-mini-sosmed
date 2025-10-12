package org.apps.minisosmed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.apps.minisosmed.entity.Comment
import kotlin.jvm.java

class CommentRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
): ICommentRepository {
    override suspend fun addComment(postId: String, text: String): Result<Comment> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User belum login"))

        return try {
            val commentId = firestore.collection("comments").document().id
            val comment = Comment(
                id = commentId,
                postId = postId,
                userId = user.uid,
                text = text,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("comments")
                .document(commentId)
                .set(comment)
                .await()

            firestore.collection("posts").document(postId)
                .update("commentCount", FieldValue.increment(1))
                .await()

            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCommentsByPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java).orEmpty()
                trySend(comments)
            }

        awaitClose { listener.remove() }
    }
}
