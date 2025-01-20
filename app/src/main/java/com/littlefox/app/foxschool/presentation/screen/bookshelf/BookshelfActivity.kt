package com.littlefox.app.foxschool.presentation.screen.bookshelf

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfAction
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfSideEffect
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.viewmodel.BookshelfViewModel
import com.littlefox.app.foxschool.presentation.screen.bookshelf.phone.BookshelfScreenV
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookshelfActivity : BaseActivity()
{
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null
    private val viewModel: BookshelfViewModel by viewModels()
    
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel.init(this)
        setupObserverViewModel()
        setContent {
            BookshelfScreenV(
                viewModel = viewModel,
                onAction = viewModel::onHandleAction)
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.sideEffect.collect{ value ->
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
                            Toast.makeText(this@BookshelfActivity, value.message, Toast.LENGTH_SHORT).show()
                        }
                        is SideEffect.ShowSuccessMessage ->
                        {
                            Log.i("message : $value.message")
                            CommonUtils.getInstance(this@BookshelfActivity).showSuccessMessage(value.message)
                        }
                        is SideEffect.ShowErrorMessage ->
                        {
                            Log.i("message : ${value.message}")
                            CommonUtils.getInstance(this@BookshelfActivity).showErrorMessage(value.message)
                        }
                        is BookshelfSideEffect.ShowContentsDeleteDialog ->
                        {
                            showBookshelfContentsDeleteDialog()
                        }
                        is BookshelfSideEffect.ShowBottomOptionDialog ->
                        {
                            showBottomBookshelfItemDialog(value.data)
                        }
                        is BookshelfSideEffect.ShowRecordPermissionDialog ->
                        {
                            showChangeRecordPermissionDialog()
                        }
                    }
                }
            }
        }
    }

    /**
     * ================ 다이얼로그 ================
     */
    private fun showBottomBookshelfItemDialog(data : ContentsBaseResult)
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(this, data)
        mBottomContentItemOptionDialog!!
            .setDeleteMode()
            .setFullName()
            .setItemOptionListener(mItemOptionListener)
            .setView()
        mBottomContentItemOptionDialog!!.show()
    }

    private fun showBookshelfContentsDeleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_question_delete_contents_in_bookshelf))
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogEventType(BookshelfViewModel.DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_record_permission))
            setDialogEventType(PlayerFactoryViewModel.DIALOG_TYPE_WARNING_RECORD_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(
                resources.getString(R.string.text_cancel),
                resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickItem(type : ActionContentsType)
        {
            viewModel.onHandleAction(
                BookshelfAction.ClickBottomContentsType(type)
            )
        }
    }

    private val mDialogListener: DialogListener = object : DialogListener
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
}