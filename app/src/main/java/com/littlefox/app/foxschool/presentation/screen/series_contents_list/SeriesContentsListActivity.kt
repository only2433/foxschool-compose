package com.littlefox.app.foxschool.presentation.screen.series_contents_list

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.phone.SeriesContentsScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.SeriesContentsListViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.search.SearchEvent
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.isLoading.collect{ loading ->
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
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.toast.collect{ message ->
                    Log.i("message : $message")
                    Toast.makeText(this@SeriesContentsListActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.successMessage.collect{ message ->
                    Log.i("message : $message")
                    CommonUtils.getInstance(this@SeriesContentsListActivity).showSuccessMessage(message)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.errorMessage.collect{ message ->
                    Log.i("message : $message")
                    CommonUtils.getInstance(this@SeriesContentsListActivity).showErrorMessage(message)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.statusBarColor.collect{ color ->
                    setStatusBar(color)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.dialogBottomOption.collect{ item->
                    showBottomContentItemDialog(item)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                viewModel.dialogBottomBookshelfContentsAdd.collect{ list ->
                    showBottomBookAddDialog(list)
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        viewModel.resume()
        Log.f("")

    }

    override fun onPause()
    {
        super.onPause()
        viewModel.pause()
        Log.f("")
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