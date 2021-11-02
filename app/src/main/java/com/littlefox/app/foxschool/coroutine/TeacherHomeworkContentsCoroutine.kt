package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.HomeworkDetailListBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class TeacherHomeworkContentsCoroutine : BaseCoroutine
{
    private var mClassID : Int = 0
    private var mHomeworkNumber : Int = 0

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_TEACHER_HOMEWORK_CONTENTS)

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : HomeworkDetailListBaseObject
        synchronized(mSync)
        {
            isRunning = true
            var response = NetworkUtil.requestServerPair(
                mContext,
                "${Common.API_TEACHER_HOMEWORK_CONTENTS}${File.separator}${mClassID}${File.separator}${mHomeworkNumber}",
                null,
                NetworkUtil.GET_METHOD
            )

            result = Gson().fromJson(response, HomeworkDetailListBaseObject::class.java)

            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mClassID = objects[0] as Int
        mHomeworkNumber = objects[1] as Int
    }
}