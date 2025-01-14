package com.littlefox.app.foxschool.presentation.mvi.intro

import com.littlefox.app.foxschool.enumerate.IntroViewMode
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class IntroState(
    val bottomType: IntroViewMode = IntroViewMode.DEFAULT,
    val progressPercent: Float = 0.0f
) : State