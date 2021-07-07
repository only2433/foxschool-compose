package com.littlefox.app.foxschool.coroutine

import android.content.Context
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.library.system.coroutine.BaseCoroutine
import com.littlefox.logmonitor.Log
import java.util.*
import kotlin.collections.ArrayList

class FileDownloadCoroutine : BaseCoroutine
{
    private var mFileDownloadUrlList : ArrayList<String> = ArrayList<String>()
    private var mFileSavePathList : ArrayList<String>    = ArrayList<String>()

    constructor(context : Context) : super(context, Common.COROUTINE_CODE_FILE_DOWNLOAD)

    override fun doInBackground() : Any?
    {
        if(isRunning == true)
        {
            return false
        }
        var result = false
        synchronized(mSync) {
            isRunning = true
            for(i in mFileDownloadUrlList.indices)
            {
                Log.f("fileUrl : ${mFileDownloadUrlList[i]}, path : ${mFileSavePathList[i]}")
                result = NetworkUtil.downloadFile(mFileDownloadUrlList[i], mFileSavePathList[i], asyncListener)
            }
        }
        return result
    }

    override fun setData(vararg objects : Any?)
    {
        mFileDownloadUrlList = objects[0] as ArrayList<String>
        mFileSavePathList = objects[1] as ArrayList<String>
    }
}