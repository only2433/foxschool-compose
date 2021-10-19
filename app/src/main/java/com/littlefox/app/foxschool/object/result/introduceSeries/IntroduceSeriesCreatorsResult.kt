package com.littlefox.app.foxschool.`object`.result.introduceSeries

import java.util.*

class IntroduceSeriesCreatorsResult
{
    private var description : String = ""
    private var story : ArrayList<CreatorsData> = ArrayList<CreatorsData>()
    private var animation : ArrayList<CreatorsData> = ArrayList<CreatorsData>()
    private var cast : ArrayList<CreatorsData> = ArrayList<CreatorsData>()

    fun getDescription() : String = description

    fun getStoryList() : ArrayList<CreatorsData> = story

    fun getAnimationList() : ArrayList<CreatorsData> = animation

    fun getCastList() : ArrayList<CreatorsData> = cast

    inner class CreatorsData
    {
        private var name = ""
        private var part : String = ""

        fun getName() : String = name

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