package com.littlefox.app.foxschool.presentation.mvi.intro

import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class IntroSideEffect: SideEffect() {
    object ShowSelectUpdateDialog: IntroSideEffect()
    object ShowForceUpdateDialog: IntroSideEffect()
    object ShowFilePermissionDialog: IntroSideEffect()
    data class ShowPasswordChangeDialog(val type: PasswordGuideType): IntroSideEffect()
    object HidePasswordChangeDialog: IntroSideEffect()
}