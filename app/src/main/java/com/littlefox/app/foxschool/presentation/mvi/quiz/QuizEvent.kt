package com.littlefox.app.foxschool.presentation.mvi.quiz

import com.littlefox.app.foxschool.enumerate.QuizAnswerViewType
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTypeData
import com.littlefox.app.foxschool.presentation.mvi.base.Event

sealed class QuizEvent: Event
{
    data class SetViewPageCount(val size: Int): QuizEvent()
    data class SetTitle(val title: String, val subTitle: String = ""): QuizEvent()
    data class LoadingComplete(val isComplete: Boolean): QuizEvent()
    data class SetResultData(val data: String): QuizEvent()
    data class SetQuizPlayData(val data: QuizTypeData): QuizEvent()
    data class EnableTaskBox(val isShow: Boolean): QuizEvent()
    data class ForceChangePage(val page: Int): QuizEvent()
    data class SetCurrentPage(val page: Int): QuizEvent()
    data class UpdatePlayTime(val time: String): QuizEvent()
    data class UpdateCorrectAnswerText(val answerText: String): QuizEvent()
    data class NotifyAnswerViewType(val type: QuizAnswerViewType): QuizEvent()
    data class EnableSaveButton(val isEnable: Boolean): QuizEvent()
}