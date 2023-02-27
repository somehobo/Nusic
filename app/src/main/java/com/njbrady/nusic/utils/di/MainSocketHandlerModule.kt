package com.njbrady.nusic.utils.di

import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.utils.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient


@Module
@InstallIn(ActivityRetainedComponent::class)
class MainSocketHandlerModule {

    @Provides
    @ActivityRetainedScoped
    fun provideTokenStorage(tokenStorage: TokenStorage): MainSocketHandler {
        return MainSocketHandler(OkHttpClient(), tokenStorage)
    }
}