package com.littlefox.app.foxschool.`object`.result.main


import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.ArrayList

class MainSongInformationResult
{
    private val categories : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()

    fun getContentByCategoriesToList() : ArrayList<SeriesInformationResult> = categories
}