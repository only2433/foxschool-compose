package com.littlefox.app.foxschool.`object`.result.forum

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ForumBaseResult
{
    // TODO 김태은 테스트용 나중에 지울 것
    constructor(isnew : String, index : Int, title : String, regDate : String)
    {
        this.isnew = isnew
        this.idx = index
        this.title = title
        this.regDate = regDate
    }

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
        if (regDate != null)
        {
            val dateFormat : DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val resultFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)
            val date = dateFormat.parse(regDate)
            return resultFormat.format(date)
        }
        return regDate
    }
}