package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.library.system.coroutine.BaseCoroutine

class BookshelfCreateCoroutine : BaseCoroutine
{
    private var mSelectBookColor : BookColor? = null
    private var mBookName = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_BOOKSHELF_CREATE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : BookshelfBaseObject
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("name", mBookName)
            list.put("color", CommonUtils.getInstance(mContext).getBookColorString(mSelectBookColor))
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_BOOKSHELF, list, NetworkUtil.PUT_METHOD)
            result = Gson().fromJson(response, BookshelfBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mBookName = objects[0] as String
        mSelectBookColor = objects[1] as BookColor
    }
}