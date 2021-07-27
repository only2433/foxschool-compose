package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Window
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.main.contract.InquireContract
import com.littlefox.app.foxschool.main.presenter.InquirePresenter

class InquireActivity : BaseActivity(), InquireContract.View
{
    private  lateinit var mInquirePresenter : InquirePresenter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)

        mInquirePresenter = InquirePresenter(this)
    }

    override fun initView()
    {
        TODO("Not yet implemented")
    }

    override fun initFont()
    {
        TODO("Not yet implemented")
    }

    override fun showLoading()
    {
        TODO("Not yet implemented")
    }

    override fun hideLoading()
    {
        TODO("Not yet implemented")
    }

    override fun showSuccessMessage(message : String)
    {
        TODO("Not yet implemented")
    }

    override fun showErrorMessage(message : String)
    {
        TODO("Not yet implemented")
    }

    override fun setInquireCategoryText(category : String)
    {
        TODO("Not yet implemented")
    }

}
