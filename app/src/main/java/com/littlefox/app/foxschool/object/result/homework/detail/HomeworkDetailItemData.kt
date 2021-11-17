package com.littlefox.app.foxschool.`object`.result.homework.detail

import com.littlefox.app.foxschool.enumerate.ContentType
import com.littlefox.app.foxschool.enumerate.HomeworkType

class HomeworkDetailItemData
{
    private var is_complete : String        = ""
    private var complete_date : String      = ""
    private var content_id : String         = ""
    private var content_type : String       = ""
    private var thumbnail_url : String      = ""
    private var hw_type : String            = ""
    private var title : String              = ""

    // 선생님 데이터
    private var mp3_expired : Int           = 0
    private var mp3_path : String           = ""

    val isComplete : Boolean
    get()
    {
        if(is_complete.equals("Y"))
            return true
        return false
    }

    fun getCompleteDate() : String = complete_date

    fun getContentID() : String = content_id

    fun getContentType() : ContentType
    {
        when (content_type)
        {
            "S" -> return ContentType.STORY
            "M" -> return ContentType.SONG
            "G" -> return ContentType.GAME
            else -> return ContentType.STORY
        }
    }

    fun getThumbnailUrl() : String = thumbnail_url

    fun getHomeworkType() : HomeworkType
    {
        when (hw_type)
        {
            "A" -> return HomeworkType.ANIMATION
            "E" -> return HomeworkType.EBOOK
            "Q" -> return HomeworkType.QUIZ
            "C" -> return HomeworkType.CROSSWORD
            "S" -> return HomeworkType.STARWORDS
            "R" -> return HomeworkType.RECORDER
            else -> return HomeworkType.ANIMATION
        }
    }

    fun getTitle() : String = title

    // 선생님용 ----------
    fun getExpired() : Int = mp3_expired

    fun getMp3Path() : String = mp3_path
}