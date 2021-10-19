package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.QuizBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log
import java.io.File

class QuizInformationRequestCoroutine : BaseCoroutine
{
    private var mContentId : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_QUIZ_INFORMATION)

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : QuizBaseObject
        synchronized(mSync) {
            isRunning = true
            val response = requestServerPair(
                mContext,
                "${Common.API_QUIZ}${mContentId}",
                null,
                NetworkUtil.GET_METHOD
            )
            result = Gson().fromJson(response, QuizBaseObject::class.java)
            Log.i("status : ${result.getStatus()}")

            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mContentId = objects[0] as String
    }
}