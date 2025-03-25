package com.littlefox.app.foxschool.presentation.mvi.quiz

import com.littlefox.app.foxschool.enumerate.QuizAnswerViewType
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTypeData
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class QuizState(
    val viewPageCount: Int = 0,
    val title: String = "",
    val subTitle: String = "",
    val isLoadingComplete: Boolean = false,
    val resultData: String = "",
    val quizPlayData: QuizTypeData = QuizTypeData.Default,
    val showTaskBox: Boolean = false,
    val forceChangePage: Int = 0,
    val currentPage: Int = 0,
    val playTime: String = "",
    val answerCorrectText: String = "",
    val answerViewType: QuizAnswerViewType = QuizAnswerViewType.HIDE,
    val enableSaveButton: Boolean = true
): State