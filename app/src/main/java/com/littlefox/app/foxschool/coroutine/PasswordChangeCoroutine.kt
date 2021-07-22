package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.util.*

class PasswordChangeCoroutine : BaseCoroutine
{
    private var mCurrentPassword : String = ""
    private var mChangePassword : String = ""
    private var mChangePasswordConfirm : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_PASSWORD_CHANGE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : BaseResult
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("password_check", mCurrentPassword)
            list.put("password", mChangePassword)
            list.put("password_confirm", mChangePasswordConfirm)

            val response = NetworkUtil.requestServerPair(mContext, Common.API_PASSWORD_CHANGE, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, BaseResult::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }

        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mCurrentPassword = objects[0] as String
        mChangePassword = objects[1] as String
        mChangePasswordConfirm = objects[2] as String
    }

}
