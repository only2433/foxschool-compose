package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForumFragmentObserver : ViewModel()
{
    var refreshData : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var webviewIDData : MutableLiveData<String> = MutableLiveData<String>()
    var pageLoadData : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var seriesIDData : MutableLiveData<String> = MutableLiveData<String>()

    fun onRequestRefresh()
    {
        refreshData.value = true
    }

    fun onShowWebView(articleID : String)
    {
        webviewIDData.value = articleID
    }

    fun onPageLoadComplete()
    {
        pageLoadData.value = true
    }

    fun onSeriesShow(seriesID : String)
    {
        seriesIDData.value = seriesID
    }
}