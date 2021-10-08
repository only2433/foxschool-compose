package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.HomeworkStatusListBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class HomeworkStatusListCoroutine : BaseCoroutine
{
    private var mHomeworkID : String = ""
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_HOMEWORK_STATUS_LIST)

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : HomeworkStatusListBaseObject
        synchronized(mSync)
        {
            isRunning = true
            var response = NetworkUtil.requestServerPair(
                mContext,
                "${Common.API_HOMEWORK_STATUS_LIST}${File.separator}${mHomeworkID}",
                null,
                NetworkUtil.GET_METHOD
            )

            result = Gson().fromJson(response, HomeworkStatusListBaseObject::class.java)

            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mHomeworkID = objects[0] as String
    }

}