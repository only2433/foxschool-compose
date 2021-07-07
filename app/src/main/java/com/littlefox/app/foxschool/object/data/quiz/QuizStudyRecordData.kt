package com.littlefox.app.foxschool.`object`.data.quiz

import java.util.*

class QuizStudyRecordData
{
    private var mContentId : String = ""
    private var mQuizResultInformationList : ArrayList<QuizUserInteractionData> = ArrayList<QuizUserInteractionData>()

    constructor(contentId : String, quizResultInformation : ArrayList<QuizUserInteractionData>)
    {
        mContentId = contentId
        mQuizResultInformationList = quizResultInformation
    }

    fun getContentId() : String = mContentId

    fun getQuizResultInformationList() : ArrayList<QuizUserInteractionData> = mQuizResultInformationList
}