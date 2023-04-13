package com.littlefox.app.foxschool.api.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.enumerate.FlashcardStudyType
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.logmonitor.Log

class FlashcardFragmentViewModel  : ViewModel()
{
    //-------------- Fragment
    private val _introTitle = MutableLiveData<FlashcardDataObject>()
    val introTitle : LiveData<FlashcardDataObject> = _introTitle

    private val _settingBookmarkButton = MutableLiveData<Boolean>()
    val settingBookmarkButton : LiveData<Boolean> = _settingBookmarkButton

    private val _nextCard = MutableLiveData<Void?>()
    val nextCardData : LiveData<Void?> = _nextCard

    private val _settingFlashcardView = MutableLiveData<FlashcardStudyType>()
    val settingFlashcardView : LiveData<FlashcardStudyType> = _settingFlashcardView

    private val _setFlashcardData = MutableLiveData<ArrayList<FlashCardDataResult>>()
    val setFlashcardData : LiveData<ArrayList<FlashCardDataResult>> = _setFlashcardData

    private val _closeHelpView = MutableLiveData<Void?>()
    val closeHelpView : LiveData<Void?> = _closeHelpView
    // ----------------------------------------//

    fun onSetIntroTitle(data : FlashcardDataObject)
    {
        _introTitle.value = data
    }

    fun onSettingBookmarkButton(enable: Boolean)
    {
        _settingBookmarkButton.value = enable
    }

    fun onShowNextCard()
    {
        _nextCard.postValue(null)
    }

    fun onSettingFlashcardView(type: FlashcardStudyType)
    {
        _settingFlashcardView.value = type
    }

    fun onSetFlashcardData(list : ArrayList<FlashCardDataResult>)
    {
        _setFlashcardData.value = list
    }

    fun onCloseHelpView()
    {
        Log.f("")
        _closeHelpView.postValue(null)
    }
}