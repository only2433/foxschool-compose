package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.TeacherClassListBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class TeacherClassListCoroutine : BaseCoroutine
{
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_TEACHER_CLASS_LIST)
    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : TeacherClassListBaseObject

        synchronized(mSync)
        {
            isRunning = true
            val response = NetworkUtil.requestServerPair(mContext, Common.API_TEACHER_CLASS_LIST, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, TeacherClassListBaseObject::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg `object` : Any?) {}

}