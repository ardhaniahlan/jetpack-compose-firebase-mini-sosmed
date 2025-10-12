package org.apps.minisosmed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.apps.minisosmed.repository.IAuthRepository
import org.apps.minisosmed.repository.ICommentRepository
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository

class ViewModelFactory(
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val imageRepository: ImageRepository,
    private val commentRepository: ICommentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(authRepository) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(userRepository, imageRepository) as T

            modelClass.isAssignableFrom(PostViewModel::class.java) ->
                PostViewModel(postRepository, userRepository, imageRepository) as T

            modelClass.isAssignableFrom(CommentViewModel::class.java) ->
                CommentViewModel(commentRepository, userRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}