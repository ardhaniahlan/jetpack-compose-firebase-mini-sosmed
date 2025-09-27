package org.apps.minisosmed.di

import com.google.firebase.auth.FirebaseAuth
import org.apps.minisosmed.repository.AuthRepositoryImpl
import org.apps.minisosmed.repository.IAuthRepository

object Injection {
    fun provideAuthRepository(): IAuthRepository {
        return AuthRepositoryImpl(FirebaseAuth.getInstance())
    }
}