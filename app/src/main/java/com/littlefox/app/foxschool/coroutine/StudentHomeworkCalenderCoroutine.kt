package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.HomeworkCalenderBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class StudentHomeworkCalenderCoroutine : BaseCoroutine
{
    private var mSearchYear : String = ""
    private var mSearchMonth : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_STUDENT_HOMEWORK_CALENDER)
    {
        mSearchYear  = ""
        mSearchMonth = ""
    }

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : HomeworkCalenderBaseObject
        var response : String = ""
        synchronized(mSync)
        {
            isRunning = true
            if(mSearchYear != "" && mSearchMonth != "")
            {
                response = NetworkUtil.requestServerPair(mContext,
                    "${Common.API_STUDENT_HOMEWORK}${File.separator}${mSearchYear}${File.separator}${mSearchMonth}",
                    null,
                    NetworkUtil.GET_METHOD)!!
            }
            else
            {
                response = NetworkUtil.requestServerPair(mContext,
                    Common.API_STUDENT_HOMEWORK,
                    null,
                    NetworkUtil.GET_METHOD)!!
            }

            result = Gson().fromJson(response, HomeworkCalenderBaseObject::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mSearchYear = objects[0] as String
        mSearchMonth = objects[1] as String
    }
}