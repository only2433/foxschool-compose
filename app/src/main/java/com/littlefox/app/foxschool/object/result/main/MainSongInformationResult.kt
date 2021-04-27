package com.littlefox.app.foxschool.`object`.result.main


import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.ArrayList

class MainSongInformationResult
{
    private val categories : ArrayList<SeriesInformationResult>? = null

    fun getContentByCategoriesToList() : ArrayList<SeriesInformationResult>?
    {
        return categories;
    }
}