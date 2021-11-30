package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyInfoShowFragmentDataObserver : ViewModel()
{
    var clickAutoLogin = MutableLiveData<Boolean>()
    var clickPush = MutableLiveData<Boolean>()
    var clickInfoChange = MutableLiveData<Boolean>()
    var clickPasswordChange = MutableLiveData<Boolean>()

    fun onClickAutoLoginSwitch()
    {
        clickAutoLogin.value = true
    }

    fun onClickPushSwitch()
    {
        clickPush.value = true
    }

    fun onClickInfoChange()
    {
        clickInfoChange.value = true
    }

    fun onClickPasswordChange()
    {
        clickPasswordChange.value = true
    }
}