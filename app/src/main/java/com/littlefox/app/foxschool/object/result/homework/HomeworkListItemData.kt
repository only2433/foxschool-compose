package com.littlefox.app.foxschool.`object`.result.homework

import com.littlefox.app.foxschool.enumerate.ContentType
import com.littlefox.app.foxschool.enumerate.HomeworkType

class HomeworkListItemData
{
    private var is_compleate : String        = ""
    private var compleate_date : String      = ""
    private var content_id : String         = ""
    private var content_type : String       = ""
    private var thumbnail_url : String      = ""
    private var hw_type : String            = ""
    private var title : String              = ""

    val isComplete : Boolean
    get()
    {
        if(is_compleate.equals("Y"))
            return true

        return false
    }

    fun getCompleteDate() : String = compleate_date

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
}