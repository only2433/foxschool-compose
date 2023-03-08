package com.littlefox.app.foxschool.api.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBasePagingResult
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ForumListViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    fun getPagingData() : Flow<PagingData<ForumBasePagingResult>>
    {
        val result:  Flow<PagingData<ForumBasePagingResult>> = repository.getForumListStream().cachedIn(viewModelScope)
        return result
    }
}
