package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult
import com.littlefox.app.foxschool.adapter.CalendarItemViewAdapter
import com.littlefox.app.foxschool.adapter.HomeworkItemViewAdapter

class HomeworkManagePresenterObserver : ViewModel()
{
    var setCalendarListView = MutableLiveData<CalendarItemViewAdapter>()
    var setCalendarMonthTitle = MutableLiveData<String>()
    var setCalendarPrevButton = MutableLiveData<Boolean>()
    var setCalendarNextButton = MutableLiveData<Boolean>()
    var setScrollTop = MutableLiveData<Boolean>()

    var setHomeworkListView = MutableLiveData<Pair<HomeworkItemViewAdapter, Boolean>>()
    var setHomeworkDateText = MutableLiveData<String>()
    var setHomeworkFilterText = MutableLiveData<String>()
    var setResultCommentLayout = MutableLiveData<HomeworkListBaseResult>()
    var setStudentCommentLayout = MutableLiveData<Boolean>()
    var setTeacherCommentLayout = MutableLiveData<Boolean>()
    var setHomeworkPrevButton = MutableLiveData<Boolean>()
    var setHomeworkNextButton = MutableLiveData<Boolean>()
    var setHomeworkLoadingProgressBar = MutableLiveData<Boolean>()
    var clearHomeworkList = MutableLiveData<Boolean>()

    var setPageType = MutableLiveData<Int>()
    var setStudentCommentData = MutableLiveData<String>()
    var setTeacherCommentData = MutableLiveData<String>()
    var clearScreenData = MutableLiveData<Boolean>()


    /**
     * 숙제관리 화면 (달력)
     */
    fun setCalendarListView(calendarAdapter : CalendarItemViewAdapter)
    {
        setCalendarListView.value = calendarAdapter
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

    fun setScrollTop()
    {
        setScrollTop.value = true
    }

    /**
     * 숙제현황 화면 (리스트)
     */
    fun setHomeworkListView(homeworkListAdapter : HomeworkItemViewAdapter, animation : Boolean)
    {
        setHomeworkListView.value = Pair<HomeworkItemViewAdapter, Boolean>(homeworkListAdapter, animation)
    }

    fun setHomeworkDateText(date : String)
    {
        setHomeworkDateText.value = date
    }

    fun setHomeworkFilterText(text : String)
    {
        setHomeworkFilterText.value = text
    }

    fun setResultCommentLayout(item : HomeworkListBaseResult)
    {
        setResultCommentLayout.value = item
    }

    fun setStudentCommentLayout(hasComment : Boolean)
    {
        setStudentCommentLayout.value = hasComment
    }

    fun setTeacherCommentLayout(hasComment : Boolean)
    {
        setTeacherCommentLayout.value = hasComment
    }

    fun setHomeworkPrevButton(isEnable : Boolean)
    {
        setHomeworkPrevButton.value = isEnable
    }

    fun setHomeworkNextButton(isEnable : Boolean)
    {
        setHomeworkNextButton.value = isEnable
    }

    fun setHomeworkLoadingProgressBar(isVisible : Boolean)
    {
        setHomeworkLoadingProgressBar.value = isVisible
    }

    fun clearHomeworkList(allClear : Boolean)
    {
        clearHomeworkList.value = allClear
    }

    /**
     * 학습자/선생님 한마디 화면
     */
    fun setPageType(position : Int)
    {
        setPageType.value = position
    }

    fun setStudentCommentData(comment : String)
    {
        setStudentCommentData.value = comment
    }

    fun setTeacherCommentData(comment : String)
    {
        setTeacherCommentData.value = comment
    }

    fun clearScreenData()
    {
        clearScreenData.value = true
    }
}