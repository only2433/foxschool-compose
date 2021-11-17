package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TeacherHomeworkStatusFragmentObserver : ViewModel()
{
    var onClickShowDetailButton = MutableLiveData<Int>()
    var onClickHomeworkContents = MutableLiveData<Boolean>()
    var onClickHomeworkBundleChecking = MutableLiveData<ArrayList<String>>()
    var onClickHomeworkChecking = MutableLiveData<Int>()

    fun onClickShowDetailButton(index : Int)
    {
        onClickShowDetailButton.value = index
    }

    fun onClickHomeworkContents()
    {
        onClickHomeworkContents.value = true
    }

    fun onClickHomeworkBundleChecking(data : ArrayList<String>)
    {
        onClickHomeworkBundleChecking.value = data
    }

    fun onClickHomeworkChecking(index : Int)
    {
        onClickHomeworkChecking.value = index
    }
}