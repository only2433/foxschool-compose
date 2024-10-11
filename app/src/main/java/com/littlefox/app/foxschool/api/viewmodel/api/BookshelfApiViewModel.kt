package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookshelfApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _contentsList = MutableStateFlow<ArrayList<ContentsBaseResult>?>(null)
    val contentsList : MutableStateFlow<ArrayList<ContentsBaseResult>?> = _contentsList

    private val _myBookshelfResult = MutableStateFlow<MyBookshelfResult?>(null)
    val myBookshelfResult : MutableStateFlow<MyBookshelfResult?> = _myBookshelfResult

    private suspend fun getBookshelfContentsList(bookshelfID: String)
    {
        val result = repository.getBookshelfContentsList(bookshelfID)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ArrayList<ContentsBaseResult>
                    _contentsList.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_BOOKSHELF_CONTENTS_LIST))
                }

                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun deleteBookshelfContents(bookshelfID : String, list: ArrayList<ContentsBaseResult>)
    {
        Log.f("bookshelfID : $bookshelfID, list size : ${list.size}")
        val result = repository.deleteBookshelfContents(bookshelfID, list)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyBookshelfResult
                    _myBookshelfResult.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        when(data.requestCode)
        {
            RequestCode.CODE_BOOKSHELF_CONTENTS_LIST ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO)
                {
                    delay(data.duration)
                    getBookshelfContentsList(
                        data.objects[0] as String
                    )
                }
            }
            RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    deleteBookshelfContents(
                        data.objects[0] as String,
                        data.objects[1] as ArrayList<ContentsBaseResult>
                    )
                }
            }
            else -> {}
        }
    }
}