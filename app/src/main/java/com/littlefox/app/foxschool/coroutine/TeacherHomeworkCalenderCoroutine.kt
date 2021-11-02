package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.HomeworkCalenderBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class TeacherHomeworkCalenderCoroutine : BaseCoroutine
{
    private var mClassID : String = ""
    private var mSearchYear : String = ""
    private var mSearchMonth : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_TEACHER_HOMEWORK_CALENDER)
    {
        mClassID = ""
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
                    "${Common.API_TEACHER_HOMEWORK}${File.separator}${mClassID}${File.separator}${mSearchYear}${File.separator}${mSearchMonth}",
                    null,
                    NetworkUtil.GET_METHOD)!!
            }
            else
            {
                response = NetworkUtil.requestServerPair(mContext,
                    "${Common.API_TEACHER_HOMEWORK}${File.separator}${mClassID}",
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
        mClassID = objects[0] as String
        if(objects.size > 1)
        {
            mSearchYear = objects[1] as String
            mSearchMonth = objects[2] as String
        }
    }
}