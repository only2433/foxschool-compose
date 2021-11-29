package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.BookshelfListItemBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class BookshelfDetailListInformationCoroutine : BaseCoroutine
{
    private var mBookshelfID : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_BOOKSHELF_DETAIL_LIST_INFO) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }

        lateinit var result : BookshelfListItemBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_BOOKSHELF + File.separator + mBookshelfID, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, BookshelfListItemBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg `object` : Any?)
    {
        mBookshelfID = `object`[0] as String
    }
}