package com.littlefox.app.foxschool.presentation.viewmodel.quiz

import com.littlefox.app.foxschool.`object`.data.quiz.QuizPhonicsTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData

sealed class QuizTypeData
{
    data class Picture(val list: ArrayList<QuizPictureData>) : QuizTypeData()
    data class Text(val list: ArrayList<QuizTextData>) : QuizTypeData()
    data class SoundText(val list: ArrayList<QuizTextData>) : QuizTypeData()
    data class Phonics(val list: ArrayList<QuizPhonicsTextData>) : QuizTypeData()
}