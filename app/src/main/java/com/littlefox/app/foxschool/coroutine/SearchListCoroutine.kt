package com.littlefox.app.foxschool.coroutine

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.SearchListBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log
import java.net.URLEncoder

class SearchListCoroutine: BaseCoroutine
{
    private var mSearchType : String    = ""
    private var mCurrentPage : Int      = 0
    private var mPageItemCount : Int    = 0
    private var mKeyword : String       = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_SEARCH_LIST)

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : SearchListBaseObject

        synchronized(mSync) {
            isRunning = true
            var response: String? = ""

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                Log.f("")
                mKeyword = URLEncoder.encode(mKeyword, "UTF-8")
            }

            if(mSearchType != "")
            {
                // 검색조건 : 전체
                response = NetworkUtil.requestServerPair(
                    mContext,
                    "${Common.API_SEARCH_LIST}?type=${mSearchType}&keyword=${mKeyword}&per_page=${mPageItemCount}&page=${mCurrentPage}",
                    null,
                    NetworkUtil.GET_METHOD
                )
            }
            else
            {
                // 검색조건 : 동화, 동요
                response = NetworkUtil.requestServerPair(
                    mContext,
                    "${Common.API_SEARCH_LIST}?keyword=${mKeyword}&per_page=${mPageItemCount}&page=${mCurrentPage}",
                    null,
                    NetworkUtil.GET_METHOD
                )
            }
            result = Gson().fromJson(response, SearchListBaseObject::class.java)

            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mKeyword        = objects[0] as String
        mCurrentPage    = objects[1] as Int
        mPageItemCount  = objects[2] as Int
        mSearchType     = objects[3] as String
    }
}