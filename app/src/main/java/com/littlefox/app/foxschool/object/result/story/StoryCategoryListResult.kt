package com.littlefox.app.foxschool.`object`.result.story

import java.util.*

class StoryCategoryListResult
{
    private var theme_info : TitleData? = null
    private var children = ArrayList<SeriesInformationResult>()

    fun getID() : String
    {
        if(theme_info != null)
        {
            return theme_info!!.id
        }
        return ""
    }

    fun getName() : String
    {
        if(theme_info != null)
        {
            return theme_info!!.name
        }
        return ""
    }

    fun getInformationList() : ArrayList<SeriesInformationResult>
    {
        return children
    }

    inner class TitleData
    {
        var id : String = ""
        var name : String = ""
    }
}