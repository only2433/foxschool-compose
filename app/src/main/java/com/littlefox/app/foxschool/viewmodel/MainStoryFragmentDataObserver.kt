package com.littlefox.app.foxschool.viewmodel

import android.view.View
import androidx.core.util.Pair
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult


class MainStoryFragmentDataObserver : ViewModel()
{
    var storyLevelsItemData : MutableLiveData<Pair<SeriesInformationResult, View>> = MutableLiveData<Pair<SeriesInformationResult, View>>()
    var storyCategoryItemData : MutableLiveData<Pair<SeriesInformationResult, View>> = MutableLiveData<Pair<SeriesInformationResult, View>>()

    fun onClickStoryLevelsItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        storyLevelsItemData.setValue(Pair<SeriesInformationResult, View>(seriesInformationResult, selectView))
    }

    fun onClickStoryCategoriesItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        storyCategoryItemData.setValue(Pair<SeriesInformationResult, View>(seriesInformationResult, selectView))
    }
}