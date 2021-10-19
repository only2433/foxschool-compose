package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult

/**
 * 숙제(달력, 리스트, 코멘트) Presenter Observer
 */
class HomeworkManagePresenterObserver : ViewModel()
{
    var setCalendarData = MutableLiveData<HomeworkCalendarBaseResult>()

    var updateHomeworkListData = MutableLiveData<HomeworkListBaseResult>()
    var setHomeworkPrevButton = MutableLiveData<Boolean>()
    var setHomeworkNextButton = MutableLiveData<Boolean>()
    var clearHomeworkList = MutableLiveData<Boolean>()

    var setPageType = MutableLiveData<Pair<Int, Boolean>>()
    var setCommentData = MutableLiveData<String>()

    /**
     * 숙제관리 화면 (달력)
     */
    fun setCalendarData(homeworkCalendarBaseResult : HomeworkCalendarBaseResult)
    {
        setCalendarData.value = homeworkCalendarBaseResult
    }

    /**
     * 숙제현황 화면 (리스트)
     */
    fun updateHomeworkListData(item : HomeworkListBaseResult)
    {
        updateHomeworkListData.value = item
    }

    fun setHomeworkPrevButton(isEnable : Boolean)
    {
        setHomeworkPrevButton.value = isEnable
    }

    fun setHomeworkNextButton(isEnable : Boolean)
    {
        setHomeworkNextButton.value = isEnable
    }

    fun clearHomeworkList(allClear : Boolean)
    {
        clearHomeworkList.value = allClear
    }

    /**
     * 학습자/선생님 한마디 화면
     */
    fun setPageType(position : Int, isCompleted : Boolean)
    {
        setPageType.value = Pair(position, isCompleted)
    }

    fun setCommentData(comment : String)
    {
        setCommentData.value = comment
    }
}