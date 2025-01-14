package com.littlefox.app.foxschool.presentation.mvi.intro

import com.littlefox.app.foxschool.enumerate.IntroViewMode
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class IntroEvent: Event
{
    data class ChangeViewMode(val mode: IntroViewMode): IntroEvent()
    data class UpdatePercent(val percent: Float): IntroEvent()
}