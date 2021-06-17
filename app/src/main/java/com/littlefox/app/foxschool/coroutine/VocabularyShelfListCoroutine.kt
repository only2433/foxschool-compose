package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfListItemBaseObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.lang.Exception

class VocabularyShelfListCoroutine : BaseCoroutine
{
    private var mVocabularyID = ""
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_VOCABULARY_SHELF) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == false)
        {
            return null
        }
        lateinit var result : VocabularyShelfListItemBaseObject
        synchronized(mSync) {
            isRunning = true
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_VOCABULARY_SHELF + mVocabularyID, null, NetworkUtil.GET_METHOD)
            result = Gson().fromJson(response, VocabularyShelfListItemBaseObject::class.java)
            if(result.getAccessToken().equals("") == false)
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mVocabularyID = objects[0] as String
    }
}