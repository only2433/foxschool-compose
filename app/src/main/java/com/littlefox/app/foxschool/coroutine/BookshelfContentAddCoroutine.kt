package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.common.NetworkUtil.requestServerPair
import com.littlefox.library.system.coroutine.BaseCoroutine

import java.io.File
import java.util.*

class BookshelfContentAddCoroutine : BaseCoroutine
{
    private var mBookshelfID = ""
    private var mSendDataList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var  result : BookshelfBaseObject
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            for(i in mSendDataList.indices)
            {
                list.put("content_ids[$i]", mSendDataList[i].getID())
            }
            val respose : String? = requestServerPair(mContext, Common.API_BOOKSHELF + File.separator + mBookshelfID + File.separator + "contents", list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(respose, BookshelfBaseObject::class.java)

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
        mSendDataList = objects[1] as ArrayList<ContentsBaseResult>
    }
}