package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.NewsListBaseObject
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent


class NewsCommunicatePresenterObserver : ViewModel()
{
    var cancelRefreshData : SingleLiveEvent<Boolean> = SingleLiveEvent()
    var settingNewsListData : SingleLiveEvent<NewsListBaseObject> = SingleLiveEvent()
    var articleIDData : SingleLiveEvent<String> = SingleLiveEvent()
    fun onCancelRefreshData()
    {
        cancelRefreshData.setValue(true)
    }

    fun onSettingNewsList(data : NewsListBaseObject?)
    {
        settingNewsListData.setValue(data)
    }

    fun onSetArticleUrl(url : String?)
    {
        articleIDData.setValue(url)
    }
}