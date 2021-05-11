package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import java.io.File
import java.lang.Exception
import java.util.ArrayList

class VocabularyContentsDeleteCoroutine : BaseCoroutine
{
    private var mVocabularyID = ""
    private var mSendDataList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_VOCABULARY_CONTENTS_DELETE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : BaseResult
        synchronized(mSync) {
            val list = ContentValues()
            for(i in mSendDataList.indices)
            {
                list.put("words[$i][content_id]", mSendDataList[i].getContentID())
                list.put("words[$i][word_id]", mSendDataList[i].getID())
            }
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_VOCABULARY_SHELF + mVocabularyID + File.separator + "words/delete", list, NetworkUtil.POST_METHOD)
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
        mVocabularyID = objects[0] as String
        mSendDataList = objects[1] as ArrayList<VocabularyDataResult>
    }
}