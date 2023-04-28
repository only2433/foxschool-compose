package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCheckingIntentParamsObject
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.TeacherHomeworkCheckingApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TeacherHomeworkCheckingFactoryViewModel @Inject constructor(private val apiViewModel : TeacherHomeworkCheckingApiViewModel) : BaseFactoryViewModel()
{
    private val _settingBeforeData = SingleLiveEvent<Pair<Int, String>>()
    val settingBeforeData: LiveData<Pair<Int, String>> get() = _settingBeforeData

    private lateinit var mContext : Context

    private lateinit var mHomeworkCheckingInformation : HomeworkCheckingIntentParamsObject

    // 통신에 입력되는 데이터
    private var mEval : String = ""
    private var mComment : String = ""

    override fun init(context : Context)
    {
        mContext = context
        setupViewModelObserver()

        mHomeworkCheckingInformation = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_HOMEWORK_CHECKING_DATA)!!
        if (mHomeworkCheckingInformation.isEvalComplete())
        {
            _settingBeforeData.value = Pair(
                mHomeworkCheckingInformation.getEval(),
                mHomeworkCheckingInformation.getComment()
            )
        }
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect {data ->
                data?.let {
                    if (data.first == RequestCode.CODE_TEACHER_HOMEWORK_CHECKING)
                    {
                        if(data.second)
                        {
                            _isLoading.postValue(true)
                        }
                        else
                        {
                            _isLoading.postValue(false)
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkChecking.collect { data ->
                data?.let {
                    viewModelScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO){
                            delay(Common.DURATION_NORMAL)
                        }
                        (mContext as AppCompatActivity).finish()
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect { data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    Log.f("status : ${result.status}, message : ${result.message} , code : $code")

                    if(result.isDuplicateLogin)
                    {
                        // 중복 로그인 재시작
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if (result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
    }

    private fun requestHomeworkCheck()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_CHECKING,
            mHomeworkCheckingInformation.getHomeworkNumber(),
            mHomeworkCheckingInformation.getClassNumber(),
            mHomeworkCheckingInformation.getID(),
            mEval,
            mComment
        )
    }

    fun onClickRegisterButton(index : Int, comment : String)
    {
        when(index)
        {
            0 -> mEval = "E0"
            1 -> mEval = "E1"
            2 -> mEval = "E2"
            else -> mEval = "E1"
        }
        mComment = comment
        requestHomeworkCheck()
    }
}