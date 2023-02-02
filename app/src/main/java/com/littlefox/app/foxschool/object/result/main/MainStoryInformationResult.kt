package com.littlefox.app.foxschool.`object`.result.main

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*
import kotlin.collections.ArrayList

class MainStoryInformationResult
{
    @SerializedName("levels")
    private var levels : ArrayList<SeriesInformationResult> = ArrayList()

    @SerializedName("categories")
    private val categories : ArrayList<SeriesInformationResult> = ArrayList()

    fun getContentByLevelToList() : ArrayList<SeriesInformationResult> = levels

    fun setContentByLevelToList(data : ArrayList<SeriesInformationResult>)
    {
        levels = data
    }

    fun getContentByCategoriesToList() : ArrayList<SeriesInformationResult> = categories

}