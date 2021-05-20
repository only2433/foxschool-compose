package com.littlefox.app.foxschool.main.presenter

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.LittlefoxLocale
import com.littlefox.app.foxschool.coroutine.AuthMeCoroutine
import com.littlefox.app.foxschool.coroutine.InitCoroutine
import com.littlefox.app.foxschool.coroutine.MainInformationCoroutine
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.app.foxschool.enumerate.IntroProcess
import com.littlefox.app.foxschool.main.contract.IntroContract
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

import java.util.*

class IntroPresenter : IntroContract.Presenter
{
    companion object
    {
        private const val PERMISSION_REQUEST : Int  = 0x01
        private const val REQUEST_CODE_LOGIN : Int      = 1001
        private const val REQUEST_CODE_GO_LOGIN : Int   = 1002
        private const val DIALOG_TYPE_SELECT_UPDATE_CONFIRM : Int   = 10001
        private const val DIALOG_TYPE_FORCE_UPDATE : Int            = 10002
        private const val MESSAGE_INIT : Int                    = 100
        private const val MESSAGE_CHECK_API_MAIN : Int          = 101
        private const val MESSAGE_REQEUST_COMPLETE_LOGIN : Int  = 102
        private const val MESSAGE_INCREASE_PERCENT : Int        = 103
        private const val MESSAGE_START_LOGIN : Int             = 104
        private const val MESSAGE_START_MAIN : Int              = 105
        private const val MESSAGE_APP_SERVER_ERROR : Int        = 106
        private const val MAX_PROGRESS_DURATION : Int       = 100
        private const val MAX_PERCENT : Int                 = 100
        private const val PROGRESS_TASK_PERIOD : Int        = 20
        private val PERCENT_SEQUENCE = intArrayOf(0, 30, 60, 100)
    }

    private lateinit var mContext : Context
    private lateinit var mPermissionList : ArrayList<String>
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mMainContractView : IntroContract.View
    private var mTimerCount : Int = 0
    private var mProgressTimer : Timer? = null
    private var mCurrentIntroProcess : IntroProcess = IntroProcess.NONE
    private var isAutoLogin = false
    private var isDisposableLogin = false
    private var mInitCoroutine : InitCoroutine? = null
    private var mAuthMeCoroutine : AuthMeCoroutine? = null
    private var mMainInformationCoroutine : MainInformationCoroutine? = null

