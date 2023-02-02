package com.littlefox.app.foxschool.base

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.logmonitor.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File

/**
 * Created by 정재현 on 2017-12-14.
 */
@HiltAndroidApp
public class MainApplication : Application()
{
    override fun onCreate()
    {
        super.onCreate()

        instance = this
    }

    companion object {

        lateinit var instance: MainApplication

        /** 1920 pixel 을 기준으로 각 pixel 에 곱해야 하는 factor  */
        var sDisplayFactor: Float = 0.0f

        /** 1080 height pixel 을 기준으로 각 pixel 에 곱해야 하는 factor  */
        var sDisplayHeightFactor = 0.0f

        /** 해당 변수는 static 변수도 어느순간 값을 회수되기 때문에 값을 가지고 있어야 하기 때문  */
        var sDisPlayMetrics: DisplayMetrics? = null
    }

    fun getDeviceUserAgent(): String
    {
        val deviceType =
            if(CommonUtils.getInstance(applicationContext).checkTablet)
                Common.DEVICE_TYPE_TABLET
            else
                Common.DEVICE_TYPE_PHONE
        val userAgent = Common.HTTP_HEADER_APP_NAME + ":" + deviceType + File.separator + CommonUtils.getInstance(applicationContext)!!.getPackageVersionName(
            Common.PACKAGE_NAME) + File.separator + Build.MODEL + File.separator + Common.HTTP_HEADER_ANDROID + ":" + Build.VERSION.RELEASE
        return userAgent
    }

    fun getUserToken(): String
    {
        var token = CommonUtils.getInstance(applicationContext)!!.getSharedPreferenceString(Common.PARAMS_ACCESS_TOKEN)
        Log.f("token : ${token}")
        return token
    }

    fun setUserToken(token: String)
    {
        Log.f("token : ${token}")
        CommonUtils.getInstance(applicationContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, token)
    }
}