package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine

class InitCoroutine : BaseCoroutine
{
    private var mDeviceID : String      = ""
    private var mPushAddress : String   = ""
    private var mPushStatus : String    = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_INIT) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : VersionBaseObject
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("device_id", mDeviceID)
            if(mPushAddress.equals("") == false)
            {
                list.put("push_address", mPushAddress)
            }
            if(mPushStatus.equals("") == false)
            {
                list.put("push_on", mPushStatus)
            }
            val response = requestServerPair(mContext, Common.API_INIT, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, VersionBaseObject::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }

            if(result.getData().isForceProgressivePlay)
            {
                CommonUtils.getInstance(mContext)
                    .setSharedPreference(Common.PARAMS_IS_FORCE_PROGRESSIVE_PLAY, true)
            } else
            {
                CommonUtils.getInstance(mContext)
                    .setSharedPreference(Common.PARAMS_IS_FORCE_PROGRESSIVE_PLAY, false)
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mDeviceID = objects[0] as String
        mPushAddress = objects[1] as String
        mPushStatus = objects[2] as String
    }
}