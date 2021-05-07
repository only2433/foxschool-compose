package com.littlefox.app.foxschool.viewmodel

import android.view.View
import androidx.core.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult

class MainSongFragmentDataObserver : ViewModel()
{
    var songCategoryItemData : MutableLiveData<Pair<SeriesInformationResult, View>> =
        MutableLiveData<Pair<SeriesInformationResult, View>>()

    fun onClickSongCategoriesItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        songCategoryItemData.setValue(
            Pair<SeriesInformationResult, View>(
                seriesInformationResult,
                selectView
            )
        )
    }
}