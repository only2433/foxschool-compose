package com.littlefox.app.foxschool.main.presenter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Message
import android.provider.Settings

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.MainInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.IntroViewModel
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.coroutine.*
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
import com.littlefox.logmonitor.enumItem.MonitorMode
import java.util.*
import kotlin.collections.ArrayList

class IntroPresenter : IntroContract.Presenter
{
    companion object
    {
        private const val PERMISSION_REQUEST : Int                  = 0x01

        private const val DIALOG_TYPE_SELECT_UPDATE_CONFIRM : Int   = 10001
        private const val DIALOG_TYPE_FORCE_UPDATE : Int            = 10002
        private const val DIALOG_TYPE_WARNING_FILE_PERMISSION       = 10003

        private const val MESSAGE_INIT : Int                        = 100
        private const val MESSAGE_REQUEST_AUTO_LOGIN : Int          = 101
        private const val MESSAGE_CHECK_API_MAIN : Int              = 102
        private const val MESSAGE_REQUEST_COMPLETE_LOGIN : Int      = 103
        private const val MESSAGE_START_LOGIN : Int                 = 104
        private const val MESSAGE_START_MAIN : Int                  = 105
        private const val MESSAGE_APP_SERVER_ERROR : Int            = 106
        private const val MESSAGE_DEVELOPER_TO_EMAIL : Int          = 107
        private const val MESSAGE_CHANGE_PASSWORD : Int             = 108

        private const val INDEX_LOGIN                               = 0

        private val PERCENT_SEQUENCE : FloatArray                   = floatArrayOf(0f, 30f, 60f, 100f)
    }

    private lateinit var mContext : Context
    private lateinit var mMainContractView : IntroContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private lateinit var mPermissionList : ArrayList<String>
    private var mCurrentIntroProcess : IntroProcess = IntroProcess.NONE

    private var isAutoLogin : Boolean           = false
    private var isDisposableLogin : Boolean     = false

    private var mUserLoginData : UserLoginData? = null // 로그인 데이터
    private var mUserInformationResult : LoginInformationResult? = null // 사용자 정보 (로그인 통신 응답)
    private var mVersionDataResult : VersionDataResult? = null // 버전정보

    // 비밀번호 변경 안내 관련 변수
    private var mPasswordChangeDialog : PasswordChangeDialog? = null

    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var isRequestPermission : Boolean = false
    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>
    private lateinit var mIntroViewModel: IntroViewModel

