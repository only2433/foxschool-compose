package com.littlefox.app.foxschool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.receiver.NetworkConnectReceiver
import com.littlefox.logmonitor.ExceptionCheckHandler
import com.littlefox.logmonitor.Log


open class BaseActivity : AppCompatActivity()
{
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

}