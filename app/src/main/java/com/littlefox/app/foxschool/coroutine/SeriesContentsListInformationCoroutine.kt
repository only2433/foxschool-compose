package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.DetailItemInformationBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log

class SeriesContentsListInformationCoroutine : BaseCoroutine
{
    private var mCurrentDisplayID = ""
    private var mContentType : String = Common.CONTENT_TYPE_STORY

    constructor(context : Context) : super(context, Common.COROUTINE_SERIES_CONTENTS_LIST_INFO) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : DetailItemInformationBaseObject
        synchronized(mSync)
        {
            isRunning = true
            var response : String? = ""
            if(mContentType == Common.CONTENT_TYPE_STORY)
            {
                response = requestServerPair(mContext, Common.API_STORY_DETAIL_LIST + mCurrentDisplayID, null, NetworkUtil.GET_METHOD)
            }
            else if(mContentType == Common.CONTENT_TYPE_SONG)
            {
                response = requestServerPair(mContext, Common.API_SONG_DETAIL_LIST + mCurrentDisplayID, null, NetworkUtil.GET_METHOD)
            }
            result = Gson().fromJson(response, DetailItemInformationBaseObject::class.java)

            Log.f("result : " + result.toString())
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mContentType = objects[0] as String
        mCurrentDisplayID = objects[1] as String
    }
}