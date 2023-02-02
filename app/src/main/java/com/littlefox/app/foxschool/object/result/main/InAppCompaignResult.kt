package com.littlefox.app.foxschool.`object`.result.main

import com.google.gson.annotations.SerializedName

class InAppCompaignResult
{
    @SerializedName("id")
    private val id : Int            = 0

    @SerializedName("article_id")
    private val article_id : Int    = 0

    @SerializedName("title")
    private val title : String      = ""

    @SerializedName("content")
    private val content : String    = ""

    @SerializedName("btn1_use")
    private val btn1_use : String   = ""

    @SerializedName("btn1_mode")
    private val btn1_mode : String  = ""

    @SerializedName("btn1_text")
    private val btn1_text : String  = ""

    @SerializedName("btn1_link")
    private val btn1_link : String  = ""

    @SerializedName("btn2_use")
    private val btn2_use : String   = ""

    @SerializedName("btn2_mode")
    private val btn2_mode : String  = ""

    @SerializedName("btn2_text")
    private val btn2_text : String  = ""

    @SerializedName("not_display_days")
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