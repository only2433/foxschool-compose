package com.littlefox.app.foxschool.`object`.result.forum

class ForumBaseResult
{
    private var idx : Int = 0
    private var title : String = ""
    private var regDate : String = ""


    fun getForumId() : String
    {
        return idx as String
    }

    fun getTitle() : String
    {
        return title
    }

    fun getRegisterDate() : String
    {
        return regDate
    }
}