package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.`object`.data.quiz.QuizStudyRecordData
import com.littlefox.app.foxschool.`object`.result.quiz.QuizInformationResult
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.*
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class QuizApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _quizInformationData = MutableStateFlow<QuizInformationResult?>(null)
    val quizInformationData: MutableStateFlow<QuizInformationResult?> = _quizInformationData

    private val _quizSaveRecordData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val quizSaveRecordData: MutableStateFlow<BaseResponse<Nothing>?> = _quizSaveRecordData

    private val _downloadQuizResource = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val downloadQuizResource: MutableStateFlow<BaseResponse<Nothing>?> = _downloadQuizResource

    private var mJob: Job? = null

    private suspend fun getQuizInformation(contentID: String)
    {
        val result = repository.getQuizInformation(contentID)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as QuizInformationResult
                    _quizInformationData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_QUIZ_INFORMATION)
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun saveQuizRecord(answerData: QuizStudyRecordData, homeworkNumber: Int = 0)
    {
        val result = repository.saveQuizRecord(answerData, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _quizSaveRecordData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_QUIZ_RECORD_SAVE)
                }
            }
        }
        enqueueCommandEnd()
    }

    private fun downloadQuizResource(urlList: ArrayList<String>, savePathList: ArrayList<String>)
    {
        var isSuccess = false
        var result: BaseResponse<Void>? = null
        var fileUrlList: List<String> = urlList as ArrayList<String>
        var savePathList: List<String> = savePathList as ArrayList<String>

        for(i in fileUrlList.indices)
        {
            isSuccess = downloadfile(
                fileUrlList[i],
                savePathList[i]
            )

            if(isSuccess == false)
            {
                mJob?.cancel()
                _errorReport.value = Pair(
                    ResultData.Fail(
                        status = 500,
                        "파일을 다운로드 하지 못했습니다."
                    ),
                    RequestCode.CODE_DOWNLOAD_QUIZ_RESOURCE
                )
                break;
            }
        }

        if(isSuccess)
        {
            _downloadQuizResource.value = BaseResponse(
                status = 200,
            )
        }
        enqueueCommandEnd()
    }


    private fun downloadfile(url : String?, dest_file_path : String?) : Boolean
    {
        Log.f("url : ${url} , dest_file_path : ${dest_file_path}")
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
            conn.readTimeout = NetworkUtil.CONNECTION_TIMEOUT
            conn.connectTimeout = NetworkUtil.CONNECTION_TIMEOUT
            conn.useCaches = false
            conn.defaultUseCaches = false
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
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        }
        catch(e : FileNotFoundException)
        {
            Log.f("message : ${e.message}")
            return false
        }
        catch(e : Exception)
        {
            Log.f("message : ${e.message}")
            return false
        }
        return true
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)
        mJob?.cancel()
        when(data.requestCode)
        {
            RequestCode.CODE_QUIZ_INFORMATION ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO){
                    delay(data.duration)
                    getQuizInformation(
                        data.objects[0] as String)
                }
            }
            RequestCode.CODE_QUIZ_RECORD_SAVE ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO){
                    delay(data.duration)
                    saveQuizRecord(
                        data.objects[0] as QuizStudyRecordData,
                        data.objects[1] as Int
                    )
                }
            }
            RequestCode.CODE_DOWNLOAD_QUIZ_RESOURCE ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO){
                    delay(data.duration)
                    downloadQuizResource(
                        data.objects[0] as ArrayList<String>,
                        data.objects[1] as ArrayList<String>
                    )
                }
            }
        }
    }

    override fun onCleared()
    {
        mJob?.cancel()
        super.onCleared()
    }
}