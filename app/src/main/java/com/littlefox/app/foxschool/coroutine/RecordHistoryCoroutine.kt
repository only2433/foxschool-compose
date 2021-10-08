package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.RecordHistoryBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log

class RecordHistoryCoroutine: BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_CLASS_RECORD_HISTORY) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : RecordHistoryBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_RECORD_HISTORY, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, RecordHistoryBaseObject::class.java)
            Log.f("result status : " + result?.getStatus())
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?) {}
}