package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.PlayerDataBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine

import java.io.File

class AuthContentPlayCoroutine : BaseCoroutine
{
    private var mContentID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_AUTH_CONTENT_PLAY) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : PlayerDataBaseObject
        var response : String? = ""
        synchronized(mSync) {
            isRunning = true
            val resolutionValue : String = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_VIDEO_HIGH_RESOLUTION, "N")
            if(resolutionValue.equals("Y"))
            {
                response = requestServerPair(mContext, Common.API_AUTH_CONTENT_PLAY + mContentID + File.separator + "Y", null, NetworkUtil.GET_METHOD)
            }
            else
            {
                response = requestServerPair(mContext, Common.API_AUTH_CONTENT_PLAY + mContentID, null, NetworkUtil.GET_METHOD)
            }

            result = Gson().fromJson(response, PlayerDataBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mContentID = objects[0] as String
    }
}