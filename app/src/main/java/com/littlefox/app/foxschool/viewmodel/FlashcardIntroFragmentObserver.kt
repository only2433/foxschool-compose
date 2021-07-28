package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class FlashcardIntroFragmentObserver : ViewModel()
{
    var startWordStudyData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()
    var startMeaningStudyData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()
    var infoButtonData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()

    fun onClickStartWordStudy()
    {
        startWordStudyData.setValue(null)
    }

    fun onClickStartMeaningStudy()
    {
        startMeaningStudyData.setValue(null)
    }

    fun onClickInformation()
    {
        infoButtonData.setValue(null)
    }
}