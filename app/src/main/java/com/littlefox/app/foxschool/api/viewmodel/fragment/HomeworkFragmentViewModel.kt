package com.littlefox.app.foxschool.api.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkStatusBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.TeacherClassItemData

class HomeworkFragmentViewModel : ViewModel()
{
    private val _classData = MutableLiveData<ArrayList<TeacherClassItemData>>()
    val classData : LiveData<ArrayList<TeacherClassItemData>> get() = _classData

    private val _calendarData = MutableLiveData<HomeworkCalendarBaseResult>()
    val calendarData : LiveData<HomeworkCalendarBaseResult> get() = _calendarData

    private val _settingCommentPage = MutableLiveData<Pair<HomeworkCommentType, String>>()
    val settingCommentPage : LiveData<Pair<HomeworkCommentType, String>> get() = _settingCommentPage

    private val _updateHomeworkListData = MutableLiveData<HomeworkDetailBaseResult>()
    val updateHomeworkListData : LiveData<HomeworkDetailBaseResult> get() = _updateHomeworkListData

    private val _clearHomeworkListData = MutableLiveData<Boolean>()
    val clearHomeworkListData : LiveData<Boolean> get() = _clearHomeworkListData

    private val _updateClassNameData = MutableLiveData<String>()
    val updateClassNameData : LiveData<String> get() = _updateClassNameData

    private val _updateHomeworkStatusListData = MutableLiveData<HomeworkStatusBaseResult>()
    val updateHomeworkStatusListData : LiveData<HomeworkStatusBaseResult> get() = _updateHomeworkStatusListData

    private val _clearHomeworkStatusListData = MutableLiveData<Void?>()
    val clearHomeworkStatusListData : LiveData<Void?> get() = _clearHomeworkStatusListData

    fun onSettingClassData(list : ArrayList<TeacherClassItemData>)
    {
        _classData.value = list
    }

    fun onSettingCalendarData(data : HomeworkCalendarBaseResult)
    {
        _calendarData.value = data
    }

    fun onSettingCommentPage(type : HomeworkCommentType, comment : String)
    {
        _settingCommentPage.value = Pair(type, comment)
    }

    fun onUpdateHomeworkListScene(data : HomeworkDetailBaseResult)
    {
        _updateHomeworkListData.value = data
    }

    fun onClearHomeworkListScene(isAllClear : Boolean)
    {
        _clearHomeworkListData.value = isAllClear
    }

    fun onUpdateClassName(name: String)
    {
        _updateClassNameData.value = name
    }

    fun onUpdateHomeworkStatusListScene(data : HomeworkStatusBaseResult)
    {
        _updateHomeworkStatusListData.value = data
    }

    fun onClearHomeworkStatusListScene()
    {
        _clearHomeworkStatusListData.postValue(null)
    }

}