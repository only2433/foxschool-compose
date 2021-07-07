package com.littlefox.app.foxschool.`object`.result.quiz

import java.util.*

class QuizInformationResult
{
    private val id : Int                                = -1
    private val time : Int                              = -1
    private val quiz_count : Int                        = -1
    private val type : String                           = ""
    private val correct_image_url : String              = ""
    private val incorrect_image_url : String            = ""
    private val content : ContentInformation?           = null
    private val questions : ArrayList<QuizItemResult>?  = null

    fun getQuizId() : Int = id

    fun getTimeLimit() : Int = time

    fun getQuizCount() : Int = quiz_count

    fun getType() : String = type

    fun getCorrectImageUrl() : String = correct_image_url

    fun getInCorrectImageUrl() : String = incorrect_image_url

    fun getContentsId() : String = content!!.id

    fun getTitle() : String = content!!.name

    fun getSubTitle() : String = content!!.sub_name ?: ""

    fun getLevel() : Int = content!!.level

    fun getQuestionItemInformationList() : ArrayList<QuizItemResult>? = questions

    fun getCorrectImageFileName() : String = "${getContentsId()}_quiz_merge.png"

    fun getInCorrectImageFileName() : String = "${getContentsId()}_incorrect_merge.png"

    private class ContentInformation
    {
        var id : String        = ""
        var name : String      = ""
        var sub_name : String  = ""
        var level : Int        = -1
    }
}