package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.enumerate.FlashcardStudyType
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class FlashcardPresenterObserver : ViewModel()
{
    var introTitleData : SingleLiveEvent<FlashcardDataObject> = SingleLiveEvent<FlashcardDataObject>()
    var settingBookmarkButtonData : SingleLiveEvent<Boolean> = SingleLiveEvent<Boolean>()
    var nextCardData : MutableLiveData<Void> = MutableLiveData<Void>()
    var initStudySettingData : MutableLiveData<FlashcardStudyType> = MutableLiveData<FlashcardStudyType>()
    var notifyListUpdateData : MutableLiveData<ArrayList<FlashCardDataResult>> = MutableLiveData<ArrayList<FlashCardDataResult>>()
    var closeHelpViewData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()

    fun setIntroTitle(data : FlashcardDataObject)
    {
        introTitleData.setValue(data)
    }

    fun setBookmarkButton(isBookmarked : Boolean)
    {
        settingBookmarkButtonData.setValue(isBookmarked)
    }

    fun onNextShowCard()
    {
        nextCardData.setValue(null)
    }

    fun onInitStudySetting(type : FlashcardStudyType)
    {
        initStudySettingData.setValue(type)
    }

    fun onNotifyUpdateList(list : ArrayList<FlashCardDataResult>)
    {
        notifyListUpdateData.setValue(list)
    }

    fun onCloseHelpView()
    {
        closeHelpViewData.setValue(null)
    }
}