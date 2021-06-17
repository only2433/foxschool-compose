package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.library.system.coroutine.BaseCoroutine


class VocabularyCreateCoroutine : BaseCoroutine
{
    private var mSelectBookColor : BookColor? = null
    private var mBookName = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_VOCABULARY_CREATE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : VocabularyShelfBaseObject
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("name", mBookName)
            list.put("color", CommonUtils.getInstance(mContext).getBookColorString(mSelectBookColor))
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_VOCABULARY_SHELF, list, NetworkUtil.POST_METHOD)
            result = Gson().fromJson(response, VocabularyShelfBaseObject::class.java)
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