    private inner class ProgressTimerTask : TimerTask()
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
    }



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
        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N") as String
        Log.f("autoLoginStatus : $autoLoginStatus")
        isAutoLogin = if(autoLoginStatus == "Y") true else false
        isDisposableLogin = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("isAutoLogin : $isAutoLogin, isDisposableLogin : $isDisposableLogin")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(CommonUtils.getInstance(mContext).getUnAuthorizePermissionList(mPermissionList)
                    .size() > 0
            )
            {
                Log.f("")
                CommonUtils.getInstance(mContext)
                    .requestPermission(mPermissionList, PERMISSION_REQUEST)
            } else
            {
                if(Feature.IS_CHINESE_MODEL)
                {
                    Log.f("IS_CHINESE_MODEL TRUE")
                    executeSequence()
                } else
                {
                    Log.f("IS_CHINESE_MODEL FALSE")
                    initPayment()
                }
            }
        } else
        {
            if(Feature.IS_CHINESE_MODEL)
            {
                Log.f("IS_CHINESE_MODEL TRUE")
                executeSequence()
            } else
            {
                Log.f("IS_CHINESE_MODEL FALSE")
                initPayment()
            }
        }
    }

    private fun initPayment()
    {
        Log.f("")
        mBillingClientHelper = BillingClientHelper.getInstance()
        setUpInAppPurchaseListener()
        mBillingClientHelper.init(mContext)
    }

    private fun setUpInAppPurchaseListener()
    {
        mBillingClientHelper.setOnBillingClientListener(object : IBillingClientListener()
        {
            fun onSkuDetailQueryFinished()
            {
            }

            fun onCheckPurchaseItem()
            {
                Log.f("")
                val item : Purchase =
                    mBillingClientHelper.getPurchasedItemResult(BillingClientHelper.IN_APP_1_MONTH)
                if(item != null)
                {
                    Log.f("Purchase data : " + item.getOriginalJson())
                    val skuDetails : SkuDetails =
                        mBillingClientHelper.getSkuDetailData(BillingClientHelper.IN_APP_1_MONTH)
                    var mPaymentBaseObject : PaymentBaseObject? = CommonUtils.getInstance(mContext)
                        .getPreferenceObject(
                            Common.PARAMS_IN_APP_ITEM_INFORMATION,
                            PaymentBaseObject::class.java
                        ) as PaymentBaseObject
                    try
                    {
                        Log.f("mPaymentBaseObject : $mPaymentBaseObject")
                    } catch(e : NullPointerException)
                    {
                        e.printStackTrace()
                    }
                    if(mPaymentBaseObject == null)
                    {
                        Log.f("Purchase data : " + item.getOriginalJson())
                        val mPaymentInAppBillingData = PaymentInAppBillingData(
                            CommonUtils.getInstance(mContext).getSecureID(),
                            skuDetails.getPriceCurrencyCode(),
                            skuDetails.getPrice(),
                            item.getOrderId(),
                            item.getPurchaseTime()
                        )
                        mPaymentBaseObject = PaymentBaseObject(mPaymentInAppBillingData)
                        CommonUtils.getInstance(mContext).setPreferenceObject(
                            Common.PARAMS_IN_APP_ITEM_INFORMATION,
                            mPaymentBaseObject
                        )
                        CommonUtils.getInstance(mContext).setSharedPreference(
                            Common.PARAMS_IN_APP_ITEM_RECEIPT,
                            item.getOriginalJson()
                        )
                    }
                    Log.f("current Time : " + System.currentTimeMillis())
                    Log.f("payment start Time : " + item.getPurchaseTime())
                    Log.f(
                        "payment end Time : " + CommonUtils.getInstance(mContext)
                            .getAdded31Days(item.getPurchaseTime())
                    )
                    if(System.currentTimeMillis() >= CommonUtils.getInstance(mContext)
                            .getAdded31Days(item.getPurchaseTime())
                    )
                    {
                        Log.f("====== consume Item ========")
                        CommonUtils.getInstance(mContext)
                            .setPreferenceObject(Common.PARAMS_IN_APP_ITEM_INFORMATION, null)
                        CommonUtils.getInstance(mContext)
                            .setSharedPreference(Common.PARAMS_IN_APP_ITEM_RECEIPT, "")
                        mBillingClientHelper.consumeItem(item)
                    }
                }
                executeSequence()
            }

            fun onPurchaseComplete(purchaseItem : Purchase?)
            {
            }

            fun onConsumeComplete(billingResult : BillingResult, purchaseToken : String)
            {
                Log.f("response Code : " + billingResult.getResponseCode() + ", purchaseToken : " + purchaseToken)
            }

            fun inFailure(status : Int, reason : String)
            {
                if(Feature.IS_APP_PAYMENT_FAIL_NO_CHECK)
                {
                    Log.f("IS_APP_PAYMENT_FAIL_NO_CHECK")
                    executeSequence()
                } else
                {
                    Log.f("status : $status, reason : $reason")
                    Toast.makeText(mContext, reason, Toast.LENGTH_LONG).show()
                    (mContext as AppCompatActivity?).finish()
                }
            }
        })
    }

    private fun checkUserStatus()
    {
        val `object` : UserLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(
            Common.PARAMS_USER_LOGIN,
            UserLoginData::class.java
        ) as UserLoginData
        if(`object` == null)
        {
            Feature.IS_FREE_USER = true
        } else
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
                CommonUtils.getInstance(mContext)
                    .setSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, false)
            }
            mMainContractView.showProgressView()
            requestInitAsync()
        } else
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
        mCurrentIntroProcess = if(Feature.IS_FREE_USER)
        {
            IntroProcess.LOGIN_COMPLTE
        } else
        {
            IntroProcess.INIT_COMPLETE
        }
        enableTimer(true)
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

    private fun showTempleteAlertDialog(type : Int, buttonType : Int, message : String)
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
        mInitCoroutine.setData(
            CommonUtils.getInstance(mContext).getSecureID(),
            CommonUtils.getInstance(mContext)
                .getSharedPreference(Common.PARAMS_FIREBASE_PUSH_TOKEN, Common.TYPE_PARAMS_STRING),
            CommonUtils.getInstance(mContext)
                .getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")
        )
        mInitCoroutine.setAsyncListener(mIntroAsyncListener)
        mInitCoroutine.execute()
    }

    private fun requestAutoLoginAsync()
    {
        Log.f("")
        mAuthMeCoroutine = AuthMeCoroutine(mContext)
        mAuthMeCoroutine.setAsyncListener(mIntroAsyncListener)
        mAuthMeCoroutine.execute()
    }

    private fun requestMainInformationAsync()
    {
        Log.f("")
        mMainInformationCoroutine = MainInformationCoroutine(mContext)
        mMainInformationCoroutine.setAsyncListener(mIntroAsyncListener)
        mMainInformationCoroutine.execute()
    }

    private fun enableTimer(isStart : Boolean)
    {
        if(isStart)
        {
            mProgressTimer = Timer()
            mProgressTimer!!.schedule(ProgressTimerTask(), 0, PROGRESS_TASK_PERIOD.toLong())
        } else
        {
            if(mProgressTimer != null)
            {
                mProgressTimer!!.cancel()
                mProgressTimer = null
            }
        }
    }

    private fun startMainActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.MAIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setIntentFlag(Intent.FLAG_ACTIVITY_CLEAR_TASK).startActivity()
        (mContext as AppCompatActivity?).finish()
    }

    private fun startMembershipActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PAYMENT)
            .setData(PaymentType.SIGN_AND_PAY).setRequestCode(REQUEST_CODE_GO_LOGIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startLoginActivity()
    {
        val isLoginFromMain = false
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.LOGIN)
            .setData(isLoginFromMain).setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_LOGIN).startActivity()
    }

    private fun release()
    {
        Log.f("")
        enableTimer(false)
        if(mAuthMeCoroutine != null)
        {
            mAuthMeCoroutine.cancel()
            mAuthMeCoroutine = null
        }
        if(mMainInformationCoroutine != null)
        {
            mMainInformationCoroutine.cancel()
            mMainInformationCoroutine = null
        }
        mMainHandler.removeCallbacksAndMessages(null)
        (mContext as AppCompatActivity?).finish()
    }

    fun onClickHomeButton()
    {
        Log.f("")
        release()
    }

    fun onClickFreeSamples()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_INTRO,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            CommonUtils.getInstance(mContext)
                .getCountryAddLabel(Common.ANALYTICS_LABEL_FREE_SAMPLES)
        )
        Feature.IS_FREE_USER = true
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, "")
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, null)
        CommonUtils.getInstance(mContext)
            .setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, null)
        mMainContractView.showProgressView()
        requestInitAsync()
    }

    fun onClickMembership()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_INTRO,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            CommonUtils.getInstance(mContext).getCountryAddLabel(Common.ANALYTICS_LABEL_MEMBERSHIP)
        )
        startMembershipActivity()
    }

    fun onClickLogin()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_INTRO,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            CommonUtils.getInstance(mContext).getCountryAddLabel(Common.ANALYTICS_LABEL_LOGIN)
        )
        startLoginActivity()
    }

    fun onRequestPermissionsResult(
        requestCode : Int,
        permissions : Array<String>,
        grantResults : IntArray
    )
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
                    (mContext as AppCompatActivity?).finish()
                } else
                {
                    Log.f("")
                    initPayment()
                }
            }
        }
    }

    fun resume()
    {
        Log.f("")
    }

    fun pause()
    {
        Log.f("")
    }

    fun destroy()
    {
        Log.f("")
        release()
    }

    fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
        when(requestCode)
        {
            REQUEST_CODE_LOGIN -> if(resultCode == Activity.RESULT_OK)
            {
                mMainContractView.showProgressView()
                /**
                 * Login Activity의 Activity 종료가 늦게되서 프로그래스랑 겹쳐 틱 되는 현상 때문에 조금 늦췃다.
                 */
                mMainHandler.sendEmptyMessageDelayed(
                    MESSAGE_REQEUST_COMPLETE_LOGIN,
                    Common.DURATION_NORMAL
                )
            }
            REQUEST_CODE_GO_LOGIN -> if(resultCode == Activity.RESULT_OK)
            {
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
            }
        }
    }

    fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_INIT -> init()
            MESSAGE_INCREASE_PERCENT -> mMainContractView.setProgressPercent(msg.arg1)
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
                Toast.makeText(
                    mContext,
                    mContext!!.resources.getString(R.string.message_warning_app_server_error),
                    Toast.LENGTH_LONG
                ).show()
                (mContext as AppCompatActivity?).finish()
                IntentManagementFactory.getInstance().initScene()
            }
        }
    }

    private val mIntroAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String)
        {
        }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult ?: return
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.ASYNC_CODE_INIT)
                {
                    val versionDataResult : VersionDataResult =
                        (result as VersionBaseObject).getData()
                    CommonUtils.getInstance(mContext)
                        .setPreferenceObject(Common.PARAMS_VERSION_INFORMATION, versionDataResult)
                    if(versionDataResult.isNeedUpdate())
                    {
                        if(versionDataResult.isForceUpdate())
                        {
                            showTempleteAlertDialog(
                                DIALOG_TYPE_FORCE_UPDATE,
                                TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1,
                                mContext!!.resources.getString(R.string.message_force_update)
                            )
                        } else
                        {
                            showTempleteAlertDialog(
                                DIALOG_TYPE_SELECT_UPDATE_CONFIRM,
                                TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2,
                                mContext!!.resources.getString(R.string.message_need_update)
                            )
                        }
                    } else
                    {
                        startAPIProcess()
                    }
                } else if(code == Common.ASYNC_CODE_ME)
                {
                    val userInformationResult : UserInformationResult =
                        (result as UserInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(
                        Common.PARAMS_USER_API_INFORMATION,
                        userInformationResult
                    )
                    mCurrentIntroProcess = IntroProcess.LOGIN_COMPLTE
                    enableTimer(true)
                } else if(code == Common.ASYNC_CODE_MAIN)
                {
                    Log.f("Main data get to API Success")
                    val mainInformationResult : MainInformationResult =
                        (`object` as MainInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
                    mCurrentIntroProcess = IntroProcess.MAIN_COMPELTE
                    enableTimer(true)
                }
            } else
            {
                Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                if(result.isAuthenticationBroken() || result.status === BaseResult.FAIL_CODE_INTERNAL_SERVER_ERROR)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity?).finish()
                    IntentManagementFactory.getInstance().initScene()
                } else
                {
                    (mContext as AppCompatActivity?).finish()
                }
            }
        }

        override fun onRunningCanceled(code : String)
        {
        }

        override fun onRunningProgress(code : String, progress : Int)
        {
        }

        override fun onRunningAdvanceInformation(code : String, `object` : Any)
        {
        }

        override fun onErrorListener(code : String, message : String)
        {
            mMainHandler.sendEmptyMessage(MESSAGE_APP_SERVER_ERROR)
        }
    }
    var mDialogListener : DialogListener = object : DialogListener()
    {
        fun onConfirmButtonClick(messageType : Int)
        {
            if(messageType == DIALOG_TYPE_FORCE_UPDATE)
            {
                (mContext as AppCompatActivity?).finish()
                if(Feature.IS_CHINESE_MODEL)
                {
                    CommonUtils.getInstance(mContext).startLinkMove(Common.CHINESE_MODEL_APP_LINK)
                } else
                {
                    CommonUtils.getInstance(mContext).startLinkMove(Common.APP_LINK)
                }
            }
        }

        fun onChoiceButtonClick(buttonType : Int, messageType : Int)
        {
            Log.f("messageType : $messageType, buttonType : $buttonType")
            if(messageType == DIALOG_TYPE_SELECT_UPDATE_CONFIRM)
            {
                if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1)
                {
                    startAPIProcess()
                } else if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                {
                    (mContext as AppCompatActivity?).finish()
                    if(Feature.IS_CHINESE_MODEL)
                    {
                        CommonUtils.getInstance(mContext)
                            .startLinkMove(Common.CHINESE_MODEL_APP_LINK)
                    } else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(Common.APP_LINK)
                    }
                }
            }
        }
    }


}