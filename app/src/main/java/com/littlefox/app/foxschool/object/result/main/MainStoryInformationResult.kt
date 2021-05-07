package com.littlefox.app.foxschool.`object`.result.main

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*
import kotlin.collections.ArrayList

class MainStoryInformationResult
{
    private var levels : ArrayList<SeriesInformationResult> = ArrayList()
    private val categories : ArrayList<SeriesInformationResult> = ArrayList()

    fun getContentByLevelToList() : ArrayList<SeriesInformationResult>
    {
        return levels;
    }

    fun setContentByLevelToList(data : ArrayList<SeriesInformationResult>)
    {
        levels = data;
    }

    fun getContentByCategoriesToList() : ArrayList<SeriesInformationResult>
    {
        return categories;
    }

}