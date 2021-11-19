package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkStatusBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.TeacherClassItemData
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType

/**
 * 숙제(달력, 리스트, 코멘트) Presenter Observer
 */
class HomeworkManagePresenterObserver : ViewModel()
{
    var setCalendarData = MutableLiveData<HomeworkCalendarBaseResult>()

    var updateHomeworkListData = MutableLiveData<HomeworkDetailBaseResult>()
    var clearHomeworkList = MutableLiveData<Boolean>()

    // 코멘트 화면
    var setPageType = MutableLiveData<Pair<HomeworkCommentType, Boolean>>()
    var setCommentData = MutableLiveData<String>()

    // 선생님 ----
    var setClassData = MutableLiveData<ArrayList<TeacherClassItemData>>()
    var setClassName = MutableLiveData<String>()

    var setStatusListData = MutableLiveData<HomeworkStatusBaseResult>()
    var clearStatusList = MutableLiveData<Boolean>()
    var setClickEnable = MutableLiveData<Boolean>()
    // ----------

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
    fun updateHomeworkListData(item : HomeworkDetailBaseResult)
    {
        updateHomeworkListData.value = item
    }

    fun clearHomeworkList(allClear : Boolean)
    {
        clearHomeworkList.value = allClear
    }

    /**
     * 학습자/선생님 한마디 화면
     */
    fun setPageType(commentType : HomeworkCommentType, isCompleted : Boolean)
    {
        setPageType.value = Pair(commentType, isCompleted)
    }

    fun setCommentData(comment : String)
    {
        setCommentData.value = comment
    }

    /**
     * 선생님
     */

    // 선생님 학급 리스트
    fun setClassData(classData : ArrayList<TeacherClassItemData>)
    {
        setClassData.value = classData
    }

    fun setClassName(className : String)
    {
        setClassName.value = className
    }

    fun setStatusListData(homeworkStatusBaseResult : HomeworkStatusBaseResult)
    {
        setStatusListData.value = homeworkStatusBaseResult
    }

    fun clearStatusList()
    {
        clearStatusList.value = true
    }

    fun setClickEnable()
    {
        setClickEnable.value = true
    }
}