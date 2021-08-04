package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.enumerate.InputDataType


class MyInfoPresenterDataObserver : ViewModel()
{
    var setMyInfoShowFragment = MutableLiveData<LoginInformationResult>()
    var changeAutoLogin = MutableLiveData<Boolean>()
    var changeBioLogin = MutableLiveData<Boolean>()
    var changePush = MutableLiveData<Boolean>()

    var setMyInfoChangeFragment = MutableLiveData<LoginInformationResult>()
    var setInputErrorView = MutableLiveData<InputDataType>()
    var setSaveInfoButtonEnable = MutableLiveData<Boolean>()
    var setSavePasswordButtonEnable = MutableLiveData<Boolean>()
    var clearMyInfoChangeFragment = MutableLiveData<Boolean>()
    var viewPagerChange = MutableLiveData<Int>()

    /**
     * 나의 정보 화면으로 전달하는 데이터
     */
    fun setMyInfoShowFragment(userInformation : LoginInformationResult)
    {
        setMyInfoShowFragment.value = userInformation
    }

    fun setAutoLoginSwitch(isEnable : Boolean)
    {
        changeAutoLogin.value = isEnable
    }

    fun setBioLoginSwitch(isEnable : Boolean)
    {
        changeBioLogin.value = isEnable
    }

    fun setPushSwitch(isEnable : Boolean)
    {
        changePush.value = isEnable
    }

    /**
     * 나의 정보 수정 / 비밀번호 변경 화면으로 전달하는 데이터
     */
    fun setMyInfoChangeFragment(userInformation : LoginInformationResult)
    {
        setMyInfoChangeFragment.value = userInformation
    }

    fun setInputError(type : InputDataType)
    {
        setInputErrorView.value = type
    }

    fun setSaveInfoButtonEnable(isEnable : Boolean)
    {
        setSaveInfoButtonEnable.value = isEnable
    }

    fun setSavePasswordButtonEnable(isEnable : Boolean)
    {
        setSavePasswordButtonEnable.value = isEnable
    }

    fun clearMyInfoChangeFragment()
    {
        clearMyInfoChangeFragment.value = true
    }

    fun setViewPagerChange(position : Int)
    {
        viewPagerChange.value = position
    }
}