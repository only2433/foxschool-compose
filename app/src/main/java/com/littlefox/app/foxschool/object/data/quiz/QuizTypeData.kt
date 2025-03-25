package com.littlefox.app.foxschool.`object`.data.quiz

sealed class QuizTypeData
{
    object Default: QuizTypeData()
    data class Picture(val list: ArrayList<QuizPictureData>) : QuizTypeData()
    data class Text(val list: ArrayList<QuizTextData>) : QuizTypeData()
    data class SoundText(val list: ArrayList<QuizTextData>) : QuizTypeData()
    data class Phonics(val list: ArrayList<QuizPhonicsTextData>) : QuizTypeData()
}