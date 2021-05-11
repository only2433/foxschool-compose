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
    private var mIsClassUser = "N"

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_AUTH_CONTENT_PLAY) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : PlayerDataBaseObject
        synchronized(mSync) {
            isRunning = true
            val resolutionValue : String = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_VIDEO_HIGH_RESOLUTION, "N")
            val response = requestServerPair(mContext, Common.API_AUTH_CONTENT_PLAY.toString() + mContentID + File.separator + "player" + "?is_high_resolution=" + resolutionValue + "&&is_class_user=" + mIsClassUser, null, NetworkUtil.GET_METHOD)
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
        if(objects.size > 1)
        {
            mIsClassUser = objects[1] as String
        }
    }
}