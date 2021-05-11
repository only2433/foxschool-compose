package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.UserInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.lang.Exception

class AuthMeCoroutine : BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_ME) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : UserInformationBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_ME, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, UserInformationBaseObject::class.java)
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                Feature.IS_FREE_USER = false
                if(result.getData().getRemainingDay() > 0)
                {
                    Feature.IS_REMAIN_DAY_END_USER = false
                } else
                {
                    Feature.IS_REMAIN_DAY_END_USER = true
                }
                LittlefoxLocale.setLocale(result.getData().getCountryCode())
            }
            else
            {
                Feature.IS_FREE_USER = true
            }
            return result
        }
    }

    override fun setData(vararg objects : Any?) {}
}