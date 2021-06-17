package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VocabularyContentsBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class VocabularyContentsListCoroutine : BaseCoroutine
{
    private var mContentID = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_VOCABULARY_CONTENTS_LIST) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == false)
        {
            return null
        }
        lateinit var result : VocabularyContentsBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_VOCABULARY_CONTENTS + mContentID + "/vocabulary", null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, VocabularyContentsBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
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