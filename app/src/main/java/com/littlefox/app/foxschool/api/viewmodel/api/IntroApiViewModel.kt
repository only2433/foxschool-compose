package com.littlefox.app.foxschool.api.viewmodel.api

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class IntroApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _versionData = SingleLiveEvent<VersionDataResult>()
    val versionData : LiveData<VersionDataResult> = _versionData

    private val _authMeData = SingleLiveEvent<LoginInformationResult>()
    val authMeData : LiveData<LoginInformationResult> = _authMeData

    private val _mainData = SingleLiveEvent<MainInformationResult>()
    val mainData : LiveData<MainInformationResult> = _mainData

    private val _changePasswordData = SingleLiveEvent<BaseResponse<Nothing>>()
    val changePasswordData : LiveData<BaseResponse<Nothing>> = _changePasswordData

    private val _changePasswordNextData = SingleLiveEvent<BaseResponse<Nothing>>()
    val changePasswordNextData : LiveData<BaseResponse<Nothing>> = _changePasswordNextData

    private val _changePasswordKeepData = SingleLiveEvent<BaseResponse<Nothing>>()
    val changePasswordKeepData : LiveData<BaseResponse<Nothing>> = _changePasswordKeepData

    private var mJob: Job? = null


    private suspend fun getVersion(deviceID : String, pushAddress : String, pushOn : String)
    {
        val result = repository.getVersion(deviceID, pushAddress, pushOn)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as VersionDataResult
                    _versionData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    _errorReport.postValue(Pair(result, RequestCode.CODE_VERSION))
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getAuthMe()
    {
        val result = repository.getAuthMe()
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as LoginInformationResult
                    _authMeData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    _errorReport.postValue(Pair(result, RequestCode.CODE_AUTH_ME))
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun getMain()
    {
        val result = repository.getMain()
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as MainInformationResult
                    _mainData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    _errorReport.postValue(Pair(result, RequestCode.CODE_MAIN))
                }
            }
        }
        enqueueCommandEnd()

    }

    private suspend fun changePassword(currentPassword: String, changePassword: String, changePasswordConfirm: String)
    {
        val result = repository.setChangePassword(currentPassword, changePassword, changePasswordConfirm)
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _changePasswordData.postValue(data)
                }
                is ResultData.Fail ->
                {
                    _errorReport.postValue(Pair(result, RequestCode.CODE_PASSWORD_CHANGE))
                }
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun changePasswordToDoNext()
    {
        val result = repository.setChangePasswordToDoNext()
        when(result)
        {
            is ResultData.Success ->
            {
                val data = result.data as BaseResponse
                _changePasswordNextData.postValue(data)
            }
            is ResultData.Fail ->
            {
                _errorReport.postValue(Pair(result, RequestCode.CODE_PASSWORD_CHANGE_NEXT))
            }
        }
        enqueueCommandEnd()

    }

    private suspend fun changePasswordToKeep()
    {
        val result = repository.setChangePasswordToKeep()
        when(result)
        {
            is ResultData.Success ->
            {
                val data = result.data as BaseResponse
                _changePasswordKeepData.postValue(data)
            }
            is ResultData.Fail ->
            {
                _errorReport.postValue(Pair(result, RequestCode.CODE_PASSWORD_CHANGE_KEEP))
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
            RequestCode.CODE_VERSION ->
            {
                mJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(data.duration)
                    getVersion(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String
                    )
                }
            }
            RequestCode.CODE_AUTH_ME ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO){
                    delay(data.duration)
                    getAuthMe()
                }
            }
            RequestCode.CODE_MAIN ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    getMain()
                }
            }
            RequestCode.CODE_PASSWORD_CHANGE ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    changePassword(
                        data.objects[0] as String,
                        data.objects[1] as String,
                        data.objects[2] as String
                    )
                }
            }
            RequestCode.CODE_PASSWORD_CHANGE_NEXT ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    changePasswordToDoNext()
                }
            }
            RequestCode.CODE_PASSWORD_CHANGE_KEEP ->
            {
                mJob = viewModelScope.launch (Dispatchers.IO) {
                    delay(data.duration)
                    changePasswordToKeep()
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