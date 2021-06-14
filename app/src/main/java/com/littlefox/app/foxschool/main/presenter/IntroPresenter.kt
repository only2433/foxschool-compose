package com.littlefox.app.foxschool.main.presenter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.MainInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.UserInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.VersionBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.UserInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.LittlefoxLocale
import com.littlefox.app.foxschool.coroutine.AuthMeCoroutine
import com.littlefox.app.foxschool.coroutine.InitCoroutine
import com.littlefox.app.foxschool.coroutine.MainInformationCoroutine
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.IntroContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

import java.util.*

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class IntroPresenter : IntroContract.Presenter
{
    companion object
    {
        private const val PERMISSION_REQUEST : Int                  = 0x01
        private const val REQUEST_CODE_LOGIN : Int                  = 1001
        private const val REQUEST_CODE_GO_LOGIN : Int               = 1002
        private const val DIALOG_TYPE_SELECT_UPDATE_CONFIRM : Int   = 10001
        private const val DIALOG_TYPE_FORCE_UPDATE : Int            = 10002
        private const val MESSAGE_INIT : Int                    = 100
        private const val MESSAGE_REQUEST_AUTO_LOGIN : Int      = 101
        private const val MESSAGE_CHECK_API_MAIN : Int          = 102
        private const val MESSAGE_REQEUST_COMPLETE_LOGIN : Int  = 103
        private const val MESSAGE_START_LOGIN : Int             = 104
        private const val MESSAGE_START_MAIN : Int              = 105
        private const val MESSAGE_APP_SERVER_ERROR : Int        = 106
        private const val MAX_PROGRESS_DURATION : Int       = 100
        private const val PROGRESS_TASK_PERIOD : Int        = 20
        private val PERCENT_SEQUENCE = floatArrayOf(0f, 30f, 60f, 100f)
    }

    private lateinit var mContext : Context
    private lateinit var mPermissionList : ArrayList<String>
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mMainContractView : IntroContract.View
    private var mTimerCount : Int = 0
    private var mProgressTimer : Timer? = null
    private var mCurrentIntroProcess : IntroProcess = IntroProcess.NONE
    private var isAutoLogin = false
    private var isDisposableLogin = false
    private var mInitCoroutine : InitCoroutine? = null
    private var mAuthMeCoroutine : AuthMeCoroutine? = null
    private var mMainInformationCoroutine : MainInformationCoroutine? = null

    /*private inner class ProgressTimerTask : TimerTask()
    {
        override fun run()
        {
            mTimerCount++
            Log.f("mCurrentIntroProcess : $mCurrentIntroProcess, mTimerCount : $mTimerCount")
            if(mCurrentIntroProcess === IntroProcess.INIT_COMPLETE)
            {
                if(mTimerCount == PERCENT_SEQUENCE[1])
                {
                    enableTimer(false)
                    requestAutoLoginAsync()
                }
            } else if(mCurrentIntroProcess === IntroProcess.LOGIN_COMPLTE)
            {
                if(mTimerCount == PERCENT_SEQUENCE[2])
                {
                    enableTimer(false)
                    requestMainInformationAsync()
                }
            } else if(mCurrentIntroProcess === IntroProcess.MAIN_COMPELTE)
            {
                if(mTimerCount == PERCENT_SEQUENCE[3])
                {
                    enableTimer(false)
                    mMainHandler.sendEmptyMessage(MESSAGE_START_MAIN)
                }
            }
            val message = Message.obtain()
            message.what = MESSAGE_INCREASE_PERCENT
            message.arg1 = (mTimerCount * 100 / MAX_PROGRESS_DURATION)
            mMainHandler.sendMessage(message)
        }
    }*/



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
            enableProgressAniamtion(IntroProcess.LOGIN_COMPLTE)
        }
        else
        {
            mCurrentIntroProcess = IntroProcess.INIT_COMPLETE
            enableProgressAniamtion(IntroProcess.INIT_COMPLETE)
        }
    }

    private fun enableProgressAniamtion(process : IntroProcess)
    {
        Log.f("process : $process")
        when(process)
        {
            IntroProcess.INIT_COMPLETE ->
            {
                mMainContractView.setProgressPercent(
                    PERCENT_SEQUENCE[0], PERCENT_SEQUENCE[1]
                )
                mMainHandler.sendEmptyMessageDelayed(
                    IntroPresenter.MESSAGE_REQUEST_AUTO_LOGIN,
                    Common.DURATION_SHORT_LONG
                )
            }
            IntroProcess.LOGIN_COMPLTE ->
            {
                if(Feature.IS_FREE_USER)
                {
                    mMainContractView.setProgressPercent(
                        PERCENT_SEQUENCE[0], PERCENT_SEQUENCE[2]
                    )
                } else
                {
                    mMainContractView.setProgressPercent(
                        PERCENT_SEQUENCE[1], PERCENT_SEQUENCE[2]
                    )
                }
                mMainHandler.sendEmptyMessageDelayed(
                    MESSAGE_CHECK_API_MAIN,
                    Common.DURATION_SHORT_LONG
                )
            }
            IntroProcess.MAIN_COMPELTE ->
            {
                mMainContractView.setProgressPercent(
                    PERCENT_SEQUENCE[2], PERCENT_SEQUENCE[3]
                )
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

    private fun showTempleteAlertDialog(type : Int, buttonType : DialogButtonType, message : String)
    {
        Log.f("Update Pop up")
        val dialog = TempleteAlertDialog(mContext)
        dialog.setMessage(message)
        dialog.setDialogEventType(type)
        dialog.setButtonType(buttonType)
        dialog.setDialogListener(mDialogListener)
        dialog.show()
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
        mMainHandler.removeCallbacksAndMessages(null)
        (mContext as AppCompatActivity).finish()
    }

    override fun onClickIntroduce()
    {
        TODO("Not yet implemented")
    }

    override fun onClickHomeButton()
    {
        Log.f("")
        release()
    }



    override fun onClickLogin()
    {
        Log.f("")
        startLoginActivity()
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
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQEUST_COMPLETE_LOGIN, Common.DURATION_NORMAL)
            }

            REQUEST_CODE_GO_LOGIN ->
            if(resultCode == Activity.RESULT_OK)
            {
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_INIT -> init()
            MESSAGE_REQUEST_AUTO_LOGIN -> requestAutoLoginAsync();
            MESSAGE_CHECK_API_MAIN -> requestMainInformationAsync()
            MESSAGE_REQEUST_COMPLETE_LOGIN -> requestInitAsync()
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
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_INIT)
                {
                    val versionDataResult : VersionDataResult = (result as VersionBaseObject).getData()
                    CommonUtils.getInstance(mContext)
                        .setPreferenceObject(Common.PARAMS_VERSION_INFORMATION, versionDataResult)
                    if(versionDataResult.isNeedUpdate)
                    {
                        if(versionDataResult.isForceUpdate())
                        {
                            showTempleteAlertDialog(
                                DIALOG_TYPE_FORCE_UPDATE,
                                DialogButtonType.BUTTON_1,
                                mContext.resources.getString(R.string.message_force_update)
                            )
                        } else
                        {
                            showTempleteAlertDialog(
                                DIALOG_TYPE_SELECT_UPDATE_CONFIRM,
                                DialogButtonType.BUTTON_2,
                                mContext.resources.getString(R.string.message_need_update)
                            )
                        }
                    } else
                    {
                        startAPIProcess()
                    }
                } else if(code == Common.COROUTINE_CODE_ME)
                {
                    val userInformationResult : UserInformationResult = (result as UserInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, userInformationResult)
                    mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                    enableProgressAniamtion(IntroProcess.LOGIN_COMPLTE)
                }
                else if(code == Common.COROUTINE_CODE_MAIN)
                {
                    Log.f("Main data get to API Success")
                    val mainInformationResult : MainInformationResult = (`object` as MainInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
                    mCurrentIntroProcess = IntroProcess.MAIN_COMPELTE
                    enableProgressAniamtion(IntroProcess.MAIN_COMPELTE)
                }
            } else
            {
                Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                if(result.isAuthenticationBroken || result.getStatus() === BaseResult.FAIL_CODE_INTERNAL_SERVER_ERROR)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    (mContext as AppCompatActivity).finish()
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
    var mDialogListener : DialogListener = object : DialogListener
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
}