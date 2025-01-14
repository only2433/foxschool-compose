package com.littlefox.app.foxschool.presentation.mvi.main

import com.littlefox.app.foxschool.`object`.result.main.InAppCompaignResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class MainSideEffect: SideEffect()
{
    object ShowLogoutDialog: MainSideEffect()
    object ShowAppEndDialog: MainSideEffect()
    object ShowNoClassStudentDialog: MainSideEffect()
    object ShowNoClassTeacherDialog: MainSideEffect()
    data class ShowIACDialog(val data: InAppCompaignResult): MainSideEffect()
}