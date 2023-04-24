package com.littlefox.app.foxschool.api.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.enumerate.ForumType
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult

class ForumFragmentViewModel : ViewModel()
{
    private val _cancelRefreshData = MutableLiveData<Boolean>()
    val cancelRefreshData : LiveData<Boolean> = _cancelRefreshData

    private val _forumListData = MutableLiveData<ForumBaseListResult>()
    val forumListData : LiveData<ForumBaseListResult> = _forumListData

    private val _articleUrlData = MutableLiveData<String>()
    val articleUrlData : LiveData<String> = _articleUrlData

    private val _forumTypeData = MutableLiveData<ForumType>()
    val forumTypeData : LiveData<ForumType> = _forumTypeData

    fun onCancelRefresh()
    {
        _cancelRefreshData.value = true
    }

    fun onSettingForumList(data : ForumBaseListResult)
    {
        _forumListData.value = data
    }

    fun onSetArticleURL(articleUrl: String)
    {
        _articleUrlData.value = articleUrl
    }

    fun onSetForumType(forumType : ForumType)
    {
        _forumTypeData.value = forumType
    }
}