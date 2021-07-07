package com.littlefox.app.foxschool.coroutine

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.data.quiz.QuizStudyRecordData
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.String

class QuizSaveRecordCoroutine : BaseCoroutine
{
    private var mQuizRequestObject : QuizStudyRecordData? = null
    constructor(context : Context) : super(context, Common.COROUTINE_CODE_QUIZ_SAVE_RECORD)

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

            val jsonArray = JSONArray()
            var jsonObject : JSONObject
            for(i in 0 until mQuizRequestObject!!.getQuizResultInformationList().size)
            {
                jsonObject = JSONObject()
                try
                {
                    jsonObject.put(
                        "chosen_number",
                        String.valueOf(mQuizRequestObject!!.getQuizResultInformationList()[i].getChosenNumber())
                    )
                    jsonObject.put(
                        "correct_number",
                        mQuizRequestObject!!.getQuizResultInformationList()[i].getCorrectNumber()
                    )
                    jsonObject.put(
                        "question_numbers",
                        String.valueOf(mQuizRequestObject!!.getQuizResultInformationList()[i].getQuestionSequence())
                    )
                } catch(e : JSONException)
                {
                    e.printStackTrace()
                }
                jsonArray.put(jsonObject)
            }

            list.put("results_json", jsonArray.toString())

            val response = NetworkUtil.requestServerPair(
                mContext,
                "${Common.API_QUIZ_SAVE_RECORD}${mQuizRequestObject!!.getContentId()}/quiz/result",
                list,
                NetworkUtil.POST_METHOD
            )
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
        mQuizRequestObject = objects[0] as QuizStudyRecordData
    }
}