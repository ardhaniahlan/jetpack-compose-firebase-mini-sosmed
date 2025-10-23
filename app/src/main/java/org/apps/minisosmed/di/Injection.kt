package org.apps.minisosmed.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.minisosmed.repository.AuthRepositoryImpl
import org.apps.minisosmed.repository.ChatRepositoryImpl
import org.apps.minisosmed.repository.CommentRepositoryImpl
import org.apps.minisosmed.repository.IAuthRepository
import org.apps.minisosmed.repository.IChatRepository
import org.apps.minisosmed.repository.ICommentRepository
import org.apps.minisosmed.repository.IPostRepository
import org.apps.minisosmed.repository.IUserRepository
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.repository.PostRepositoryImpl
import org.apps.minisosmed.repository.UserRepositoryImpl

object Injection {
    fun provideAuthRepository(): IAuthRepository {
        return AuthRepositoryImpl(FirebaseAuth.getInstance())
    }

    fun provideUserRepository(): IUserRepository {
        return UserRepositoryImpl(FirebaseAuth.getInstance())
    }

    fun provideImageRepository(): ImageRepository {
        return ImageRepository()
    }

    fun providePostRepository(): IPostRepository {
        return PostRepositoryImpl(FirebaseAuth.getInstance())
    }

    fun provideCommentRepository(): ICommentRepository {
        return CommentRepositoryImpl(FirebaseAuth.getInstance())
    }

    fun provideChatRepository(): IChatRepository {
        return ChatRepositoryImpl(FirebaseAuth.getInstance())
    }
}