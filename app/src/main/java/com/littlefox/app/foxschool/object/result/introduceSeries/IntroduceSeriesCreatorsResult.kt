package com.littlefox.app.foxschool.`object`.result.introduceSeries

import java.util.*

class IntroduceSeriesCreatorsResult
{
    private var description : String = ""
    private var story : ArrayList<CreatorsData> = ArrayList<CreatorsData>()
    private var animation : ArrayList<CreatorsData> = ArrayList<CreatorsData>()
    private var cast : ArrayList<CreatorsData> = ArrayList<CreatorsData>()

    fun getDescription() : String
    {
        return description
    }

    fun getStoryList() : ArrayList<CreatorsData>
    {
        return story
    }

    fun getAnimationList() : ArrayList<CreatorsData>
    {
        return animation
    }

    fun getCastList() : ArrayList<CreatorsData>
    {
        return cast
    }

    inner class CreatorsData
    {
        private var name = ""
        private var part : String = ""

        fun getName() : String
        {
            return name
        }

        fun getPart() : String
        {
            if (part == null)
            {
                return ""
            }
            return part
        }

    }
}