package com.littlefox.app.foxschool.`object`.result.player

import com.littlefox.app.foxschool.`object`.result.common.ContentsBaseResult
import java.util.*

class PlayItemResult
{
    private var id : String                 = ""
    private var record_key : String         = ""
    private var total_time : Int            = 0
    private var mp4_url : String            = ""
    private var m3u8_url : String           = ""
    private var preview_time : Int          = 0
    private var subtitle_group_count : Int  = 0
    private var next_content : ContentsBaseResult? = null
    private var subtitle : ArrayList<CaptionDetailInformationResult> = ArrayList<CaptionDetailInformationResult>()

    fun getContentID() : String
    {
        return id;
    }

    fun getRecordKey() : String
    {
        return record_key;
    }

    fun getTotalTime() : Int
    {
        return total_time;
    }

    fun getMovieUrl() : String
    {
        return mp4_url;
    }

    fun getMovieHlsUrl() : String
    {
        return m3u8_url;
    }

    fun getPreviewTime() : Int
    {
        return preview_time;
    }

    fun getPageByPageMaxCount() : Int
    {
        return subtitle_group_count;
    }

    fun getCaptionList() : ArrayList<CaptionDetailInformationResult>
    {
        return subtitle;
    }

    fun getNextContentData() : ContentsBaseResult?
    {
        return next_content;
    }
}