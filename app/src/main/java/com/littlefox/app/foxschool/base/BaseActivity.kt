package com.littlefox.app.foxschool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.ExceptionCheckHandler
import com.littlefox.logmonitor.Log


open class BaseActivity : AppCompatActivity()
{
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionCheckHandler(this))
        IntentManagementFactory.getInstance().setCurrentActivity(this)
    }

    override fun onResume()
    {
        super.onResume()
        Log.f("")
         IntentManagementFactory.getInstance().setCurrentActivity(this)

    }

    override fun onPause()
    {
        super.onPause()
        Log.f("")
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    open fun showLoading()
    {
        Log.f("")
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    open fun hideLoading()
    {
        Log.f("")
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    open fun initView() {}
    open fun initFont() {}

    open fun setupObserverViewModel(){}

}