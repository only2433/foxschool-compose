package com.littlefox.app.foxschool.presentation.screen.bookshelf

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.BookshelfFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.screen.bookshelf.phone.BookshelfScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.BookshelfViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookshelfActivity : BaseActivity()
{
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null
    private val viewModel: BookshelfViewModel by viewModels()
    
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        
        viewModel.init(this)
        setupObserverViewModel()
        
        setContent {
            BookshelfScreenV(
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
            Toast.makeText(this@BookshelfActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@BookshelfActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@BookshelfActivity).showErrorMessage(message)
        }

        viewModel.dialogBookshelfContentsDelete.observe(this){
            showBookshelfContentsDeleteDialog()
        }

        viewModel.dialogBottomOption.observe(this){ data ->
            showBottomBookshelfItemDialog(data)
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
        override fun onClickQuiz()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.QUIZ
                )
            )
        }

        override fun onClickTranslate()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.TRANSLATE
                )
            )
        }

        override fun onClickVocabulary()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.VOCABULARY
                )
            )
        }

        override fun onClickBookshelf()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.ADD_BOOKSHELF
                )
            )
        }

        override fun onClickEbook()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.EBOOK
                )
            )
        }

        override fun onClickGameStarwords()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.STARWORDS
                )
            )
        }

        override fun onClickGameCrossword()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.CROSSWORD
                )
            )
        }

        override fun onClickFlashCard()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.FLASHCARD
                )
            )
        }

        override fun onClickRecordPlayer()
        {
            viewModel.onHandleViewEvent(
                SeriesContentsListEvent.onClickBottomContentsType(
                    BottomDialogContentsType.RECORD_PLAYER
                )
            )
        }
    }

    private val mDialogListener: DialogListener = object : DialogListener
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