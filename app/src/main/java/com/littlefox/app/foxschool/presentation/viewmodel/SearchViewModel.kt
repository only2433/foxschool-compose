package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.SearchListItemPagingAdapter
import com.littlefox.app.foxschool.adapter.listener.SearchItemListener
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.paging.SearchPagingSource
import com.littlefox.app.foxschool.api.viewmodel.api.SearchApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.SearchType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.SearchListContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.search.SearchListResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.search.SearchEvent
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(private val apiViewModel : SearchApiViewModel) : BaseViewModel()
{
    companion object
    {
        const val DIALOG_TYPE_WARNING_RECORD_PERMISSION : Int   = 10001
    }

    private val _isContentsLoading = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isContentsLoading : SharedFlow<Boolean> = _isContentsLoading

    private val _dialogBottomOption = MutableSharedFlow<ContentsBaseResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogBottomOption : SharedFlow<ContentsBaseResult> = _dialogBottomOption

    private val _dialogBottomBookshelfContentsAdd = MutableSharedFlow<ArrayList<MyBookshelfResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogBottomBookshelfContentsAdd : SharedFlow<ArrayList<MyBookshelfResult>> = _dialogBottomBookshelfContentsAdd

    private val _dialogRecordPermission = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogRecordPermission : SharedFlow<Unit> = _dialogRecordPermission


    private var mCurrentSearchType: String = Common.CONTENT_TYPE_ALL
    private var mCurrentKeyword: String = ""

    private val _searchQuery = MutableStateFlow<Pair<String, String>>(Common.CONTENT_TYPE_ALL to "")

    val searchItemList: Flow<PagingData<ContentBasePagingResult>> = _searchQuery.flatMapLatest { (type, keyword) ->
        apiViewModel.getPagingData(
            type,
            keyword
        )
    }

    private lateinit var mContext : Context
    private lateinit var mSearchListContractView : SearchListContract.View
    private var mCurrentSearchListBaseResult : SearchListResult? = null
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null

    /**
     * 검색의 타입. ALL = "" , Stories = S , Songs = M
     */

    private var mRequestPagePosition = 1
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mMainHandler : WeakReferenceHandler
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentSelectItem: ContentsBaseResult? = null
    private var mJob: Job? = null



    override fun init(context : Context)
    {
        mContext = context
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        onHandleApiObserver()
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.onBackPressed -> {
                (mContext as AppCompatActivity).onBackPressed()
            }
            is SearchEvent.onClickBottomContentsType ->
            {
                checkBottomSelectItemType(event.type)
            }
            is SearchEvent.onClickSearchType -> {
                onClickSearchType(event.type)
            }
            is SearchEvent.onClickSearchExecute ->{
                onClickSearchExecute(event.keyword)
            }
            is SearchEvent.onClickThumbnail ->{
                onClickItemThumbnail(event.item)
            }
            is SearchEvent.onClickOption ->{
                onClickItemOption(event.item)
            }
            is SearchEvent.onAddContentsInBookshelf ->
            {
                onDialogAddBookshelfClick(event.index)
            }
        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect {data ->
                data?.let {
                    if(data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
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

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.addBookshelfContentsData.collect{ data ->
                data?.let {
                    updateBookshelfData(data)
                    viewModelScope.launch(Dispatchers.Main){
                        withContext(Dispatchers.IO){
                            delay(Common.DURATION_NORMAL)
                        }
                        _successMessage.emit(mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf))
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect{ data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    Log.f("status : ${result.status}, message : ${result.message} , code : $code")
                    if(result.isDuplicateLogin)
                    {
                        //중복 로그인 시 재시작
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if(result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        viewModelScope.launch {
                            _toast.emit(result.message)
                        }
                    }
                }
            }
        }
    }

    override fun resume()
    {
        TODO("Not yet implemented")
    }

    override fun pause()
    {
        TODO("Not yet implemented")
    }

    override fun destroy()
    {
        TODO("Not yet implemented")
    }



    private fun updateSearchQuery() {
        _searchQuery.value = mCurrentSearchType to mCurrentKeyword
    }


    private fun checkBottomSelectItemType(type: BottomDialogContentsType)
    {
        when(type)
        {
            BottomDialogContentsType.QUIZ -> startQuizActivity()
            BottomDialogContentsType.EBOOK -> startEbookActivity()
            BottomDialogContentsType.FLASHCARD -> startFlashcardActivity()
            BottomDialogContentsType.VOCABULARY -> startVocabularyActivity()
            BottomDialogContentsType.CROSSWORD -> startGameCrosswordActivity()
            BottomDialogContentsType.STARWORDS -> startGameStarwordsActivity()
            BottomDialogContentsType.TRANSLATE -> startOriginTranslateActivity()
            BottomDialogContentsType.RECORD_PLAYER -> {
                Log.f("")
                if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
                {
                    viewModelScope.launch {
                        _dialogRecordPermission.emit(Unit)
                    }
                }
                else
                {
                    startRecordPlayerActivity()
                }
            }
            BottomDialogContentsType.ADD_BOOKSHELF -> {
                Log.f("")
                mCurrentSelectItem?.let { item ->
                    mSendBookshelfAddList.clear()
                    mSendBookshelfAddList.add(item)
                    viewModelScope.launch {
                        _dialogBottomBookshelfContentsAdd.emit(mMainInformationResult.getBookShelvesList())
                    }
                }
            }
            else -> {}
        }
    }


    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in mMainInformationResult.getBookShelvesList().indices)
        {
            if(mMainInformationResult.getBookShelvesList()[i].getID() == result.getID())
            {
                Log.f("update Index :$i")
                mMainInformationResult.getBookShelvesList()[i] = result
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
    }

    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult>)
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_ADD,
            mCurrentBookshelfAddResult!!.getID(),
            data
        )
    }

    private fun searchDataList()
    {
        Log.f("searchType : $mCurrentSearchType, keyword : $mCurrentKeyword")
        mJob?.cancel()
        mJob = viewModelScope.launch {
            updateSearchQuery()
        }
    }

    /** ====================== StartActivity ====================== */
    private fun startCurrentSelectMovieActivity()
    {
        mCurrentSelectItem?.let { item ->
            Log.f("Movie ID : " + item.getID())
            val sendItemList = ArrayList<ContentsBaseResult>()
            sendItemList.add(item)
            val playerParamsObject = PlayerIntentParamsObject(sendItemList)

            IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerParamsObject)
                .startActivity()
        }

    }

    private fun startQuizActivity()
    {
        mCurrentSelectItem?.let { item ->
            Log.f("Quiz ID : " + item.getID())
            val quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(item.getID())
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.QUIZ)
                .setData(quizIntentParamsObject)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }

    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
                .setData(item.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }

    }

    private fun startEbookActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.getID())

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
                .setData(data)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }

    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val title = item.getVocabularyName()
            val myVocabularyResult = MyVocabularyResult(
                item.getID(),
                title,
                VocabularyType.VOCABULARY_CONTENTS)

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.VOCABULARY)
                .setData(myVocabularyResult)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.getID())

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
                .setData(data)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.getID())

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
                .setData(data)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val data = FlashcardDataObject(
                item.getID(),
                item.getName(),
                item.getSubName(),
                VocabularyType.VOCABULARY_CONTENTS
            )

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.FLASHCARD)
                .setData(data)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
    }

    private fun startRecordPlayerActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val recordIntentParamsObject = RecordIntentParamsObject(item)

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.RECORD_PLAYER)
                .setData(recordIntentParamsObject)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
    }

    private fun onClickSearchType(type: SearchType)
    {
        var searchType = when(type)
        {
            SearchType.ALL -> Common.CONTENT_TYPE_ALL
            SearchType.STORY -> Common.CONTENT_TYPE_STORY
            SearchType.SONG -> Common.CONTENT_TYPE_SONG
        }


        if(mCurrentSearchType == searchType)
        {
            return
        }

        if(mCurrentKeyword != "")
        {
            mRequestPagePosition = 1
            mCurrentSearchListBaseResult = null

            viewModelScope.launch {
                _isContentsLoading.emit(true)
                mCurrentSearchType = searchType
                searchDataList()
                _isContentsLoading.emit(false)
            }
        }
    }

    private fun onClickSearchExecute(keyword: String)
    {
        Log.f("keyword : $keyword")
        if(keyword.trim().length < 2)
        {
            viewModelScope.launch {
                _errorMessage.emit(mContext.resources.getString(R.string.message_warning_search_input_2_or_more))
            }
            return
        }
        mRequestPagePosition = 1
        mCurrentSearchListBaseResult = null

        viewModelScope.launch {
            _isContentsLoading.emit(true)
            mCurrentKeyword = keyword
            searchDataList()
            _isContentsLoading.emit(false)
        }

    }


    private fun onDialogAddBookshelfClick(index : Int)
    {
        mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList()[index]
        Log.f("Add Item : " + mCurrentBookshelfAddResult!!.getName())
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            requestBookshelfContentsAddAsync(mSendBookshelfAddList)
        }
    }

    private fun onClickItemThumbnail(item: ContentsBaseResult)
    {
        Log.f("index : ${item.getID()}")
        mCurrentSelectItem = item
        startCurrentSelectMovieActivity()
    }

    private fun onClickItemOption(item: ContentsBaseResult)
    {
        Log.f("index : ${item.getID()}")
        mCurrentSelectItem = item
        viewModelScope.launch {
            _dialogBottomOption.emit(item)
        }
    }


    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        if(eventType == DIALOG_TYPE_WARNING_RECORD_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    viewModelScope.launch {
                        _errorMessage.emit(mContext.getString(R.string.message_warning_record_permission))
                    }
                }
                DialogButtonType.BUTTON_2 ->
                {
                    // [권한 변경하기] 앱 정보 화면으로 이동
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", mContext.packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mContext.startActivity(intent)
                }
            }
        }
    }
}