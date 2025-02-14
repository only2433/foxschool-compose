package com.littlefox.app.foxschool.api.base

import com.google.gson.Gson
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.base.MainApplication
import com.littlefox.app.foxschool.common.Common
import com.littlefox.logmonitor.Log
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>) : ResultData<T>?
{
    var result: ResultData<T>? = null
    try
    {
        val response = call.invoke()
        if(response.isSuccessful)
        {
            when(val temp = response.body())
            {
                is BaseResponse<*> ->
                {
                    if(temp.isSuccess)
                    {
                        if (temp.data != null)
                        {
                            result = ResultData.Success(temp.data as T)
                        }
                        else
                        {
                            result = ResultData.Success(temp)
                        }
                    }
                    else
                    {
                        result = ResultData.Fail(temp.status, temp.message)
                    }

                    if(temp.access_token != "")
                    {
                        Log.f("temp.access_token : ${temp.access_token}")
                        MainApplication.instance.setUserToken(temp.access_token)
                    }
                }
            }
        }
        else
        {
            Log.i("------- Error")
            result = getErrorMessageFromGenericResponse(response.errorBody())
        }

    }
    catch (e: HttpException)
    {
        e.printStackTrace()
        result = ResultData.Fail(Common.FAIL_CODE_NETWORK_NOT_CONNECT,"Http Internet error")
    }
    catch (e: SocketTimeoutException)
    {
        result = ResultData.Fail(Common.FAIL_CODE_INTERNAL_SERVER_ERROR,"요청한 시간이 초과되었습니다.", )
    }
    catch (e: UnknownHostException)
    {
        result = ResultData.Fail(Common.FAIL_CODE_NETWORK_NOT_CONNECT,"네트워크에 연결할 수 없습니다.\n" +
                "네트워크 상태 확인 후 다시 시도해 주세요", )
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        result = ResultData.Fail(Common.FAIL_CODE_INTERNAL_SERVER_ERROR,e.message ?: "Internet error runs")
    }
    return result
}

fun getNetworkErrorJson() : String
{
    val data = JSONObject()
    try
    {
        data.put("status", BaseResult.FAIL_CODE_NETWORK_NOT_CONNECT)
        data.put("message", "네트워크에 접속할 수 없습니다.\n네트워크 연결 상태를 확인해 주세요.")
    }
    catch(e : JSONException)
    {
        e.printStackTrace()
    }
    return data.toString()
}

private fun getErrorMessageFromGenericResponse(body: ResponseBody?): ResultData.Fail?
{
    Log.i("")
    var data: ResultData.Fail? = null
    try
    {
        val adapter = Gson().getAdapter(BaseResponse::class.java)
        val errorParser = adapter.fromJson(body?.string())

        Log.i("error status : ${errorParser.status}, message: ${errorParser.message}")
        if(errorParser.access_token != "")
        {
            Log.f("temp.access_token : ${errorParser.access_token}")
            MainApplication.instance.setUserToken(errorParser.access_token)
        }
        return ResultData.Fail(errorParser.status, errorParser.message)
    }
    catch (e: java.lang.Exception)
    {
        e.printStackTrace()
    }

    return ResultData.Fail(105, "Internal Error")
}

private fun getErrorMessageFromGenericResponse(httpException: HttpException): String?
{
    var errorMessage: String? = null
    try
    {
        val body = httpException.response()?.errorBody()
        val adapter = Gson().getAdapter(BaseResult::class.java)
        val errorParser = adapter.fromJson(body?.string())
        errorMessage = errorParser.getMessage()

        Log.i("error Data : ${errorParser.toString()}")
    }
    catch (e: IOException)
    {
        e.printStackTrace()
    }
    return errorMessage
}