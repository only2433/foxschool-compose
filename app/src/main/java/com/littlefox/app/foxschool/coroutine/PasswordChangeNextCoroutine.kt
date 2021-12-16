package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

/**
 * 비밀번호 다음에 변경
 */
class PasswordChangeNextCoroutine : BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_PASSWORD_CHANGE_NEXT) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : BaseResult
        synchronized(mSync) {
            isRunning = true

            val response = NetworkUtil.requestServerPair(mContext, Common.API_PASSWORD_CHANGE_NEXT, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, BaseResult::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }

        return result
    }

    override fun setData(vararg objects : Any?) {}
}