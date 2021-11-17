package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeworkCalendarFragmentObserver : ViewModel()
{
    var onClickCalendarBefore = MutableLiveData<Boolean>()
    var onClickCalendarAfter = MutableLiveData<Boolean>()
    var onClickCalendarItem = MutableLiveData<Int>()
    var onCompletedCalendarSet = MutableLiveData<Boolean>()

    // 선생님 ---------
    var onClickClassPicker = MutableLiveData<Int>()

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

    fun onCompletedCalendarSet()
    {
        onCompletedCalendarSet.value = true
    }

    // 선생님 ---------
    fun onClickClassPicker(index : Int)
    {
        onClickClassPicker.value = index
    }
}