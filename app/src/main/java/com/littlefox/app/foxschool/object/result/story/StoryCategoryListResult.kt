package com.littlefox.app.foxschool.`object`.result.story

import java.util.*

class StoryCategoryListResult
{
    private var id : String = ""
    private var name : String = ""
    private var children = ArrayList<SeriesInformationResult>()

    fun getID() : String
    {
        return id
    }

    fun getName() : String
    {
        return name
    }

    fun getInformationList() : ArrayList<SeriesInformationResult>
    {
        return children
    }
}