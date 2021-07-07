package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.quiz.QuizResultViewData

class QuizPresenterDataObserver : ViewModel()
{
    var resultData : MutableLiveData<QuizResultViewData> = MutableLiveData<QuizResultViewData>()

    fun setResult(quizPlayingCount : Int, quizCorrectAnswerCount : Int)
    {
        resultData.value = QuizResultViewData(quizPlayingCount, quizCorrectAnswerCount)
    }
}