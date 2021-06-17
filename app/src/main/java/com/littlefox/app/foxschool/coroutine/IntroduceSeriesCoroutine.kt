package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.IntroduceSeriesBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log

class IntroduceSeriesCoroutine: BaseCoroutine
{
    private var mSeriesID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_INTRODUCE_SERIES){}

    override fun doInBackground() : Any?
    {
        if(isRunning == false)
        {
            return null
        }

        lateinit var result : IntroduceSeriesBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_INTRODUCE_SERIES.toString() + mSeriesID, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, IntroduceSeriesBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mSeriesID = objects[0] as String
    }
}