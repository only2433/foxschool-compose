package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.CheckUserInput
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.enumerate.MyInfoInputType
import com.littlefox.app.foxschool.enumerate.PasswordChangeInputType
import com.littlefox.app.foxschool.main.contract.MyInfoChangeContract
import com.littlefox.app.foxschool.main.contract.PasswordChangeContract
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class PasswordChangePresenter : PasswordChangeContract.Presenter
{
    private lateinit var mContext : Context
    private lateinit var mPasswordChangeView : PasswordChangeContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private var mLoginData : UserLoginData? = null

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mPasswordChangeView = mContext as PasswordChangeContract.View
        mPasswordChangeView.initView()
        mPasswordChangeView.initFont()

        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")
        mLoginData = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?
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
     * 기존 비밀번호와 일치한지 체크
     */
    override fun checkPassword(password : String)
    {
        // 기존 비밀번호와 일치한지 체크
        if (mLoginData != null)
        {
            val result = CheckUserInput.getInstance(mContext).checkPasswordData(
                SimpleCrypto.decode(mLoginData!!.userPassword),
                password
            ).getResultValue()

            if (result == CheckUserInput.WARNING_PASSWORD_NOT_EQUAL_CONFIRM)
            {
                mPasswordChangeView.showInputError(
                    PasswordChangeInputType.PASSWORD,
                    mContext.resources.getString(R.string.message_warning_password_confirm)
                )
            }
        }
    }

    /**
     * 새 비밀번호가 유효한지 체크
     * 1. 비밀번호 규칙 체크
     */
    override fun checkNewPasswordAvailable(newPassword : String)
    {
        val result = CheckUserInput.getInstance(mContext).checkPasswordData(newPassword).getResultValue()

        if (result == CheckUserInput.WARNING_PASSWORD_WRONG_INPUT)
        {
            mPasswordChangeView.showInputError(
                PasswordChangeInputType.NEW_PASSWORD,
                mContext.resources.getString(R.string.message_warning_input_password)
            )
        }
    }

    /**
     * 새 비밀번호가 유효한지 체크
     * 1. 새 비밀번호 확인 입력 체크
     * 2. 새 비밀번호 확인과 일치한지 체크
     */
    override fun checkNewPasswordConfirm(newPassword : String, newPasswordConfirm : String)
    {
        val result = CheckUserInput.getInstance(mContext).checkPasswordData(
            newPassword,
            newPasswordConfirm
        ).getResultValue()

        if (result == CheckUserInput.WARNING_PASSWORD_NOT_INPUT_CONFIRM)
        {
            mPasswordChangeView.showInputError(
                PasswordChangeInputType.NEW_PASSWORD_CONFIRM,
                mContext.resources.getString(R.string.message_warning_input_new_password_confirm)
            )
        }
        else if (result == CheckUserInput.WARNING_PASSWORD_NOT_EQUAL_CONFIRM)
        {
            mPasswordChangeView.showInputError(
                PasswordChangeInputType.NEW_PASSWORD_CONFIRM,
                mContext.resources.getString(R.string.message_warning_new_password_confirm)
            )
        }
    }

    /**
     * 저장 버튼 클릭 이벤트
     */
    override fun onClickSave(password : String, newPassword : String, newPasswordConfirm : String)
    {
        // TODO 추후 비밀번호 변경 통신 추가 예정
    }
}