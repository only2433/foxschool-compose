package com.littlefox.app.foxschool.crashtics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.littlefox.logmonitor.Log

/**
 * 구글 애널리틱스를 사용하게하는  Helper 클래스
 * @author 정재현
 */
class GoogleAnalyticsHelper
{
    private var mFirebaseAnalytics : FirebaseAnalytics? = null

    companion object
    {
        const val PROPERTY_ID = "UA-37277849-1"
        var sGoogleAnalyticsHelper : GoogleAnalyticsHelper? = null
        fun getInstance(context : Context) : GoogleAnalyticsHelper?
        {
            if(sGoogleAnalyticsHelper == null)
            {
                sGoogleAnalyticsHelper = GoogleAnalyticsHelper()
                sGoogleAnalyticsHelper!!.init(context)
            }
            return sGoogleAnalyticsHelper
        }
    }

    /**
     * 파이어 애널리틱스를 초기화 시킨다.
     * @param context
     */
    private fun init(context : Context)
    {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    /**
     * 현재 사용자의 이벤트를 전달한다.
     * @param category 액티비티
     * @param action 특정 행동
     */
    fun sendCurrentEvent(category : String, action : String)
    {
        Log.i("Category : $category, Action : $action")
        val bundle = Bundle()
        bundle.putString("action", action)
        mFirebaseAnalytics!!.logEvent(category, bundle)
    }

    /**
     * 현재 사용자의 이벤트를 전달한다.
     * @param category 액티비티
     * @param action 특정 행동
     * @param label 특정 정보
     */
    fun sendCurrentEvent(category : String, action : String, label : String)
    {
        Log.i("Category : $category, Action : $action, Label : $label")
        val bundle = Bundle()
        bundle.putString("action", action)
        bundle.putString("label", label)
        mFirebaseAnalytics!!.logEvent(category, bundle)
    }


}