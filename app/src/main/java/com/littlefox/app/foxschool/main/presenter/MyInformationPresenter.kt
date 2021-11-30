package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.messaging.FirebaseMessaging
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
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
import com.littlefox.app.foxschool.enumerate.BioCheckResultType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.MyInformationContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.MyInfoChangeFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoShowFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoPresenterDataObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
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

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
        }
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
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog.setMessage(message)
        mTemplateAlertDialog.setDialogEventType(type)
        mTemplateAlertDialog.setButtonType(buttonType)
        mTemplateAlertDialog.setDialogListener(mDialogListener)
        mTemplateAlertDialog.show()
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
     * 지문인증 로그인 스위치 클릭 이벤트
     */
    private fun onClickBioLoginSwitch()
    {
        Log.f("")
        val bioCheck = CommonUtils.getInstance(mContext).checkCanBioLogin()
        when(bioCheck)
        {
            BioCheckResultType.BIO_CANT_USE_HARDWARE ->
            {
                // 생체인증 사용 불가능 기기
                mMyInformationContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_cant_use_bio))
                return
            }
            BioCheckResultType.BIO_UNABLE ->
            {
                // 생체인증 기능 OFF 상태
                mMyInformationContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_unable_bio))
                return
            }
            BioCheckResultType.BIO_NONE ->
            {
                // 등록된 생체인증 정보가 없음
                mMyInformationContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_none_bio))
                return
            }
            else -> {}
        }
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
     * 이름 입력값 유효성 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkNameAvailable(name : String, showMessage : Boolean = false) : Boolean
    {
        val nameResult = CheckUserInput.getInstance(mContext).checkNameData(name).getResultValue()
        if(nameResult == CheckUserInput.WARNING_NAME_WRONG_INPUT)
        {
            if (showMessage)
            {
                mMyInfoPresenterDataObserver.setInputError(CheckUserInput().getErrorTypeFromResult(nameResult))
                mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(nameResult))
            }
            return false
        }
        return true
    }

    /**
     * 이메일 입력값 유효성 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkEmailAvailable(email : String, showMessage : Boolean = false) : Boolean
    {
        val emailResult = CheckUserInput.getInstance(mContext).checkEmailData(email).getResultValue()
        if(emailResult == CheckUserInput.WARNING_EMAIL_WRONG_INPUT)
        {
            if (showMessage)
            {
                mMyInfoPresenterDataObserver.setInputError(CheckUserInput().getErrorTypeFromResult(emailResult))
                mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(emailResult))
            }
            return false
        }
        return true
    }

    /**
     * 전화번호 입력값 유효성 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkPhoneAvailable(phone : String, showMessage : Boolean = false) : Boolean
    {
        val data = CommonUtils.getInstance(mContext).getReplaceBothEndTrim(phone)
        val convertData = CommonUtils.getInstance(mContext).getPhoneTypeNumber(data)
        val phoneResult = CheckUserInput.getInstance(mContext).checkPhoneData(convertData).getResultValue()

        if(phoneResult == CheckUserInput.WARNING_PHONE_WRONG_INPUT)
        {
            if (showMessage)
            {
                mMyInfoPresenterDataObserver.setInputError(CheckUserInput().getErrorTypeFromResult(phoneResult))
                mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(phoneResult))
            }
            return false
        }
        return true
    }

    /**
     * 입력값 유효한지 체크
     * 이메일, 전화번호는 선택사항이기 때문에 입력된 경우에만 유효성을 체크한다.
     */
    private fun checkInfoInputData(name : String, email : String, phone : String) : Boolean
    {
        if (name.isEmpty() || (checkNameAvailable(name) == false))
        {
            return false
        }
        else if (email.isNotEmpty() && (checkEmailAvailable(email) == false))
        {
            return false
        }
        else if (phone.isNotEmpty() && (checkPhoneAvailable(phone) == false))
        {
            return false
        }
        return true
    }

    /**
     * 기존 비밀번호와 일치한지 체크 (다이얼로그 표시)
     */
    private fun checkPasswordOnDialog(password : String)
    {
        // 기존 비밀번호와 일치한지 체크
        if (mLoginData != null)
        {
            val result = CheckUserInput.getInstance(mContext).checkPasswordData(
                SimpleCrypto.decode(mLoginData!!.userPassword),
                password
            ).getResultValue()

            if (result == CheckUserInput.INPUT_SUCCESS)
            {
                requestMyInformationChange(password)
            }
            else
            {
                showTemplateAlertDialog(DIALOG_PASSWORD_CONFIRM_ERR, mContext.resources.getString(R.string.message_warning_password_confirm_retry), DialogButtonType.BUTTON_1)
            }
        }
    }

    /**
     * 비밀번호 확인 다이얼로그 표시
     */
    private fun showPasswordCheckDialog()
    {
        mPasswordCheckDialog = TemplateAlertDialog(mContext)
        mPasswordCheckDialog.setMessage(mContext.resources.getString(R.string.message_password_check_for_change_user_info))
        mPasswordCheckDialog.setPasswordConfirmView(true)
        mPasswordCheckDialog.setDialogEventType(DIALOG_PASSWORD_CONFIRM)
        mPasswordCheckDialog.setButtonType(DialogButtonType.BUTTON_2)
        mPasswordCheckDialog.setDialogListener(mPasswordCheckDialogListener)
        mPasswordCheckDialog.show()
    }
    /** ========================================================= */

    /**
     * =========================================================
     *    비밀번호 변경 화면 관련 함수 (MyPasswordChangeFragment)
     * =========================================================
     */
    /**
     * 기존 비밀번호와 일치한지 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkPassword(password : String, showMessage : Boolean = false) : Boolean
    {
        // 기존 비밀번호와 일치한지 체크
        if (mLoginData != null)
        {
            val result = CheckUserInput.getInstance(mContext)
                .checkPasswordData(SimpleCrypto.decode(mLoginData!!.userPassword), password)
                .getResultValue()

            if (result != CheckUserInput.INPUT_SUCCESS)
            {
                if (showMessage)
                {
                    mMyInfoPresenterDataObserver.setInputError(InputDataType.PASSWORD)
                    mMyInformationContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_password_confirm))
                }
                return false
            }
            return true
        }
        return false
    }

    /**
     * 새 비밀번호가 유효한지 체크
     * 1. 비밀번호 규칙 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkNewPasswordAvailable(newPassword : String, showMessage : Boolean = false) : Boolean
    {
        val result = CheckUserInput.getInstance(mContext).checkPasswordData(newPassword).getResultValue()

        if (result == CheckUserInput.WARNING_PASSWORD_WRONG_INPUT)
        {
            if (showMessage)
            {
                mMyInfoPresenterDataObserver.setInputError(InputDataType.NEW_PASSWORD)
                mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(result))
            }
            return false
        }
        return true
    }

    /**
     * 새 비밀번호가 유효한지 체크
     * 1. 새 비밀번호 확인 입력 체크
     * 2. 새 비밀번호 확인과 일치한지 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkNewPasswordConfirm(newPassword : String, newPasswordConfirm : String, showMessage : Boolean = false) : Boolean
    {
        val result = CheckUserInput.getInstance(mContext)
            .checkPasswordData(newPassword, newPasswordConfirm)
            .getResultValue()

        if (result == CheckUserInput.WARNING_PASSWORD_NOT_INPUT_CONFIRM ||
            result == CheckUserInput.WARNING_PASSWORD_NOT_EQUAL_CONFIRM)
        {
            if (showMessage)
            {
                mMyInfoPresenterDataObserver.setInputError(CheckUserInput().getErrorTypeFromResult(result))
                mMyInformationContractView.showErrorMessage(CheckUserInput().getErrorMessage(result))
            }
            return false
        }
        return true
    }

    /**
     * 비밀번호 변경화면 입력값 다 유효한지 체크
     */
    private fun checkPasswordInputData(oldPassword : String, newPassword : String, confirmPassword : String) : Boolean
    {
        if (oldPassword.isEmpty() || (checkPassword(oldPassword) == false))
        {
            return false
        }
        else if (newPassword.isEmpty() || (checkNewPasswordAvailable(newPassword) == false))
        {
            return false
        }
        else if (confirmPassword.isEmpty() || (checkNewPasswordConfirm(newPassword, confirmPassword) == false))
        {
            return false
        }
        return true
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
        mMyInfoUpdateCoroutine = MyInfoUpdateCoroutine(mContext)
        mMyInfoUpdateCoroutine!!.setData(mName, mEmail, phone, password)
        mMyInfoUpdateCoroutine!!.asyncListener = mAsyncListener
        mMyInfoUpdateCoroutine!!.execute()
    }

    /**
     * 비밀번호 변경 통신 요청
     */
    private fun requestPasswordChange()
    {
        mMyInformationContractView.showLoading()
        mPasswordChangeCoroutine = PasswordChangeCoroutine(mContext)
        mPasswordChangeCoroutine!!.setData(mPassword, mNewPassword, mConfirmPassword)
        mPasswordChangeCoroutine!!.asyncListener = mAsyncListener
        mPasswordChangeCoroutine!!.execute()
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
        mMyInfoShowFragmentDataObserver.clickAutoLogin.observe(mContext as AppCompatActivity, {
            onClickAutoLoginSwitch()
        })

        // 푸시알림 스위치 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickPush.observe(mContext as AppCompatActivity, {
            onClickPushSwitch()
        })

        // 정보 수정 버튼 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickInfoChange.observe(mContext as AppCompatActivity, {
            Log.f("나의 정보 수정 화면으로 이동")
            mMyInfoPresenterDataObserver.setViewPagerChange(Common.PAGE_MY_INFO_CHANGE)
            mMyInformationContractView.setCurrentViewPage(Common.PAGE_MY_INFO_CHANGE)
        })

        // 비밀번호 변경 버튼 클릭 이벤트
        mMyInfoShowFragmentDataObserver.clickPasswordChange.observe(mContext as AppCompatActivity, {
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
        // 이름 유효성 체크
        mMyInfoChangeFragmentDataObserver.checkNameAvailable.observe(mContext as AppCompatActivity, {name ->
            checkNameAvailable(name, true)
        })

        // 이메일 유효성 체크
        mMyInfoChangeFragmentDataObserver.checkEmailAvailable.observe(mContext as AppCompatActivity, {email ->
            checkEmailAvailable(email, true)
        })

        // 휴대폰 번호 유효성 체크
        mMyInfoChangeFragmentDataObserver.checkPhoneAvailable.observe(mContext as AppCompatActivity, {phone ->
            checkPhoneAvailable(phone, true)
        })

        // 저장버튼 활성화 가능한지 체크
        mMyInfoChangeFragmentDataObserver.checkInfoInputDataAvailable.observe(mContext as AppCompatActivity, {data ->
            val name = data["name"] as String
            val email = data["email"] as String
            val phone = data["phone"] as String
            val isEnable = checkInfoInputData(name, email, phone)
            mMyInfoPresenterDataObserver.setSaveInfoButtonEnable(isEnable)
        })

        // 저장버튼 클릭 이벤트
        mMyInfoChangeFragmentDataObserver.clickInfoChangeButton.observe(mContext as AppCompatActivity, {data ->
            mName = data["name"] as String
            mEmail = data["email"] as String
            mPhone = data["phone"] as String
            showPasswordCheckDialog()
        })
    }

    /**
     * [비밀번호 변경화면] 이벤트
     */
    private fun setupMyPasswordChangeFragmentListener()
    {
        // 비밀번호 기존과 동일한지 체크
        mMyInfoChangeFragmentDataObserver.checkPassword.observe(mContext as AppCompatActivity, {password ->
            if (password != "") checkPassword(password, true)
        })

        // 새로운 비밀번호 유효성 체크
        mMyInfoChangeFragmentDataObserver.checkNewPasswordAvailable.observe(mContext as AppCompatActivity, {password ->
            checkNewPasswordAvailable(password, true)
        })

        // 비밀번호 확인 유효성 체크
        mMyInfoChangeFragmentDataObserver.checkNewPasswordConfirm.observe(mContext as AppCompatActivity, {data ->
            val password = data["oldPassword"] as String
            val newPassword = data["newPassword"] as String
            checkNewPasswordConfirm(password, newPassword, true)
        })

        // 저장버튼 활성화 가능한지 체크
        mMyInfoChangeFragmentDataObserver.checkPasswordInputDataAvailable.observe(mContext as AppCompatActivity, {data ->
            val password = data["oldPassword"] as String
            val newPassword = data["newPassword"] as String
            val confirmPassword = data["confirmPassword"] as String
            val isEnable = checkPasswordInputData(password, newPassword, confirmPassword)
            mMyInfoPresenterDataObserver.setSavePasswordButtonEnable(isEnable)
        })

        // 저장버튼 클릭 이벤트
        mMyInfoChangeFragmentDataObserver.clickPasswordChangeButton.observe(mContext as AppCompatActivity, {data ->
            mPassword = data["oldPassword"] as String
            mNewPassword = data["newPassword"] as String
            mConfirmPassword = data["confirmPassword"] as String
            requestPasswordChange()
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
                if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_MY_INFO_UPDATE)
                    {
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
                        // TODO 추후 API 방식에 따라 바로 통신으로 날릴수도 있음
                        // 입력된 비밀번호 체크
                        checkPasswordOnDialog(mPasswordCheckDialog.getPasswordInputData())
                    }
                }
            }
        }
    }
}