package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.StoryCategoryListBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine


class StoryCategoryListInformationCoroutine : BaseCoroutine
{
    private var mCurrentDisplayID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_STORY_CATEGORY_LIST_INFO) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : StoryCategoryListBaseObject
        synchronized(mSync) {
            isRunning = true
            var response : String? = ""
            response = requestServerPair(mContext, Common.API_STORY_DETAIL_LIST + mCurrentDisplayID, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, StoryCategoryListBaseObject::class.java)
            if(result.getAccessToken().equals("") === false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mCurrentDisplayID = objects[0] as String
    }
}