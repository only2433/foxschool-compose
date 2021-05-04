package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import java.util.*

class ErrorQuizTypeNotEqualData : BaseGenerator
{
    constructor(contentID : String, quizID : String, title : String, subTitle : String, ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", CrashlyticsHelper.ERROR_CODE_QUIZ_TYPE_NOT_EQUAL.toString())
        crashlyticsData.put("content_id", contentID)
        crashlyticsData.put("quiz_id", quizID)
        crashlyticsData.put("title", title)
        if(subTitle == "" == false)
        {
            crashlyticsData.put("subTitle", subTitle)
        }
        exception = ex
    }

}