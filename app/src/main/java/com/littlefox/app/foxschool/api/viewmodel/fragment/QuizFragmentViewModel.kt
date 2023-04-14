package com.littlefox.app.foxschool.api.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.quiz.QuizResultViewData

class QuizFragmentViewModel : ViewModel()
{
    //Fragment
    private val _setTitle = MutableLiveData<Pair<String, String>>()
    val setTitle: LiveData<Pair<String, String>> = _setTitle

    private val _loadingComplete = MutableLiveData<Void?>()
    val loadingComplete: LiveData<Void?> = _loadingComplete

    private val _resultData = MutableLiveData<QuizResultViewData>()
    val resultData: LiveData<QuizResultViewData> = _resultData

    private val _showSaveButton = MutableLiveData<Void?>()
    val showSaveButton: LiveData<Void?> = _showSaveButton

    fun onSetTitle(title: String, subTitle: String)
    {
        _setTitle.value = Pair(title, subTitle)
    }

    fun onLoadingComplete()
    {
        _loadingComplete.postValue(null)
    }

    fun onSetResultData(data : QuizResultViewData)
    {
        _resultData.value = data
    }

    fun onShowSaveButton()
    {
        _showSaveButton.postValue(null)
    }
}