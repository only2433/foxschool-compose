package com.littlefox.app.foxschool.presentation.screen.main

import MainScreenV
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.MainFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.presentation.screen.main.phone.SubStoryScreenV

import com.littlefox.app.foxschool.presentation.viewmodel.MainViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity()
{
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private val viewModel: MainViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        setupObserverViewModel()

        setContent {
            MainScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent)
        }
    }

    override fun onResume()
    {
        super.onResume()
        viewModel.resume()
    }

    override fun onPause()
    {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        viewModel.destroy()
    }


    override fun setupObserverViewModel()
    {
        viewModel.isLoading.observe(this){ loading ->
            Log.i("loading : $loading")
            if(loading)
            {
                showLoading()
            }
            else
            {
                hideLoading()
            }
        }

        viewModel.toast.observe(this){ message ->
            Log.i("message : $message")
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@MainActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@MainActivity).showErrorMessage(message)
        }

        viewModel.showAppEndDialog.observe(this){
            showAppEndDialog()
        }

        viewModel.showLogoutDialog.observe(this){
            showLogoutDialog()
        }
    }

    private fun showTemplateAlertDialog(message : String, eventType : Int, buttonType : DialogButtonType)
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(message)
            setDialogEventType(eventType)
            setButtonType(buttonType)
            setDialogListener(mDialogListener)
            setGravity(Gravity.LEFT)
            show()
        }
    }

    private fun showLogoutDialog()
    {
        showTemplateAlertDialog(
            getString(R.string.message_try_logout),
            MainViewModel.DIALOG_TYPE_LOGOUT,
            DialogButtonType.BUTTON_2
        )
    }

    private fun showAppEndDialog()
    {
        Log.f("Check End App")
        showTemplateAlertDialog(
            this.resources.getString(R.string.message_check_end_app),
            MainViewModel.DIALOG_TYPE_APP_END,
            DialogButtonType.BUTTON_2
        )
    }

    private val mDialogListener: DialogListener = object : DialogListener{
        override fun onConfirmButtonClick(eventType : Int)
        {
            viewModel.onHandleViewEvent(
                event = BaseEvent.DialogClick(
                    eventType
                )
            )
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            viewModel.onHandleViewEvent(
                event = BaseEvent.DialogChoiceClick(
                    buttonType,
                    eventType
                )
            )
        }

    }
}