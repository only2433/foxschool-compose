package com.littlefox.app.foxschool.api.di

import android.app.Application
import android.content.Context
import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.viewmodel.api.IntroApiViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Injection
{
    @Singleton
    @Provides
    fun provideFoxSchoolRepository() : FoxSchoolRepository{
        return FoxSchoolRepository(ApiService.create())
    }

    @Provides
    fun provideIntroApiViewModel() : IntroApiViewModel {
        return IntroApiViewModel(provideFoxSchoolRepository())
    }

}