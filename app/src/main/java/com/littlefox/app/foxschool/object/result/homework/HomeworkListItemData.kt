package com.littlefox.app.foxschool.`object`.result.homework

class HomeworkListItemData
{
    private var is_complete : String        = ""
    private var complete_date : String      = ""
    private var content_id : String         = ""
    private var thumbnail_url : String      = ""
    private var hw_type : String            = ""
    private var title : String              = ""

    val isComplete : Boolean
    get()
    {
        if(is_complete.equals("Y"))
            return true

        return false
    }

    fun getCompleteDate() : String
    {
        return complete_date
    }

    fun getContentID() : String
    {
        return content_id
    }

    fun getThumbnailUrl() : String
    {
        return thumbnail_url
    }

    fun getHomeworkType() : String
    {
        return hw_type
    }

    fun getTitle() : String
    {
        return title
    }
}