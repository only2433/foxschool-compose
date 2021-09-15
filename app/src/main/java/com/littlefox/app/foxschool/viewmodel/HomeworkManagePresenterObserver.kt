package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData

class HomeworkManagePresenterObserver : ViewModel()
{
    var setCalendarData = MutableLiveData<ArrayList<CalendarData>>()
    var setCalendarMonthTitle = MutableLiveData<String>()
    var setCalendarPrevButton = MutableLiveData<Boolean>()
    var setCalendarNextButton = MutableLiveData<Boolean>()

    fun setCalendarData(calendarData : ArrayList<CalendarData>)
    {
        setCalendarData.value = calendarData
    }

    fun setCalendarMonthTitle(title : String)
    {
        setCalendarMonthTitle.value = title
    }

    fun setCalendarPrevButton(isEnable : Boolean)
    {
        setCalendarPrevButton.value = isEnable
    }

    fun setCalendarNextButton(isEnable : Boolean)
    {
        setCalendarNextButton.value = isEnable
    }
}