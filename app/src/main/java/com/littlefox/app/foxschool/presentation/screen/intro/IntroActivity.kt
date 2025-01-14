package com.littlefox.app.foxschool.presentation.screen.intro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.IntroFactoryViewModel

import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.PasswordChangeDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.PasswordChangeListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.enumerate.ResultLauncherCode
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.intro.IntroAction
import com.littlefox.app.foxschool.presentation.mvi.intro.IntroSideEffect
import com.littlefox.app.foxschool.presentation.mvi.intro.viewmodel.IntroViewModel

import com.littlefox.app.foxschool.presentation.screen.intro.phone.IntroScreenV
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IntroActivity : BaseActivity()
{
    companion object
    {
        private const val SYSTEM_DIALOG_REASON_KEY : String         = "reason"
        private const val SYSTEM_DIALOG_REASON_HOME_KEY : String    = "homekey"
    }

    // 비밀번호 변경 안내 관련 변수
    private var mPasswordChangeDialog : PasswordChangeDialog? = null
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private val viewModel: IntroViewModel by viewModels()
    private var mHomeKeyIntentFilter : IntentFilter? = null

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel.init(this)
        viewModel.onAddResultLaunchers(
            mLoginActivityResult
        )
        CommonUtils.getInstance(this).windowInfo()
        CommonUtils.getInstance(this).showDeviceInfo()
        CommonUtils.getInstance(this).initFeature()
        mHomeKeyIntentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)

        setContent {
            IntroScreenV(
                viewModel = viewModel,
                onAction = viewModel::onHandleAction
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
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent : Intent?)
    {
        Log.i("")
        super.onNewIntent(intent)
    }

    override fun setupObserverViewModel()
    {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.sideEffect.collect{ data ->
                    when(data)
                    {
                        is SideEffect.EnableLoading ->
                        {
                            if(data.isLoading)
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
                            Log.i("message : ${data.message}")
                            Toast.makeText(this@IntroActivity, data.message, Toast.LENGTH_SHORT).show()
                        }
                        is SideEffect.ShowSuccessMessage ->
                        {
                            Log.i("message : $data.message")
                            CommonUtils.getInstance(this@IntroActivity).showSuccessMessage(data.message)
                        }
                        is SideEffect.ShowErrorMessage ->
                        {
                            Log.i("message : ${data.message}")
                            CommonUtils.getInstance(this@IntroActivity).showErrorMessage(data.message)
                        }
                        is IntroSideEffect.ShowSelectUpdateDialog ->
                        {
                            showSelectUpdateDialog()
                        }
                        is IntroSideEffect.ShowForceUpdateDialog ->
                        {
                            showForceUpdateDialog()
                        }
                        is IntroSideEffect.ShowFilePermissionDialog ->
                        {
                            showChangeFilePermissionDialog()
                        }
                        is IntroSideEffect.ShowPasswordChangeDialog ->
                        {
                            showPasswordChangeDialog(data.type)
                        }
                        is IntroSideEffect.HidePasswordChangeDialog ->
                        {
                            hidePasswordChangeDialog()
                        }
                    }
                }
            }
        }
    }

    /**
     * 파일 권한 허용 요청 다이얼로그
     * - 로그 파일 저장을 위해
     */
    private fun showChangeFilePermissionDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_warning_storage_permission))
            setDialogEventType(IntroViewModel.DIALOG_TYPE_WARNING_FILE_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(resources.getString(R.string.text_cancel), resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showForceUpdateDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_force_update))
            setDialogEventType(IntroViewModel.DIALOG_TYPE_FORCE_UPDATE)
            setButtonType(DialogButtonType.BUTTON_1)
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showSelectUpdateDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_need_update))
            setDialogEventType(IntroViewModel.DIALOG_TYPE_SELECT_UPDATE_CONFIRM)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
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

    private val mPasswordChangeDialogListener : PasswordChangeListener = object : PasswordChangeListener
    {
        /**
         * [비밀번호 변경] 버튼 클릭 이벤트
         */
        override fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
        {
            viewModel.onHandleAction(
                IntroAction.ClickChangeButton(
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
            viewModel.onHandleAction(
                IntroAction.ClickLaterButton
            )
        }

        /**
         * [현재 비밀번호로 유지하기] 버튼 클릭 이벤트
         */
        override fun onClickKeepButton()
        {
            viewModel.onHandleAction(
                IntroAction.ClickKeepButton
            )
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            viewModel.onDialogClick(
                eventType
            )
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            viewModel.onDialogChoiceClick(
                buttonType,
                eventType
            )
        }
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
                        viewModel.onHandleAction(
                            IntroAction.ClickHomeButton
                        )
                    }
                }
            }
        }
    }

    private val mLoginActivityResult = registerForActivityResult(StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK)
        {
            viewModel.onActivityResult(
                ResultLauncherCode.DEFAULT,
                result.data
            )
        }
    }

}