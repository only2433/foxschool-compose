package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.MainInformationBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log
import java.lang.Exception

class MainInformationCoroutine: BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_MAIN) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : MainInformationBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_MAIN, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, MainInformationBaseObject::class.java)
            Log.f("result status : " + result?.getStatus())
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?) {}
}