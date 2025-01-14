package com.littlefox.app.foxschool.presentation.mvi.intro

import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class IntroAction: Action
{
    object ActivateEasterEgg: IntroAction()
    object DeactivateEasterEgg: IntroAction()
    object ClickIntroduce: IntroAction()
    object ClickLogin: IntroAction()
    object ClickHomeButton: IntroAction()
    object ClickLaterButton: IntroAction()
    object ClickKeepButton: IntroAction()
    data class ClickChangeButton(val oldPassword: String, val newPassword: String, val confirmPassword: String): IntroAction()
}