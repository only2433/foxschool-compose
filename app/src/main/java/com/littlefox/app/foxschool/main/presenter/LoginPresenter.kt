package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
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
import com.littlefox.app.foxschool.coroutine.LoginCoroutine
import com.littlefox.app.foxschool.coroutine.SchoolListCoroutine
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.enc.SimpleCrypto
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

    constructor(context : Context)
    {
        mContext = context
        mLoginContractView = mContext as LoginContract.View
        mLoginContractView.initView()
        mLoginContractView.initFont()
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)

        requestSchoolList()
    }

    override fun resume()
    {
        Log.f("")
        requestSchoolList()
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
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

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
        }
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
        // TODO 아이디찾기 화면 연결 (웹)
    }

    /**
     * 비밀번호 찾기 클릭 이벤트
     */
    override fun onClickFindPassword()
    {
        // TODO 비밀번호찾기 화면 연결 (웹)
    }

    /**
     * 학교 리스트 요청
     */
    private fun requestSchoolList()
    {
        mSchoolListCoroutine = SchoolListCoroutine(mContext)
        mSchoolListCoroutine!!.asyncListener = mAsyncListener
        mSchoolListCoroutine!!.execute()
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
                (schoolCode == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_school)
                (id == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_id)
                (password == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_password)
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

        mLoginCoroutine = LoginCoroutine(mContext)
        mLoginCoroutine!!.setData(id, password, schoolCode)
        mLoginCoroutine!!.asyncListener = mAsyncListener
        mLoginCoroutine!!.execute()
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
                    Feature.IS_FREE_USER = false // TODO 무료이용자 플래그 추후 제거 예정
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, if(isAutoLogin) "Y" else "N")
                    mUserInformationResult = (result as LoginBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)
                    val isTeacher = mUserInformationResult!!.getUserInformation().getUserType() != Common.USER_TYPE_STUDENT
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_TEACHER_MODE, isTeacher)
                    mMainHandler.sendEmptyMessage(MESSAGE_FINISH)
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
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}
