package com.littlefox.app.foxschool.presentation.screen.quiz

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.presentation.screen.quiz.phone.QuizScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizActivity : BaseActivity()
{
    private val viewModel : QuizViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        viewModel.init(this)
        setContent {
            QuizScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent
            )
        }
        setupObserverViewModel()
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

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
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
            Toast.makeText(this@QuizActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@QuizActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@QuizActivity).showErrorMessage(message)
        }

        viewModel.dialogWarningText.observe(this){ message ->
            showMessageAlertDialog(message)
        }
    }

    private fun showMessageAlertDialog(message : String)
    {
        TemplateAlertDialog(this).apply {
            setMessage(message)
            setDialogEventType(TemplateAlertDialog.DIALOG_EVENT_DEFAULT)
            setButtonType(DialogButtonType.BUTTON_1)
            show()
        }
    }
}