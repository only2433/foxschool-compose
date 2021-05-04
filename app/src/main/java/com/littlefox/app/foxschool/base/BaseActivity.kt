package com.littlefox.app.foxschool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.receiver.NetworkConnectReceiver
import com.littlefox.logmonitor.ExceptionCheckHandler


open class BaseActivity : AppCompatActivity()
{
    private var mConnectReceiver  : NetworkConnectReceiver? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionCheckHandler(this))
        mConnectReceiver =  NetworkConnectReceiver();
        IntentManagementFactory.getInstance().setCurrentActivity(this)
    }

    protected override fun onResume() {
        super.onResume()
         IntentManagementFactory.getInstance().setCurrentActivity(this)
        mConnectReceiver?.register(this);
    }

    protected override fun onPause() {
        super.onPause()
        mConnectReceiver?.unregister(this);
    }

    protected override fun onDestroy() {
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
    }
}