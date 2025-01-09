package com.littlefox.app.foxschool.presentation.viewmodel
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.enumerate.IntroViewMode
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.IntroApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.LittlefoxLocale
import com.littlefox.app.foxschool.common.NetworkUtil
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.intro.IntroEvent
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import com.littlefox.logmonitor.enumItem.MonitorMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@HiltViewModel
class IntroViewModel @Inject constructor(private val apiViewModel : IntroApiViewModel) : BaseViewModel()
{
    companion object
    {
        private const val PERMISSION_REQUEST : Int          = 0x01

        const val DIALOG_TYPE_SELECT_UPDATE_CONFIRM : Int   = 10001
        const val DIALOG_TYPE_FORCE_UPDATE : Int            = 10002
        const val DIALOG_TYPE_WARNING_FILE_PERMISSION       = 10003

        private const val INDEX_LOGIN                               = 0
        private val PERCENT_SEQUENCE : FloatArray                   = floatArrayOf(0f, 30f, 60f, 100f)
    }

    private val _bottomType = SingleLiveEvent<IntroViewMode>()
    val bottomType: LiveData<IntroViewMode> get() = _bottomType

    private val _progressPercent = SingleLiveEvent<Float>()
    val progressPercent: LiveData<Float> get() = _progressPercent

    private val _dialogSelectUpdate = SingleLiveEvent<Void>()
    val dialogSelectUpdate: LiveData<Void> get() = _dialogSelectUpdate

    private val _dialogForceUpdate = SingleLiveEvent<Void>()
    val dialogForceUpdate: LiveData<Void> get() = _dialogForceUpdate

    private val _dialogFilePermission = SingleLiveEvent<Void>()
    val dialogFilePermission: LiveData<Void> get() = _dialogFilePermission

    private val _showDialogPasswordChange = SingleLiveEvent<PasswordGuideType>()
    val showDialogPasswordChange: LiveData<PasswordGuideType> get() = _showDialogPasswordChange

    private val _hideDialogPasswordChange = SingleLiveEvent<Void>()
    val hideDialogPasswordChange: LiveData<Void> get() = _hideDialogPasswordChange


    private lateinit var mContext : Context
    private lateinit var mPermissionList : ArrayList<String>
    private var mCurrentIntroProcess : IntroProcess = IntroProcess.NONE

    private var isAutoLogin : Boolean           = false
    private var isDisposableLogin : Boolean     = false

    private var mUserLoginData : UserLoginData? = null // 로그인 데이터
    private var mUserInformationResult : LoginInformationResult? = null // 사용자 정보 (로그인 통신 응답)
    private var mVersionDataResult : VersionDataResult? = null // 버전정보

    private var mPassword : String  = ""
    private var mNewPassword : String = ""
    private var mConfirmPassword : String = ""

    private var isRequestPermission : Boolean = false
    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>

    private var mEasterEggJob: Job? = null


    override fun init(context : Context)
    {
        mContext = context
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

        onHandleApiObserver()

        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }

