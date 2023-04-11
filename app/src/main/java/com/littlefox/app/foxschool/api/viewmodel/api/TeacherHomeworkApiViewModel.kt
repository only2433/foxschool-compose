package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkStatusBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.TeacherClassItemData
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class TeacherHomeworkApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _teacherHomeworkClassListData = MutableStateFlow<ArrayList<TeacherClassItemData>?>(null)
    val teacherHomeworkClassListData : MutableStateFlow<ArrayList<TeacherClassItemData>?> = _teacherHomeworkClassListData

    private val _teacherHomeworkCalendarData = MutableStateFlow<HomeworkCalendarBaseResult?>(null)
    val teacherHomeworkCalendarData : MutableStateFlow<HomeworkCalendarBaseResult?> = _teacherHomeworkCalendarData

    private val _teacherHomeworkStatusData = MutableStateFlow<HomeworkStatusBaseResult?>(null)
    val teacherHomeworkStatusData : MutableStateFlow<HomeworkStatusBaseResult?> = _teacherHomeworkStatusData

    private val _teacherHomeworkDetailData = MutableStateFlow<HomeworkDetailBaseResult?>(null)
    val teacherHomeworkDetailData : MutableStateFlow<HomeworkDetailBaseResult?> = _teacherHomeworkDetailData

    private val _teacherHomeworkContentsData = MutableStateFlow<HomeworkDetailBaseResult?>(null)
    val teacherHomeworkContentsData : MutableStateFlow<HomeworkDetailBaseResult?> = _teacherHomeworkContentsData

    private var mJob: Job? = null

    private suspend fun getTeacherHomeworkClassList()
    {
        val result = repository.getTeacherHomeworkClassList()
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as ArrayList<TeacherClassItemData>
                    _teacherHomeworkClassListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_CLASS_LIST)
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getTeacherHomeworkCalendar(classId : String, year : String, month : String)
    {
        val result = repository.getTeacherHomeworkCalendar(classId, year, month)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkCalendarBaseResult
                    _teacherHomeworkCalendarData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_CALENDAR)
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getTeacherHomeworkStatus(classId : Int, homeworkNumber : Int)
    {
        val result = repository.getTeacherHomeworkStatus(classId, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkStatusBaseResult
                    _teacherHomeworkStatusData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_STATUS)
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getTeacherHomeworkDetail(classId : Int, homeworkNumber : Int, mUserID : String)
    {
        val result = repository.getTeacherHomeworkDetail(classId, homeworkNumber, mUserID)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkDetailBaseResult
                    _teacherHomeworkDetailData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_DETAIL_LIST)
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getTeacherHomeworkContents(classId : Int, homeworkNumber : Int)
    {
        val result = repository.getTeacherHomeworkContents(classId, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkDetailBaseResult
                    _teacherHomeworkContentsData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_CONTENTS)
                }
            }
        }
        enqueueCommandEnd()
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        mJob?.cancel()
        when(data.requestCode)
        {
            RequestCode.CODE_TEACHER_HOMEWORK_CLASS_LIST ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getTeacherHomeworkClassList()
                }
            }
            RequestCode.CODE_TEACHER_HOMEWORK_CALENDAR ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getTeacherHomeworkCalendar(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String,
                    )
                }
            }
            RequestCode.CODE_TEACHER_HOMEWORK_STATUS ->
            {
                // 숙제 현황
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getTeacherHomeworkStatus(
                        data.objects[0] as Int,
                        data.objects[1] as Int
                    )
                }
            }
            RequestCode.CODE_TEACHER_HOMEWORK_DETAIL_LIST ->
            {
                // 숙제 현황 상세 보기
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getTeacherHomeworkDetail(
                        data.objects[0] as Int,
                        data.objects[1] as Int,
                        data.objects[2] as String
                    )
                }
            }
            RequestCode.CODE_TEACHER_HOMEWORK_CONTENTS ->
            {
                // 숙제 현황 상세 보기
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getTeacherHomeworkContents(
                        data.objects[0] as Int,
                        data.objects[1] as Int
                    )
                }
            }
        }
    }

    override fun onCleared()
    {
        mJob?.cancel()
        super.onCleared()
    }
}