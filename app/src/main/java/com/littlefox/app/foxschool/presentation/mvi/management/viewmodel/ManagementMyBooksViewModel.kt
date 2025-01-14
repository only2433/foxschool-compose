package com.littlefox.app.foxschool.presentation.mvi.management.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.ManagementMyBooksApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.management.ManagementMyBooksAction

import com.littlefox.app.foxschool.presentation.mvi.management.ManagementMyBooksEvent
import com.littlefox.app.foxschool.presentation.mvi.management.ManagementMyBooksSideEffect
import com.littlefox.app.foxschool.presentation.mvi.management.ManagementMyBooksState

import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManagementMyBooksViewModel @Inject constructor(private val apiViewModel : ManagementMyBooksApiViewModel) : BaseMVIViewModel<ManagementMyBooksState, ManagementMyBooksEvent, SideEffect>(
    ManagementMyBooksState()
)
{
    companion object
    {
        const val DIALOG_EVENT_DELETE_BOOKSHELF : Int       = 10001
        const val DIALOG_EVENT_DELETE_VOCABULARY : Int      = 10002

        private const val MAX_NAME_SIZE : Int = 15
    }
    private lateinit var mContext : Context
    private var mMainInformationResult : MainInformationResult? = null
    private lateinit var mManagementBooksData : ManagementBooksData
    private var mSelectBookColor : String = ""
    private var mBookName : String? = null

    override fun init(context : Context)
    {
        mContext = context
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mManagementBooksData = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_MANAGEMENT_MYBOOKS_DATA)!!
        mSelectBookColor = mManagementBooksData.getColor().takeIf { it.isNotEmpty() } ?: "red"

        onHandleApiObserver()

        postEvent(
            ManagementMyBooksEvent.UpdateData(mManagementBooksData)
        )
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.isLoading.collect {data ->
                    data?.let {
                        if(data.first == RequestCode.CODE_CREATE_BOOKSHELF
                            || data.first == RequestCode.CODE_UPDATE_BOOKSHELF
                            || data.first == RequestCode.CODE_DELETE_BOOKSHELF
                            || data.first == RequestCode.CODE_CREATE_VOCABULARY
                            || data.first == RequestCode.CODE_UPDATE_VOCABULARY
                            || data.first == RequestCode.CODE_DELETE_VOCABULARY)
                        {
                            viewModelScope.launch {
                                if(data.second)
                                {
                                    postSideEffect(
                                        SideEffect.EnableLoading(true)
                                    )
                                }
                                else
                                {
                                    postSideEffect(
                                        SideEffect.EnableLoading(false)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.createBookshelfData.collect { data ->
                    Log.f("")
                    data?.let {
                        val myBookshelfResult : MyBookshelfResult = data
                        mMainInformationResult!!.getBookShelvesList().add(myBookshelfResult)
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.updateBookshelfData.collect { data ->
                    data?.let {
                        val myBookshelfResult : MyBookshelfResult = data
                        updateBookshelfData(myBookshelfResult)
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.deleteBookshelfData.collect{ data ->
                    data?.let {
                        deleteCurrentBookshelfData()
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.createVocabularyData.collect{ data ->
                    data?.let {
                        val myVocabularyResult : MyVocabularyResult = data
                        mMainInformationResult!!.getVocabulariesList().add(myVocabularyResult)
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.updateVocabularyData.collect{ data ->
                    data?.let {
                        val myVocabularyResult : MyVocabularyResult = data
                        updateVocabularyData(myVocabularyResult)
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.deleteVocabularyData.collect{ data ->

                    data?.let {
                        deleteCurrentVocabularyData()
                        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_SHORT)
                            (mContext as AppCompatActivity).finish()
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                apiViewModel.errorReport.collect{ data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        Log.f("status : ${result.status}, message : ${result.message} , code : $code")
                        if(result.isDuplicateLogin)
                        {
                            //중복 로그인 시 재시작
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initAutoIntroSequence()
                            }
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            Log.f("== isAuthenticationBroken ==")
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initScene()
                            }
                        }
                        else
                        {
                            postSideEffect(
                                SideEffect.ShowErrorMessage(result.message)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onHandleAction(action : Action)
    {
        when(action)
        {
            is ManagementMyBooksAction.SelectBooksItem ->
            {
                mSelectBookColor = action.color
            }
            is ManagementMyBooksAction.SelectSaveButton ->
            {
                onSelectSaveButton(action.bookName)
            }
            is ManagementMyBooksAction.CancelDeleteButton ->
            {
                onCancelActionButton()
            }
            else -> {}
        }
    }

    override suspend fun reduceState(current : ManagementMyBooksState, event : ManagementMyBooksEvent) : ManagementMyBooksState
    {
        return when(event)
        {
            is ManagementMyBooksEvent.UpdateData ->
            {
                current.copy(
                    booksData = event.data
                )
            }

            else -> current
        }
    }

    override fun onBackPressed()
    {
        (mContext as AppCompatActivity).finish()
    }

    /**
     * 책장 생성
     */
    private fun requestBookshelfCreateAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_CREATE_BOOKSHELF,
            mBookName,
            mSelectBookColor
        )
    }

    /**
     * 책장 수정
     */
    private fun requestBookshelfUpdateAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_UPDATE_BOOKSHELF,
            mManagementBooksData.getID(),
            mBookName,
            mSelectBookColor
        )
    }

    /**
     * 책장 삭제
     */
    private fun requestBookshelfDeleteAsync()
    {
        Log.f("Delete Bookshelf ID : " + mManagementBooksData.getID())
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_DELETE_BOOKSHELF,
            mManagementBooksData.getID()
        )
    }

    /**
     * 단어장 생성
     */
    private fun requestVocabularyCreateAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_CREATE_VOCABULARY,
            mBookName,
            mSelectBookColor
        )
    }

    /**
     * 단어장 수정
     */
    private fun requestVocabularyUpdateAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_UPDATE_VOCABULARY,
            mManagementBooksData.getID(),
            mBookName,
            mSelectBookColor
        )
    }

    /**
     * 단어장 삭제
     */
    private fun requestVocabularyDeleteAsync()
    {
        Log.f("Delete Vocabulary ID : " + mManagementBooksData.getID())
        apiViewModel.enqueueCommandStart(
            code = RequestCode.CODE_DELETE_VOCABULARY,
            mManagementBooksData.getID()
        )
    }

    /**
     * 책장 데이터 업데이트
     */

    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in mMainInformationResult!!.getBookShelvesList().indices)
        {
            if(mMainInformationResult!!.getBookShelvesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("update Bookshelf Index :$i")
                mMainInformationResult!!.getBookShelvesList()[i] = result
            }
        }
    }

    /**
     * 책장 삭제
     */
    private fun deleteCurrentBookshelfData()
    {
        for(i in mMainInformationResult!!.getBookShelvesList().indices)
        {
            if(mMainInformationResult!!.getBookShelvesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("Delete Bookshelf Index :" + i + ", ID : " + mManagementBooksData.getID())
                mMainInformationResult!!.getBookShelvesList().removeAt(i)
                return
            }
        }
    }

    /**
     * 단어장 데이터 업데이트
     */
    private fun updateVocabularyData(result : MyVocabularyResult)
    {
        for(i in mMainInformationResult!!.getVocabulariesList().indices)
        {
            if(mMainInformationResult!!.getVocabulariesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("update Vocabulary Index :$i")
                mMainInformationResult!!.getVocabulariesList()[i] = result
            }
        }
    }

    /**
     * 단어장 삭제
     */
    private fun deleteCurrentVocabularyData()
    {
        for(i in mMainInformationResult!!.getVocabulariesList().indices)
        {
            if(mMainInformationResult!!.getVocabulariesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("Delete Vocabulary Index :" + i + ", ID : " + mManagementBooksData.getID())
                mMainInformationResult!!.getVocabulariesList().removeAt(i)
                return
            }
        }
    }

    private fun onSelectSaveButton(bookName : String)
    {
        if(bookName == "")
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(mContext.resources.getString(R.string.message_warning_empty_bookshelf_name))
            )
            return
        }
        else if(bookName.length > MAX_NAME_SIZE)
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(mContext.resources.getString(R.string.message_warning_add_bookshelf_maximum_15_word))
            )
            return
        }

        mBookName = bookName


        Log.f("bookName : " + mBookName + ", type : " + mManagementBooksData.getBooksType())
        when(mManagementBooksData.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD -> requestBookshelfCreateAsync()
            MyBooksType.BOOKSHELF_MODIFY -> requestBookshelfUpdateAsync()
            MyBooksType.VOCABULARY_ADD -> requestVocabularyCreateAsync()
            MyBooksType.VOCABULARY_MODIFY -> requestVocabularyUpdateAsync()
        }
    }

    private fun onCancelActionButton()
    {
        Log.f("Book Type : " + mManagementBooksData.getBooksType())
        when(mManagementBooksData.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD, MyBooksType.VOCABULARY_ADD -> (mContext as AppCompatActivity).finish()
            MyBooksType.BOOKSHELF_MODIFY ->
            {
                postSideEffect(
                    ManagementMyBooksSideEffect.ShowDeleteBookshelfDialog
                )
            }
            MyBooksType.VOCABULARY_MODIFY ->
            {
                postSideEffect(
                    ManagementMyBooksSideEffect.ShowDeleteVocabularyDialog
                )
            }
        }
    }


    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        Log.f("eventType : $eventType, buttonType : $buttonType")
        when(eventType)
        {
            ManagementMyBooksViewModel.DIALOG_EVENT_DELETE_BOOKSHELF ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    requestBookshelfDeleteAsync()
                }
            }
            ManagementMyBooksViewModel.DIALOG_EVENT_DELETE_VOCABULARY ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    requestVocabularyDeleteAsync()
                }
            }
        }
    }
}