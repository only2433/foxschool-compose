package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class LoginCoroutine : BaseCoroutine
{
    private var mLoginID : String = ""
    private var mPassword : String = "";
    private var mSchoolID : String = "";

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_LOGIN) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return false
        }
        lateinit var result : LoginBaseObject

        synchronized(mSync)
        {
            isRunning = true
            val list = ContentValues()
            list.put("login_id", mLoginID)
            list.put("password", mPassword)
            list.put("school_id", mSchoolID)
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_LOGIN, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, LoginBaseObject::class.java)
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mLoginID = objects[0] as String
        mPassword = objects[1] as String
        mSchoolID = objects[2] as String
    }
}