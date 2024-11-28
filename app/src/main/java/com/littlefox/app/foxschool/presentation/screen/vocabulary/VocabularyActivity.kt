package com.littlefox.app.foxschool.presentation.screen.vocabulary

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomIntervalSelectDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.IntervalSelectListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType

import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.presentation.screen.vocabulary.phone.VocabularyScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.VocabularyViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.vocabulary.VocabularyEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VocabularyActivity : BaseActivity()
{
    private var mTemplateAlertDialog : TemplateAlertDialog? = null
    private var mBottomIntervalSelectDialog : BottomIntervalSelectDialog? = null
    private var mBottomBookAddDialog : BottomBookAddDialog? = null

    private val viewModel: VocabularyViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.init(this)
        setupObserverViewModel()
        setContent {
            VocabularyScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent)
        }
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
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
            Toast.makeText(this@VocabularyActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@VocabularyActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@VocabularyActivity).showErrorMessage(message)
        }


        viewModel.dialogIntervalSelect.observe(this){ second ->
            showBottomIntervalDialog(second)
        }

        viewModel.dialogBottomVocabularyContentsAdd.observe(this){ list ->
            showBottomVocabularyAddDialog(list)
        }

        viewModel.dialogVocabularyContentsDelete.observe(this) {
            showVocabularyContentDeleteDialog()
        }
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    /**
     * ================ 다이얼로그 ================
     */
    private fun showBottomIntervalDialog(currentIntervalSecond: Int)
    {
        Log.f("")
        mBottomIntervalSelectDialog = BottomIntervalSelectDialog(this, currentIntervalSecond).apply {
            setCancelable(true)
            setOnIntervalSelectListener(mIntervalSelectListener)
            show()
        }
    }

    private fun showBottomVocabularyAddDialog(list : ArrayList<MyVocabularyResult>)
    {
        mBottomBookAddDialog = BottomBookAddDialog(this).apply {
            setCancelable(true)
            setVocabularyData(list)
            setBookSelectListener(mBookAddListener)
            show()
        }
    }

    private fun showVocabularyContentDeleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_question_delete_contents_in_vocabulary))
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogEventType(VocabularyViewModel.DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS)
            setDialogListener(mDialogListener)
            show()
        }
    }

    private val mIntervalSelectListener : IntervalSelectListener = object : IntervalSelectListener
    {
        override fun onClickIntervalSecond(second : Int)
        {
            Log.f("second : $second")
            viewModel.onHandleViewEvent(
                VocabularyEvent.onSelectIntervalSecond(
                    second
                )
            )

        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("index : $index")
            viewModel.onHandleViewEvent(
                VocabularyEvent.onAddContentsInVocabulary(
                    index
                )
            )
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {}

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