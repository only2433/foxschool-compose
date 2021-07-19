package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.view.Gravity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.BioCheckResultType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.main.contract.MyInformationContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class MyInformationPresenter : MyInformationContract.Presenter
{
    companion object
    {
        private const val DIALOG_BIO_LOGIN_ON : Int     = 10001     // 지문인증 로그인 활성화 알림 다이얼로그 플래그
        private const val DIALOG_BIO_LOGIN_OFF : Int    = 10002     // 지문인증 로그인 비활성화 알림 다이얼로그 플래그
    }

    private lateinit var mContext : Context
    private lateinit var mMyInformationContractView : MyInformationContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private lateinit var mTempleteAlertDialog : TempleteAlertDialog // 지문인증 로그인 활성/비활성 알림 다이얼로그
    private var mLoginInformation : LoginInformationResult? = null  // 로그인 시 응답받은 회원정보
    private var mCheckAutoLogin : Boolean   = false                 // 자동로그인 ON/OFF
    private var mCheckBioLogin : Boolean    = false                 // 생체인증 로그인 ON/OFF
    private var mCheckPush : Boolean        = false                 // 푸쉬수신 ON/OFF

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mMyInformationContractView = mContext as MyInformationContract.View
        mMyInformationContractView.initView()
        mMyInformationContractView.initFont()

        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")

        // SharedPreference에 저장된 값 가져와서 데이터 세팅
        mLoginInformation = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?
        if (mLoginInformation != null)
        {
            mMyInformationContractView.setUserInformation(mLoginInformation!!)
        }

        // 자동로그인 설정값 가져오기
        val autoLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
        Log.f("autoLoginStatus : $autoLoginStatus")
        mCheckAutoLogin = (autoLoginStatus == "Y")

        // 생체인증 로그인 설정값 가져오기
        val bioLoginStatus = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_BIO_LOGIN_DATA, "N")
        Log.f("bioLoginStatus : $bioLoginStatus")
        mCheckBioLogin = (bioLoginStatus == "Y")

        // 푸쉬 설정값 가져오기
        val isPushEnable = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_PUSH_SEND, "Y")
        Log.f("isPushEnable : $isPushEnable")
        mCheckPush = (isPushEnable == "Y")

        // 플래그값에 따라 스위치 상태 세팅
        mMyInformationContractView.setSwitchAutoLogin(mCheckAutoLogin)
        mMyInformationContractView.setSwitchBioLogin(mCheckBioLogin)
        mMyInformationContractView.setSwitchPush(mCheckPush)
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
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
        }
    }

    /**
     * 알림 다이얼로그 표시
     * - 생체인증 로그인 활성/비활성 알림 다이얼로그
     */
    private fun showTempleteAlertDialog(type : Int, message : String)
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog.setMessage(message)
        mTempleteAlertDialog.setDialogEventType(type)
        mTempleteAlertDialog.setButtonType(DialogButtonType.BUTTON_2)
        mTempleteAlertDialog.setDialogListener(mDialogListener)
        mTempleteAlertDialog.show()
    }

    /**
     * 자동로그인 스위치 클릭 이벤트
     */
    override fun onClickAutoLoginSwitch()
    {
        Log.f("")
        mCheckAutoLogin = !mCheckAutoLogin
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, if(mCheckAutoLogin) "Y" else "N")
        mMyInformationContractView.setSwitchAutoLogin(mCheckAutoLogin)
    }

    /**
     * 지문인증 로그인 스위치 클릭 이벤트
     */
    override fun onClickBioLoginSwitch()
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

        // 지문인증 로그인 활성/비활성 알림 다이얼로그 표시
        // 사용자가 다이얼로그에서 선택하는 값에 따라 스위치 ON/OFF 설정
        if (mCheckBioLogin)
        {
            // 지문인증 ON -> OFF
            showTempleteAlertDialog(DIALOG_BIO_LOGIN_OFF, mContext.getString(R.string.message_bio_login_off))
        }
        else
        {
            // 지문인증 OFF -> ON
            showTempleteAlertDialog(DIALOG_BIO_LOGIN_ON, mContext.getString(R.string.message_bio_login_on))
        }
    }

    /**
     * 푸시 알림 스위치 클릭 이벤트
     */
    override fun onClickPushSwitch()
    {
        Log.f("")
        mCheckPush = !mCheckPush
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_PUSH_SEND, if(mCheckPush) "Y" else "N")
        mMyInformationContractView.setSwitchPush(mCheckPush)
    }

    /**
     * 정보 수정 버튼 클릭 이벤트
     */
    override fun onClickInfoChange()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MY_INFORMATION_CHANGE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 비밀번호 변경 버튼 클릭 이벤트
     */
    override fun onClickPasswordChange()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PASSWORD_CHANGE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            if (eventType == DIALOG_BIO_LOGIN_ON)
            {
                // 지문인증 활성화 알림 다이얼로그
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 지문인증 활성화 취소 (OFF)
                        Log.f("Set bio login : OFF->OFF")
                        mCheckBioLogin = false
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 지문인증 활성화 확인 (ON)
                        Log.f("Set bio login : OFF->ON")
                        mCheckBioLogin = true
                        mMyInformationContractView.showSuccessMessage(mContext.resources.getString(R.string.message_bio_login_enable))
                    }
                }
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_BIO_LOGIN_DATA, if(mCheckBioLogin) "Y" else "N")
                mMyInformationContractView.setSwitchBioLogin(mCheckBioLogin)
            }
            else if (eventType == DIALOG_BIO_LOGIN_OFF)
            {
                // 지문인증 비활성화 알림 다이얼로그
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 지문인증 비활성화 취소 (ON)
                        Log.f("Set bio login : ON->ON")
                        mCheckBioLogin = true
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 지문인증 비활성화 확인 (OFF)
                        Log.f("Set bio login : ON->OFF")
                        mCheckBioLogin = false
                        mMyInformationContractView.showSuccessMessage(mContext.resources.getString(R.string.message_bio_login_disable))
                    }
                }
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_BIO_LOGIN_DATA, if(mCheckBioLogin) "Y" else "N")
                mMyInformationContractView.setSwitchBioLogin(mCheckBioLogin)
            }
        }
    }
}