package com.littlefox.app.foxschool.api.di

import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.viewmodel.api.*
import com.littlefox.app.foxschool.api.viewmodel.api.PlayerApiViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.SearchViewModel
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
    fun provideFoxSchoolRepository() : FoxSchoolRepository
    {
        return FoxSchoolRepository(ApiService.create())
    }

    @Provides
    fun provideIntroApiViewModel() : IntroApiViewModel
    {
        return IntroApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideLoginApiViewModel() : LoginApiViewModel
    {
        return LoginApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideMainApiViewModel() : MainApiViewModel
    {
        return MainApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideSeriesContentsApiViewModel() : SeriesContentsListApiViewModel
    {
        return SeriesContentsListApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideManagementMyBooksApiViewModel() : ManagementMyBooksApiViewModel
    {
        return ManagementMyBooksApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun providePlayerApiViewModel() : PlayerApiViewModel
    {
        return PlayerApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideQuizApiViewModel() : QuizApiViewModel
    {
        return QuizApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideFlashcardApiViewModel() : FlashcardApiViewModel
    {
        return FlashcardApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideBookshelfApiViewModel() : BookshelfApiViewModel
    {
        return BookshelfApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideForumApiViewModel() : ForumApiViewModel
    {
        return ForumApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideSearchApiViewModel() : SearchApiViewModel
    {
        return SearchApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideStudentHomeworkApiViewModel() : StudentHomeworkApiViewModel
    {
        return StudentHomeworkApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideTeacherHomeworkApiViewModel() : TeacherHomeworkApiViewModel
    {
        return TeacherHomeworkApiViewModel(provideFoxSchoolRepository())
    }

    @Provides
    fun provideTeacherHomeworkCheckingApiViewModel() : TeacherHomeworkCheckingApiViewModel
    {
        return TeacherHomeworkCheckingApiViewModel(provideFoxSchoolRepository())
    }
}