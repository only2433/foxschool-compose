package com.littlefox.app.foxschool.`object`.result.content

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*

class DetailItemInformationResult
{
    private var latest_study_content_id : String = ""
    private var contents : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var children : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()
    private var series : SeriesInformation? = null

    val seriesID : String
        get() = series?.getID() ?: ""

    val isSingleSeries : Boolean
        get() = series?.isSingle ?: true

    val seriesLevel : Int
        get() = series?.getLevel() ?: 0

    val seriesARLevel : String
        get()
        {
            if(series == null || series?.getARLevel() == 0.0f)
            {
                return "0.0"
            }
            else
                return series!!.getARLevel().toString()
        }

    val lastStudyContentID : String
        get() = latest_study_content_id ?: ""

    /**
     * 시리즈가 연재중인지 여부를 확인
     */
    val isStillOnSeries : Boolean
        get()
        {
            if(series == null)
            {
                return false
            }
            return if(contents.size < series!!.getTotalCount()) true else false
        }

    fun getContentsList() : ArrayList<ContentsBaseResult>
    {
        return contents
    }

    fun getCategoryList() : ArrayList<SeriesInformationResult>
    {
        return children
    }

    inner class SeriesInformation
    {
        private var id : String = ""
        private var is_single : String = ""
        private var level : Int = -1
        private var total_count : Int = 0
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

        fun getID() : String
        {
            return id
        }

        fun getLevel() : Int
        {
            return level
        }

        fun getTotalCount() : Int
        {
            return total_count
        }

        fun getARLevel() : Float
        {
            return ar_level
        }
    }
}