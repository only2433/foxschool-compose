package com.littlefox.app.foxschool.viewmodel

import androidx.core.util.Pair
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class FlashcardBookmarkFragmentObserver : ViewModel()
{
    var enableBookmarkData : SingleLiveEvent<Pair<String, Boolean>> = SingleLiveEvent()
    var saveVocabularyData : SingleLiveEvent<Void> = SingleLiveEvent()
    var startWordStudyData : SingleLiveEvent<Void> = SingleLiveEvent()
    var startMeaningStudyData : SingleLiveEvent<Void> = SingleLiveEvent()

    fun onClickBookmark(wordID : String, isEnable : Boolean)
    {
        enableBookmarkData.setValue(Pair(wordID, isEnable))
    }

    fun onClickSaveVocabulary()
    {
        saveVocabularyData.setValue(null)
    }

    fun onClickStartWordStudyData()
    {
        startWordStudyData.setValue(null)
    }

    fun onClickStartMeaningStudyData()
    {
        startMeaningStudyData.setValue(null)
    }
}