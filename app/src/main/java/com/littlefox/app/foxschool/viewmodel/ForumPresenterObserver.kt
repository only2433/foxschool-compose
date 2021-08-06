package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.ForumListBaseObject
import com.littlefox.app.foxschool.enumerate.ForumType
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class ForumPresenterObserver : ViewModel()
{
    var cancelRefreshData : SingleLiveEvent<Boolean> = SingleLiveEvent()
    var settingForumListData : SingleLiveEvent<ForumListBaseObject> = SingleLiveEvent()
    var articleIDData : SingleLiveEvent<String> = SingleLiveEvent()
    var setForumType : SingleLiveEvent<ForumType> = SingleLiveEvent()

    fun onCancelRefreshData()
    {
        cancelRefreshData.value = true
    }

    fun onSettingForumList(data : ForumListBaseObject?)
    {
        settingForumListData.value = data
    }

    fun onSetArticleUrl(url : String?)
    {
        articleIDData.value = url
    }

    fun setForumType(type : ForumType)
    {
        setForumType.value = type
    }
}