package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import java.util.*

class ErrorVideoPlayData : BaseGenerator
{
    constructor(contentID : String, videoUrl : String, title : String, subTitle : String, ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", CrashlyticsHelper.ERROR_CODE_VIDEO_ENCORDING.toString())
        crashlyticsData.put("content_id", contentID)
        crashlyticsData.put("video_url", videoUrl)
        crashlyticsData.put("title", title)
        if(subTitle == "" == false)
        {
            crashlyticsData.put("subtitle", subTitle)
        }
        exception = ex
    }

}