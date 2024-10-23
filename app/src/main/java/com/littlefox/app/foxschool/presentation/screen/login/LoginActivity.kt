package com.littlefox.app.foxschool.presentation.screen.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.littlefox.app.foxschool.R

import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.PasswordChangeDialog
import com.littlefox.app.foxschool.dialog.listener.PasswordChangeListener
import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.presentation.screen.intro.IntroActivity
import com.littlefox.app.foxschool.presentation.screen.login.phone.LoginScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.LoginViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.intro.IntroEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity()
{
    // 비밀번호 변경 안내 관련 변수
    private var mPasswordChangeDialog : PasswordChangeDialog? = null

    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        setupObserverViewModel()

        setContent {
            LoginScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent
            )
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

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }


    override fun onNewIntent(intent : Intent?)
    {
        Log.i("")
        super.onNewIntent(intent)
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
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@LoginActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@LoginActivity).showErrorMessage(message)
        }

        viewModel.showDialogPasswordChange.observe(this){ type ->
            showPasswordChangeDialog(type)
        }

        viewModel.hideDialogPasswordChange.observe(this){
            hidePasswordChangeDialog()
        }

        viewModel.finishActivity.observe(this){
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun showPasswordChangeDialog(type: PasswordGuideType)
    {
        Log.f("")
        mPasswordChangeDialog = PasswordChangeDialog(this, type).apply {
            setPasswordChangeListener(mPasswordChangeDialogListener)
            setCancelable(false)
            show()
        }
    }

    private fun hidePasswordChangeDialog()
    {
        mPasswordChangeDialog!!.dismiss()
    }

    private val mPasswordChangeDialogListener : PasswordChangeListener = object :
        PasswordChangeListener
    {
        /**
         * [비밀번호 변경] 버튼 클릭 이벤트
         */
        override fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
        {
            viewModel.onHandleViewEvent(
                IntroEvent.onClickChangeButton(
                    oldPassword,

                    newPassword,
                    confirmPassword
                )
            )
        }

        /**
         * [다음에 변경] 버튼 클릭 이벤트
         */
        override fun onClickLaterButton()
        {
            viewModel.onHandleViewEvent(
                IntroEvent.onClickLasterButton
            )
        }

        /**
         * [현재 비밀번호로 유지하기] 버튼 클릭 이벤트
         */
        override fun onClickKeepButton()
        {
            viewModel.onHandleViewEvent(
                IntroEvent.onClickKeepButton
            )
        }
    }


}