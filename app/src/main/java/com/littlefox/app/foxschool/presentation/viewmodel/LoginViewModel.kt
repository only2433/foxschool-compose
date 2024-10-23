package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorLoginData
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.LoginApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.login.LoginEvent
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val apiViewModel : LoginApiViewModel) : BaseViewModel()
{
    private val _schoolList = SingleLiveEvent<List<SchoolItemDataResult>>()
    val schoolList: LiveData<List<SchoolItemDataResult>> get() = _schoolList

    private val _inputEmptyMessage = SingleLiveEvent<String>()
    val inputEmptyMessage: LiveData<String> get() = _inputEmptyMessage

    private val _showDialogPasswordChange = SingleLiveEvent<PasswordGuideType>()
    val showDialogPasswordChange: LiveData<PasswordGuideType> get() = _showDialogPasswordChange

    private val _hideDialogPasswordChange = SingleLiveEvent<Void>()
    val hideDialogPasswordChange: LiveData<Void> get() = _hideDialogPasswordChange

    private val _finishActivity = SingleLiveEvent<Void>()
    val finishActivity: LiveData<Void> get() = _finishActivity


    private var mUserLoginData : UserLoginData? = null // 로그인 input
    private var isAutoLogin : Boolean = false // 자동로그인 체크 여부

    private var mUserInformationResult : LoginInformationResult? = null // 사용자 정보 (로그인 통신 응답)

    // 비밀번호 변경 안내 관련 변수
    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""

    private val _requestSchoolList = arrayListOf<SchoolItemDataResult>()

    private lateinit var mContext : Context
    private var mSearchSchoolList: List<SchoolItemDataResult> = arrayListOf()


    override fun init(context : Context)
    {
        mContext = context
        onHandleApiObserver()
        requestSchoolList()

    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is LoginEvent.onInputSchoolNameChanged ->
            {
                checkTextChanged(
                    event.name
                )
            }
            is LoginEvent.onSchoolNameSelected ->
            {
                _schoolList.value = emptyList()
            }
            is LoginEvent.onClickFindID ->
            {
                onClickFindID()
            }
            is LoginEvent.onClickFindPassword ->
            {
                onClickFindPassword()
            }
            is LoginEvent.onClickLaterButton ->
            {
                onClickLaterButton()
            }
            is LoginEvent.onClickKeepButton ->
            {
                onClickKeepButton()
            }
            is LoginEvent.onCheckAutoLogin ->
            {
                onCheckAutoLogin(
                    event.autoLogin
                )
            }
            is LoginEvent.onClickLogin ->
            {
                onClickLogin(
                    event.data
                )
            }
            is LoginEvent.onClickChangeButton ->
            {
                onClickChangeButton(
                    event.oldPassword,
                    event.newPassword,
                    event.confirmPassword
                )
            }
        }
    }


    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_SCHOOL_LIST)
                        {
                            if(data.second)
                            {
                                Log.i("isLoading = true")
                                _isLoading.value = true
                            }
                            else
                            {
                                Log.i("isLoading = false")
                                _isLoading.value = false
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.schoolListData.collect { data ->
                    data?.let {
                        _requestSchoolList.addAll(data)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.loginData.collect { data ->
                    data?.let {
                        Log.f("Login Complete")
                        mUserInformationResult = data
                        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, if(isAutoLogin) "Y" else "N")
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)
                        val isTeacher = mUserInformationResult!!.getUserInformation().getUserType() != Common.USER_TYPE_STUDENT
                        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_TEACHER_MODE, isTeacher)

                        if (mUserInformationResult!!.isNeedChangePassword())
                        {
                            // 비밀번호 변경 날짜가 90일을 넘어가는 경우 비밀번호 변경 안내 다이얼로그를 표시한다.
                            _showDialogPasswordChange.value = mUserInformationResult!!.getPasswordChangeType()
                        }
                        else
                        {
                            _finishActivity.call()
                        }
                    }
                }
            }

        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.changePasswordData.collect { data ->
                    data?.let {
                        // 비밀번호 변경 성공
                        Log.f("Password Change Complete")
                        changeUserLoginData()

                        _toast.value = mContext.getString(R.string.message_password_change_complete)
                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)
                            }
                            _hideDialogPasswordChange.call()
                            _finishActivity.call()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.changePasswordNextData.collect { data ->
                    data?.let {
                        // 다음에 변경
                        _hideDialogPasswordChange.call()
                        _finishActivity.call()
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.changePasswordKeepData.collect { data ->
                    data?.let {
                        // 현재 비밀번호 유지
                        _toast.value = mContext.getString(R.string.message_password_change_complete)

                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)
                            }
                            _hideDialogPasswordChange.call()
                            _finishActivity.call()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        Log.f("status : ${result.status}, message : ${result.message} , code : $code")

                        if(result.isAuthenticationBroken)
                        {
                            Log.f("== isAuthenticationBroken ==")
                            (mContext as AppCompatActivity).finish()
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else if (result.isNetworkError)
                        {
                            Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                            (mContext as AppCompatActivity).finish()
                        }
                        else
                        {
                            if (code == RequestCode.CODE_LOGIN)
                            {
                                _errorMessage.value = result.message
                                if (Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                                {
                                    val errorData = ErrorLoginData(
                                        mUserLoginData!!.userID,
                                        result.status,
                                        result.message,
                                        java.lang.Exception()
                                    )
                                    CrashlyticsHelper.getInstance(mContext).sendCrashlytics(errorData)
                                }
                            }
                            else if (code == RequestCode.CODE_PASSWORD_CHANGE ||
                                code == RequestCode.CODE_PASSWORD_CHANGE_NEXT ||
                                code == RequestCode.CODE_PASSWORD_CHANGE_KEEP)
                            {
                                _toast.value = result.message
                            }
                            else
                            {
                                (mContext as AppCompatActivity).finish()
                            }
                        }
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

    private fun checkTextChanged(searchText: String)
    {
        if(searchText.isNotEmpty())
        {
            viewModelScope.launch {
                val searchList: List<SchoolItemDataResult> = _requestSchoolList.filter {
                    (it.getSchoolName()).contains(searchText)
                }
                _schoolList.value = searchList
            }
        }
    }

    /**
     * 학교 리스트 요청
     */
    private fun requestSchoolList()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_SCHOOL_LIST,
            Common.DURATION_SHORT_LONG
        )
    }

    /**
     * 비밀번호 변경하기 통신 요청
     */
    private fun requestPasswordChange()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_PASSWORD_CHANGE,
            mPassword,
            mNewPassword,
            mConfirmPassword)
    }

    /**
     * 비밀번호 변경하기 통신 요청 (다음에 변경)
     */
    private fun requestPasswordChangeNext()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(RequestCode.CODE_PASSWORD_CHANGE_NEXT)
    }

    /**
     * 비밀번호 변경하기 통신 요청 (비밀번호 유지)
     */
    private fun requestPasswordChangeKeep()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(RequestCode.CODE_PASSWORD_CHANGE_KEEP)
    }

    private fun requestLoginAsync(id : String, password : String, schoolCode : String)
    {
        if(id == "" || password == "" || schoolCode == "")
        {
            CommonUtils.getInstance(mContext).hideKeyboard()
            when
            {
                (schoolCode == "") ->
                {
                    _errorMessage.value = mContext.resources.getString(R.string.message_warning_empty_school)
                }
                (id == "") ->
                {
                    _errorMessage.value = mContext.resources.getString(R.string.message_warning_empty_id)
                }
                (password == "") ->
                {
                    _errorMessage.value = mContext.resources.getString(R.string.message_warning_empty_password)
                }
            }
            return
        }

        try
        {
            mUserLoginData = UserLoginData(id, SimpleCrypto.encode(password), schoolCode)
        } catch(e : Exception)
        {
            e.printStackTrace()
            Log.f("errorMessage : " + e.message)
        }

        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_LOGIN,
            Common.DURATION_SHORT_LONG,
            id,
            password,
            schoolCode
        )
    }

    /**
     * 바뀐 비밀번호 저장
     */
    private fun changeUserLoginData()
    {
        mUserLoginData = UserLoginData(mUserLoginData!!.userID, SimpleCrypto.encode(mNewPassword), mUserLoginData!!.userSchoolCode)
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
    }

    private fun onCheckAutoLogin(autoLogin : Boolean)
    {
        isAutoLogin = autoLogin
        Log.f("change AutoLogin : $isAutoLogin")
    }

    private fun onClickLogin(data : UserLoginData)
    {
        Log.f("Login")
        requestLoginAsync(data.userID, data.userPassword, data.userSchoolCode)
    }

    private fun onClickFindID()
    {
        Log.f("findID")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_USER_FIND_INFORMATION)
            .setData(FindType.ID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun onClickFindPassword()
    {
        Log.f("findPassword")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_USER_FIND_INFORMATION)
            .setData(FindType.PASSWORD)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * [비밀번호 변경] 버튼 클릭 이벤트
     */
    private fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
    {
        mPassword = oldPassword
        mNewPassword = newPassword
        mConfirmPassword = confirmPassword

        requestPasswordChange()
    }

    /**
     * [다음에 변경] 버튼 클릭 이벤트
     */
    private fun onClickLaterButton()
    {
        requestPasswordChangeNext()
    }

    /**
     * [현재 비밀번호로 유지하기] 버튼 클릭 이벤트
     */
    private fun onClickKeepButton()
    {
        requestPasswordChangeKeep()
    }
}