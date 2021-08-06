package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class MyInfoUpdateCoroutine : BaseCoroutine
{
    private var mUserName : String = ""
    private var mUserEmail : String = ""
    private var mUserPhoneNumber : String = ""
    private var mUserPassword : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_MY_INFO_UPDATE) {}

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
            list.put("name", mUserName)

            // 이메일, 비밀번호는 선택사항
            if(mUserEmail != "")
            {
                list.put("email", mUserEmail)
            }
            if(mUserPhoneNumber != "")
            {
                list.put("phone", mUserPhoneNumber)
            }

            list.put("password", mUserPassword)
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_MY_INFO_UPDATE, list, NetworkUtil.POST_METHOD)
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
        mUserName = objects[0] as String
        mUserEmail = objects[1] as String
        mUserPhoneNumber = objects[2] as String
        mUserPassword = objects[3] as String
    }
}