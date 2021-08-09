package com.littlefox.app.foxschool.main.presenter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessaging
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.MainInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.coroutine.AuthMeCoroutine
import com.littlefox.app.foxschool.coroutine.InitCoroutine
import com.littlefox.app.foxschool.coroutine.MainInformationCoroutine
import com.littlefox.app.foxschool.coroutine.PasswordChangeCoroutine
import com.littlefox.app.foxschool.dialog.PasswordChangeDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.PasswordChangeListener
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.IntroContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

import java.util.*

class IntroPresenter : IntroContract.Presenter
{
    companion object
    {
        private const val PERMISSION_REQUEST : Int                  = 0x01
        private const val REQUEST_CODE_LOGIN : Int                  = 1001
        private const val REQUEST_CODE_GO_LOGIN : Int               = 1002

        private const val DIALOG_TYPE_SELECT_UPDATE_CONFIRM : Int   = 10001
        private const val DIALOG_TYPE_FORCE_UPDATE : Int            = 10002

        private const val MESSAGE_INIT : Int                        = 100
        private const val MESSAGE_REQUEST_AUTO_LOGIN : Int          = 101
        private const val MESSAGE_CHECK_API_MAIN : Int              = 102
        private const val MESSAGE_REQUEST_COMPLETE_LOGIN : Int      = 103
        private const val MESSAGE_START_LOGIN : Int                 = 104
        private const val MESSAGE_START_MAIN : Int                  = 105
        private const val MESSAGE_APP_SERVER_ERROR : Int            = 106

        private val PERCENT_SEQUENCE : FloatArray                   = floatArrayOf(0f, 30f, 60f, 100f)
    }

    private lateinit var mContext : Context
    private lateinit var mMainContractView : IntroContract.View
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mAuthMeCoroutine : AuthMeCoroutine? = null
    private var mMainInformationCoroutine : MainInformationCoroutine? = null

    private lateinit var mPermissionList : ArrayList<String>
    private var mCurrentIntroProcess : IntroProcess = IntroProcess.NONE
    private var mInitCoroutine : InitCoroutine? = null
    private var isAutoLogin : Boolean           = false
    private var isDisposableLogin : Boolean     = false

    private var mUserLoginData : UserLoginData? = null // 로그인 데이터
    private var mUserInformationResult : LoginInformationResult? = null // 사용자 정보 (로그인 통신 응답)

    // 비밀번호 변경 안내 관련 변수
    private var mPasswordChangeDialog : PasswordChangeDialog? = null
    private var mPasswordChangeCoroutine : PasswordChangeCoroutine? = null
    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""

    constructor(context : Context)
    {
        mContext = context
        mMainContractView = mContext as IntroContract.View
        mMainContractView.initView()
        mMainContractView.initFont()
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        FirebaseApp.initializeApp(mContext)
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(
            mContext as AppCompatActivity, object : OnSuccessListener<InstanceIdResult?>
            {
                override fun onSuccess(instanceIdResult : InstanceIdResult?)
                {
                    Log.f("new Token : " + instanceIdResult?.getToken())
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_FIREBASE_PUSH_TOKEN, instanceIdResult!!.getToken())
                }
            })

