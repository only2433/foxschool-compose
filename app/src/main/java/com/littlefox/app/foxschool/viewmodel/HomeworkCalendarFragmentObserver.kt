package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkItemData

class HomeworkCalendarFragmentObserver : ViewModel()
{
    var onClickCalendarBefore = MutableLiveData<Boolean>()
    var onClickCalendarAfter = MutableLiveData<Boolean>()
    var onClickCalendarItem = MutableLiveData<HomeworkItemData>()
    var onCompletedListSet = MutableLiveData<Boolean>()

    fun onClickCalendarBefore()
    {
        onClickCalendarBefore.value = true
    }

    fun onClickCalendarAfter()
    {
        onClickCalendarAfter.value = true
    }

    fun onClickCalendarItem(item : HomeworkItemData)
    {
        onClickCalendarItem.value = item
    }

    fun onCompletedListSet(isCompleted : Boolean)
    {
        onCompletedListSet.value = isCompleted
    }
}