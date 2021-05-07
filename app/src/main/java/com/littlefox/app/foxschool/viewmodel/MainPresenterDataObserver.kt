package com.littlefox.app.foxschool.viewmodel

import androidx.core.util.Pair
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.enumerate.FragmentDataMode


class MainPresenterDataObserver : ViewModel()
{
    var updateHomeData : MutableLiveData<Pair<FragmentDataMode, MainInformationResult>> =
        MutableLiveData<Pair<FragmentDataMode, MainInformationResult>>()
    var updateStoryData : MutableLiveData<MainInformationResult> = MutableLiveData<MainInformationResult>()
    var updateSongData : MutableLiveData<MainInformationResult> = MutableLiveData<MainInformationResult>()
    var updateMyBooksData : MutableLiveData<MainInformationResult> = MutableLiveData<MainInformationResult>()
    var updateClassData : MutableLiveData<MainInformationResult> = MutableLiveData<MainInformationResult>()

    fun notifyDataChangeAll(mode : FragmentDataMode, mainInformationResult : MainInformationResult)
    {
        updateHomeData.setValue(
            Pair<FragmentDataMode, MainInformationResult>(
                mode,
                mainInformationResult
            )
        )
        updateStoryData.setValue(mainInformationResult)
        updateSongData.setValue(mainInformationResult)
        updateMyBooksData.setValue(mainInformationResult)
        updateClassData.setValue(mainInformationResult)
    }

    fun notifyHomeDataChanged(mode : FragmentDataMode, mainInformationResult : MainInformationResult)
    {
        updateHomeData.setValue(
            Pair<FragmentDataMode, MainInformationResult>(
                mode,
                mainInformationResult
            )
        )
    }

    fun notifyStoryDataChanged(mainInformationResult : MainInformationResult?)
    {
        updateStoryData.setValue(mainInformationResult)
    }

    fun notifySongDataChanged(mainInformationResult : MainInformationResult?)
    {
        updateSongData.setValue(mainInformationResult)
    }

    fun notifyMyBooksDataChanged(mainInformationResult : MainInformationResult?)
    {
        updateMyBooksData.setValue(mainInformationResult)
    }

    fun notifyClassDataChanged(mainInformationResult : MainInformationResult?)
    {
        updateClassData.setValue(mainInformationResult)
    }
}