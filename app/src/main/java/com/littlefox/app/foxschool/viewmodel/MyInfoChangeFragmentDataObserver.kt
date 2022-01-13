package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.myinfo.MyInformationData
import com.littlefox.app.foxschool.`object`.data.myinfo.MyPasswordData

class MyInfoChangeFragmentDataObserver : ViewModel()
{
    var checkInfoInputDataAvailable = MutableLiveData<MyInformationData>()
    var clickInfoChangeButton = MutableLiveData<MyInformationData>()

    var checkPasswordInputDataAvailable = MutableLiveData<MyPasswordData>()
    var clickPasswordChangeButton = MutableLiveData<MyPasswordData>()

    /**
     * 나의 정보 수정 화면 데이터
     */
    fun checkInfoInputDataAvailable(inputData : MyInformationData)
    {
        checkInfoInputDataAvailable.value = inputData
    }

    fun onClickInfoChangeButton(inputData : MyInformationData)
    {
        clickInfoChangeButton.value = inputData
    }

    /**
     * 비밀번호 변경 화면 데이터
     */
    fun checkPasswordInputDataAvailable(passwordData : MyPasswordData)
    {
        checkPasswordInputDataAvailable.value = passwordData
    }

    fun onClickPasswordChangeButton(passwordData : MyPasswordData)
    {
        clickPasswordChangeButton.value = passwordData
    }
}