package com.littlefox.app.foxschool.presentation.viewmodel.quiz

import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class QuizEvent : BaseEvent()
{
    data class onSelectedUserAnswer(val data: QuizUserInteractionData) : QuizEvent()

    object onPageSelected : QuizEvent()
    object onClickSaveStudyInformation : QuizEvent()
    object onClickQuizPlaySound : QuizEvent()
    object onClickNextQuiz : QuizEvent()
    object onClickReplay : QuizEvent()

}