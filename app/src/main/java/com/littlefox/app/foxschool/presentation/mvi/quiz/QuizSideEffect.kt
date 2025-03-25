package com.littlefox.app.foxschool.presentation.mvi.quiz

import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class QuizSideEffect: SideEffect()
{
    data class ShowWarningMessageDialog(val text: String): QuizSideEffect()
}