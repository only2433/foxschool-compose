package com.littlefox.app.foxschool.`object`.result.main

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*

class MainStoryInformationResult
{
    private var levels : ArrayList<SeriesInformationResult>? = null
    private val categories : ArrayList<SeriesInformationResult>? = null

    fun getContentByLevelToList() : ArrayList<SeriesInformationResult>?
    {
        return levels;
    }

    fun setContentByLevelToList(data : ArrayList<SeriesInformationResult>?)
    {
        levels = data;
    }

    fun getContentByCategoriesToList() : ArrayList<SeriesInformationResult>?
    {
        return categories;
    }

}