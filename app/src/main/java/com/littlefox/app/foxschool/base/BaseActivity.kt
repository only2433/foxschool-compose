package com.littlefox.app.foxschool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.logmonitor.ExceptionCheckHandler


class BaseActivity : AppCompatActivity() {
    //private NetworkConnectReceiver mConnectReceiver;
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionCheckHandler(this))
        //mConnectReceiver = new NetworkConnectReceiver();
       // IntentManagementFactory.getInstance().setCurrentActivity(this)
    }

    protected override fun onResume() {
        super.onResume()
      //  IntentManagementFactory.getInstance().setCurrentActivity(this)
        //mConnectReceiver.register(this);
    }

    protected override fun onPause() {
        super.onPause()
        //mConnectReceiver.unregister(this);
    }

    protected override fun onDestroy() {
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
    }
}