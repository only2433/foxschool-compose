package com.littlefox.app.foxschool.presentation.mvi.quiz

import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent

sealed class QuizAction : Action
{
    object PageSelected: QuizAction()
    object ClickSaveStudyInformation: QuizAction()
    object ClickQuizPlaySound: QuizAction()
    object ClickNextQuiz: QuizAction()
    object ClickReplay: QuizAction()
    data class SelectUserAnswer(val data: QuizUserInteractionData): QuizAction()
}