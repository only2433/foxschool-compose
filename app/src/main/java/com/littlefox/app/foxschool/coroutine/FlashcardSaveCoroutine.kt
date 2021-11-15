package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File

class FlashcardSaveCoroutine : BaseCoroutine
{
    private var mContentID : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_FLASHCARD_SAVE)
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

            val response = NetworkUtil.requestServerPair(mContext,
                "${Common.API_FLASHCARD_SAVE}${File.separator}${mContentID}",
                null,
                NetworkUtil.GET_METHOD)
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
        mContentID = objects[0] as String
    }
}