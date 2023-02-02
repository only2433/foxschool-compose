package com.littlefox.app.foxschool.api

import android.os.Build
import android.util.Base64
import com.littlefox.app.foxschool.base.MainApplication
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.DataType
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File

class HeaderInterceptor() : Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val token = MainApplication.instance.getUserToken()
        val userAgent = MainApplication.instance.getDeviceUserAgent()

        val origin = chain.request()
        val request = origin.newBuilder()
            .addHeader("api-user-agent", userAgent)
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}