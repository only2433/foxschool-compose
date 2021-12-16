package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyInfoChangeFragmentDataObserver : ViewModel()
{
    var checkNameAvailable = MutableLiveData<String>()
    var checkEmailAvailable = MutableLiveData<String>()
    var checkPhoneAvailable = MutableLiveData<String>()
    var checkInfoInputDataAvailable = MutableLiveData<MutableMap<String, String>>()
    var clickInfoChangeButton = MutableLiveData<MutableMap<String, String>>()

    var checkNewPasswordAvailable = MutableLiveData<String>()
    var checkNewPasswordConfirm = MutableLiveData<MutableMap<String, String>>()
    var checkPasswordInputDataAvailable = MutableLiveData<MutableMap<String, String>>()
    var clickPasswordChangeButton = MutableLiveData<MutableMap<String, String>>()

    /**
     * 나의 정보 수정 화면 데이터
     */
    fun checkNameAvailable(name : String)
    {
        checkNameAvailable.value = name
    }

    fun checkEmailAvailable(email : String)
    {
        checkEmailAvailable.value = email
    }

    fun checkPhoneAvailable(phone : String)
    {
        checkPhoneAvailable.value = phone
    }

    fun checkInfoInputDataAvailable(name : String, email : String, phone : String)
    {
        val data : MutableMap<String, String> = HashMap()
        data["name"] = name
        data["email"] = email
        data["phone"] = phone
        checkInfoInputDataAvailable.value = data
    }

    fun onClickInfoChangeButton(name : String, email : String, phone : String)
    {
        val data : MutableMap<String, String> = HashMap()
        data["name"] = name
        data["email"] = email
        data["phone"] = phone
        clickInfoChangeButton.value = data
    }

    /**
     * 비밀번호 변경 화면 데이터
     */
    fun checkNewPasswordAvailable(password : String)
    {
        checkNewPasswordAvailable.value = password
    }

    fun checkNewPasswordConfirm(newPassword : String, confirmPassword : String)
    {
        val data : MutableMap<String, String> = HashMap()
        data["newPassword"] = newPassword
        data["confirmPassword"] = confirmPassword
        checkNewPasswordConfirm.value = data
    }

    fun checkPasswordInputDataAvailable(oldPassword : String, newPassword : String, confirmPassword : String)
    {
        val data : MutableMap<String, String> = HashMap()
        data["oldPassword"] = oldPassword
        data["newPassword"] = newPassword
        data["confirmPassword"] = confirmPassword
        checkPasswordInputDataAvailable.value = data
    }

    fun onClickPasswordChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
    {
        val data : MutableMap<String, String> = HashMap()
        data["oldPassword"] = oldPassword
        data["newPassword"] = newPassword
        data["confirmPassword"] = confirmPassword
        clickPasswordChangeButton.value = data
    }
}