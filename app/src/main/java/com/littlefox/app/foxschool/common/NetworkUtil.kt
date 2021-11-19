package com.littlefox.app.foxschool.common

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.gson.Gson
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.logmonitor.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.*
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

object NetworkUtil
{
    const val GET_METHOD = 0
    const val POST_METHOD = 1
    const val PUT_METHOD = 2
    const val DELETE_METHOD = 3
    const val TYPE_WIFI = 1
    const val TYPE_MOBILE = 2
    const val TYPE_NOT_CONNECTED = 0
    const val CONNECTION_TIMEOUT = 15000
    const val SOCKET_TIMEOUT = 15000

    fun getConnectivityStatus(context : Context) : Int
    {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if(null != activeNetwork)
        {
            if(activeNetwork.type == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI
            if(activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun isConnectNetwork(context : Context) : Boolean
    {
        if(getConnectivityStatus(context) == TYPE_NOT_CONNECTED)
        {
            return false
        }
        else
            return true
    }

    fun getConnectivityStatusString(context : Context) : String?
    {
        val conn = getConnectivityStatus(context)
        var status : String? = null
        if(conn == TYPE_WIFI)
        {
            status = "Wifi enabled"
        }
        else if(conn == TYPE_MOBILE)
        {
            status = "Mobile data enabled"
        }
        else if(conn == TYPE_NOT_CONNECTED)
        {
            status = "Not connected to Internet"
        }
        return status
    }

    fun getErrorJson(response : String?) : BaseResult?
    {
        var item : BaseResult? = null
        try
        {
            item = Gson().fromJson<BaseResult>(response, BaseResult::class.java)
        }
        catch(e : Exception)
        {
            Log.f("getErrorJson Error : " + e.message)
        }
        return item
    }

    fun getNetworkErrorJson(context : Context) : String
    {
        val data = JSONObject()
        try
        {
            data.put("status", BaseResult.FAIL_CODE_NETWORK_NOT_CONNECT)
            data.put("message", context.resources.getString(R.string.message_toast_network_error))
        }
        catch(e : JSONException)
        {
            e.printStackTrace()
        }
        return data.toString()
    }

    @JvmOverloads
    fun requestServerPair(context : Context, strUrl : String, postDataList : ContentValues?, connectionType : Int, apiVersion : String? = null) : String?
    {
        if(postDataList != null)
        {
            Log.f("request URL : " + strUrl + ", data : " + postDataList.toString() + ", connectionType : " + connectionType)
        }
        else
        {
            Log.f("request URL : $strUrl, connectionType : $connectionType")
        }
        var response : String? = null
        if(isConnectNetwork(context) == false)
        {
            Log.f("NETWORK NOT CONNECTED")
            response = getNetworkErrorJson(context)
            return response
        }
        val url : URL
        val connection : HttpURLConnection
        try
        {
            url = URL(strUrl)
            trustAllHosts()
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.hostnameVerifier = HostnameVerifier {s, sslSession -> true}
            connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.connectTimeout = CONNECTION_TIMEOUT
            val deviceType =
                if(CommonUtils.getInstance(context).checkTablet)
                    Common.DEVICE_TYPE_TABLET
                else
                    Common.DEVICE_TYPE_PHONE
            val token = "Bearer " + CommonUtils.getInstance(context)!!.getSharedPreference(Common.PARAMS_ACCESS_TOKEN, DataType.TYPE_STRING) as String
            val userAgent = Common.HTTP_HEADER_APP_NAME + ":" + deviceType + File.separator + CommonUtils.getInstance(context)!!.getPackageVersionName(Common.PACKAGE_NAME) + File.separator + Build.MODEL + File.separator + Common.HTTP_HEADER_ANDROID + ":" + Build.VERSION.RELEASE
            connection.addRequestProperty("api-user-agent", userAgent)
            if(Feature.IS_FREE_USER == false) connection.addRequestProperty("Authorization", token)
            Log.f("Feature.IS_FREE_USER : " + Feature.IS_FREE_USER)
            Log.f("userAgent : $userAgent")
            Log.f("token : $token")
            connection.addRequestProperty("api-locale", Locale.getDefault().toString())
            if(apiVersion != null)
            {
                connection.addRequestProperty("api_version", apiVersion)
            }
            when(connectionType)
            {
                GET_METHOD -> connection.requestMethod = "GET"
                POST_METHOD -> connection.requestMethod = "POST"
                PUT_METHOD -> connection.requestMethod = "PUT"
                DELETE_METHOD -> connection.requestMethod = "DELETE"
            }
            if(connectionType == GET_METHOD)
            {
                connection.doOutput = false
                connection.doInput = true
            }
            else
            {
                connection.doOutput = true
                connection.doInput = true
                if(connectionType == DELETE_METHOD && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                {
                    connection.doOutput = false
                }
            }
            connection.useCaches = false
            connection.defaultUseCaches = false
            if(postDataList != null)
            {
                val outputStream = connection.outputStream
                val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
                bufferedWriter.write(getURLQuery(postDataList))
                bufferedWriter.flush()
                bufferedWriter.close()
                outputStream.close()
            }
            connection.connect()
            val responseStringBuilder = StringBuilder()
            var reader : Reader
            val bufferedReader : BufferedReader
            Log.f("connection.getResponseCode() : " + connection.responseCode)

            if(connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_CREATED)
            {
                bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            }
            else
            {
                bufferedReader = BufferedReader(InputStreamReader(connection.errorStream))
            }
            while(true)
            {
                val stringLine = bufferedReader.readLine();

                if(stringLine == null)
                {
                    break;
                }
                responseStringBuilder.append(stringLine +'\n')
            }
            bufferedReader.close()
            response = responseStringBuilder.toString()
            Log.f("connection.getResponseCode() : " + connection.responseCode)
            Log.f("Response : $response")
            connection.disconnect()
        }
        catch(e : ConnectException)
        {
            Log.exception(e)
            response = getNetworkErrorJson(context)
        }
        catch(e : Exception)
        {
            Log.f("e : "+ e.message);
            Log.exception(e)
            response = getNetworkErrorJson(context)
        }
        return response
    }

    fun initializeSSLContext(mContext : Context)
    {
        try
        {
            SSLContext.getInstance("TLSv1.2")
        }
        catch(e : NoSuchAlgorithmException)
        {
            e.printStackTrace()
        }
        try
        {
            ProviderInstaller.installIfNeeded(mContext.applicationContext)
        }
        catch(e : GooglePlayServicesRepairableException)
        {
            e.printStackTrace()
        }
        catch(e : GooglePlayServicesNotAvailableException)
        {
            e.printStackTrace()
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getURLQuery(params : ContentValues) : String
    {
        val result = StringBuilder()
        var first = true
        for((key, value) in params.valueSet())
        {
            if(first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }

    private fun trustAllHosts()
    {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager
        {
            override fun getAcceptedIssuers() : Array<X509Certificate>
            {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain : Array<X509Certificate>, authType : String)
            {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain : Array<X509Certificate>, authType : String)
            {
            }
        })

        // Install the all-trusting trust manager
        try
        {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        }
        catch(e : Exception)
        {
            e.printStackTrace()
        }
    }

    fun downloadFile(url : String?, dest_file_path : String?, listener : AsyncListener?) : Boolean
    {
        var count = 0
        try
        {
            val dest_file = File(dest_file_path)
            val folderPath = File(dest_file.parent)
            if(folderPath.exists() == false)
            {
                folderPath.mkdir()
            }
            dest_file.createNewFile()
            val resultUrl = URL(url)
            val conn = resultUrl.openConnection()
            conn.connect()
            val fileLength = conn.contentLength
            Log.f("fileLength : $fileLength")
            val input : InputStream = BufferedInputStream(resultUrl.openStream())
            val output : OutputStream = FileOutputStream(dest_file)
            val data = ByteArray(1024)
            var currentDownloadFileSize : Long = 0
            var progressPercent = 0
            while(input.read(data).also {count = it} != -1)
            {
                currentDownloadFileSize += count.toLong()
                progressPercent = (currentDownloadFileSize * 100 / fileLength).toInt()
                if(listener != null)
                {
                    listener.onRunningAdvanceInformation(Common.COROUTINE_CODE_FILE_DOWNLOAD, progressPercent)
                }
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        }
        catch(e : FileNotFoundException)
        {
            if(listener != null)
            {
                listener.onErrorListener("-1", e.message)
            }
            return false
        }
        catch(e : Exception)
        {
            if(listener != null)
            {
                listener.onErrorListener("-1", e.message)
            }
            return false
        }
        return true
    }
}