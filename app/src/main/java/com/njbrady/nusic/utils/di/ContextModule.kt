package com.njbrady.nusic.utils.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object MyModule {
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext
}