package com.littlefox.app.foxschool.api


import okhttp3.Interceptor
import okhttp3.ResponseBody
import okio.BufferedSource

import com.littlefox.logmonitor.Log
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

class SaveLogInterceptor : Interceptor
{
    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response
    {
        var buffer: Buffer
        val request: Request = chain.request()
        buffer = Buffer()
        request.body?.writeTo(buffer)
        Log.f("Request ==> [URL] : ${request.url} , [HEADER] : ${request.headers} , [DATA] : ${buffer.readString(UTF8)}")
        val response: Response = chain.proceed(request)
        val responseBody = response.body
        val source = responseBody!!.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        buffer = source.buffer()
        Log.f("Response ==> [URL] : ${request.url} , [CODE] : ${response.code} , [MESSAGE] : ${response.message} , [DATA] : ${buffer.clone().readString(UTF8)}")
        return response
    }
}