package com.littlefox.app.foxschool.presentation.screen.management_mybooks

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

import com.littlefox.app.foxschool.presentation.screen.management_mybooks.phone.ManagementMyBooksScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.ManagementMyBooksViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManagementMyBooksActivity : BaseActivity()
{
    private val viewModel : ManagementMyBooksViewModel by viewModels()

    private var mTemplateAlertDialog : TemplateAlertDialog? = null
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        setupObserverViewModel()

        setContent {
            ManagementMyBooksScreenV(
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

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }


    override fun setupObserverViewModel()
    {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.isLoading.collect{ isLoading ->
                    if(isLoading)
                    {
                        showLoading()
                    }
                    else
                    {
                        hideLoading()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.toast.collect{ message ->
                    Log.i("message : $message")
                    Toast.makeText(this@ManagementMyBooksActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.successMessage.collect{ message ->
                    Log.i("message : $message")
                    CommonUtils.getInstance(this@ManagementMyBooksActivity).showSuccessMessage(message)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.errorMessage.collect{ message ->
                    Log.i("message : $message")
                    CommonUtils.getInstance(this@ManagementMyBooksActivity).showErrorMessage(message)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.dialogDeleteBookshelf.collect{
                    showDeleteBookshelfDialog()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.dialogDeleteVocabulary.collect{
                    showDeleteVocabularyDialog()
                }
            }
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

    private fun showDeleteBookshelfDialog()
    {
        showTemplateAlertDialog(
            resources.getString(R.string.message_delete_bookshelf),
            ManagementMyBooksViewModel.DIALOG_EVENT_DELETE_BOOKSHELF,
            DialogButtonType.BUTTON_2
        )
    }

    private fun showDeleteVocabularyDialog()
    {
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
            viewModel.onHandleViewEvent(
                BaseEvent.DialogChoiceClick(
                    buttonType,
                    eventType
                )
            )
        }
    }

}