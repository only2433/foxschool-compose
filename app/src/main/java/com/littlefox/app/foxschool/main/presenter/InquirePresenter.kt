package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import com.littlefox.app.foxschool.main.contract.InquireContract

class InquirePresenter : InquireContract.Presenter
{
    private lateinit var mContext : Context
    private lateinit var mInquireContractView : InquireContract.View
    constructor(context : Context)
    {
        mContext = context
        mInquireContractView = mContext as InquireContract.View
        mInquireContractView.initView()
        mInquireContractView.initFont()
    }

    override fun onShowInquireCategoryDialog()
    {
        TODO("Not yet implemented")
    }

    override fun onClickSendToEmail()
    {
        TODO("Not yet implemented")
    }

    override fun onClickRegister(email : String, text : String)
    {
        TODO("Not yet implemented")
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

}
