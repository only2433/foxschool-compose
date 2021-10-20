package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File


class StudyLogSaveCoroutine : BaseCoroutine
{
    private var mContentsID : String = ""
    private var mContentPlayType : String  = ""
    private var mStudyPlayTime : Int = 0
    private var mHomeworkNumber : Int = 0


    constructor(context : Context) : super(context, Common.COROUTINE_CODE_STUDY_LOG_SAVE) {}
    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : BaseResult
        synchronized(mSync)
        {
            isRunning = true
            val list = ContentValues()
            list.put("content_id", mContentsID)
            list.put("play_type", mContentPlayType)
            list.put("play_time", mStudyPlayTime)
            if(mHomeworkNumber != 0)
            {
                list.put("hw_no", mHomeworkNumber)
            }

            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_STUDY_LOG_SAVE, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, BaseResult::class.java)
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mContentsID = objects[0] as String
        mContentPlayType = objects[1] as String
        mStudyPlayTime = objects[2] as Int
        if(objects.size > 3)
        {
            mHomeworkNumber = objects[3] as Int
        }
    }
}