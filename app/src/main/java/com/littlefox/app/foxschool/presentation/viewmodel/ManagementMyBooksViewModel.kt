package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.widget.Toast
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
import com.littlefox.app.foxschool.coroutine.BookshelfCreateCoroutine
import com.littlefox.app.foxschool.coroutine.BookshelfDeleteCoroutine
import com.littlefox.app.foxschool.coroutine.BookshelfUpdateCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyCreateCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyDeleteCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyUpdateCoroutine
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.main.presenter.ManagementItemMyBooksPresenter
import com.littlefox.app.foxschool.main.presenter.ManagementItemMyBooksPresenter.Companion
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.manage_mybooks.ManagementMyBooksEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManagementMyBooksViewModel @Inject constructor(private val apiViewModel : ManagementMyBooksApiViewModel) : BaseViewModel()
{
    companion object
    {
        const val DIALOG_EVENT_DELETE_BOOKSHELF : Int       = 10001
        const val DIALOG_EVENT_DELETE_VOCABULARY : Int      = 10002

        private const val MAX_NAME_SIZE : Int = 15
    }

    private val _managementBooksData = MutableSharedFlow<ManagementBooksData>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val managementBooksData : SharedFlow<ManagementBooksData> = _managementBooksData

    private val _dialogDeleteBookshelf = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogDeleteBookshelf : SharedFlow<Unit> = _dialogDeleteBookshelf

    private val _dialogDeleteVocabulary = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogDeleteVocabulary : SharedFlow<Unit> = _dialogDeleteVocabulary
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

        viewModelScope.launch {
            _managementBooksData.emit(mManagementBooksData)
        }

    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
           is BaseEvent.DialogChoiceClick -> {
                onDialogChoiceClick(
                    event.buttonType,
                    event.eventType
                )
            }

            is ManagementMyBooksEvent.onSelectBooksItem -> {
                mSelectBookColor = event.color
            }

            is ManagementMyBooksEvent.onSelectSaveButton ->{
                onSelectSaveButton(
                    event.bookName
                )
            }

            is ManagementMyBooksEvent.onCancelDeleteButton ->{
                onCancelActionButton()
            }

        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
                                    _isLoading.emit(true)
                                }
                                else
                                {
                                    _isLoading.emit(false)
                                }
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED)
            {
                apiViewModel.errorReport.collect{ data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        Log.f("status : ${result.status}, message : ${result.message} , code : $code")
                        if(result.isDuplicateLogin)
                        {
                            //중복 로그인 시 재시작
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            Log.f("== isAuthenticationBroken ==")
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else
                        {
                            viewModelScope.launch {
                                _errorMessage.emit(result.message)
                            }
                        }
                    }
                }
            }
        }
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
            viewModelScope.launch {
                _errorMessage.emit(mContext.resources.getString(R.string.message_warning_empty_bookshelf_name))
            }
            return
        }
        else if(bookName.length > MAX_NAME_SIZE)
        {
            viewModelScope.launch {
                _errorMessage.emit(mContext.resources.getString(R.string.message_warning_add_bookshelf_maximum_15_word))
            }
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
                viewModelScope.launch {
                    _dialogDeleteBookshelf.emit(Unit)
                }
            }
            MyBooksType.VOCABULARY_MODIFY ->
            {
                viewModelScope.launch {
                    _dialogDeleteVocabulary.emit(Unit)

                }
            }
        }
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        Log.f("eventType : $eventType, buttonType : $buttonType")
        when(eventType)
        {
            DIALOG_EVENT_DELETE_BOOKSHELF ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    requestBookshelfDeleteAsync()
                }
            }
            DIALOG_EVENT_DELETE_VOCABULARY ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    requestVocabularyDeleteAsync()
                }
            }
        }
    }
}