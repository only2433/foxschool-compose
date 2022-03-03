package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.library.system.coroutine.BaseCoroutine

class AuthMeCoroutine : BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_ME) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : LoginBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_ME, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, LoginBaseObject::class.java)
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
            return result
        }
    }

    override fun setData(vararg objects : Any?) {}
}