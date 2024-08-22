package com.littlefox.app.foxschool.presentation.viewmodel.intro

import com.littlefox.app.foxschool.enumerate.IntroViewMode
import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseState
import com.littlefox.app.foxschool.presentation.viewmodel.base.VoidState

data class IntroState(
    val base: BaseState = BaseState(),
    val bottomType: IntroViewMode = IntroViewMode.DEFAULT,
    val progressPercent: Float = 0f,
    val dialogSelectUpdate: VoidState = VoidState(),
    val dialogForceUpdate: VoidState = VoidState(),
    val dialogFilePermission: VoidState = VoidState(),
    val showDialogPasswordChange: PasswordGuideType = PasswordGuideType.CHANGE90,
    val hideDialogPasswordChange: VoidState = VoidState()
) : BaseState()