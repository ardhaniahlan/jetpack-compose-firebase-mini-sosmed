package org.apps.minisosmed.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): IAuthRepository =
        AuthRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideUserRepository(auth: FirebaseAuth): IUserRepository =
        UserRepositoryImpl(auth)

    @Provides
    @Singleton
    fun providePostRepository(auth: FirebaseAuth): IPostRepository =
        PostRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideCommentRepository(auth: FirebaseAuth): ICommentRepository =
        CommentRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideChatRepository(auth: FirebaseAuth): IChatRepository =
        ChatRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideImageRepository(): ImageRepository =
        ImageRepository()
}

