package com.littlefox.app.foxschool.`object`.result.introduceSeries

import java.util.ArrayList

class IntroduceSeriesInformationResult
{
    private var id : String = ""
    private var name : String = ""
    private var level : Int = -1
    private var introduction : String = ""
    private var current_count : Int = -1
    private var total_count : Int = -1
    private var categories : String = ""
    private var is_single : String = ""
    private var mp4_url : String = ""
    private var m3u8_url : String = ""
    private var introduce_thumbnail_url : String = ""
    private var characters : ArrayList<IntroduceSeriesCharacterResult> = ArrayList<IntroduceSeriesCharacterResult>()
    private var creators : IntroduceSeriesCreatorsResult? = null

    fun getSeriesID() : String
    {
        return id
    }

    fun getTitle() : String
    {
        return name
    }

    fun getLevel() : Int
    {
        return level
    }

    fun getIntroduction() : String
    {
        return introduction
    }

    fun getCurrentReleaseCount() : Int
    {
        return current_count
    }

    fun getTotalCount() : Int
    {
        return total_count
    }

    fun getCategories() : String
    {
        return categories
    }

    fun getCharacterInformationList() : ArrayList<IntroduceSeriesCharacterResult>
    {
        return characters
    }

    fun getCreatorInformation() : IntroduceSeriesCreatorsResult?
    {
        return creators
    }

    fun getIntroduceVideoMp4() : String
    {
        return mp4_url
    }

    fun getIntroduceVideoHls() : String
    {
        return m3u8_url
    }

    fun getIntroduceThumbnail() : String
    {
        return introduce_thumbnail_url
    }

    val isSingleSeries : Boolean
        get()
        {
            if(is_single == "")
            {
                return false
            }
            return if(is_single == "Y") true else false
        }
}