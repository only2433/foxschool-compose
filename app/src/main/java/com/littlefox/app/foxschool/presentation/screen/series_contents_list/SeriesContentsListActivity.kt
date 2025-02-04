package com.littlefox.app.foxschool.presentation.screen.series_contents_list

import android.content.pm.ActivityInfo
import android.graphics.Color
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
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListAction
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListEvent
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListSideEffect
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.viewmodel.SeriesContentsListViewModel
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.phone.SeriesContentsScreenV
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class SeriesContentsListActivity : BaseActivity()
{
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null
    private var mBottomBookAddDialog: BottomBookAddDialog? = null

    private val viewModel: SeriesContentsListViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel.init(this)
        setupObserverViewModel()
        setContent{
            SeriesContentsScreenV(
                viewModel = viewModel,
                onAction = viewModel::onHandleAction
            )
        }
    }

    override fun setupObserverViewModel()
    {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.sideEffect.collect { value ->
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
                            Toast.makeText(this@SeriesContentsListActivity, value.message, Toast.LENGTH_SHORT).show()
                        }
                        is SideEffect.ShowSuccessMessage ->
                        {
                            Log.i("message : $value.message")
                            CommonUtils.getInstance(this@SeriesContentsListActivity).showSuccessMessage(value.message)
                        }
                        is SideEffect.ShowErrorMessage ->
                        {
                            Log.i("message : ${value.message}")
                            CommonUtils.getInstance(this@SeriesContentsListActivity).showErrorMessage(value.message)
                        }
                        is SeriesContentsListSideEffect.SetStatusBarColor ->
                        {
                            setStatusBar(value.color)
                        }
                        is SeriesContentsListSideEffect.ShowBottomOptionDialog ->
                        {
                            showBottomContentItemDialog(value.data)
                        }
                        is SeriesContentsListSideEffect.ShowBookshelfContentsAddDialog ->
                        {
                            showBottomBookAddDialog(value.list)
                        }
                    }
                }
            }
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

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    private fun setStatusBar(statusColor : String)
    {
        CommonUtils.getInstance(this).setStatusBar(Color.parseColor(statusColor))
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

    private fun showBottomContentItemDialog(result : ContentsBaseResult)
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(this, result)
            ?.setItemOptionListener(mItemOptionListener)
            ?.setFullName()
            ?.setView()
        mBottomContentItemOptionDialog?.show()
    }

    private fun showBottomBookAddDialog(list: ArrayList<MyBookshelfResult>)
    {
        mBottomContentItemOptionDialog?.dismiss()

        mBottomBookAddDialog = BottomBookAddDialog(this).apply {
            setCancelable(true)
            setBookshelfData(list)
            setBookSelectListener(mBookAddListener)
            show()
        }
    }

    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickItem(type : ActionContentsType)
        {
            viewModel.onHandleAction(
                SeriesContentsListAction.ClickBottomContentsType(
                    type
                )
            )
        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            viewModel.onHandleAction(
                SeriesContentsListAction.AddContentsInBookshelf(index)
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
}