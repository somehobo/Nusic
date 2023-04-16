package com.njbrady.nusic.utils.di

import android.content.Context
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.LocalStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped


@Module
@InstallIn(ActivityRetainedComponent::class)
class ExoMiddleManModule {

    @Provides
    @ActivityRetainedScoped
    fun provideExoMiddleMan(@ApplicationContext context: Context): ExoMiddleMan {
        return ExoMiddleMan(context)
    }
}