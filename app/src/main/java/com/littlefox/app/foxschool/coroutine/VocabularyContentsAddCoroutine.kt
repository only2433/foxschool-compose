package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

import java.io.File
import java.util.*

class VocabularyContentsAddCoroutine : BaseCoroutine
{
    private var mVocabularyID = ""
    private var mContentID = ""
    private var mSendDataList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_VOCABULARY_CONTENTS_ADD) {}
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
            list.put("content_id", mContentID)
            for(i in mSendDataList.indices)
            {
                list.put("word_ids[$i]", mSendDataList[i].getID())
            }
            val response : String? = NetworkUtil.requestServerPair(mContext, Common.API_VOCABULARY_SHELF + File.separator + mVocabularyID + File.separator + "words", list, NetworkUtil.PUT_METHOD)
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
        mContentID = objects[0] as String
        mVocabularyID = objects[1] as String
        mSendDataList = objects[2] as ArrayList<VocabularyDataResult>
    }
}