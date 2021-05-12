package com.littlefox.app.foxschool.`object`.result.main

class InAppCompaignResult
{
    private val id : Int            = 0
    private val article_id : Int    = 0
    private val title : String      = ""
    private val content : String    = ""
    private val btn1_use : String   = ""
    private val btn1_mode : String  = ""
    private val btn1_text : String  = ""
    private val btn1_link : String  = ""
    private val btn2_use : String   = ""
    private val btn2_mode : String  = ""
    private val btn2_text : String  = ""
    private val not_display_days : Int  = 0

    val isButton1Use : Boolean
        get()
        {
            if(btn1_use == "Y")
            {
                return true
            }
            else
                return false
        }

    val isButton2Use : Boolean
        get()
        {
            if(btn2_use == "Y")
            {
                return true
            }
            else
                return false
        }

    fun getID() : Int
    {
        return id;
    }

    fun getArticleID() : Int
    {
        return article_id;
    }

    fun getTitle() : String
    {
        return title;
    }

    fun getContent() : String
    {
        return content;
    }

    fun getButton1Mode() : String
    {
        return btn1_mode;
    }

    fun getButton1Text() : String
    {
        return btn1_text;
    }

    fun getButton1Link() : String
    {
        return btn1_link;
    }

    fun getButton2Mode() : String
    {
        return btn2_mode;
    }

    fun getButton2Text() : String
    {
        return btn2_text;
    }

    fun getNotDisplayDays() : Int
    {
        return not_display_days;
    }
}