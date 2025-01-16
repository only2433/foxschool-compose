package com.littlefox.app.foxschool.presentation.mvi.login.viewmodel

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.IntroApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.LoginApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.FindType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorLoginData
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.intro.IntroEvent
import com.littlefox.app.foxschool.presentation.mvi.intro.IntroState
import com.littlefox.app.foxschool.presentation.mvi.login.LoginAction
import com.littlefox.app.foxschool.presentation.mvi.login.LoginEvent
import com.littlefox.app.foxschool.presentation.mvi.login.LoginSideEffect
import com.littlefox.app.foxschool.presentation.mvi.login.LoginState
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(val apiViewModel : LoginApiViewModel): BaseMVIViewModel<LoginState, LoginEvent, SideEffect>(
    LoginState()
)
{
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
                                postSideEffect(
                                    SideEffect.EnableLoading(true)
                                )
                            }
                            else
                            {
                                Log.i("isLoading = false")
                                postSideEffect(
                                    SideEffect.EnableLoading(false)
                                )
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
                            postSideEffect(
                                LoginSideEffect.ShowPasswordChangeDialog(
                                    mUserInformationResult!!.getPasswordChangeType()
                                )
                            )
                        }
                        else
                        {
                            (mContext as AppCompatActivity).apply {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
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
                        postSideEffect(
                            SideEffect.ShowToast(
                                mContext.getString(R.string.message_password_change_complete)
                            )
                        )
                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)
                            }
                            postSideEffect(
                                LoginSideEffect.HidePasswordChangeDialog
                            )
                            (mContext as AppCompatActivity).apply {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
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
                        postSideEffect(
                            LoginSideEffect.HidePasswordChangeDialog
                        )
                        (mContext as AppCompatActivity).apply {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.changePasswordKeepData.collect { data ->
                    data?.let {
                        // 현재 비밀번호 유지
                        postSideEffect(
                            SideEffect.ShowToast(
                                mContext.getString(R.string.message_password_change_complete)
                            )
                        )
                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)
                            }
                            postSideEffect(
                                LoginSideEffect.HidePasswordChangeDialog
                            )
                            (mContext as AppCompatActivity).apply {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
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
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initScene()
                            }
                        }
                        else if (result.isNetworkError)
                        {
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                            }
                        }
                        else
                        {
                            if (code == RequestCode.CODE_LOGIN)
                            {
                                postSideEffect(
                                    SideEffect.ShowErrorMessage(
                                        result.message
                                    )
                                )
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
                                postSideEffect(
                                    SideEffect.ShowToast(
                                        result.message
                                    )
                                )
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

    override fun onHandleAction(action : Action)
    {
        when(action)
        {
            is LoginAction.ClickFindID ->
            {
                onClickFindID()
            }
            is LoginAction.ClickFindPassword ->
            {
                onClickFindPassword()
            }
            is LoginAction.ClickLaterButton ->
            {
                onClickLaterButton()
            }
            is LoginAction.ClickKeepButton ->
            {
                onClickKeepButton()
            }
            is LoginAction.SelectSchoolName ->
            {
                postEvent(
                    LoginEvent.NotifySchoolList(
                        emptyList()
                    )
                )
            }
            is LoginAction.InputSchoolNameChanged ->
            {
                checkTextChanged(
                    action.name
                )
            }
            is LoginAction.CheckAutoLogin ->
            {
                onCheckAutoLogin(
                    action.autoLogin
                )
            }
            is LoginAction.ClickLogin ->
            {
                onClickLogin(
                    action.data
                )
            }
            is LoginAction.ClickChangeButton ->
            {
                onClickChangeButton(
                    action.oldPassword,
                    action.newPassword,
                    action.confirmPassword
                )
            }
        }
    }

    override suspend fun reduceState(current : LoginState, event : LoginEvent) : LoginState
    {
        return when(event)
        {
            is LoginEvent.NotifySchoolList ->
            {
                current.copy(
                    schoolList = event.dataList
                )
            }
        }
    }


    private fun checkTextChanged(searchText: String)
    {
        if(searchText.isNotEmpty())
        {
            viewModelScope.launch {
                val searchList: List<SchoolItemDataResult> = _requestSchoolList.filter {
                    (it.getSchoolName()).contains(searchText)
                }
                postEvent(
                    LoginEvent.NotifySchoolList(
                        searchList
                    )
                )
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
                    postSideEffect(
                        SideEffect.ShowErrorMessage(
                            mContext.resources.getString(R.string.message_warning_empty_school)
                        )
                    )
                }
                (id == "") ->
                {
                    postSideEffect(
                        SideEffect.ShowErrorMessage(
                            mContext.resources.getString(R.string.message_warning_empty_id)
                        )
                    )
                }
                (password == "") ->
                {
                    postSideEffect(
                        SideEffect.ShowErrorMessage(
                            mContext.resources.getString(R.string.message_warning_empty_password)
                        )
                    )
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