    constructor(context : Context, viewModel : IntroViewModel)
    {
        mContext = context
        mIntroViewModel = viewModel
        mMainContractView = (mContext as IntroContract.View).apply {
            initView()
            initFont()
        }
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        FirebaseApp.initializeApp(mContext)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            try
            {
                if (it.isComplete)
                {
                    val token = it.result.toString()
                    Log.f("new Token : " + token)
                    CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_FIREBASE_PUSH_TOKEN, token)
                }
            }catch(e : Exception){}
        }

        // 푸쉬 설정값 가져오기
        val isPushEnable = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "")
        Log.f("setSubscribeTopic : $isPushEnable")
        if (isPushEnable == "")
        {
            FirebaseMessaging.getInstance().subscribeToTopic(Common.PUSH_TOPIC_NAME)
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_PUSH_SEND, "Y")
        }

        setupViewModelObserver()

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_INIT, Common.DURATION_NORMAL)
    }

    private fun init()
    {
        Log.init(mContext, Common.LOG_FILE , MonitorMode.RELEASE_MODE)

        CommonUtils.getInstance(mContext).windowInfo()
        CommonUtils.getInstance(mContext).showDeviceInfo()
        CommonUtils.getInstance(mContext).initFeature()
        LittlefoxLocale.setLocale(Locale.getDefault().toString())
        mPermissionList = ArrayList<String>().apply {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.RECORD_AUDIO)
        }

        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")

        isAutoLogin = if(autoLoginStatus == "Y") true else false
        isDisposableLogin = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("isAutoLogin : $isAutoLogin, isDisposableLogin : $isDisposableLogin")

        checkPermission()
    }

    private fun setupViewModelObserver()
    {
        mIntroViewModel._versionData.observe(mContext as AppCompatActivity){ data ->

            mVersionDataResult = data
            CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_VERSION_INFORMATION, mVersionDataResult)
            if(mVersionDataResult!!.isNeedUpdate)
            {
                if(mVersionDataResult!!.isForceUpdate())
                {
                    showTemplateAlertDialog(
                        DIALOG_TYPE_FORCE_UPDATE,
                        DialogButtonType.BUTTON_1,
                        mContext.resources.getString(R.string.message_force_update)
                    )
                }
                else
                {
                    showTemplateAlertDialog(
                        DIALOG_TYPE_SELECT_UPDATE_CONFIRM,
                        DialogButtonType.BUTTON_2,
                        mContext.resources.getString(R.string.message_need_update)
                    )
                }
            }
            else
            {
                startAPIProcess()
            }
        }

        mIntroViewModel._authMeData.observe(mContext as AppCompatActivity){ data ->

            mUserInformationResult = data
            CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)

            if (mUserInformationResult!!.isNeedChangePassword())
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

        mIntroViewModel._mainData.observe(mContext as AppCompatActivity){ data ->

            Log.f("Main data get to API Success")
            CommonUtils.getInstance(mContext).saveMainData(data)
            mCurrentIntroProcess = IntroProcess.MAIN_COMPELTE
            enableProgressAnimation(IntroProcess.MAIN_COMPELTE)
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_MAIN, Common.DURATION_SHORT_LONG)
        }

        mIntroViewModel._changePasswordData.observe(mContext as AppCompatActivity){

            // 비밀번호 변경 성공
            Log.f("Password Change Complete")
            changeUserLoginData()
            mPasswordChangeDialog!!.hideLoading()
            mPasswordChangeDialog!!.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_CHANGE_PASSWORD, Common.DURATION_LONG)
        }

        mIntroViewModel._changePasswordNextData.observe(mContext as AppCompatActivity){

            // 다음에 변경
            mPasswordChangeDialog!!.hideLoading()
            mMainHandler.sendEmptyMessage(MESSAGE_CHANGE_PASSWORD)
        }

        mIntroViewModel._changePasswordKeepData.observe(mContext as AppCompatActivity){

            // 현재 비밀번호 유지
            mPasswordChangeDialog!!.hideLoading()
            mPasswordChangeDialog!!.showSuccessMessage(mContext.getString(R.string.message_password_change_complete))
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_CHANGE_PASSWORD, Common.DURATION_LONG)
        }

        mIntroViewModel._errorReport.observe(mContext as AppCompatActivity){ data ->

            val result = data.first as ResultData.Fail
            val code = data.second

            Log.f("status : ${result.status}, message : ${result.message} , code : ${data.second}")

            Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
            if(result.isAuthenticationBroken || result.status == BaseResult.FAIL_CODE_INTERNAL_SERVER_ERROR)
            {
                Log.f("== isAuthenticationBroken ==")
                (mContext as AppCompatActivity).finish()
                IntentManagementFactory.getInstance().initScene()
            }
            else
            {
                if (code == RequestCode.CODE_PASSWORD_CHANGE ||
                    code == RequestCode.CODE_PASSWORD_CHANGE_NEXT ||
                    code == RequestCode.CODE_PASSWORD_CHANGE_KEEP)
                {
                    if (mPasswordChangeDialog!!.isShowing)
                    {
                        mPasswordChangeDialog!!.hideLoading()
                    }
                    mPasswordChangeDialog!!.showErrorMessage(result.message)
                }
                else
                {
                    (mContext as AppCompatActivity).finish()
                }
            }
        }
    }

    override fun onAddActivityResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(0))
    }

    override fun onActivityResultLogin()
    {
        Log.f("")
        mMainContractView.showProgressView()
        /**
         * Login Activity의 Activity 종료가 늦게되서 프로그래스랑 겹쳐 틱 되는 현상 때문에 조금 늦췃다.
         */
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_COMPLETE_LOGIN, Common.DURATION_NORMAL)
    }

    override fun resume()
    {
        Log.f("")

        if(isRequestPermission)
        {
            isRequestPermission = false
            checkPermission()
        }
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

    private fun checkPermission()
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
            requestAutoLoginAsync()
            requestMainInformationAsync()
        }
        else
        {
            mMainContractView.showItemSelectView()
        }
    }

    /**
     * 버젼정보를 보고 버젼이 서버와 같거나, 또는 사용자가 업데이트를 하지않아도 판단될때 API 프로세스를 진행 시킨다.
     */
    private fun startAPIProcess()
    {
        Log.f("")
        mCurrentIntroProcess = IntroProcess.INIT_COMPLETE
        enableProgressAnimation(IntroProcess.INIT_COMPLETE)
    }

    private fun enableProgressAnimation(process : IntroProcess)
    {
        Log.f("process : $process")
        when(process)
        {
            IntroProcess.INIT_COMPLETE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[0], PERCENT_SEQUENCE[1])
            }
            IntroProcess.LOGIN_COMPLTE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[1], PERCENT_SEQUENCE[2])
            }
            IntroProcess.MAIN_COMPELTE ->
            {
                mMainContractView.setProgressPercent(PERCENT_SEQUENCE[2], PERCENT_SEQUENCE[3])
            }
        }
    }

    private fun settingLogFile()
    {
        val logfileSize = Log.getLogfileSize()
        Log.f("Log file Size : $logfileSize")
        if(logfileSize > Common.MAXIMUM_LOG_FILE_SIZE || logfileSize == 0L)
        {
            Log.initWithDeleteFile(mContext, Common.LOG_FILE, MonitorMode.RELEASE_MODE)
        }
    }

    /**
     * 바뀐 비밀번호 저장
     */
    private fun changeUserLoginData()
    {
        mUserLoginData = UserLoginData(mUserLoginData!!.userID, SimpleCrypto.encode(mNewPassword), mUserLoginData!!.userSchoolCode)
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, mUserLoginData)
    }

    private fun showTemplateAlertDialog(type : Int, buttonType : DialogButtonType, message : String)
    {
        Log.f("Update Pop up")
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(message)
            setDialogEventType(type)
            setButtonType(buttonType)
            setDialogListener(mDialogListener)
            setCancelPossible(false)
            show()
        }
    }

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
     * 파일 권한 허용 요청 다이얼로그
     * - 로그 파일 저장을 위해
     */
    private fun showChangeFilePermissionDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_warning_storage_permission))
            setDialogEventType(DIALOG_TYPE_WARNING_FILE_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun requestInitAsync()
    {
        Log.f("")

        var deviceID = CommonUtils.getInstance(mContext).secureDeviceID
        var pushAddress: String = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_FIREBASE_PUSH_TOKEN)
        var pushOn = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")

        mIntroViewModel.enqueueCommandStart(
            RequestCode.CODE_VERSION,
            Common.DURATION_SHORT_LONG,
            deviceID,
            pushAddress,
            pushOn)
    }

    private fun requestAutoLoginAsync()
    {
        Log.f("")

        mIntroViewModel.enqueueCommandStart(
            RequestCode.CODE_AUTH_ME,
            Common.DURATION_SHORT_LONG
        )
    }

    private fun requestMainInformationAsync()
    {
        Log.f("")

        mIntroViewModel.enqueueCommandStart(
            RequestCode.CODE_MAIN,
            Common.DURATION_SHORT_LONG
        )
    }

    /**
     * 비밀번호 변경하기 통신 요청
     */
    private fun requestPasswordChange()
    {
        mPasswordChangeDialog!!.showLoading()

        mIntroViewModel.enqueueCommandStart(
            RequestCode.CODE_PASSWORD_CHANGE,
            0L,
            mPassword,
            mNewPassword,
            mConfirmPassword)
    }

    /**
     * 비밀번호 변경하기 통신 요청 (다음에 변경)
     */
    private fun requestPasswordChangeNext()
    {
        mPasswordChangeDialog!!.showLoading()

        mIntroViewModel.enqueueCommandStart(RequestCode.CODE_PASSWORD_CHANGE_NEXT)
    }

    /**
     * 비밀번호 변경하기 통신 요청 (비밀번호 유지)
     */
    private fun requestPasswordChangeKeep()
    {
        mPasswordChangeDialog!!.showLoading()

        mIntroViewModel.enqueueCommandStart(RequestCode.CODE_PASSWORD_CHANGE_KEEP)
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
            .setResultLauncher(mResultLauncherList.get(INDEX_LOGIN))
            .startActivity()
    }

    private fun release()
    {
        Log.f("")
        mMainHandler.removeCallbacksAndMessages(null)
        (mContext as AppCompatActivity).finish()
    }

    override fun onClickIntroduce()
    {
        Log.f("")
        if(NetworkUtil.isConnectNetwork(mContext))
        {
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_FOXSCHOOL_INTRODUCE)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
        else
        {
            Toast.makeText(mContext, mContext.resources.getString(R.string.message_toast_network_error), Toast.LENGTH_LONG).show()
            (mContext as AppCompatActivity).finish()
        }

    }

    override fun onClickHomeButton()
    {
        Log.f("")
        release()
    }

    override fun onClickLogin()
    {
        Log.f("")
        if(NetworkUtil.isConnectNetwork(mContext))
        {
            startLoginActivity()
        }
        else
        {
            Toast.makeText(mContext, mContext.resources.getString(R.string.message_toast_network_error), Toast.LENGTH_LONG).show()
            (mContext as AppCompatActivity).finish()
        }

    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
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
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED && permissions[i] != Manifest.permission.RECORD_AUDIO)
                    {
                        isAllCheckSuccess = false
                    }
                    i++
                }

                if(isAllCheckSuccess == false)
                {
                    showChangeFilePermissionDialog()
                }
                else
                {
                    executeSequence()
                }
            }
        }
    }

    override fun onActivateEasterEgg()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_DEVELOPER_TO_EMAIL, Common.DURATION_EASTER_EGG)
    }

    override fun onDeactivateEasterEgg()
    {
        Log.f("")
        mMainHandler.removeMessages(MESSAGE_DEVELOPER_TO_EMAIL)
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
            MESSAGE_DEVELOPER_TO_EMAIL ->
            {
                Log.f("Send to email ------- Developer")
                CommonUtils.getInstance(mContext).inquireForDeveloper(Common.DEVELOPER_EMAIL)
            }
            MESSAGE_CHANGE_PASSWORD ->
            {
                mPasswordChangeDialog!!.dismiss()
                mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
            }
        }
    }


    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int)
        {
            if(messageType == DIALOG_TYPE_FORCE_UPDATE)
            {
                (mContext as AppCompatActivity).finish()
                CommonUtils.getInstance(mContext).startLinkMove(mVersionDataResult!!.getStoreUrl())
            }
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, messageType : Int)
        {
            Log.f("messageType : $messageType, buttonType : $buttonType")
            if(messageType == DIALOG_TYPE_SELECT_UPDATE_CONFIRM)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        startAPIProcess()
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        (mContext as AppCompatActivity).finish()
                        CommonUtils.getInstance(mContext).startLinkMove(mVersionDataResult!!.getStoreUrl())
                    }
                }

            }
            else if(messageType == DIALOG_TYPE_WARNING_FILE_PERMISSION)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // [취소] 컨텐츠 사용 불가 메세지 표시
                       // mMainContractView.showErrorMessage(mContext.getString(R.string.message_warning_storage_permission))
                        Toast.makeText(mContext, mContext.getString(R.string.message_warning_storage_permission), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).finish()
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // [권한 변경하기] 앱 정보 화면으로 이동
                        isRequestPermission = true
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", mContext.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mContext.startActivity(intent)
                    }
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