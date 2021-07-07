package com.littlefox.app.foxschool.`object`.data.quiz

class QuizResultViewData
{
    private var quizPlayingCount : Int          = 0
    private var quizCorrectAnswerCount : Int    = 0

    constructor(quizPlayingCount : Int, quizCorrectAnswerCount : Int)
    {
        this.quizPlayingCount = quizPlayingCount
        this.quizCorrectAnswerCount = quizCorrectAnswerCount
    }

    fun getQuizPlayingCount() : Int = quizPlayingCount
    fun getQuizCorrectAnswerCount() : Int = quizCorrectAnswerCount
}