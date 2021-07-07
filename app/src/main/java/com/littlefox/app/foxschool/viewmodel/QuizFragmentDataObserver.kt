package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData

/**
 * 해당 뷰 모델은 QuizFragment에서 Presenter로 데이터 전달을 하기 위한 용도로 사용.
 * 기존의 리스너에서 안정적인 ViewModel Observer로 대체
 */
class QuizFragmentDataObserver : ViewModel()
{
    var playSoundData = MutableLiveData<Boolean>()
    var choiceItemData : MutableLiveData<QuizUserInteractionData> =
        MutableLiveData<QuizUserInteractionData>()
    var nextData = MutableLiveData<Boolean>()
    var studyInformationData = MutableLiveData<Boolean>()
    var replayData = MutableLiveData<Boolean>()

    fun onPlaySound()
    {
        playSoundData.value = true
    }

    fun onChoiceItem(`object` : QuizUserInteractionData)
    {
        choiceItemData.value = `object`
    }

    fun onGoNext()
    {
        nextData.value = true
    }

    fun onSaveStudyInformation()
    {
        studyInformationData.value = true
    }

    fun onGoReplay()
    {
        replayData.value = true
    }
}