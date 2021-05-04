package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import java.util.*

class ErrorQuizImageNotHaveData : BaseGenerator
{
    constructor(contentID : String,
                quizID : String,
                title : String,
                subTitle : String,
                correctImageUrl : String,
                inCorrectImageUrl : String,
                ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", CrashlyticsHelper.ERROR_CODE_QUIZ_IMAGE_NOT_HAVE.toString())
        crashlyticsData.put("content_id", contentID)
        crashlyticsData.put("quiz_id", quizID)
        crashlyticsData.put("title", title)
        if(subTitle.equals("") == false)
        {
            crashlyticsData.put("subTitle", subTitle)
        }
        crashlyticsData.put("correct_image_url", correctImageUrl)
        crashlyticsData.put("incorrect_image_url", inCorrectImageUrl)
        exception = ex
    }
}