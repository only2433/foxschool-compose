package com.littlefox.app.foxschool.presentation.screen.management_mybooks

import android.content.pm.ActivityInfo
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
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.management.ManagementMyBooksSideEffect
import com.littlefox.app.foxschool.presentation.mvi.management.viewmodel.ManagementMyBooksViewModel

import com.littlefox.app.foxschool.presentation.screen.management_mybooks.phone.ManagementMyBooksScreenV
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManagementMyBooksActivity : BaseActivity()
{
    private val viewModel : com.littlefox.app.foxschool.presentation.mvi.management.viewmodel.ManagementMyBooksViewModel by viewModels()

    private var mTemplateAlertDialog : TemplateAlertDialog? = null
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel.init(this)
        setupObserverViewModel()
        setContent {
            ManagementMyBooksScreenV(
                viewModel = viewModel,
                onEvent = viewModel::postAction)
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

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }


    override fun setupObserverViewModel()
    {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
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
                            Toast.makeText(this@ManagementMyBooksActivity, data.message, Toast.LENGTH_SHORT).show()
                        }
                        is SideEffect.ShowSuccessMessage ->
                        {
                            Log.i("message : $data.message")
                            CommonUtils.getInstance(this@ManagementMyBooksActivity).showSuccessMessage(data.message)
                        }
                        is SideEffect.ShowErrorMessage ->
                        {
                            Log.i("message : ${data.message}")
                            CommonUtils.getInstance(this@ManagementMyBooksActivity).showErrorMessage(data.message)
                        }
                        is ManagementMyBooksSideEffect.ShowDeleteBookshelfDialog ->
                        {
                            showDeleteBookshelfDialog()
                        }
                        is ManagementMyBooksSideEffect.ShowDeleteVocabularyDialog ->
                        {
                            showDeleteBookshelfDialog()
                        }
                    }
                }
            }
        }

/*        viewModel.isLoading.observe(this){ isLoading ->
            if(isLoading)
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
            Toast.makeText(this@ManagementMyBooksActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@ManagementMyBooksActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@ManagementMyBooksActivity).showErrorMessage(message)
        }

        viewModel.dialogDeleteBookshelf.observe(this){
            showDeleteBookshelfDialog()
        }

        viewModel.dialogDeleteVocabulary.observe(this){
            showDeleteVocabularyDialog()
        }*/
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

    private fun showDeleteBookshelfDialog()
    {
        Log.i("")
        showTemplateAlertDialog(
            resources.getString(R.string.message_delete_bookshelf),
            ManagementMyBooksViewModel.DIALOG_EVENT_DELETE_BOOKSHELF,
            DialogButtonType.BUTTON_2
        )
    }

    private fun showDeleteVocabularyDialog()
    {
        Log.i("")
        showTemplateAlertDialog(
            resources.getString(R.string.message_delete_vocabulary),
            ManagementMyBooksViewModel.DIALOG_EVENT_DELETE_VOCABULARY,
            DialogButtonType.BUTTON_2
        )
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            Log.f("eventType : $eventType, buttonType : $buttonType")
            viewModel.onDialogChoiceClick(
                buttonType,
                eventType
            )
        }
    }

}