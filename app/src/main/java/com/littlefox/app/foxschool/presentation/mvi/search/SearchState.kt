package com.littlefox.app.foxschool.presentation.mvi.search

import androidx.paging.PagingData
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.presentation.mvi.base.State
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Query

data class SearchState(
    val isContentsLoading: Boolean = false,
    val searchResult: Flow<PagingData<ContentBasePagingResult>>? = null
) : State