package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeworkCalendarFragmentObserver : ViewModel()
{
    var onClickCalendarBefore = MutableLiveData<Boolean>()
    var onClickCalendarAfter = MutableLiveData<Boolean>()
    var onClickCalendarItem = MutableLiveData<Int>()
    var onCompletedListSet = MutableLiveData<Boolean>()

    fun onClickCalendarBefore()
    {
        onClickCalendarBefore.value = true
    }

    fun onClickCalendarAfter()
    {
        onClickCalendarAfter.value = true
    }

    fun onClickCalendarItem(calendarItem : Int)
    {
        onClickCalendarItem.value = calendarItem
    }

    fun onCompletedListSet(isCompleted : Boolean)
    {
        onCompletedListSet.value = isCompleted
    }
}