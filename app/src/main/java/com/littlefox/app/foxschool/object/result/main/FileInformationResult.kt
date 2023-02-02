package com.littlefox.app.foxschool.`object`.result.main

import com.google.gson.annotations.SerializedName

class FileInformationResult
{
    @SerializedName("teachers_manual")
    private val teachers_manual : String = ""

    @SerializedName("school_letter")
    private val school_letter : String = ""

    fun getTeacherManualLink() : String = teachers_manual

    fun getHomeNewsPaperLink() : String = school_letter
}