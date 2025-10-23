package org.apps.minisosmed.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.viewmodel.ChatViewModel

@Composable
fun ChatListScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    currentUserId: String,
) {
    val chats by chatViewModel.chatsWithUser.collectAsState()

    LaunchedEffect(Unit) {
        chatViewModel.listenToChats(currentUserId)
    }

    LazyColumn {
        items(chats) { chatItem ->
            val chat = chatItem.chat
            val user = chatItem.otherUser

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("chat/${chat.id}") }
                    .padding(16.dp),
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

                Spacer(Modifier.width(12.dp))

                Column {
                    user.displayName?.let { Text(it, fontWeight = FontWeight.Bold) }
                    Text(chat.lastMessage, color = Color.Gray, maxLines = 1)
                }
            }
        }
    }
}

