package org.apps.minisosmed.screen.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.formatTimestamp
import org.apps.minisosmed.repository.ImageRepository

@Composable
fun PostItem(
    user: User,
    post: Post,
    isOwner: Boolean,
    onEditPost: (Post) -> Unit,
    onDeleteClick: (String) -> Unit,
    onShowComments: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            user.photoUrl?.let { base64 ->
                val bitmap by produceState<Bitmap?>(initialValue = null, key1 = user.photoUrl) {
                    value = withContext(Dispatchers.IO) {
                        user.photoUrl.let { ImageRepository().base64ToBitmap(it) }
                    }
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = user.displayName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = formatTimestamp(post.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isOwner) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEditPost(post)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Hapus", color = Color.Red) },
                            onClick = {
                                expanded = false
                                onDeleteClick(post.id)
                            }
                        )
                    }
                }
            }
        }

        post.photoUrl?.let { base64 ->
            val bitmap by produceState<Bitmap?>(initialValue = null, key1 = post.photoUrl) {
                value = withContext(Dispatchers.IO) {
                    post.photoUrl.let { ImageRepository().base64ToBitmap(it) }
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onShowComments(post.id) }) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment"
                )
            }

            Text(
                text = "${post.commentCount}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }

        post.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}