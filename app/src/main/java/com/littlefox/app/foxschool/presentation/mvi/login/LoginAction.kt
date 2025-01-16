package com.littlefox.app.foxschool.presentation.mvi.login

import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.viewmodel.login.LoginEvent

sealed class LoginAction : Action
{
    object ClickFindID : LoginAction()
    object ClickFindPassword : LoginAction()
    object ClickLaterButton : LoginAction()
    object ClickKeepButton : LoginAction()
    object SelectSchoolName: LoginAction()

    data class InputSchoolNameChanged(val name: String) : LoginAction()
    data class CheckAutoLogin(val autoLogin: Boolean) : LoginAction()
    data class ClickLogin(val data: UserLoginData) : LoginAction()
    data class ClickChangeButton(val oldPassword: String, val newPassword: String, val confirmPassword: String) : LoginAction()
}