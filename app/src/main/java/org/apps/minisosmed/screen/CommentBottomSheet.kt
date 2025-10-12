package org.apps.minisosmed.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.relation.CommentWithUser
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.viewmodel.CommentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: String,
    commentViewModel: CommentViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by commentViewModel.uiState
    var commentText by remember { mutableStateOf("") }
    val isLoading = uiState.isLoading
    
    val comments = commentViewModel.commentsPerPost[postId] ?: emptyList()

    LaunchedEffect(postId) {
        commentViewModel.loadComments(postId)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Komentar",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Tutup")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            CommentScreenContent(
                commentWithUser = comments,
                modifier = Modifier.weight(1f)
            )
        }

        Divider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Tambahkan komentar...") },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            )

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        commentViewModel.addComment(postId, commentText)
                        commentText = ""
                    }
                },
                enabled = commentText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Kirim")
                }
            }
        }
    }
}


@Composable
fun CommentScreenContent(
    commentWithUser: List<CommentWithUser>,
    modifier: Modifier = Modifier
) {
    if (commentWithUser.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada komentar")
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(commentWithUser, key = { it.comment.id }) { item ->
                CommentItem(commentWithUser = item)
            }
        }
    }
}



@Composable
fun CommentItem(commentWithUser: CommentWithUser) {
    val user = commentWithUser.user
    val comment = commentWithUser.comment

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        user.photoUrl?.let { base64 ->
            val bitmap by produceState<Bitmap?>(initialValue = null, key1 = base64) {
                value = withContext(Dispatchers.IO) {
                    ImageRepository().base64ToBitmap(base64)
                }
            }
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = user.displayName ?: "Unknown", fontWeight = FontWeight.Bold)
            Text(text = comment.text)
        }
    }
}
