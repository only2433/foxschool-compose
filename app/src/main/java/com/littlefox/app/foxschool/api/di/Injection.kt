package com.littlefox.app.foxschool.api.di

import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.viewmodel.api.IntroApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.LoginApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.MainApiViewModel
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

    @Provides
    fun provideLoginApiViewModel() : LoginApiViewModel {
        return LoginApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideMainApiViewModel() : MainApiViewModel {
        return MainApiViewModel(provideFoxSchoolRepository())
    }

}