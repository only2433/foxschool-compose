package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.data.forum.InquireData
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.enumerate.InquireType
import com.littlefox.library.system.coroutine.BaseCoroutine

class InquireCoroutine : BaseCoroutine
{
    private lateinit var mInquireData : InquireData

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_INQUIRE) {}

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return null
        }
        lateinit var result : BaseResult
        synchronized(mSync) {
            isRunning = true
            val list = ContentValues()
            list.put("inq_type", mInquireData.getInquireType())
            list.put("inq_text", mInquireData.getInquireText())
            list.put("user_email", mInquireData.getUserEmail())

            val response = NetworkUtil.requestServerPair(
                mContext,
                Common.API_INQUIRE,
                list,
                NetworkUtil.POST_METHOD
            )
            result = Gson().fromJson(response, VersionBaseObject::class.java)
            if(result.getAccessToken() != "")
            {
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, result.getAccessToken())
            }
        }
        return result
    }

    override fun setData(vararg `object` : Any?)
    {
        mInquireData = `object` as InquireData
    }
}
