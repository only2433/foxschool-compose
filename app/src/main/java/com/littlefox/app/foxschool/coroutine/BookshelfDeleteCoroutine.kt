package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class BookshelfDeleteCoroutine : BaseCoroutine
{
    private var mBookshelfID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_BOOKSHELF_DELETE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : BaseResult
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_BOOKSHELF + File.separator + mBookshelfID, null, NetworkUtil.DELETE_METHOD)
            result = Gson().fromJson(response, BaseResult::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mBookshelfID = objects[0] as String
    }
}