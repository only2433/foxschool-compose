package com.littlefox.app.foxschool.`object`.result.base

import com.littlefox.app.foxschool.`object`.result.quiz.QuizInformationResult

class QuizBaseObject : BaseResult()
{
    private val data : QuizInformationResult? = null

    fun getData() : QuizInformationResult = data!!
}