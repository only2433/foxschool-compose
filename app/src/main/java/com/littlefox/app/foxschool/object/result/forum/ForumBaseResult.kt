package com.littlefox.app.foxschool.`object`.result.forum

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ForumBaseResult
{
    private var isnew : String = ""
    private var idx : Int = 0
    private var title : String = ""
    private var regDate : String = ""

    fun isShowNewIcon() : Boolean
    {
        if (isnew == "Y")
        {
            return true
        }
        return false
    }

    fun getForumId() : String
    {
        return idx.toString()
    }

    fun getTitle() : String
    {
        return title
    }

    fun getRegisterDate() : String
    {
        val dateFormat : DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val resultFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)
        val date = dateFormat.parse(regDate)
        return resultFormat.format(date)
    }
}