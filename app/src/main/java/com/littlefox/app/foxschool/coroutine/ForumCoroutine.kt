package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.ForumListBaseObject
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class ForumCoroutine : BaseCoroutine
{
    private var mBoardUrl : String = ""
    private var mCurrentPage : Int = 0
    private var mPageItemCount : Int = 0

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_FORUM) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : ForumListBaseObject
        synchronized(mSync)
        {
            isRunning = true

            val response = NetworkUtil.requestServerPair(
                mContext,
                mBoardUrl+"?per_page="+mPageItemCount+"&page="+mCurrentPage,
                null,
                NetworkUtil.GET_METHOD
            )
            result = Gson().fromJson(response, ForumListBaseObject::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }

        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mBoardUrl = objects[0] as String
        mCurrentPage = objects[1] as Int
        mPageItemCount = objects[2] as Int
    }

}