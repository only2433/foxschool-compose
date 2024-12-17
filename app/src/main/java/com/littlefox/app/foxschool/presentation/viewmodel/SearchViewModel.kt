package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.SearchApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.ActionContentsType
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
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _isContentsLoading = SingleLiveEvent<Boolean>()
    val isContentsLoading: LiveData<Boolean> get() = _isContentsLoading

    private val _dialogBottomOption = SingleLiveEvent<ContentsBaseResult>()
    val dialogBottomOption: LiveData<ContentsBaseResult> get() = _dialogBottomOption

    private val _dialogBottomBookshelfContentsAdd = SingleLiveEvent<ArrayList<MyBookshelfResult>>()
    val dialogBottomBookshelfContentsAdd: LiveData<ArrayList<MyBookshelfResult>> get() = _dialogBottomBookshelfContentsAdd

    private val _dialogRecordPermission = SingleLiveEvent<Void>()
    val dialogRecordPermission: LiveData<Void> get() = _dialogRecordPermission


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
                                _isLoading.value = true
                            }
                            else
                            {
                                _isLoading.value = false
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
                        _successMessage.value = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
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
                        _toast.value = result.message
                        viewModelScope.launch {
                            withContext(Dispatchers.IO)
                            {
                                delay(Common.DURATION_SHORT)
                            }
                            (mContext as AppCompatActivity).finish()
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                    }
                    else if(result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        _toast.value = result.message
                        viewModelScope.launch {
                            withContext(Dispatchers.IO)
                            {
                                delay(Common.DURATION_SHORT)
                            }
                            (mContext as AppCompatActivity).finish()
                            IntentManagementFactory.getInstance().initScene()
                        }
                    }
                    else
                    {
                        _toast.value = result.message
                    }
                }
            }
        }
    }

    override fun resume()
    {
        Log.i("mCurrentKeyword : $mCurrentKeyword")
    }

    override fun pause()
    {

    }

    override fun destroy()
    {

    }



    private fun updateSearchQuery() {
        _searchQuery.value = mCurrentSearchType to mCurrentKeyword
    }


    private fun checkBottomSelectItemType(type: ActionContentsType)
    {
        when(type)
        {
            ActionContentsType.QUIZ -> startQuizActivity()
            ActionContentsType.EBOOK -> startEbookActivity()
            ActionContentsType.FLASHCARD -> startFlashcardActivity()
            ActionContentsType.VOCABULARY -> startVocabularyActivity()
            ActionContentsType.CROSSWORD -> startGameCrosswordActivity()
            ActionContentsType.STARWORDS -> startGameStarwordsActivity()
            ActionContentsType.TRANSLATE -> startOriginTranslateActivity()
            ActionContentsType.RECORD_PLAYER -> {
                Log.f("")
                if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
                {
                    _dialogRecordPermission.call()
                }
                else
                {
                    startRecordPlayerActivity()
                }
            }
            ActionContentsType.ADD_BOOKSHELF -> {
                Log.f("")
                mCurrentSelectItem?.let { item ->
                    mSendBookshelfAddList.clear()
                    mSendBookshelfAddList.add(item)
                    _dialogBottomBookshelfContentsAdd.value = mMainInformationResult.getBookShelvesList()
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
            Log.f("Movie ID : " + item.id)
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
            Log.f("Quiz ID : " + item.id)
            val quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(item.id)
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
                .setData(item.id)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }

    }

    private fun startEbookActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.id)

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
                item.id,
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
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.id)

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
            val data : WebviewIntentParamsObject = WebviewIntentParamsObject(item.id)

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
                item.id,
                item.name,
                item.sub_name,
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
                _isContentsLoading.value = true
                mCurrentSearchType = searchType
                searchDataList()
                _isContentsLoading.value = false
            }
        }
    }

    private fun onClickSearchExecute(keyword: String)
    {
        Log.f("keyword : $keyword")
        if(keyword.trim().length < 2)
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_warning_search_input_2_or_more)
            return
        }
        mRequestPagePosition = 1
        mCurrentSearchListBaseResult = null

        viewModelScope.launch {
            _isContentsLoading.value = true
            mCurrentKeyword = keyword
            searchDataList()
            _isContentsLoading.value = false
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
        Log.f("index : ${item.id}")
        mCurrentSelectItem = item
        startCurrentSelectMovieActivity()
    }

    private fun onClickItemOption(item: ContentsBaseResult)
    {
        Log.f("index : ${item.id}")
        mCurrentSelectItem = item
        _dialogBottomOption.value = item
    }


    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        if(eventType == DIALOG_TYPE_WARNING_RECORD_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    _errorMessage.value = mContext.getString(R.string.message_warning_record_permission)
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