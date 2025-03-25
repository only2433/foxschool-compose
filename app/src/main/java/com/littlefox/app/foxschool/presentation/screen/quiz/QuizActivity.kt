package com.littlefox.app.foxschool.presentation.screen.quiz

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizSideEffect
import com.littlefox.app.foxschool.presentation.mvi.quiz.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.screen.quiz.phone.QuizScreenV
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                onAction = viewModel::onHandleAction
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.sideEffect.collect {value ->
                    when(value)
                    {
                        is SideEffect.EnableLoading ->
                        {
                            if(value.isLoading)
                            {
                                showLoading()
                            }
                            else
                            {
                                hideLoading()
                            }
                        }
                        is SideEffect.ShowToast ->
                        {
                            Log.i("message : ${value.message}")
                            Toast.makeText(this@QuizActivity, value.message, Toast.LENGTH_SHORT).show()
                        }
                        is SideEffect.ShowSuccessMessage ->
                        {
                            Log.i("message : $value.message")
                            CommonUtils.getInstance(this@QuizActivity).showSuccessMessage(value.message)
                        }
                        is SideEffect.ShowErrorMessage ->
                        {
                            Log.i("message : ${value.message}")
                            CommonUtils.getInstance(this@QuizActivity).showErrorMessage(value.message)
                        }
                        is QuizSideEffect.ShowWarningMessageDialog ->
                        {
                            showMessageAlertDialog(value.text)
                        }
                    }
                }
            }
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