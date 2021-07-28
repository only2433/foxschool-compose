package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

class FlashcardResultFragmentObserver : ViewModel()
{
    var replayStudyData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()
    var bookmarkStudyData : SingleLiveEvent<Void> = SingleLiveEvent<Void>()

    fun onClickReplayStudy()
    {
        replayStudyData.setValue(null)
    }

    fun onClickBookmarkStudy()
    {
        bookmarkStudyData.setValue(null)
    }
}