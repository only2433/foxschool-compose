package com.littlefox.app.foxschool.viewmodel

import androidx.core.util.Pair
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class FlashcardStudyFragmentObserver : ViewModel()
{
    var enableBookmarkData : SingleLiveEvent<Pair<String, Boolean>> = SingleLiveEvent()
    var touchStartSoundData : SingleLiveEvent<String> = SingleLiveEvent()
    var autoStartSoundData : SingleLiveEvent<String> = SingleLiveEvent()
    var studyEndData : SingleLiveEvent<Void> = SingleLiveEvent()
    var buttonClickData : SingleLiveEvent<Void> = SingleLiveEvent()


    fun onClickBookmark(wordID : String, isEnable : Boolean)
    {
        enableBookmarkData.setValue(Pair(wordID, isEnable))
    }

    fun onClickSound(wordID : String?)
    {
        touchStartSoundData.setValue(wordID)
    }

    fun onActionAutoSound(wordID : String?)
    {
        autoStartSoundData.setValue(wordID)
    }

    fun onEndStudyFlashCard()
    {
        studyEndData.setValue(null)
    }

    fun onActionStudyCard()
    {
        buttonClickData.setValue(null)
    }
}