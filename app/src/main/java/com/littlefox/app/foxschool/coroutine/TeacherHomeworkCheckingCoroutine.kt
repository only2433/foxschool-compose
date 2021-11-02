package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine

class TeacherHomeworkCheckingCoroutine : BaseCoroutine
{
    private var mHomeworkNumber : Int = 0
    private var mClassID : Int = 0
    private var mUserID : String = ""
    private var mEvaluationState : String = ""
    private var mEvaluationComment : String = ""

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_TEACHER_HOMEWORK_CHECKING)
    {
        mHomeworkNumber = 0
        mClassID = 0
        mUserID = ""
        mEvaluationState = ""
        mEvaluationComment = ""
    }

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

            val list = ContentValues()
            list.put("hw_no", mHomeworkNumber)
            list.put("school_class_id", mClassID)
            list.put("fu_id", mUserID)
            list.put("eval", mEvaluationState)

            if(mEvaluationComment != "")
            {
                list.put("eval_comment", mEvaluationComment)
            }

            val response  = NetworkUtil.requestServerPair(
                mContext,
                Common.API_TEACHER_HOMEWORK,
                list,
                NetworkUtil.POST_METHOD)

            result = Gson().fromJson(response, BaseResult::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }

        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mHomeworkNumber = objects[0] as Int
        mClassID = objects[1] as Int
        mUserID = objects[2] as String
        mEvaluationState = objects[3] as String

        if(objects.size > 4)
        {
            mEvaluationComment = objects[4] as String
        }
    }
}