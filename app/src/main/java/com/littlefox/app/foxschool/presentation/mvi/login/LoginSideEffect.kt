package com.littlefox.app.foxschool.presentation.mvi.login

import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class LoginSideEffect: SideEffect()
{
    object HidePasswordChangeDialog: LoginSideEffect()
    data class ShowPasswordChangeDialog(val type: PasswordGuideType): LoginSideEffect()
}