package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import java.util.*

class ErrorRequestData : BaseGenerator
{
    constructor(errorCode : Int, contentID : String, serverCode : Int, serverMessage : String, ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", errorCode.toString())
        crashlyticsData.put("content_id", contentID)
        crashlyticsData.put("server_code", serverCode.toString())
        crashlyticsData.put("server_message", serverMessage)
        exception = ex
    }

}