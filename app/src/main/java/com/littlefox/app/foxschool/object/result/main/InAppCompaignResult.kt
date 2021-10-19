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

    fun getID() : Int = id

    fun getArticleID() : Int = article_id

    fun getTitle() : String = title

    fun getContent() : String = content

    fun getButton1Mode() : String = btn1_mode

    fun getButton1Text() : String = btn1_text

    fun getButton1Link() : String = btn1_link

    fun getButton2Mode() : String = btn2_mode

    fun getButton2Text() : String = btn2_text

    fun getNotDisplayDays() : Int = not_display_days
}