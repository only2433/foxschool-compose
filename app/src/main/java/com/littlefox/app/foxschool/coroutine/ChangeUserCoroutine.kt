package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.UserInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.library.system.coroutine.BaseCoroutine

class ChangeUserCoroutine : BaseCoroutine
{
    private var mChangeUserID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_CHANGE_USER) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : UserInformationBaseObject
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("sub_user_id", mChangeUserID)
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_CHANGE_USER, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, UserInformationBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                Feature.IS_FREE_USER = false
                if(result.getData().getRemainingDay() > 0)
                {
                    Feature.IS_REMAIN_DAY_END_USER = false
                } else
                {
                    Feature.IS_REMAIN_DAY_END_USER = true
                }
                LittlefoxLocale.setLocale(result.getData().getCountryCode())
            } else
            {
                Feature.IS_FREE_USER = true
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mChangeUserID = objects[0] as String
    }
}