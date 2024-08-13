package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBasePagingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ForumApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _forumListData = MutableStateFlow<ForumBaseListResult?>(null)
    val forumListData : StateFlow<ForumBaseListResult?> = _forumListData


    private suspend fun getFAQList(pageCount : Int, currentPage : Int)
    {
        val result = repository.getForumFAQList(pageCount, currentPage)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ForumBaseListResult
                    _forumListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_FORUM_FAQ_LIST)
                }
                else -> {}
            }
        }
    }

    private suspend fun getNewsList(pageCount : Int, currentPage : Int)
    {
        val result = repository.getForumNewsList(pageCount, currentPage)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ForumBaseListResult
                    _forumListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_FORUM_NEWS_LIST)
                }
                else -> {}
            }
        }
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        when(data.requestCode)
        {
            RequestCode.CODE_FORUM_FAQ_LIST ->
            {
                mJob = viewModelScope.launch(Dispatchers.Main) {
                    delay(data.duration)
                    getFAQList(
                        data.objects[0] as Int,
                        data.objects[1] as Int
                    )
                }
            }
            RequestCode.CODE_FORUM_NEWS_LIST ->
            {
                mJob = viewModelScope.launch(Dispatchers.Main) {
                    delay(data.duration)
                    getNewsList(
                        data.objects[0] as Int,
                        data.objects[1] as Int
                    )
                }
            }
            else -> {}
        }
    }

    fun getPagingData() : Flow<PagingData<ForumBasePagingResult>>
    {
        val result: Flow<PagingData<ForumBasePagingResult>> = repository.getForumListStream().cachedIn(viewModelScope)
        return result
    }


}