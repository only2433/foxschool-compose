package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VocabularyApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _contentsListData = MutableStateFlow<ArrayList<VocabularyDataResult>?>(null)
    val contentsListData : MutableStateFlow<ArrayList<VocabularyDataResult>?> = _contentsListData

    private val _addVocabularyContentsData = MutableStateFlow<MyVocabularyResult?>(null)
    val addVocabularyContentsData : MutableStateFlow<MyVocabularyResult?> = _addVocabularyContentsData

    private val _deleteVocabularyContentsData = MutableStateFlow<MyVocabularyResult?>(null)
    val deleteVocabularyContentsData : MutableStateFlow<MyVocabularyResult?> = _deleteVocabularyContentsData


    private suspend fun getVocabularyContentsList(id: String)
    {
        val result = repository.getVocabularyContentsList(id)
        Log.i("result : ${result.toString()}")
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ArrayList<VocabularyDataResult>
                    _contentsListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_VOCABULARY_CONTENTS_LIST))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun addVocabularyContents(contentID : String, vocabularyID : String, itemList : ArrayList<VocabularyDataResult>)
    {
        val result = repository.addVocabularyContents(contentID, vocabularyID, itemList)
        Log.i("result : ${result.toString()}")
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyVocabularyResult
                    _addVocabularyContentsData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_VOCABULARY_CONTENTS_ADD))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun deleteVocabularyContents(vocabularyID : String, itemList : ArrayList<VocabularyDataResult>)
    {
        val result = repository.deleteVocabularyContents(vocabularyID, itemList)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MyVocabularyResult
                    _deleteVocabularyContentsData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_VOCABULARY_CONTENTS_DELETE))
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
            RequestCode.CODE_VOCABULARY_CONTENTS_LIST ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    getVocabularyContentsList(
                        data.objects[0] as String
                    )
                }
            }
            RequestCode.CODE_VOCABULARY_CONTENTS_ADD ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    addVocabularyContents(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as ArrayList<VocabularyDataResult>
                    )
                }
            }
            RequestCode.CODE_VOCABULARY_CONTENTS_DELETE ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    deleteVocabularyContents(
                        data.objects[0] as String,
                        data.objects[1] as ArrayList<VocabularyDataResult>
                    )
                }
            }
            else -> {}
        }
    }
}