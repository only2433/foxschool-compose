package com.littlefox.app.foxschool.api.viewmodel.api

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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class IntroApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _versionData = MutableStateFlow<VersionDataResult?>(null)
    val versionData : MutableStateFlow<VersionDataResult?> = _versionData

    private val _authMeData = MutableStateFlow<LoginInformationResult?>(null)
    val authMeData : MutableStateFlow<LoginInformationResult?> = _authMeData

    private val _mainData = MutableStateFlow<MainInformationResult?>(null)
    val mainData : MutableStateFlow<MainInformationResult?> = _mainData

    private val _changePasswordData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val changePasswordData : MutableStateFlow<BaseResponse<Nothing>?> = _changePasswordData

    private val _changePasswordNextData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val changePasswordNextData : MutableStateFlow<BaseResponse<Nothing>?> = _changePasswordNextData

    private val _changePasswordKeepData = MutableStateFlow<BaseResponse<Nothing>?>(null)
    val changePasswordKeepData : MutableStateFlow<BaseResponse<Nothing>?> = _changePasswordKeepData

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
                    _versionData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_VERSION)
                }
                else -> {}
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
                    _authMeData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_AUTH_ME)
                }
                else -> {}
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
                    _mainData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_MAIN)
                }
                else -> {}

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
                    _changePasswordData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_PASSWORD_CHANGE)
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    private suspend fun changePasswordToDoNext()
    {
        val result = repository.setChangePasswordToDoNext()
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _changePasswordNextData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_PASSWORD_CHANGE_NEXT)
                }
                else -> {}
            }
        }
        enqueueCommandEnd()

    }

    private suspend fun changePasswordToKeep()
    {
        val result = repository.setChangePasswordToKeep()
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as BaseResponse
                    _changePasswordKeepData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.value = Pair(result, RequestCode.CODE_PASSWORD_CHANGE_KEEP)
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
            else ->{}
        }
    }

}