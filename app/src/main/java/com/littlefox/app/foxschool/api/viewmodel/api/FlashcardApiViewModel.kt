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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class FlashcardApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _getVocabularyContentsListData = MutableStateFlow<ArrayList<VocabularyDataResult>?>(null)
    val getVocabularyContentsListData : MutableStateFlow<ArrayList<VocabularyDataResult>?> = _getVocabularyContentsListData

    private val _addVocabularyContentsData = MutableStateFlow<MyVocabularyResult?>(null)
    val addVocabularyContentsData : MutableStateFlow<MyVocabularyResult?> = _addVocabularyContentsData

    private val _saveFlashcardData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val saveFlashcardData : MutableStateFlow<BaseResponse<Nothing>?> = _saveFlashcardData

    private suspend fun getVocabularyContentsList(contentID : String)
    {
        val result = repository.getVocabularyContentsList(contentID)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ArrayList<VocabularyDataResult>
                    _getVocabularyContentsListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_VOCABULARY_CONTENTS_LIST)
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun addFlashcardContents(contentID : String, vocabularyID : String, itemList : ArrayList<VocabularyDataResult>)
    {
        val result = repository.addVocabularyContents(contentID, vocabularyID, itemList)
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
                    _errorReport.value = Pair(result, RequestCode.CODE_VOCABULARY_CONTENTS_ADD)
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun saveFlashcardRecord(contentID: String)
    {
        val result = repository.flashcardSaveAsync(contentID)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _saveFlashcardData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_FLASHCARD_RECORD_SAVE)
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
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    getVocabularyContentsList(
                        data.objects[0] as String
                    )
                }
            }
            RequestCode.CODE_VOCABULARY_CONTENTS_ADD ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    addFlashcardContents(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as ArrayList<VocabularyDataResult>
                    )
                }
            }
            RequestCode.CODE_FLASHCARD_RECORD_SAVE ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    saveFlashcardRecord(data.objects[0] as String)
                }
            }
            else -> {}
        }
    }
}