package com.littlefox.app.foxschool.api.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.api.base.BaseViewModel
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseViewModel()
{
    private val versionData = MutableLiveData<VersionDataResult>()
    val _versionData : LiveData<VersionDataResult> = versionData

    private val authMeData = MutableLiveData<LoginInformationResult>()
    val _authMeData : LiveData<LoginInformationResult> = authMeData

    private val mainData = MutableLiveData<MainInformationResult>()
    val _mainData : LiveData<MainInformationResult> = mainData

    private val changePasswordData = MutableLiveData<BaseResponse<Nothing>>()
    val _changePasswordData : LiveData<BaseResponse<Nothing>> = changePasswordData

    private val changePasswordNextData = MutableLiveData<BaseResponse<Nothing>>()
    val _changePasswordNextData : LiveData<BaseResponse<Nothing>> = changePasswordNextData

    private val changePasswordKeepData = MutableLiveData<BaseResponse<Nothing>>()
    val _changePasswordKeepData : LiveData<BaseResponse<Nothing>> = changePasswordKeepData


    private fun getVersion(deviceID : String, pushAddress : String, pushOn : String)
    {
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getVersion(deviceID, pushAddress, pushOn)
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as VersionDataResult
                    versionData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_INIT))
                }
            }
            enqueueCommandEnd()
        }
    }

    private fun getAuthMe()
    {
        viewModelScope.launch (Dispatchers.Main){
            val result = repository.getAuthMe()
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as LoginInformationResult
                    authMeData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_ME))
                }
            }
            enqueueCommandEnd()
        }
    }

    private fun getMain()
    {
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getMain()
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MainInformationResult
                    mainData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_MAIN))
                }
            }
            enqueueCommandEnd()
        }
    }

    private  fun changePassword(currentPassword: String, changePassword: String, changePasswordConfirm: String)
    {
        viewModelScope.launch(Dispatchers.Main){
            val result = repository.setChangePassword(currentPassword, changePassword, changePasswordConfirm)
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    changePasswordData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_PASSWORD_CHANGE))
                }
            }
            enqueueCommandEnd()
        }
    }

    private fun changePasswordToDoNext()
    {
        viewModelScope.launch(Dispatchers.Main){
            val result = repository.setChangePasswordToDoNext()
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    changePasswordNextData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_PASSWORD_CHANGE_NEXT))
                }
            }
            enqueueCommandEnd()
        }
    }

    private fun changePasswordToKeep()
    {
        viewModelScope.launch(Dispatchers.Main){
            val result = repository.setChangePasswordToKeep()
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    changePasswordKeepData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    errorReport.postValue(Pair(result, Common.COROUTINE_CODE_PASSWORD_CHANGE_KEEP))
                }
            }
            enqueueCommandEnd()
        }
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)

        when(data.requestCode)
        {
            RequestCode.CODE_VERSION ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    getVersion(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String
                    )
                }, data.duration)
            }
            RequestCode.CODE_AUTH_ME ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    getAuthMe()
                }, data.duration)
            }
            RequestCode.CODE_MAIN ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    getMain()
                }, data.duration)
            }
            RequestCode.CODE_PASSWORD_CHANGE ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    changePassword(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String
                    )
                }, data.duration)
            }
            RequestCode.CODE_PASSWORD_CHANGE_NEXT ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    changePasswordToDoNext()
                }, data.duration)
            }
            RequestCode.CODE_PASSWORD_CHANGE_KEEP ->
            {
                Handler(Looper.getMainLooper()).postDelayed({
                    changePasswordToKeep()
                }, data.duration)
            }
        }
    }
}