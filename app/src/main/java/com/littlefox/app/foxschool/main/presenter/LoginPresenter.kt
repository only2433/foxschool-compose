package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorLoginData
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.SchoolListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.*
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.dialog.PasswordChangeDialog
import com.littlefox.app.foxschool.dialog.listener.PasswordChangeListener
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.LoginContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class LoginPresenter : LoginContract.Presenter
{
    companion object
    {
        private const val MESSAGE_FINISH : Int                      = 100   // 로그인 로직 종료
        private const val MESSAGE_INPUT_EMPTY_ERROR : Int           = 101   // 입력값 비어있을 때
        private const val MESSAGE_WARNING_INACTIVE_ACCOUNT : Int    = 102   // 등록되지 않은 사용자
        private const val MESSAGE_PASSWORD_CHANGE : Int             = 103   // 비밀번호 변경
    }

    private lateinit var mContext : Context
    private lateinit var mLoginContractView : LoginContract.View
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mSchoolListCoroutine : SchoolListCoroutine? = null
    private var mLoginCoroutine : LoginCoroutine? = null
    private var mUserLoginData : UserLoginData? = null // 로그인 input

    private var mSchoolListBaseObject : SchoolListBaseObject? = null // 통신에서 응답 받은 학교 리스트
    private val mSchoolList : ArrayList<SchoolItemDataResult> = ArrayList<SchoolItemDataResult>() // 사용자에 의해 필터링 된 학교 리스트
    private var mUserInformationResult : LoginInformationResult? = null // 사용자 정보 (로그인 통신 응답)
    private var isAutoLogin : Boolean = false // 자동로그인 체크 여부

    // 비밀번호 변경 안내 관련 변수
    private var mPasswordChangeDialog : PasswordChangeDialog? = null
    private var mPasswordChangeCoroutine : PasswordChangeCoroutine? = null
    private var mPasswordChangeNextCoroutine : PasswordChangeNextCoroutine? = null
    private var mPasswordChangeKeepCoroutine : PasswordChangeKeepCoroutine? = null
    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""

    constructor(context : Context)
    {
        mContext = context
        mLoginContractView = (mContext as LoginContract.View).apply {
            initView()
            initFont()
        }
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
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
        mSchoolListCoroutine?.cancel()
        mSchoolListCoroutine = null
        mLoginCoroutine?.cancel()
        mLoginCoroutine = null
        mPasswordChangeCoroutine?.cancel()
        mPasswordChangeCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_FINISH ->
            {
                (mContext as AppCompatActivity).setResult(Activity.RESULT_OK)
                (mContext as AppCompatActivity).finish()
            }
            MESSAGE_INPUT_EMPTY_ERROR -> mLoginContractView.showErrorMessage(msg.obj.toString())
            MESSAGE_WARNING_INACTIVE_ACCOUNT -> mLoginContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_inactive_account))
            MESSAGE_PASSWORD_CHANGE ->
            {
                mPasswordChangeDialog!!.dismiss()
                mMainHandler.sendEmptyMessage(MESSAGE_FINISH)
            }
        }
    }

    /**
     * 학교 리스트 수신데이터 전달
     */
    private fun notifySchoolList()
    {
        Log.f("")
        mSchoolList.addAll(mSchoolListBaseObject!!.getData())
        mLoginContractView.setSchoolList(mSchoolList)
    }

    /**
     * 바뀐 비밀번호 저장
     */
    private fun changeUserLoginData()
    {
        mUserLoginData = UserLoginData(mUserLoginData!!.userID, SimpleCrypto.encode(mNewPassword), mUserLoginData!!.userSchoolCode)
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
    }

    /**
     * 로그인 상태 유지 클릭 이벤트
     */
    override fun onCheckAutoLogin(autoLogin : Boolean)
    {
        isAutoLogin = autoLogin
        Log.f("isAutoLogin : $isAutoLogin")
    }

    /**
     * 로그인 버튼 클릭 이벤트
     */
    override fun onClickLogin(data : UserLoginData)
    {
        Log.f("Login")
        requestLoginAsync(data.userID, data.userPassword, data.userSchoolCode)
    }

    /**
     * 아이디 찾기 클릭 이벤트
     */
    override fun onClickFindID()
    {
        Log.f("findID")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_USER_FIND_INFORMATION)
            .setData(FindType.ID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 비밀번호 찾기 클릭 이벤트
     */
    override fun onClickFindPassword()
    {
        Log.f("findPassword")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_USER_FIND_INFORMATION)
            .setData(FindType.PASSWORD)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 비밀번호 변경 화면으로 이동
     */
    private fun showPasswordChangeDialog()
    {
        Log.f("")
        mPasswordChangeDialog = PasswordChangeDialog(mContext, mUserLoginData!!, mUserInformationResult!!).apply {
            setPasswordChangeListener(mPasswordChangeDialogListener)
            setCancelable(false)
            show()
        }
    }

    /**
     * 학교 리스트 요청
     */
    private fun requestSchoolList()
    {
        mSchoolListCoroutine = SchoolListCoroutine(mContext).apply {
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 로그인 요청
     */
    private fun requestLoginAsync(id : String, password : String, schoolCode : String)
    {
        if(id == "" || password == "" || schoolCode == "")
        {
            CommonUtils.getInstance(mContext).hideKeyboard()
            val message = Message.obtain()
            message.what = MESSAGE_INPUT_EMPTY_ERROR
            when
            {
                (schoolCode == "") ->
                {
                    message.obj = mContext.resources.getString(R.string.message_warning_empty_school)
                }
                (id == "") ->
                {
                    message.obj = mContext.resources.getString(R.string.message_warning_empty_id)
                }
                (password == "") ->
                {
                    message.obj = mContext.resources.getString(R.string.message_warning_empty_password)
                }
            }
            mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
            return
        }

        mLoginContractView.showLoading()
        try
        {
            mUserLoginData = UserLoginData(id, SimpleCrypto.encode(password), schoolCode)
        } catch(e : Exception)
        {
            e.printStackTrace()
            Log.f("errorMessage : " + e.message)
        }

        mLoginCoroutine = LoginCoroutine(mContext).apply {
            setData(id, password, schoolCode)
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 비밀번호 변경하기 통신 요청
     */
    private fun requestPasswordChange()
    {
        mPasswordChangeDialog!!.showLoading()
        mPasswordChangeCoroutine = PasswordChangeCoroutine(mContext).apply {
            setData(mPassword, mNewPassword, mConfirmPassword)
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 비밀번호 변경하기 통신 요청 (다음에 변경)
     */
    private fun requestPasswordChangeNext()
    {
        mPasswordChangeDialog!!.showLoading()
        mPasswordChangeNextCoroutine = PasswordChangeNextCoroutine(mContext).apply {
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 비밀번호 변경하기 통신 요청 (비밀번호 유지)
     */
    private fun requestPasswordChangeKeep()
    {
        mPasswordChangeDialog!!.showLoading()
        mPasswordChangeKeepCoroutine = PasswordChangeKeepCoroutine(mContext).apply {
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            mLoginContractView.hideLoading()

            val result : BaseResult = mObject as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_SCHOOL_LIST)
                {
                    // 학교 리스트 요청 성공
                    mSchoolListBaseObject = mObject as SchoolListBaseObject
                    notifySchoolList()
                }
                else if (code == Common.COROUTINE_CODE_LOGIN)
                {
                    // 로그인 성공
                    Log.f("Login Complete")
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, if(isAutoLogin) "Y" else "N")
                    mUserInformationResult = (result as LoginBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)
                    val isTeacher = mUserInformationResult!!.getUserInformation().getUserType() != Common.USER_TYPE_STUDENT
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_TEACHER_MODE, isTeacher)

                    if (mUserInformationResult!!.isNeedChangePassword())
                    {
                        // 비밀번호 변경 날짜가 90일을 넘어가는 경우 비밀번호 변경 안내 다이얼로그를 표시한다.
                        showPasswordChangeDialog()
                    }
                    else
                    {
                        mMainHandler.sendEmptyMessage(MESSAGE_FINISH)
                    }
                }
                else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE)
                {
                    // 비밀번호 변경 성공
                    Log.f("Password Change Complete")
                    changeUserLoginData()
                    mPasswordChangeDialog!!.hideLoading()
                    mPasswordChangeDialog!!.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_PASSWORD_CHANGE, Common.DURATION_LONG)
                }
                else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE_NEXT)
                {
                    // 다음에 변경
                    mPasswordChangeDialog!!.hideLoading()
                    mMainHandler.sendEmptyMessage(MESSAGE_PASSWORD_CHANGE)
                }
                else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE_KEEP)
                {
                    // 현재 비밀번호 유지
                    mPasswordChangeDialog!!.hideLoading()
                    mPasswordChangeDialog!!.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_PASSWORD_CHANGE, Common.DURATION_LONG)
                }
            }
            else
            {
                if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                }
                else if (result.isInActiveAccount)
                {
                    Log.f("== InActiveAccount ==")
                    mMainHandler.sendEmptyMessage(MESSAGE_WARNING_INACTIVE_ACCOUNT)
                }
                else if(result.isNetworkErrorStatus)
                {
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    (mContext as AppCompatActivity).finish()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_LOGIN)
                    {
                        mLoginContractView.showErrorMessage(result.getMessage())
                        if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                        {
                            val data = ErrorLoginData(
                                mUserLoginData!!.userID,
                                result.getStatus(),
                                result.getMessage(),
                                java.lang.Exception()
                            )
                            CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
                        }
                    }
                    else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE ||
                             code == Common.COROUTINE_CODE_PASSWORD_CHANGE_NEXT ||
                             code == Common.COROUTINE_CODE_PASSWORD_CHANGE_KEEP)
                    {
                        if(mPasswordChangeDialog?.isShowing == true)
                        {
                            mPasswordChangeDialog?.hideLoading()
                            mPasswordChangeDialog?.showErrorMessage(result.getMessage())
                        }
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }

    /**
     * 비밀번호 변경 다이얼로그 Listener
     */
    val mPasswordChangeDialogListener : PasswordChangeListener = object : PasswordChangeListener
    {
        /**
         * [비밀번호 변경] 버튼 클릭 이벤트
         */
        override fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
        {
            mPassword = oldPassword
            mNewPassword = newPassword
            mConfirmPassword = confirmPassword
            requestPasswordChange()
        }

        /**
         * [다음에 변경] 버튼 클릭 이벤트
         */
        override fun onClickLaterButton()
        {
            requestPasswordChangeNext()
        }

        /**
         * [현재 비밀번호로 유지하기] 버튼 클릭 이벤트
         */
        override fun onClickKeepButton()
        {
            requestPasswordChangeKeep()
        }
    }
}