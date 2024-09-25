package com.littlefox.app.foxschool.presentation.viewmodel.login

import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class LoginEvent : BaseEvent()
{
    object onClickFindID : LoginEvent()
    object onClickFindPassword : LoginEvent()
    object onClickLaterButton : LoginEvent()
    object onClickKeepButton : LoginEvent()
    object onSchoolNameSelected: LoginEvent()


    data class onInputSchoolNameChanged(val name: String) : LoginEvent()
    data class onCheckAutoLogin(val autoLogin: Boolean) : LoginEvent()
    data class onClickLogin(val data: UserLoginData) : LoginEvent()
    data class onClickChangeButton(val oldPassword: String, val newPassword: String, val confirmPassword: String) : LoginEvent()

}