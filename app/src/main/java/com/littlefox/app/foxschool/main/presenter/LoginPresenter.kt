package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.main.contract.LoginContract
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback

class LoginPresenter : LoginContract.Presenter
{
    private lateinit var mContext : Context
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mLoginContractView : LoginContract.View

    constructor(context : Context)
    {
        mContext = context;
        mLoginContractView = mContext as LoginContract.View
        mLoginContractView.initView()
        mLoginContractView.initFont()
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)

    }

    override fun resume()
    {
        TODO("Not yet implemented")
    }

    override fun pause()
    {
        TODO("Not yet implemented")
    }

    override fun destroy()
    {
        TODO("Not yet implemented")
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        TODO("Not yet implemented")
    }

    override fun sendMessageEvent(msg : Message)
    {
        TODO("Not yet implemented")
    }

    override fun onCheckAutoLogin(autoLogin : Boolean)
    {
        TODO("Not yet implemented")
    }

    override fun onClickLogin(data : UserLoginData)
    {
        TODO("Not yet implemented")
    }

    override fun onClickFindID()
    {
        TODO("Not yet implemented")
    }

    override fun onClickFindPassword()
    {
        TODO("Not yet implemented")
    }

}
