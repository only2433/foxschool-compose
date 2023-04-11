package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
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
class TeacherHomeworkCheckingApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _teacherHomeworkChecking = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val teacherHomeworkChecking : MutableStateFlow<BaseResponse<Nothing>?> = _teacherHomeworkChecking

    private var mJob: Job? = null

    private suspend fun setTeacherHomeworkChecking(homeworkNumber : Int, classID : Int, userID : String, evaluationState : String, evaluationComment : String)
    {
        val result = repository.setTeacherHomeworkChecking(homeworkNumber, classID, userID, evaluationState, evaluationComment)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _teacherHomeworkChecking.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_TEACHER_HOMEWORK_CHECKING)
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
            RequestCode.CODE_TEACHER_HOMEWORK_CHECKING ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    setTeacherHomeworkChecking(
                        data.objects[0] as Int,
                        data.objects[1] as Int,
                        data.objects[2] as String,
                        data.objects[3] as String,
                        data.objects[4] as String
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
