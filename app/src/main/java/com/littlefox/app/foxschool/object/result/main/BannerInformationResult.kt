package com.littlefox.app.foxschool.`object`.result.main

class BannerInformationResult
{
    private val id : String             = ""
    private val link_url : String       = ""
    private val image_url : String      = ""
    private val type : String           = ""
    private val article_num : String    = ""


    fun getBannerID() : String
    {
        return id
    }

    fun getLinkUrl() : String
    {
        return link_url
    }

    fun getImageUrl() : String
    {
        return image_url
    }

    fun getType() : String
    {
        return type
    }

    fun getArticleNumber() : String
    {
        return article_num
    }
}