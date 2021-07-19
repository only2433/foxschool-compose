package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.CheckUserInput
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.MyInfoInputType
import com.littlefox.app.foxschool.main.contract.MyInfoChangeContract
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class MyInfoChangePresenter : MyInfoChangeContract.Presenter
{
    companion object
    {
        private const val DIALOG_PASSWORD_CONFIRM : Int     = 10001     // 비밀번호 확인 다이얼로그
        private const val DIALOG_PASSWORD_CONFIRM_ERR : Int = 10002     // 비밀번호 확인 오류 다이얼로그
    }

    private lateinit var mContext : Context
    private lateinit var mMyInfoChangeContractView : MyInfoChangeContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private lateinit var mTempleteAlertDialog : TempleteAlertDialog // 비밀번호 확인 다이얼로그
    private var mLoginInformation : LoginInformationResult? = null
    private var mLoginData : UserLoginData? = null
    private var mName : String  = ""
    private var mEmail : String = ""
    private var mPhone : String = ""

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mMyInfoChangeContractView = mContext as MyInfoChangeContract.View
        mMyInfoChangeContractView.initView()
        mMyInfoChangeContractView.initFont()

        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")
        mLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?
        mLoginInformation = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?
        if (mLoginInformation != null)
        {
            mMyInfoChangeContractView.setUserInformation(mLoginInformation!!)
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
     * 이름 입력값 유효성 체크
     */
    override fun checkNameAvailable(name : String)
    {
        val nameResult = CheckUserInput.getInstance(mContext).checkNameData(name).getResultValue()
        if(nameResult == CheckUserInput.WARNING_NAME_WRONG_INPUT)
        {
            mMyInfoChangeContractView.showInputError(
                MyInfoInputType.NAME,
                mContext.resources.getString(R.string.message_warning_input_name)
            )
        }
    }

    /**
     * 이메일 입력값 유효성 체크
     */
    override fun checkEmailAvailable(email : String)
    {
        val emailResult = CheckUserInput.getInstance(mContext).checkEmailData(email).getResultValue()
        if(emailResult == CheckUserInput.WARNING_EMAIL_WRONG_INPUT)
        {
            mMyInfoChangeContractView.showInputError(
                MyInfoInputType.EMAIL,
                mContext.resources.getString(R.string.message_warning_input_email)
            )
        }
    }

    /**
     * 전화번호 입력값 유효성 체크
     */
    override fun checkPhoneAvailable(phone : String, showMessage : Boolean) : Boolean
    {
        val phoneResult = CheckUserInput.getInstance(mContext).checkPhoneData(phone).getResultValue()
        if(phoneResult == CheckUserInput.WARNING_PHONE_WRONG_INPUT)
        {
            if (showMessage)
            {
                mMyInfoChangeContractView.showInputError(
                    MyInfoInputType.PHONE,
                    mContext.resources.getString(R.string.message_warning_input_phone)
                )
            }
            return false
        }
        return true
    }

    /**
     * 입력값 유효한지 체크
     * 이름과 이메일만 체크, 전화번호는 선택사항이기 때문에 체크하지 않는다.
     */
    override fun checkInputData(name : String, email : String, phone : String) : Boolean
    {
        if (phone.isNotEmpty() && (checkPhoneAvailable(phone, showMessage = false) == false))
        {
            return false
        }
        return (name.isNotEmpty() && email.isNotEmpty())
    }

    /**
     * 기존 비밀번호와 일치한지 체크
     */
    private fun checkPassword(password : String)
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
                showPasswordCheckErrDialog()
            }
        }
    }

    /**
     * 비밀번호 확인 다이얼로그 표시
     */
    private fun showPasswordCheckDialog()
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog.setMessage(mContext.resources.getString(R.string.message_password_check_for_change_user_info))
        mTempleteAlertDialog.setPasswordConfirmView(true)
        mTempleteAlertDialog.setDialogEventType(DIALOG_PASSWORD_CONFIRM)
        mTempleteAlertDialog.setButtonType(DialogButtonType.BUTTON_2)
        mTempleteAlertDialog.setDialogListener(mDialogListener)
        mTempleteAlertDialog.show()
    }

    /**
     * 비밀번호 확인 오류 다이얼로그 표시
     */
    private fun showPasswordCheckErrDialog()
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog.setMessage(mContext.resources.getString(R.string.message_warning_password_confirm_retry))
        mTempleteAlertDialog.setDialogEventType(DIALOG_PASSWORD_CONFIRM_ERR)
        mTempleteAlertDialog.setButtonType(DialogButtonType.BUTTON_1)
        mTempleteAlertDialog.setDialogListener(mDialogListener)
        mTempleteAlertDialog.show()
    }

    /**
     * 저장버튼 클릭 이벤트
     */
    override fun onClickSave(name : String, email : String, phone : String)
    {
        mName = name
        mEmail = email
        mPhone = phone

        showPasswordCheckDialog()
    }

    /**
     * 나의정보수정 통신 요청
     */
    private fun requestMyInformationChange(password : String)
    {
        // TODO 사용자 정보 수정 통신 연결
    }

    /**
     * 비밀번호 변경 다이얼로그 리스너
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            if (eventType == DIALOG_PASSWORD_CONFIRM_ERR)
            {
                showPasswordCheckDialog()
            }
        }

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
                        // 입력된 비밀번호 체크 TODO 추후 API 방식에 따라 바로 통신으로 날릴수도 있음
                        checkPassword(mTempleteAlertDialog.getPasswordInputData())
                    }
                }
            }
        }
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        { // 통신 수신 처리
            val result : BaseResult = mObject as BaseResult

            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            { // 통신 성공
            } else
            { // 통신 실패
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}