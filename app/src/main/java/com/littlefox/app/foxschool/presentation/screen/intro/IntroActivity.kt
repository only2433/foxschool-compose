package com.littlefox.app.foxschool.presentation.screen.intro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import com.littlefox.app.foxschool.api.viewmodel.factory.IntroFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.IntroViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ResultLauncherCode

import com.littlefox.app.foxschool.presentation.screen.intro.phone.IntroScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.intro.IntroEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroActivity : BaseActivity()
{

    companion object
    {
        private const val SYSTEM_DIALOG_REASON_KEY : String         = "reason"
        private const val SYSTEM_DIALOG_REASON_HOME_KEY : String    = "homekey"
    }

    private val viewModel: IntroViewModel by viewModels()
    private var mHomeKeyIntentFilter : IntentFilter? = null

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        viewModel.onHandleViewEvent(
            IntroEvent.onAddResultLaunchers(
                mLoginActivityResult
            )
        )
        CommonUtils.getInstance(this).windowInfo()
        CommonUtils.getInstance(this).showDeviceInfo()
        CommonUtils.getInstance(this).initFeature()
        mHomeKeyIntentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)

        setContent {
            IntroScreenV(
                state = viewModel.state,
                onEvent = viewModel::onHandleViewEvent
            )
        }
    }


    override fun onResume()
    {
        super.onResume()
        viewModel.resume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            registerReceiver(mBroadcastReceiver, mHomeKeyIntentFilter, RECEIVER_NOT_EXPORTED)
        }
        else
        {
            registerReceiver(mBroadcastReceiver, mHomeKeyIntentFilter)
        }

    }

    override fun onPause()
    {
        super.onPause()
        viewModel.pause()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onStop()
    {
        Log.f("")
        super.onStop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        viewModel.destroy()

    }


    override fun onUserLeaveHint()
    {
        super.onUserLeaveHint()
        Log.f("")
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onHandleViewEvent(
            IntroEvent.onRequestPermissionResult(requestCode, permissions, grantResults)
        )
    }

    override fun onNewIntent(intent : Intent?)
    {
        Log.i("")
        super.onNewIntent(intent)
    }

    private val mBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context : Context, intent : Intent)
        {
            Log.i("", ">>> Home Event")
            val action : String? = intent.getAction()
            if(action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            {
                val reason : String = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)!!
                if(reason != null)
                {
                    if(reason == SYSTEM_DIALOG_REASON_HOME_KEY)
                    {
                        viewModel.onHandleViewEvent(
                            IntroEvent.onClickHomeButton
                        )
                    }
                }
            }
        }
    }

    private val mLoginActivityResult = registerForActivityResult(StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK)
        {
            viewModel.onHandleViewEvent(
                IntroEvent.onActivityResult(
                    ResultLauncherCode.DEFAULT,
                    result.data
                )
            )
        }
    }



}