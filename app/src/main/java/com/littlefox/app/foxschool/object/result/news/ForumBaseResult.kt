package com.littlefox.app.foxschool.`object`.result.news

class ForumBaseResult
{
    private var id : String = ""
    private var title : String = ""
    private var hit_count : Int = 0
    private var recom_count : Int = 0
    private var thumbnail_url : String = ""
    private var reg_date : String = ""

    val thumbnailUrl : String
        get() = thumbnail_url ?: ""

    fun getForumId() : String
    {
        return id
    }

    fun getTitle() : String
    {
        return title
    }

    fun getHitCount() : Int
    {
        return hit_count
    }

    fun getRecommandCount() : Int
    {
        return recom_count
    }

    fun getRegisterDate() : String
    {
        return reg_date
    }
}