package com.littlefox.app.foxschool.presentation.screen.search

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.littlefox.app.foxschool.R
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
import com.littlefox.app.foxschool.presentation.screen.search.phone.SearchScreen
import com.littlefox.app.foxschool.presentation.viewmodel.SearchViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.search.SearchEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class SearchActivity : BaseActivity()
{
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null
    private var mBottomBookAddDialog: BottomBookAddDialog? = null

    private val viewModel: SearchViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        viewModel.init(this)
        setupObserverViewModel()

        setContent {
            SearchScreen(
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
        viewModel.toast.observe(this){ message ->
            Log.i("message : $message")
            Toast.makeText(this@SearchActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@SearchActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@SearchActivity).showErrorMessage(message)
        }

        viewModel.dialogBottomOption.observe(this){ item->
            showBottomContentItemDialog(item)
        }

        viewModel.dialogBottomBookshelfContentsAdd.observe(this){ list ->
            showBottomBookAddDialog(list)
        }

        viewModel.dialogRecordPermission.observe(this){
            showChangeRecordPermissionDialog()
        }
    }

    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_record_permission))
            setDialogEventType(SearchViewModel.DIALOG_TYPE_WARNING_RECORD_PERMISSION)
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
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.QUIZ
                )
            )
        }

        override fun onClickTranslate()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.TRANSLATE
                )
            )
        }

        override fun onClickVocabulary()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.VOCABULARY
                )
            )
        }

        override fun onClickBookshelf()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.ADD_BOOKSHELF
                )
            )
        }

        override fun onClickEbook()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.EBOOK
                )
            )
        }

        override fun onClickGameStarwords()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.STARWORDS
                )
            )
        }

        override fun onClickGameCrossword()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.CROSSWORD
                )
            )
        }

        override fun onClickFlashCard()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
                    ActionContentsType.FLASHCARD
                )
            )
        }

        override fun onClickRecordPlayer()
        {
            viewModel.onHandleViewEvent(
                SearchEvent.onClickBottomContentsType(
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
                SearchEvent.onAddContentsInBookshelf(
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