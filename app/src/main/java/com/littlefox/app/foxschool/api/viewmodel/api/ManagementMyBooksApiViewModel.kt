package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManagementMyBooksApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _createBookshelfData = MutableStateFlow<MyBookshelfResult?>(null)
    val createBookshelfData : MutableStateFlow<MyBookshelfResult?> = _createBookshelfData

    private val _updateBookshelfData = MutableStateFlow<MyBookshelfResult?>(null)
    val updateBookshelfData : MutableStateFlow<MyBookshelfResult?>  = _updateBookshelfData

    private val _deleteBookshelfData = MutableStateFlow<MyBookshelfResult?>(null)
    val deleteBookshelfData : MutableStateFlow<MyBookshelfResult?> = _deleteBookshelfData

    private val _createVocabularyData = MutableStateFlow<MyVocabularyResult?>(null)
    val createVocabularyData : MutableStateFlow<MyVocabularyResult?> = _createVocabularyData

    private val _updateVocabularyData = MutableStateFlow<MyVocabularyResult?>(null)
    val updateVocabularyData : MutableStateFlow<MyVocabularyResult?> = _updateVocabularyData

    private val _deleteVocabularyData = MutableStateFlow<MyVocabularyResult?>(null)
    val deleteVocabularyData : MutableStateFlow<MyVocabularyResult?> = _deleteVocabularyData



    private suspend fun createBookshelf(name: String, color: String)
    {
        val result = repository.createBookshelf(name, color)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyBookshelfResult
                    _createBookshelfData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_CREATE_BOOKSHELF))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun updateBookshelf(bookshelfID: String, name: String, color: String)
    {
        val result = repository.updateBookshelf(bookshelfID, name, color)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyBookshelfResult
                    _updateBookshelfData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_UPDATE_BOOKSHELF))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun deleteBookshelf(bookshelfID : String)
    {
        val result = repository.deleteBookshelf(bookshelfID)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyBookshelfResult
                    _deleteBookshelfData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_DELETE_BOOKSHELF))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun createVocabulary(name: String, color: String)
    {
        val result = repository.createVocabulary(name, color)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyVocabularyResult
                    _createVocabularyData.value = data
                }
                is ResultData.Fail ->
                {
                   // _errorReport.value = Pair(result, RequestCode.CODE_CREATE_VOCABULARY)
                    _errorReport.emit(Pair(result, RequestCode.CODE_CREATE_VOCABULARY))

                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun updateVocabulary(vocabularyID: String, name: String, color: String)
    {
        val result = repository.updateVocabulary(vocabularyID, name, color)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyVocabularyResult
                    _updateVocabularyData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_UPDATE_VOCABULARY))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun deleteVocabulary(vocabularyID: String)
    {
        val result = repository.deleteVocabulary(vocabularyID)
        Log.i("result : ${result.toString()}")

        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyVocabularyResult
                    _deleteVocabularyData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_DELETE_VOCABULARY))
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
            RequestCode.CODE_CREATE_BOOKSHELF ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    createBookshelf(
                        name = data.objects[0] as String,
                        color = data.objects[1] as String
                    )
                }
            }
            RequestCode.CODE_UPDATE_BOOKSHELF ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    updateBookshelf(
                        bookshelfID = data.objects[0] as String,
                        name = data.objects[1] as String,
                        color = data.objects[2] as String
                    )
                }
            }
            RequestCode.CODE_DELETE_BOOKSHELF ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    deleteBookshelf(
                        bookshelfID = data.objects[0] as String
                    )
                }
            }
            RequestCode.CODE_CREATE_VOCABULARY ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    createVocabulary(
                        name = data.objects[0] as String,
                        color = data.objects[1] as String
                    )
                }
            }
            RequestCode.CODE_UPDATE_VOCABULARY ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    updateVocabulary(
                        vocabularyID = data.objects[0] as String,
                        name = data.objects[1] as String,
                        color = data.objects[2] as String
                    )
                }
            }
            RequestCode.CODE_DELETE_VOCABULARY ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    deleteVocabulary(
                        vocabularyID = data.objects[0] as String
                    )
                }
            }
            else -> {}
        }
    }
}