package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.HomeworkManageCalenderBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log
import java.io.File

class HomeworkManageStudentCoroutine : BaseCoroutine
{
    private var mSearchYear : String = ""
    private var mSearchMonth : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT)
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

        lateinit var result : HomeworkManageCalenderBaseObject
        var response : String = ""
        synchronized(mSync)
        {
            isRunning = true
            if(mSearchYear != "" && mSearchMonth != "")
            {
                response = NetworkUtil.requestServerPair(mContext,
                    "${Common.API_HOMEWORK_MANAGE_STUDENT}${File.pathSeparator}${mSearchYear}${File.pathSeparator}${mSearchMonth}",
                    null,
                    NetworkUtil.GET_METHOD)!!
            }
            else
            {
                response = NetworkUtil.requestServerPair(mContext,
                    Common.API_HOMEWORK_MANAGE_STUDENT,
                    null,
                    NetworkUtil.GET_METHOD)!!
            }

            result = Gson().fromJson(response, HomeworkManageCalenderBaseObject::class.java)
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