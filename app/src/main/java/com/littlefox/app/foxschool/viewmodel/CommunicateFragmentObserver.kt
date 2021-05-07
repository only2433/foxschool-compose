package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CommunicateFragmentObserver : ViewModel()
{
    var refreshData : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var webviewIDData : MutableLiveData<String> = MutableLiveData<String>()
    var pageLoadData : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var seriesIDData : MutableLiveData<String> = MutableLiveData<String>()
    fun onRequestRefresh()
    {
        refreshData.setValue(true)
    }

    fun onShowWebView(articleID : String?)
    {
        webviewIDData.setValue(articleID)
    }

    fun onPageLoadComplete()
    {
        pageLoadData.setValue(true)
    }

    fun onSeriesShow(seriesID : String?)
    {
        seriesIDData.setValue(seriesID)
    }

}