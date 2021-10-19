package com.littlefox.app.foxschool.`object`.result.content

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*

class DetailItemInformationResult
{
    private var latest_study : String = ""
    private var list : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var children : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()
    private var info : ArrayList<SeriesInformation> = ArrayList<SeriesInformation>()

    val seriesID : String
        get() = info[0].getID() ?: ""

    val isSingleSeries : Boolean
        get() = info[0].isSingle ?: true

    val seriesLevel : Int
        get() = info[0].getLevel() ?: 0

    val seriesARLevel : String
        get()
        {
            if(info[0].getARLevel() == 0.0f)
            {
                return "0.0"
            }
            else
                return info[0].getARLevel().toString()
        }

    val lastStudyContentID : String
        get() = latest_study ?: ""

    /**
     * 시리즈가 연재중인지 여부를 확인
     */
    val isStillOnSeries : Boolean
        get()
        {
            return if(list.size < info[0].getTotalCount()) true else false
        }

    fun getContentsList() : ArrayList<ContentsBaseResult> = list

    fun getCategoryList() : ArrayList<SeriesInformationResult> = children

    inner class SeriesInformation
    {
        private var id : String = ""
        private var is_single : String = ""
        private var level : Int = -1
        private var contents_count : Int = 0
        private var ar_level = 0.0f

        val isSingle : Boolean
            get()
            {
                if(is_single == null || is_single == "")
                {
                    return false
                }
                return if(is_single == "Y") true else false
            }

        fun getID() : String = id

        fun getLevel() : Int = level

        fun getTotalCount() : Int = contents_count

        fun getARLevel() : Float = ar_level
    }
}