            prepare()
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.DialogClick ->
            {
                onDialogClick(
                    event.eventType
                )
            }
            is BaseEvent.DialogChoiceClick ->
            {
                onDialogChoiceClick(
                    event.buttonType,
                    event.eventType
                )
            }
            is IntroEvent.onActivateEasterEgg ->
            {
                onActivateEasterEgg()
            }
            is IntroEvent.onDeactivateEasterEgg ->
            {
                onDeactiveEasterEgg()
            }
            is IntroEvent.onClickIntroduce ->
            {
                onClickIntroduce()
            }
            is IntroEvent.onClickLogin ->
            {
                onClickLogin()
            }
            is IntroEvent.onClickHomeButton ->
            {
                (mContext as AppCompatActivity).finish()
            }
            is IntroEvent.onClickChangeButton ->
            {
                onClickChangeButton(
                    event.oldPassword,
                    event.confirmPassword,
                    event.newPassword
                )
            }
            else ->
            {
                onHandleActivityResult(event)
            }

        }
    }


    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch  {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                // Your code here
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_PASSWORD_CHANGE ||
                            data.first == RequestCode.CODE_PASSWORD_CHANGE_NEXT ||
                            data.first == RequestCode.CODE_PASSWORD_CHANGE_KEEP)
                        {
                            if(data.second)
                            {
                                _isLoading.value = true
                            }
                            else
                            {
                                _isLoading.value = false
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.versionData.collect{ data ->
                    data?.let {
                        mVersionDataResult = data
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_VERSION_INFORMATION, mVersionDataResult)
                        if(mVersionDataResult!!.isNeedUpdate)
                        {
                            if(mVersionDataResult!!.isForceUpdate())
                            {
                                _dialogSelectUpdate.call()
                            }
                            else
                            {
                                _dialogForceUpdate.call()
                            }
                        }
                        else
                        {
                            startAPIProcess()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.authMeData.collect{ data ->
                    data?.let {
                        mUserInformationResult = data
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mUserInformationResult)
                        if (mUserInformationResult!!.isNeedChangePassword())
                        {
                            // 비밀번호 변경 날짜가 90일을 넘어가는 경우 비밀번호 변경 안내 다이얼로그를 표시한다.
                            mUserLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?
                            _showDialogPasswordChange.value = mUserInformationResult!!.getPasswordChangeType()
                        }
                        else
                        {
                            // 자동로그인 완료
                            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.mainData.collect{ data ->
                    data?.let {
                        Log.f("Main data get to API Success")
                        CommonUtils.getInstance(mContext).saveMainData(data)
                        mCurrentIntroProcess = IntroProcess.MAIN_COMPELTE
                        enableProgressAnimation(IntroProcess.MAIN_COMPELTE)
                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_SHORT_LONG)
                            }
                            startMainActivity()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.changePasswordData.collect { data ->
                    data?.let {
                        // 비밀번호 변경 성공
                        Log.f("Password Change Complete")
                        changeUserLoginData()

                        _toast.value = mContext.getString(R.string.message_password_change_complete)
                        viewModelScope.launch {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)
                            }
                            _hideDialogPasswordChange.call()
                            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.changePasswordNextData.collect { data ->
                    data?.let {
                        // 다음에 변경
                        _hideDialogPasswordChange.call()
                        mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                        enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.changePasswordKeepData.collect { data ->
                    data?.let {
                        // 현재 비밀번호 유지
                        _toast.value = mContext.getString(R.string.message_password_change_complete)

                        viewModelScope.launch {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_LONG)

                            }
                            _hideDialogPasswordChange.call()
                            mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                            enableProgressAnimation(IntroProcess.LOGIN_COMPLTE)
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        Log.f("status : ${result.status}, message : ${result.message} , code : $code")

                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                        if(result.isAuthenticationBroken || result.status == BaseResult.FAIL_CODE_INTERNAL_SERVER_ERROR)
                        {
                            Log.f("== isAuthenticationBroken ==")
                            _toast.value = result.message
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initScene()
                            }
                        }
                        else
                        {
                            if (code == RequestCode.CODE_PASSWORD_CHANGE ||
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
    }

    private fun onHandleActivityResult(event: BaseEvent)
    {
        when(event)
        {
            is IntroEvent.onAddResultLaunchers ->
            {
                onAddResultLaunchers(
                    event.launchers
                )
            }
            is IntroEvent.onRequestPermissionResult ->
            {
                onRequestPermissionsResult(
                    event.requestCode,
                    event.permissions,
                    event.gransResults
                )
            }
            is IntroEvent.onActivityResult ->
            {
                onActivityResult(
                    event.code,
                    event.intent
                )
            }
        }
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

            _bottomType.value = IntroViewMode.PROGRESS

            requestInitAsync()
            requestAutoLoginAsync()
            requestMainInformationAsync()
        }
        else
        {
            _bottomType.value = IntroViewMode.SELECT
        }
    }

    private fun prepare()
    {
        Log.init(mContext, Common.LOG_FILE , MonitorMode.RELEASE_MODE)

        LittlefoxLocale.setLocale(Locale.getDefault().toString())
        mPermissionList = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            ArrayList<String>().apply {
                add(Manifest.permission.RECORD_AUDIO)
            }
        } else
        {
            ArrayList<String>().apply {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.RECORD_AUDIO)
            }
        }

        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")

        isAutoLogin = if(autoLoginStatus == "Y") true else false
        isDisposableLogin = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("isAutoLogin : $isAutoLogin, isDisposableLogin : $isDisposableLogin")

        checkPermission()
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
                _progressPercent.value = PERCENT_SEQUENCE[1]
            }
            IntroProcess.LOGIN_COMPLTE ->
            {
                _progressPercent.value = PERCENT_SEQUENCE[2]
            }
            IntroProcess.MAIN_COMPELTE ->
            {
                _progressPercent.value = PERCENT_SEQUENCE[3]
            }
            else ->{}
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
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun requestInitAsync()
    {
        Log.f("")
        val deviceID = CommonUtils.getInstance(mContext).secureDeviceID
        val pushAddress: String = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_FIREBASE_PUSH_TOKEN)
        val pushOn = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VERSION,
            Common.DURATION_SHORT_LONG,
            deviceID,
            pushAddress,
            pushOn)
    }

    private fun requestAutoLoginAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_AUTH_ME,
            Common.DURATION_SHORT_LONG
        )
    }

    private fun requestMainInformationAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_MAIN,
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

    private fun onAddResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(0))
    }

    private fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
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
                    _dialogFilePermission.call()
                }
                else
                {
                    executeSequence()
                }
            }
        }
    }

    private fun onActivityResult(code : ResultLauncherCode, intent : Intent?)
    {
        _bottomType.value = IntroViewMode.PROGRESS
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }
            requestInitAsync()
            requestAutoLoginAsync()
            requestMainInformationAsync()
        }
    }

    override fun onDialogClick(eventType : Int)
    {
        if(eventType == DIALOG_TYPE_FORCE_UPDATE)
        {
            (mContext as AppCompatActivity).finish()
            CommonUtils.getInstance(mContext).startLinkMove(mVersionDataResult!!.getStoreUrl())
        }
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        Log.f("messageType : $eventType, buttonType : $buttonType")
        if(eventType == DIALOG_TYPE_SELECT_UPDATE_CONFIRM)
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
        else if(eventType == DIALOG_TYPE_WARNING_FILE_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    // [취소] 컨텐츠 사용 불가 메세지 표시
                    _toast.value = mContext.getString(R.string.message_warning_storage_permission)
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

    private fun onActivateEasterEgg()
    {
        Log.f("")
        mEasterEggJob = viewModelScope.launch {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_EASTER_EGG)
            }
            CommonUtils.getInstance(mContext).inquireForDeveloper(Common.DEVELOPER_EMAIL)
        }
    }

    private fun onDeactiveEasterEgg()
    {
        Log.f("")
        mEasterEggJob?.cancel()
    }

    private fun onClickIntroduce()
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
            _toast.value = mContext.resources.getString(R.string.message_toast_network_error)
            (mContext as AppCompatActivity).finish()
        }
    }

    private fun onClickLogin()
    {
        Log.f("")
        if(NetworkUtil.isConnectNetwork(mContext))
        {
            startLoginActivity()
        }
        else
        {
            _toast.value = mContext.resources.getString(R.string.message_toast_network_error)
            (mContext as AppCompatActivity).finish()
        }
    }

    private fun onClickHomeButton()
    {
        (mContext as AppCompatActivity).finish()
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