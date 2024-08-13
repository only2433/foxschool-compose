package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class StudentHomeworkApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _studentHomeworkCalendarData = MutableStateFlow<HomeworkCalendarBaseResult?>(null)
    val studentHomeworkCalendarData : MutableStateFlow<HomeworkCalendarBaseResult?> = _studentHomeworkCalendarData

    private val _studentHomeworkDetailData = MutableStateFlow<HomeworkDetailBaseResult?>(null)
    val studentHomeworkDetailData : MutableStateFlow<HomeworkDetailBaseResult?> = _studentHomeworkDetailData

    private val _studentCommentRegisterData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val studentCommentRegisterData : MutableStateFlow<BaseResponse<Nothing>?> = _studentCommentRegisterData

    private val _studentCommentUpdateData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val studentCommentUpdateData : MutableStateFlow<BaseResponse<Nothing>?> = _studentCommentUpdateData

    private val _studentCommentDeleteData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val studentCommentDeleteData : MutableStateFlow<BaseResponse<Nothing>?> = _studentCommentDeleteData

    private suspend fun getStudentHomeworkCalendar(year : String, month : String)
    {
        val result = repository.getStudentHomeworkCalendar(year, month)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkCalendarBaseResult
                    _studentHomeworkCalendarData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_STUDENT_HOMEWORK_CALENDAR)
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getStudentHomeworkList(homeworkNumber : Int)
    {
        val result = repository.getStudentHomeworkList(homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as HomeworkDetailBaseResult
                    _studentHomeworkDetailData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_STUDENT_HOMEWORK_DETAIL_LIST)
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun setStudentCommentRegister(comment : String, homeworkNumber : Int)
    {
        val result = repository.setStudentCommentRegister(comment, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _studentCommentRegisterData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_STUDENT_COMMENT_REGISTER)
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun setStudentCommentUpdate(comment : String, homeworkNumber : Int)
    {
        val result = repository.setStudentCommentUpdate(comment, homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _studentCommentUpdateData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_STUDENT_COMMENT_UPDATE)
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun setStudentCommentDelete(homeworkNumber : Int)
    {
        val result = repository.setStudentCommentDelete(homeworkNumber)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _studentCommentDeleteData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_STUDENT_COMMENT_DELETE)
                }
                else ->{}
            }
        }
        enqueueCommandEnd()
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        when(data.requestCode)
        {
            RequestCode.CODE_STUDENT_HOMEWORK_CALENDAR ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getStudentHomeworkCalendar(
                        data.objects[0] as String,
                        data.objects[1] as String,
                    )
                }
            }
            RequestCode.CODE_STUDENT_HOMEWORK_DETAIL_LIST ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getStudentHomeworkList(
                        data.objects[0] as Int
                    )
                }
            }
            RequestCode.CODE_STUDENT_COMMENT_REGISTER ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    setStudentCommentRegister(
                        data.objects[0] as String,
                        data.objects[1] as Int
                    )
                }
            }
            RequestCode.CODE_STUDENT_COMMENT_UPDATE ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    setStudentCommentUpdate(
                        data.objects[0] as String,
                        data.objects[1] as Int
                    )
                }
            }
            RequestCode.CODE_STUDENT_COMMENT_DELETE ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    setStudentCommentDelete(
                        data.objects[0] as Int
                    )
                }
            }
            else ->{}
        }
    }

    override fun onCleared()
    {
        mJob?.cancel()
        super.onCleared()
    }
}