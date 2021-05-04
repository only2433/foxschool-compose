package com.littlefox.app.foxschool.crashtics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator

class CrashlyticsHelper
{
    companion object
    {
        const val ERROR_CODE_VIDEO_REQUEST : Int        = 10001
        const val ERROR_CODE_VIDEO_ENCORDING : Int      = 10002
        const val ERROR_CODE_QUIZ_REQUEST : Int         = 20001
        const val ERROR_CODE_QUIZ_TYPE_NOT_EQUAL : Int  = 20002
        const val ERROR_CODE_QUIZ_IMAGE_NOT_HAVE : Int  = 20003
        const val ERROR_CODE_PAYMENT_REGISTER : Int     = 30001
        const val ERROR_CODE_PAYMENT_INAPP : Int        = 30002
        const val ERROR_CODE_PAYMENT_COUPON : Int       = 30003
        const val ERROR_CODE_LOGIN : Int                = 40001

        private var sCrashlyticsHelper : CrashlyticsHelper? = null
        private var sContext : Context? = null
        fun getInstance(context : Context) : CrashlyticsHelper
        {
            if(sCrashlyticsHelper == null)
            {
                sCrashlyticsHelper = CrashlyticsHelper()
            }
            sContext = context
            return sCrashlyticsHelper!!
        }
    }

    fun sendCrashlytics(data : Any)
    {
        val result : BaseGenerator = data as BaseGenerator
        for(key in result.crashlyticsData.keys)
        {
            FirebaseCrashlytics.getInstance().setCustomKey(key, result.crashlyticsData.get(key).toString())
        }
        FirebaseCrashlytics.getInstance().recordException(result.exception)
    }
}