package com.littlefox.app.foxschool.api.viewmodel.factory

import android.app.Activity
import android.content.Context
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.SearchListItemPagingAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.adapter.listener.SearchItemListener
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.SearchApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.SearchListCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.SearchListContract
import com.littlefox.app.foxschool.main.presenter.SearchListPresenter
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.search.SearchListResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import java.util.ArrayList

@HiltViewModel
class SearchFactoryViewModel @Inject constructor(private val apiViewModel : SearchApiViewModel) : BaseFactoryViewModel()
{
    private val _showSearchListView = SingleLiveEvent<SearchListItemPagingAdapter>()
    val showSearchListView : LiveData<SearchListItemPagingAdapter> = _showSearchListView

    private val _showContentsLoading = SingleLiveEvent<Void>()
    val showContentsLoading : LiveData<Void> = _showContentsLoading

    private val _hideContentsLoading = SingleLiveEvent<Void>()
    val hideContentsLoading : LiveData<Void> = _hideContentsLoading

    private val _enableRefreshLoading = SingleLiveEvent<Boolean>()
    val enableRefreshLoading : LiveData<Boolean> = _enableRefreshLoading

    private val _dialogBottomOption = SingleLiveEvent <ContentsBaseResult>()
    val dialogBottomOption: LiveData<ContentsBaseResult> get() = _dialogBottomOption

    private val _dialogBottomBookshelfContentAdd = SingleLiveEvent<ArrayList<MyBookshelfResult>>()
    val dialogBottomBookshelfContentAdd: LiveData<ArrayList<MyBookshelfResult>> get() = _dialogBottomBookshelfContentAdd

    private val _dialogRecordPermission = SingleLiveEvent<Void>()
    val dialogRecordPermission: LiveData<Void> get() = _dialogRecordPermission


    private lateinit var mContext : Context
    private lateinit var mSearchListContractView : SearchListContract.View
    private var mCurrentSearchListBaseResult : SearchListResult? = null
    private val mSearchItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null

    /**
     * 검색의 타입. ALL = "" , Stories = S , Songs = M
     */
    private var mCurrentSearchType : String = Common.CONTENT_TYPE_ALL
    private var mCurrentKeyword = ""
    private var mRequestPagePosition = 1
    private var mSearchListItemAdapter : SearchListItemPagingAdapter? = null

    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mMainHandler : WeakReferenceHandler
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentSelectItem: ContentsBaseResult? = null
    private var mJob: Job? = null
    override fun init(context : Context)
    {
        mContext = context
        Log.f("onCreate")
        init()
        setupViewModelObserver()
    }

    private fun init()
    {
        Log.f("")
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        initRecyclerView()
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect {data ->
                data?.let {
                    if(data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        if(data.second)
                        {
                            _isLoading.postValue(true)
                        }
                        else
                        {
                            _isLoading.postValue(false)
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
                        _toast.value = result.message
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

    private fun initRecyclerView()
    {
        mSearchListItemAdapter = SearchListItemPagingAdapter(mContext)
            .setDetailItemListener(mSearchItemListener)
        mSearchListItemAdapter?.addLoadStateListener { state ->

            if(state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading ||
                state.prepend is LoadState.Loading)
            {
                _enableRefreshLoading.value = true
            }
            else
            {
                _enableRefreshLoading.value = false
            }

            val error = when {
                state.prepend is LoadState.Error -> state.prepend as LoadState.Error
                state.append is LoadState.Error -> state.append as LoadState.Error
                state.refresh is LoadState.Error -> state.refresh as LoadState.Error
                else -> null
            }
            error?.let {
                _toast.value = it.error.message
            }
        }
        _showSearchListView.value = mSearchListItemAdapter!!
    }

    private fun clearData()
    {
        mRequestPagePosition = 1
        mCurrentSearchListBaseResult = null
        mCurrentKeyword = ""
        mSearchItemList.clear()
        mSearchListItemAdapter?.notifyDataSetChanged()
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

    private fun getSearchDataList()
    {
        Log.f("mCurrentKeyword : $mCurrentKeyword")
        Log.f("position : $mRequestPagePosition, searchType : $mCurrentSearchType")
        mJob?.cancel()
        mJob = viewModelScope.launch {

            apiViewModel.getPagingData(mCurrentSearchType, mCurrentKeyword).collectLatest { data ->
                mSearchListItemAdapter?.submitData(data)
            }
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


    fun onClickSearchType(type: String)
    {
        if(mCurrentSearchType == type)
        {
            return
        }
        Log.f("type : $type")
        mCurrentSearchType = type

        if(mCurrentKeyword != "")
        {
            mRequestPagePosition = 1
            mCurrentSearchListBaseResult = null
            mSearchItemList.clear()
            if(mSearchListItemAdapter != null)
            {
                mSearchListItemAdapter!!.notifyDataSetChanged()
            }
            getSearchDataList()
        }
    }

    fun onClickSearchExecute(keyword: String)
    {
        Log.f("keyword : $keyword")
        if(keyword.trim().length < 2)
        {
            mSearchListContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_search_input_2_or_more))
            return
        }
        clearData()
        mCurrentKeyword = keyword
        getSearchDataList()
    }

    fun onClickQuizButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startQuizActivity()
        }
    }

    fun onClickTranslateButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startOriginTranslateActivity()
        }
    }

    fun onClickVocabularyButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startVocabularyActivity()
        }
    }

    fun onClickAddBookshelfButton()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList.add(item)
            _dialogBottomBookshelfContentAdd.value = mMainInformationResult.getBookShelvesList()
        }
       // mBottomContentItemOptionDialog.dismiss()
      //  mMainHandler.sendEmptyMessageDelayed(SearchListPresenter.MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG, Common.DURATION_SHORT)
    }

     fun onClickEbookButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startEbookActivity()
        }
    }

    fun onClickStarwordsButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameStarwordsActivity()
        }
    }

    fun onClickCrosswordButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameCrosswordActivity()
        }
    }

    fun onClickFlashcardButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startFlashcardActivity()
        }
    }

    fun onClickRecordPlayerButton()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
        {
            _dialogRecordPermission.call()
        }
        else
        {
            viewModelScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO){
                    delay(Common.DURATION_SHORT)
                }
                startRecordPlayerActivity()
            }
        }
    }

    fun onDialogAddBookshelfClick(index : Int)
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


    private val mSearchItemListener = object : SearchItemListener
    {
        override fun onItemClickThumbnail(item : ContentsBaseResult)
        {
            Log.f("index : ${item.id}")
            mCurrentSelectItem = item
            startCurrentSelectMovieActivity()
        }

        override fun onItemClickOption(item : ContentsBaseResult)
        {
            Log.f("index : ${item.id}")
            mCurrentSelectItem = item
            _dialogBottomOption.value = item
        }
    }

}