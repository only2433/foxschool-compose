package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.ForumListBaseObject
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent


class NewsCommunicatePresenterObserver : ViewModel()
{
    var cancelRefreshData : SingleLiveEvent<Boolean> = SingleLiveEvent()
    var settingForumListData : SingleLiveEvent<ForumListBaseObject> = SingleLiveEvent()
    var articleIDData : SingleLiveEvent<String> = SingleLiveEvent()
    fun onCancelRefreshData()
    {
        cancelRefreshData.setValue(true)
    }

    fun onSettingNewsList(data : ForumListBaseObject?)
    {
        settingForumListData.setValue(data)
    }

    fun onSetArticleUrl(url : String?)
    {
        articleIDData.setValue(url)
    }
}