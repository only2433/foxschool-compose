package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.messaging.FirebaseMessaging
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.data.myinfo.MyInformationData
import com.littlefox.app.foxschool.`object`.data.myinfo.MyPasswordData
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.adapter.MyInformationPagerAdapter
import com.littlefox.app.foxschool.common.CheckUserInput
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.MyInfoUpdateCoroutine
import com.littlefox.app.foxschool.coroutine.PasswordChangeCoroutine
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.MyInformationContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoChangeFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoShowFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoPresenterDataObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.logmonitor.Log
import java.util.ArrayList

/**
 * 나의 정보 화면 Presenter
 *  @author 김태은
 */
class MyInformationPresenter : MyInformationContract.Presenter
{
    companion object
    {
        // MyInfoChange
        private const val DIALOG_PASSWORD_CONFIRM : Int     = 10001    // 비밀번호 확인 다이얼로그 플래그
        private const val DIALOG_PASSWORD_CONFIRM_ERR : Int = 10002     // 비밀번호 확인 오류 다이얼로그 플래그
    }

    private lateinit var mContext : Context
    private lateinit var mMyInformationContractView : MyInformationContract.View

    private val mMyInfoFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private var mMyInformationPagerAdapter : MyInformationPagerAdapter? = null

    private lateinit var mMyInfoPresenterDataObserver : MyInfoPresenterDataObserver
    private lateinit var mMyInfoShowFragmentDataObserver : MyInfoShowFragmentDataObserver
    private lateinit var mMyInfoChangeFragmentDataObserver : MyInfoChangeFragmentDataObserver

    private var mMyInfoUpdateCoroutine : MyInfoUpdateCoroutine? = null
    private var mPasswordChangeCoroutine : PasswordChangeCoroutine? = null

    private var mLoginInformation : LoginInformationResult? = null  // 로그인 시 응답받은 회원정보
    private var mLoginData : UserLoginData? = null // 로그인 정보 (아이디, 비밀번호, 학교코드)

    // MyInfo
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog // 지문인증 로그인 활성/비활성 알림 다이얼로그
    private var mCheckAutoLogin : Boolean   = false                 // 자동로그인 ON/OFF
    private var mCheckPush : Boolean        = false                 // 푸쉬수신 ON/OFF

    // MyInfoChange
    private lateinit var mPasswordCheckDialog : TemplateAlertDialog // 비밀번호 확인 다이얼로그
    private var mName : String  = ""
    private var mEmail : String = ""
    private var mPhone : String = ""

    // PasswordChange
    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""

    constructor(context : Context)
    {
        mContext = context
        mMyInformationContractView = mContext as MyInformationContract.View
        mMyInformationContractView.initView()
        mMyInformationContractView.initFont()

        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")

        // set ViewPager
        mMyInformationPagerAdapter = MyInformationPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mMyInfoFragmentList)
        mMyInformationPagerAdapter!!.setFragment()
        mMyInformationContractView.initViewPager(mMyInformationPagerAdapter!!)

        // set Observer
        mMyInfoPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(MyInfoPresenterDataObserver::class.java)
        mMyInfoShowFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(MyInfoShowFragmentDataObserver::class.java)
        mMyInfoChangeFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(MyInfoChangeFragmentDataObserver::class.java)
        setupMyInfoFragmentListener()
        setupMyInfoChangeFragmentListener()
        setupMyPasswordChangeFragmentListener()

        // SharedPreference에 저장된 값 가져와서 데이터 세팅
        mLoginInformation = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?
        if (mLoginInformation != null)
        {
            mMyInfoPresenterDataObserver.setMyInfoShowFragment(mLoginInformation!!)
            mMyInfoPresenterDataObserver.setMyInfoChangeFragment(mLoginInformation!!)
        }
        mLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?

        // [MY INFO]
        // 자동로그인 설정값 가져오기
        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
        Log.f("autoLoginStatus : $autoLoginStatus")
        mCheckAutoLogin = (autoLoginStatus == "Y")

        // 푸쉬 설정값 가져오기
        val isPushEnable = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")
        Log.f("isPushEnable : $isPushEnable")
        mCheckPush = (isPushEnable == "Y")

