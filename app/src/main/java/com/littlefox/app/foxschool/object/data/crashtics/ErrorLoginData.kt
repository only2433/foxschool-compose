package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import java.util.*

class ErrorLoginData : BaseGenerator
{
    constructor(id : String, serverCode : Int, serverMessage : String, ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("id", id)
        crashlyticsData.put("country_code", Locale.getDefault().toString())
        crashlyticsData.put("server_code", serverCode.toString())
        crashlyticsData.put("server_message", serverMessage)
        exception = ex
    }
}