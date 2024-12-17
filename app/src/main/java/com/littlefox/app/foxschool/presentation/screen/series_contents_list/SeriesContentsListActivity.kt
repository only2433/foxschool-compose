package com.littlefox.app.foxschool.presentation.screen.series_contents_list

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.phone.SeriesContentsScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.SeriesContentsListViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
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
                onEvent = viewModel::onHandleViewEvent
            )
        }
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
            Toast.makeText(this@SeriesContentsListActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@SeriesContentsListActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@SeriesContentsListActivity).showErrorMessage(message)
        }

        viewModel.statusBarColor.observe(this){ color ->
            setStatusBar(color)
        }

        viewModel.dialogBottomOption.observe(this){ item ->
            showBottomContentItemDialog(item)
        }

        viewModel.dialogBottomBookshelfContentsAdd.observe(this){ list ->
            showBottomBookAddDialog(list)
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
        override fun onClickQuiz()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.QUIZ
                )
            )
        }

        override fun onClickTranslate()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.TRANSLATE
                )
            )
        }

        override fun onClickVocabulary()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.VOCABULARY
                )
            )
        }

        override fun onClickBookshelf()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.ADD_BOOKSHELF
                )
            )
        }

        override fun onClickEbook()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.EBOOK
                )
            )
        }

        override fun onClickGameStarwords()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.STARWORDS
                )
            )
        }

        override fun onClickGameCrossword()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.CROSSWORD
                )
            )
        }

        override fun onClickFlashCard()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.FLASHCARD
                )
            )
        }

        override fun onClickRecordPlayer()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    ActionContentsType.RECORD_PLAYER
                )
            )
        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onAddContentsInBookshelf(
                    index
                )
            )
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            viewModel.onHandleViewEvent(
                BaseEvent.DialogClick(
                    eventType
                )
            )
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            viewModel.onHandleViewEvent(
                BaseEvent.DialogChoiceClick(
                    buttonType,
                    eventType
                )
            )
        }
    }
}