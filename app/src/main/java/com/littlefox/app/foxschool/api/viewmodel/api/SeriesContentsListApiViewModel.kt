package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.DetailItemInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class SeriesContentsListApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _storyContentsListData = MutableStateFlow<DetailItemInformationResult?>(null)
    val storyContentsListData : MutableStateFlow<DetailItemInformationResult?> = _storyContentsListData

    private val _songContentsListData = MutableStateFlow<DetailItemInformationResult?>(null)
    val songContentsListData : MutableStateFlow<DetailItemInformationResult?> = _songContentsListData

    private val _addBookshelfContentsData = MutableStateFlow<MyBookshelfResult?>(null)
    val addBookshelfContentsData: MutableStateFlow<MyBookshelfResult?> = _addBookshelfContentsData

    private suspend fun getStoryContentsListData(displayID: String)
    {
        val result = repository.getStoryContentsList(displayID)
        Log.i("result : ${result.toString()}")
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as DetailItemInformationResult
                    _storyContentsListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_CONTENTS_STORY_LIST))
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getSongContentsListData(displayID : String)
    {
        val result = repository.getSongContentsList(displayID)
        Log.i("result : ${result.toString()}")
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as DetailItemInformationResult
                    _songContentsListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_CONTENTS_SONG_LIST))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun addBookshelfContents(bookshelfID: String, contentsList: ArrayList<ContentsBaseResult>)
    {
        val result = repository.addBookshelfContents(bookshelfID, contentsList)
        Log.i("result : ${result.toString()}")
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
            RequestCode.CODE_CONTENTS_STORY_LIST ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    getStoryContentsListData(
                        data.objects[0] as String
                    )
                }
            }
            RequestCode.CODE_CONTENTS_SONG_LIST ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    getSongContentsListData(
                        data.objects[0] as String
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
            else -> {}
        }
    }
}