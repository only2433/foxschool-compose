package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.player.PlayItemResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class PlayerApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _authContentData = MutableStateFlow<PlayItemResult?>(null)
    val authContentData: MutableStateFlow<PlayItemResult?> = _authContentData

    private val _savePlayerStudyLogData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val savePlayerStudyLogData: MutableStateFlow<BaseResponse<Nothing>?> = _savePlayerStudyLogData

    private val _addBookshelfContentsData = MutableStateFlow<MyBookshelfResult?>(null)
    val addBookshelfContentsData: MutableStateFlow<MyBookshelfResult?> = _addBookshelfContentsData

    private suspend fun getAuthContentPlayData(contentID: String, isHighRevoluation: Boolean)
    {
        val result = repository.getAuthContentPlay(contentID, isHighRevoluation)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as PlayItemResult
                    _authContentData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_AUTH_CONTENT_PLAY))
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun savePlayerStudyLog(contentID: String, playType: String, playTime: String, homeworkNumber: Int)
    {
        val result = repository.savePlayerStudyLog(contentID, playType, playTime, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _savePlayerStudyLogData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_PLAY_CONTENTS_LOG_SAVE))
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun addBookshelfContents(bookshelfID: String, contentsList: ArrayList<ContentsBaseResult>)
    {
        val result = repository.addBookshelfContents(bookshelfID, contentsList)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyBookshelfResult
                    _addBookshelfContentsData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_BOOKSHELF_CONTENTS_ADD))
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }


    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        when(data.requestCode)
        {
            RequestCode.CODE_AUTH_CONTENT_PLAY ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    getAuthContentPlayData(
                        data.objects[0] as String,
                        data.objects[1] as Boolean
                    )
                }
            }
            RequestCode.CODE_PLAY_CONTENTS_LOG_SAVE ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    savePlayerStudyLog(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String,
                        data.objects[3] as Int
                     )
                }
            }
            RequestCode.CODE_BOOKSHELF_CONTENTS_ADD ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    addBookshelfContents(
                        data.objects[0] as String,
                        data.objects[1] as ArrayList<ContentsBaseResult>
                    )
                }
            }
            else ->{}
        }
    }


}