        // 플래그값에 따라 스위치 상태 세팅
        mMyInfoPresenterDataObserver.setAutoLoginSwitch(mCheckAutoLogin)
        mMyInfoPresenterDataObserver.setPushSwitch(mCheckPush)
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
        mMyInfoUpdateCoroutine?.cancel()
        mMyInfoUpdateCoroutine = null
        mPasswordChangeCoroutine?.cancel()
        mPasswordChangeCoroutine = null
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what) { }
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트
     * 뒤로가기 버튼은 정보수정/비밀번호 변경 화면에서만 표시되며, 나의정보 화면으로 이동한다.
     */
    override fun onClickBackButton()
    {
        Log.f("")
        mMyInfoPresenterDataObserver.setMyInfoChangeFragment(mLoginInformation!!)
        mMyInfoPresenterDataObserver.clearMyInfoChangeFragment()
        mMyInformationContractView.setCurrentViewPage(Common.PAGE_MY_INFO)
    }

    /**
     * 알림 다이얼로그 표시
     * - 생체인증 로그인 활성/비활성 알림 다이얼로그
     * - 비밀번호 확인 오류 다이얼로그
     */
    private fun showTemplateAlertDialog(type : Int, message : String, buttonType : DialogButtonType)
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(message)
            setDialogEventType(type)
            setButtonType(buttonType)
            setDialogListener(mDialogListener)
            show()
        }

    }

    /**
     * =========================================================
     *           나의 정보 화면 관련 함수 (MyInfoFragment)
     * =========================================================
     */

    /**
     * 자동로그인 스위치 클릭 이벤트
     */
    private fun onClickAutoLoginSwitch()
    {
        Log.f("")
        mCheckAutoLogin = !mCheckAutoLogin
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, if(mCheckAutoLogin) "Y" else "N")
        mMyInfoPresenterDataObserver.setAutoLoginSwitch(mCheckAutoLogin)
    }

    /**
     * 푸시 알림 스위치 클릭 이벤트
     */
    private fun onClickPushSwitch()
    {
        Log.f("")
        mCheckPush = !mCheckPush
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_PUSH_SEND, if(mCheckPush) "Y" else "N")
        Log.f("setSubscribeTopic : $mCheckPush")
        if (mCheckPush)
        {
            FirebaseMessaging.getInstance().subscribeToTopic(Common.PUSH_TOPIC_NAME)
        }
        else
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.PUSH_TOPIC_NAME)
        }
        mMyInfoPresenterDataObserver.setPushSwitch(mCheckPush)
    }

    /** ========================================================= */

    /**
     * =========================================================
     *      나의 정보 수정 화면 관련 함수 (MyInfoChangeFragment)
     * =========================================================
     */

    /**
     * 나의 정보 수정 입력값 유효한지 체크
     */
    private fun checkAllInformationData(informationData : MyInformationData, isRequest : Boolean = false)
    {
        Log.f("")
        var result = CheckUserInput.INPUT_SUCCESS

        // 이름 체크
        if (informationData.getName() != "")
        {
            result = CheckUserInput.getInstance(mContext).checkNameData(informationData.getName()).getResultValue()
        }

        // 이메일 체크
        if (result == CheckUserInput.INPUT_SUCCESS && informationData.getEmail() != "")
        {
            result = CheckUserInput.getInstance(mContext).checkEmailData(informationData.getEmail()).getResultValue()
        }

        // 전화번호 체크
        if (result == CheckUserInput.INPUT_SUCCESS && informationData.getPhone() != "")
        {
            result = CheckUserInput.getInstance(mContext).checkPhoneData(informationData.getPhone()).getResultValue()
        }

        if (result == CheckUserInput.INPUT_SUCCESS)
        {
            mMyInfoPresenterDataObserver.onInputDataSuccess(InputDataType.NAME)
            mMyInfoPresenterDataObserver.onInputDataSuccess(InputDataType.EMAIL)
            mMyInfoPresenterDataObserver.onInputDataSuccess(InputDataType.PHONE)
        }
        else
        {
            mMyInfoPresenterDataObserver.onInputDataError(CheckUserInput().getErrorTypeFromResult(result))
            mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(result))
            return
        }

        // 통신요청 전 비밀번호 확인
        if (isRequest)
        {
            mName = informationData.getName()
            mEmail = informationData.getEmail()
            mPhone = informationData.getPhone()
            showPasswordCheckDialog()
        }
    }

    /**
     * 비밀번호 확인 다이얼로그 표시
     */
    private fun showPasswordCheckDialog()
    {
        mPasswordCheckDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_password_check_for_change_user_info))
            setPasswordConfirmView(true)
            setDialogEventType(DIALOG_PASSWORD_CONFIRM)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mPasswordCheckDialogListener)
            show()
        }

    }
    /** ========================================================= */

    /**
     * =========================================================
     *    비밀번호 변경 화면 관련 함수 (MyPasswordChangeFragment)
     * =========================================================
     */
    private fun checkPasswordInputData(passwordData : MyPasswordData, isRequest : Boolean = false)
    {
        Log.f("")
        var result = 0

        if (passwordData.getNewPassword() != "" && passwordData.getNewPasswordConfirm() == "")
        {
            result = CheckUserInput.getInstance(mContext).checkPasswordData(passwordData.getNewPassword()).getResultValue()
        }
        else if (passwordData.getNewPassword() != "" && passwordData.getNewPasswordConfirm() != "")
        {
            result = CheckUserInput.getInstance(mContext).checkPasswordData(passwordData.getNewPassword(), passwordData.getNewPasswordConfirm()).getResultValue()
        }

        if (result == CheckUserInput.INPUT_SUCCESS)
        {
            mMyInfoPresenterDataObserver.onInputDataSuccess(InputDataType.NEW_PASSWORD)
            mMyInfoPresenterDataObserver.onInputDataSuccess(InputDataType.NEW_PASSWORD_CONFIRM)
        }
        else
        {
            if (result == CheckUserInput.WARNING_PASSWORD_WRONG_INPUT)
            {
                mMyInfoPresenterDataObserver.onInputDataError(InputDataType.NEW_PASSWORD)
            }
            else
            {
                mMyInfoPresenterDataObserver.onInputDataError(CheckUserInput().getErrorTypeFromResult(result))
            }
            mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(result))
            return
        }

        // 비밀번호 변경 통신요청
        if (isRequest)
        {
            mPassword = passwordData.getPassword()
            mNewPassword = passwordData.getNewPassword()
            mConfirmPassword = passwordData.getNewPasswordConfirm()
            requestPasswordChange()
        }
    }
    /** ========================================================= */

    /**
     * =========================================================
     *                      통신 요청
     * =========================================================
     */

    /**
     * 나의정보수정 통신 요청
     */
    private fun requestMyInformationChange(password : String)
    {
        val phone = CommonUtils.getInstance(mContext).getPhoneTypeNumber(mPhone) // 전화번호 하이픈 추가
        mMyInformationContractView.showLoading()
        mMyInfoUpdateCoroutine = MyInfoUpdateCoroutine(mContext).apply {
            setData(mName, mEmail, phone, password)
            asyncListener = mAsyncListener
            execute()
        }

    }

    /**
     * 비밀번호 변경 통신 요청
     */
    private fun requestPasswordChange()
    {
        mMyInformationContractView.showLoading()
        mPasswordChangeCoroutine = PasswordChangeCoroutine(mContext).apply {
            setData(mPassword, mNewPassword, mConfirmPassword)
            asyncListener = mAsyncListener
            execute()
        }

    }
    /** ========================================================= */

    /**
     * =========================================================
     *                      Listeners
     * =========================================================
     */

    /**
     * [나의 정보 화면] 이벤트
     */
    private fun setupMyInfoFragmentListener()
    {
        // 자동로그인 스위치 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickAutoLogin.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                onClickAutoLoginSwitch()
            })

        // 푸시알림 스위치 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickPush.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                onClickPushSwitch()
            })

        // 정보 수정 버튼 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickInfoChange.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("나의 정보 수정 화면으로 이동")
                mMyInfoPresenterDataObserver.setMyInfoChangeFragment(mLoginInformation!!) // 데이터 초기화
                mMyInfoPresenterDataObserver.setViewPagerChange(Common.PAGE_MY_INFO_CHANGE)
                mMyInformationContractView.setCurrentViewPage(Common.PAGE_MY_INFO_CHANGE)
            })

        // 비밀번호 변경 버튼 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickPasswordChange.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("비밀번호 변경 화면으로 이동")
                mMyInfoPresenterDataObserver.setViewPagerChange(Common.PAGE_PASSWORD_CHANGE)
                mMyInformationContractView.setCurrentViewPage(Common.PAGE_PASSWORD_CHANGE)
            })
    }

    /**
     * [나의 정보 수정 화면] 이벤트
     */
    private fun setupMyInfoChangeFragmentListener()
    {
        // 전체 입력데이터 체크
        mMyInfoChangeFragmentDataObserver.checkInfoInputDataAvailable.observe(mContext as AppCompatActivity,
            Observer<MyInformationData> { data ->
                checkAllInformationData(data)
            })

        // 저장버튼 클릭 이벤트
        mMyInfoChangeFragmentDataObserver.clickInfoChangeButton.observe(mContext as AppCompatActivity,
            Observer<MyInformationData> { inputData ->
                checkAllInformationData(inputData, true)
            })
    }

    /**
     * [비밀번호 변경화면] 이벤트
     */
    private fun setupMyPasswordChangeFragmentListener()
    {
        // 입력데이터 체크
        mMyInfoChangeFragmentDataObserver.checkPasswordInputDataAvailable.observe(mContext as AppCompatActivity,
            Observer<MyPasswordData> { data ->
                if (data.getNewPassword() != "")
                {
                    // 신규 비밀번호 입력했을 때 유효성 체크
                    checkPasswordInputData(data)
                }
            })

        // 저장버튼 클릭 이벤트
        mMyInfoChangeFragmentDataObserver.clickPasswordChangeButton.observe(mContext as AppCompatActivity,
            Observer<MyPasswordData> { data ->
                checkPasswordInputData(data, true)
            })
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            // 통신 수신 처리
            val result : BaseResult = mObject as BaseResult

            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                // 통신 성공
                if (code == Common.COROUTINE_CODE_MY_INFO_UPDATE)
                {
                    // 나의 정보 수정 성공
                    Log.f("My Info Change Complete")
                    mMyInformationContractView.hideLoading()
                    // 응답받은 데이터로 회원정보 다시 세팅
                    mLoginInformation = (result as LoginBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mLoginInformation)
                    // 성공 메세지 표시하고 나의 정보 화면으로 이동
                    mMyInformationContractView.showSuccessMessage(mContext.getString(R.string.message_myinfo_change_complete))
                    mMyInfoPresenterDataObserver.setMyInfoShowFragment(mLoginInformation!!)
                    MainObserver.updateUserStatus()
                    onClickBackButton()
                }
                else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE)
                {
                    // 비밀번호 변경 성공
                    Log.f("Password Change Complete")
                    mMyInformationContractView.hideLoading()
                    // 바뀐 사용자 비밀번호로 다시 저장
                    mLoginData!!.userPassword = SimpleCrypto.encode(mNewPassword)
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mLoginData)
                    // 성공 메세지 표시하고 나의 정보 화면으로 이동
                    mMyInformationContractView.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
                    onClickBackButton()
                }
            }
            else
            {
                // 통신 실패
                if (result.isDuplicateLogin)
                {
                    // 중복 로그인 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_MY_INFO_UPDATE)
                    {
//                        showTemplateAlertDialog(DIALOG_PASSWORD_CONFIRM_ERR, mContext.resources.getString(R.string.message_warning_password_confirm_retry), DialogButtonType.BUTTON_1)
                        mMyInformationContractView.hideLoading()
                        mMyInformationContractView.showErrorMessage(result.getMessage())
                    }
                    else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE)
                    {
                        mMyInformationContractView.hideLoading()
                        mMyInformationContractView.showErrorMessage(result.getMessage())
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
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {
            if (eventType == DIALOG_PASSWORD_CONFIRM_ERR)
            {
                showPasswordCheckDialog()
            }
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int) {}
    }

    /**
     * 비밀번호 변경 다이얼로그 리스너
     */
    private val mPasswordCheckDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            if (eventType == DIALOG_PASSWORD_CONFIRM)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 비밀번호 확인 취소
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        requestMyInformationChange(mPasswordCheckDialog.getPasswordInputData())
                    }
                }
            }
        }
    }
}