        // 푸쉬 설정값 가져오기
        val isPushEnable = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "")
        Log.f("setSubscribeTopic : $isPushEnable")
        if (isPushEnable == "")
        {
            FirebaseMessaging.getInstance().subscribeToTopic(Common.PUSH_TOPIC_NAME)
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_PUSH_SEND, "Y")
        }


        mMainHandler.sendEmptyMessageDelayed(MESSAGE_INIT, Common.DURATION_NORMAL)
    }

    private fun init()
    {
        Log.init(Common.LOG_FILE)
        CommonUtils.getInstance(mContext).windowInfo()
        CommonUtils.getInstance(mContext).showDeviceInfo()
        CommonUtils.getInstance(mContext).initFeature()
        LittlefoxLocale.setLocale(Locale.getDefault().toString())
        mPermissionList = ArrayList()
        mPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        mPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        checkUserStatus()
        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
        Log.f("autoLoginStatus : $autoLoginStatus")
        isAutoLogin = if(autoLoginStatus == "Y") true else false
        isDisposableLogin = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("isAutoLogin : $isAutoLogin, isDisposableLogin : $isDisposableLogin")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(CommonUtils.getInstance(mContext).getUnAuthorizePermissionList(mPermissionList).size > 0)
            {
                Log.f("")
                CommonUtils.getInstance(mContext).requestPermission(mPermissionList, PERMISSION_REQUEST)
            }
            else
            {
                executeSequence()
            }
        }
        else
        {
            executeSequence()
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
        release()
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
        when(requestCode)
        {
            REQUEST_CODE_LOGIN ->
                if(resultCode == Activity.RESULT_OK)
                {
                    mMainContractView.showProgressView()
                    /**
                     * Login Activity의 Activity 종료가 늦게되서 프로그래스랑 겹쳐 틱 되는 현상 때문에 조금 늦췃다.
                     */
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_COMPLETE_LOGIN, Common.DURATION_NORMAL)
                }

            REQUEST_CODE_GO_LOGIN ->
                if(resultCode == Activity.RESULT_OK)
                {
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
                }
        }
    }

    /**
     * 사용자 체크 (무료/유료)
     * 팍스스쿨은 무료 이용자가 없으므로 추후 수정 예정
     */
    private fun checkUserStatus()
    {
        val `object` : UserLoginData? = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?
        if(`object` == null)
        {
            Feature.IS_FREE_USER = true
        }
        else
        {
            Feature.IS_FREE_USER = false
        }
    }

    private fun executeSequence()
    {
        settingLogFile()
        if(isAutoLogin || isDisposableLogin)
        {
            if(isDisposableLogin)
            {
                isDisposableLogin = false
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, false)
            }
            mMainContractView.showProgressView()
            requestInitAsync()
        }
        else
        {
            Feature.IS_FREE_USER = true
            mMainContractView.showItemSelectView()
        }
    }

    /**
     * 버젼정보를 보고 버젼이 서버와 같거나, 또는 사용자가 업데이트를 하지않아도 판단될때 API 프로세스를 진행 시킨다.
     */
    private fun startAPIProcess()
    {
        Log.f("")
        if(Feature.IS_FREE_USER)
        {
            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
        }
        else
        {
            mCurrentIntroProcess = IntroProcess.INIT_COMPLETE
            enableProgressAnimation(IntroProcess.INIT_COMPLETE)
        }
    }

    private fun enableProgressAnimation(process : IntroProcess)
    {
        Log.f("process : $process")
        when(process)
        {
            IntroProcess.INIT_COMPLETE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[0], PERCENT_SEQUENCE[1])
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_AUTO_LOGIN, Common.DURATION_SHORT_LONG)
            }
            IntroProcess.LOGIN_COMPLTE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[1], PERCENT_SEQUENCE[2])
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_API_MAIN, Common.DURATION_SHORT_LONG)
            }
            IntroProcess.MAIN_COMPELTE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[2], PERCENT_SEQUENCE[3])
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_MAIN, Common.DURATION_SHORT_LONG)
            }
        }
    }

    private fun settingLogFile()
    {
        val logfileSize = Log.getLogfileSize()
        Log.f("Log file Size : $logfileSize")
        if(logfileSize > Common.MAXIMUM_LOG_FILE_SIZE || logfileSize == 0L)
        {
            Log.initWithDeleteFile(Common.LOG_FILE)
        }
    }

    private fun showTemplateAlertDialog(type : Int, buttonType : DialogButtonType, message : String)
    {
        Log.f("Update Pop up")
        val dialog = TemplateAlertDialog(mContext)
        dialog.setMessage(message)
        dialog.setDialogEventType(type)
        dialog.setButtonType(buttonType)
        dialog.setDialogListener(mDialogListener)
        dialog.show()
    }

    private fun showPasswordChangeDialog()
    {
        Log.f("")
        mPasswordChangeDialog = PasswordChangeDialog(mContext)
        mPasswordChangeDialog?.setPasswordChangeListener(mPasswordChangeDialogListener)
        mPasswordChangeDialog?.setCancelable(false)
        mPasswordChangeDialog?.show()
    }

    private fun requestInitAsync()
    {
        Log.f("")
        mInitCoroutine = InitCoroutine(mContext)
        mInitCoroutine?.setData(
            CommonUtils.getInstance(mContext).secureDeviceID,
            CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_FIREBASE_PUSH_TOKEN, DataType.TYPE_STRING),
            CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")
        )
        mInitCoroutine?.asyncListener = mIntroAsyncListener
        mInitCoroutine?.execute()
    }

    private fun requestAutoLoginAsync()
    {
        Log.f("")
        mAuthMeCoroutine = AuthMeCoroutine(mContext)
        mAuthMeCoroutine?.asyncListener = mIntroAsyncListener
        mAuthMeCoroutine?.execute()
    }

    private fun requestMainInformationAsync()
    {
        Log.f("")
        mMainInformationCoroutine = MainInformationCoroutine(mContext)
        mMainInformationCoroutine?.asyncListener = mIntroAsyncListener
        mMainInformationCoroutine?.execute()
    }

    /**
     * 비밀번호 변경하기 통신 요청
     */
    private fun requestPasswordChange()
    {
        mPasswordChangeDialog!!.showLoading()
        mPasswordChangeCoroutine = PasswordChangeCoroutine(mContext)
        mPasswordChangeCoroutine!!.setData(mPassword, mNewPassword, mConfirmPassword)
        mPasswordChangeCoroutine!!.asyncListener = mIntroAsyncListener
        mPasswordChangeCoroutine!!.execute()
    }


    private fun startMainActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MAIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setIntentFlag(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .startActivity()
        (mContext as AppCompatActivity).finish()
    }


    private fun startLoginActivity()
    {
        val isLoginFromMain = false
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.LOGIN)
            .setData(isLoginFromMain)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_LOGIN)
            .startActivity()
    }

    private fun release()
    {
        Log.f("")
        mAuthMeCoroutine?.cancel()
        mAuthMeCoroutine = null
        mMainInformationCoroutine?.cancel()
        mMainInformationCoroutine = null
        mPasswordChangeCoroutine?.cancel()
        mPasswordChangeCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
        (mContext as AppCompatActivity).finish()
    }

    override fun onClickIntroduce()
    {
        // TODO 팍스스쿨 소개 (WEB)
    }

    override fun onClickHomeButton()
    {
        Log.f("")
        release()
    }

    override fun onClickLogin()
    {
        Log.f("")
        //startLoginActivity()
        mPasswordChangeDialog = PasswordChangeDialog(mContext)
        mPasswordChangeDialog?.setPasswordChangeListener(mPasswordChangeDialogListener)
        mPasswordChangeDialog?.setCancelable(true)
        mPasswordChangeDialog?.show()
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<String>, grantResults : IntArray)
    {
        Log.f("requestCode : $requestCode")
        var isAllCheckSuccess = true
        when(requestCode)
        {
            PERMISSION_REQUEST ->
            {
                var i = 0
                while(i < permissions.size)
                {
                    Log.f("permission : " + permissions[i] + ", grantResults : " + grantResults[i])
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        isAllCheckSuccess = false
                    }
                    i++
                }

                if(isAllCheckSuccess == false)
                {
                    (mContext as AppCompatActivity).finish()
                }
                else
                {
                    executeSequence()
                }
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_INIT -> init()
            MESSAGE_REQUEST_AUTO_LOGIN -> requestAutoLoginAsync()
            MESSAGE_CHECK_API_MAIN -> requestMainInformationAsync()
            MESSAGE_REQUEST_COMPLETE_LOGIN -> requestInitAsync()
            MESSAGE_START_LOGIN -> startLoginActivity()
            MESSAGE_START_MAIN ->
            {
                Log.f("MESSAGE_START_MAIN")
                startMainActivity()
            }
            MESSAGE_APP_SERVER_ERROR ->
            {
                Log.f("== Server Error  ==")
                Toast.makeText(mContext, mContext.resources.getString(R.string.message_warning_app_server_error), Toast.LENGTH_LONG).show()
                (mContext as AppCompatActivity).finish()
                IntentManagementFactory.getInstance().initScene()
            }
        }
    }

    private val mIntroAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) {}

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult

            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_INIT)
                {
                    val versionDataResult : VersionDataResult = (result as VersionBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_VERSION_INFORMATION, versionDataResult)
                   /* if(versionDataResult.isNeedUpdate)
                    {
                        if(versionDataResult.isForceUpdate())
                        {
                            showTemplateAlertDialog(
                                DIALOG_TYPE_FORCE_UPDATE,
                                DialogButtonType.BUTTON_1,
                                mContext.resources.getString(R.string.message_force_update)
                            )
                        } else
                        {
                            showTemplateAlertDialog(
                                DIALOG_TYPE_SELECT_UPDATE_CONFIRM,
                                DialogButtonType.BUTTON_2,
                                mContext.resources.getString(R.string.message_need_update)
                            )
                        }
                    } else
                    {
                        startAPIProcess()
                    }*/
                    startAPIProcess()
                } else if(code == Common.COROUTINE_CODE_ME)
                {
                    mUserInformationResult = (result as LoginBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)

                    if (mUserInformationResult!!.getChangeDate() >= 90)
                    {
                        // 비밀번호 변경 날짜가 90일을 넘어가는 경우 비밀번호 변경 안내 다이얼로그를 표시한다.
                        mUserLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?
                        showPasswordChangeDialog()
                    }
                    else
                    {
                        // 자동로그인 완료
                        mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                        enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
                    }
                }
                else if(code == Common.COROUTINE_CODE_MAIN)
                {
                    Log.f("Main data get to API Success")
                    val mainInformationResult : MainInformationResult = (`object` as MainInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
                    mCurrentIntroProcess = IntroProcess.MAIN_COMPELTE
                    enableProgressAnimation(IntroProcess.MAIN_COMPELTE)
                }
                else if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE)
                {
                    // 비밀번호 변경 성공
                    // 성공 메세지 표시하고 자동로그인 해제, 다시 로그인 할 수 있도록 로그인 화면으로 이동한다.
                    Log.f("Password Change Complete")
                    mPasswordChangeDialog!!.hideLoading()
                    mPasswordChangeDialog!!.dismiss()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
                    isAutoLogin = false
                    mMainContractView.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
                    onClickLogin()
                }
            } else
            {
                Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                if(result.isAuthenticationBroken || result.getStatus() == BaseResult.FAIL_CODE_INTERNAL_SERVER_ERROR)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_PASSWORD_CHANGE && mPasswordChangeDialog!!.isShowing)
                    {
                        mPasswordChangeDialog!!.hideLoading()
                        mPasswordChangeDialog!!.showErrorMessage(result.getMessage())
                    }
                    else
                    {
                        (mContext as AppCompatActivity).finish()
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String) {}

        override fun onRunningProgress(code : String, progress : Int) {}

        override fun onRunningAdvanceInformation(code : String, `object` : Any) {}

        override fun onErrorListener(code : String, message : String)
        {
            mMainHandler.sendEmptyMessage(MESSAGE_APP_SERVER_ERROR)
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int)
        {
            if(messageType == DIALOG_TYPE_FORCE_UPDATE)
            {
                (mContext as AppCompatActivity).finish()
                CommonUtils.getInstance(mContext).startLinkMove(Common.APP_LINK)
            }
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, messageType : Int)
        {
            Log.f("messageType : $messageType, buttonType : $buttonType")
            if(messageType == DIALOG_TYPE_SELECT_UPDATE_CONFIRM)
            {
                if(buttonType == DialogButtonType.BUTTON_1)
                {
                    startAPIProcess()
                }
                else if(buttonType == DialogButtonType.BUTTON_2)
                {
                    (mContext as AppCompatActivity).finish()
                    CommonUtils.getInstance(mContext).startLinkMove(Common.APP_LINK)
                }
            }
        }
    }

    /**
     * 비밀번호 변경 다이얼로그 Listener
     */
    val mPasswordChangeDialogListener : PasswordChangeListener = object : PasswordChangeListener
    {
        /**
         * 화면 유형 가져오기 (90일/180일)
         */
        override fun getScreenType() : PasswordGuideType
        {
           /* if (mUserInformationResult!!.getChangeDate() >= 180)
            {
                return PasswordGuideType.CHANGE180
            }
            else if (mUserInformationResult!!.getChangeDate() >= 90)
            {
                return PasswordGuideType.CHANGE90
            }*/
            return PasswordGuideType.CHANGE90
        }

        /**
         * 기존 비밀번호와 일치한지 체크
         * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
         */
        override fun checkPassword(password : String, showMessage : Boolean) : Boolean
        {
            // 기존 비밀번호와 일치한지 체크
            if (mUserLoginData != null && password != "")
            {
                val result = CheckUserInput.getInstance(mContext)
                    .checkPasswordData(SimpleCrypto.decode(mUserLoginData!!.userPassword), password)
                    .getResultValue()

                if (result != CheckUserInput.INPUT_SUCCESS)
                {
                    if (showMessage)
                    {
                        mPasswordChangeDialog!!.setInputError(InputDataType.PASSWORD, mContext.resources.getString(R.string.message_warning_password_confirm))
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
        override fun checkNewPasswordAvailable(newPassword : String, showMessage : Boolean) : Boolean
        {
            val result = CheckUserInput.getInstance(mContext).checkPasswordData(newPassword).getResultValue()

            if (result == CheckUserInput.WARNING_PASSWORD_WRONG_INPUT)
            {
                if (showMessage)
                {
                    mPasswordChangeDialog!!.setInputError(InputDataType.NEW_PASSWORD, CheckUserInput().getErrorMessage(result))
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
        override fun checkNewPasswordConfirm(newPassword : String, newPasswordConfirm : String, showMessage : Boolean) : Boolean
        {
            val result = CheckUserInput.getInstance(mContext)
                .checkPasswordData(newPassword, newPasswordConfirm)
                .getResultValue()

            if (result == CheckUserInput.WARNING_PASSWORD_NOT_INPUT_CONFIRM ||
                result == CheckUserInput.WARNING_PASSWORD_NOT_EQUAL_CONFIRM)
            {
                if (showMessage)
                {
                    mPasswordChangeDialog!!.setInputError(CheckUserInput().getErrorTypeFromResult(result), CheckUserInput().getErrorMessage(result))
                }
                return false
            }
            return true
        }

        /**
         * 비밀번호 변경화면 입력값 다 유효한지 체크
         */
        override fun checkAllAvailable(oldPassword : String, newPassword : String, confirmPassword : String) : Boolean
        {
            if (oldPassword.isEmpty() || (checkPassword(oldPassword) == false))
            {
                mPasswordChangeDialog!!.setChangeButtonEnable(false)
                return false
            }
            else if (newPassword.isEmpty() || (checkNewPasswordAvailable(newPassword) == false))
            {
                mPasswordChangeDialog!!.setChangeButtonEnable(false)
                return false
            }
            else if (confirmPassword.isEmpty() || (checkNewPasswordConfirm(newPassword, confirmPassword) == false))
            {
                mPasswordChangeDialog!!.setChangeButtonEnable(false)
                return false
            }
            mPasswordChangeDialog!!.setChangeButtonEnable(true)
            return true
        }

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
         * TODO 김태은 추후 로직 확인 필요
         */
        override fun onClickLaterButton()
        {
            mPasswordChangeDialog!!.dismiss()
            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
        }

        /**
         * [현재 비밀번호로 유지하기] 버튼 클릭 이벤트
         * TODO 김태은 추후 로직 확인 필요
         */
        override fun onClickKeepButton()
        {
            mPasswordChangeDialog!!.dismiss()
            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
        }
    }
}