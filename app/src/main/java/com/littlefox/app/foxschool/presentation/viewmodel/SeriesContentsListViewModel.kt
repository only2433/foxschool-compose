package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.SeriesContentsListApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.enumerate.VocabularyType

import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.series.SeriesViewData
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.filterList
import java.util.Collections

import javax.inject.Inject


@HiltViewModel
class SeriesContentsListViewModel @Inject constructor(private val apiViewModel : SeriesContentsListApiViewModel) : BaseViewModel()
{

    private val _isContentsLoading = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isContentsLoading : SharedFlow<Boolean> = _isContentsLoading

    private val _contentsList = MutableSharedFlow<ArrayList<ContentsBaseResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val contentsList: SharedFlow<ArrayList<ContentsBaseResult>> = _contentsList

    private val _isSingleSeries = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isSingleSeries: SharedFlow<Boolean> = _isSingleSeries

    private val _showToolbarInformationView = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showToolbarInformationView: SharedFlow<Boolean> = _showToolbarInformationView

    private val _seriesTitle = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val seriesTitle: SharedFlow<String> = _seriesTitle

    private val _backgroundViewData = MutableSharedFlow<TopThumbnailViewData>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val backgroundView: SharedFlow<TopThumbnailViewData> = _backgroundViewData

    private val _seriesDataView = MutableSharedFlow<SeriesViewData>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val seriesViewData: SharedFlow<SeriesViewData> = _seriesDataView

    private val _statusBarColor = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST

    )
    val statusBarColor: SharedFlow<String> = _statusBarColor

    private val _itemSelectedCount =  MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val itemSelectedCount: SharedFlow<Int> = _itemSelectedCount

    private val _dialogBottomOption = MutableSharedFlow<ContentsBaseResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogBottomOption : SharedFlow<ContentsBaseResult> = _dialogBottomOption

    private val _dialogBottomBookshelfContentsAdd = MutableSharedFlow<java.util.ArrayList<MyBookshelfResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogBottomBookshelfContentsAdd : SharedFlow<java.util.ArrayList<MyBookshelfResult>> = _dialogBottomBookshelfContentsAdd

    private val _dialogRecordPermission = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val dialogRecordPermission : SharedFlow<Unit> = _dialogRecordPermission



    private lateinit var mCurrentSeriesBaseResult : SeriesBaseResult
    private lateinit var mDetailItemInformationResult : DetailItemInformationResult
    private lateinit var mMainInformationResult : MainInformationResult

    private var mCurrentContentsItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mSendBookshelfAddList : java.util.ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()

    private var mCurrentPlayIndex : Int     = 0
    private var mCurrentOptionIndex : Int   = 0
    private var mCurrentSelectItem: ContentsBaseResult? = null
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null
    private var isStillOnSeries : Boolean   = false
    private lateinit var mContext : Context


    override fun init(context : Context)
    {
        mContext = context
        mCurrentSeriesBaseResult = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_STORY_SERIES_DATA)!!
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        onHandleApiObserver()

        prepareUI()
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                delay(Common.DURATION_LONG)
            }
            requestDetailInformation()
        }

    }

    private fun prepareUI()
    {
        viewModelScope.launch {
            _seriesTitle.emit(mCurrentSeriesBaseResult.getSeriesName())
        }

        viewModelScope.launch {
            _statusBarColor.emit(mCurrentSeriesBaseResult.statusBarColor)
        }

        val data = TopThumbnailViewData(
            thumbnail = mCurrentSeriesBaseResult.getThumbnailUrl(),
            titleColor = mCurrentSeriesBaseResult.titleColor,
            transitionType = mCurrentSeriesBaseResult.getTransitionType()
            )

        viewModelScope.launch {
            _backgroundViewData.emit(data)
        }
        viewModelScope.launch {
            _isContentsLoading.emit(true)
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is SeriesContentsListEvent.onClickSelectAll ->
            {
                checkSelectedItemAll(
                    isSelected = true
                )
            }
            is SeriesContentsListEvent.onClickSelectPlay ->
            {

            }
            is SeriesContentsListEvent.onClickAddBookshelf ->
            {
                addContentsListInBookshelf()
            }
            is SeriesContentsListEvent.onClickCancel ->
            {
                checkSelectedItemAll(
                    isSelected = false
                )
            }

            is SeriesContentsListEvent.onClickBottomContentsType ->
            {
                checkBottomSelectItemType(event.type)
            }

            is SeriesContentsListEvent.onSelectedItem ->
            {
                onSelectItem(event.index)
            }
            is SeriesContentsListEvent.onClickThumbnail ->
            {
                mCurrentSelectItem = event.item
                startCurrentSelectMovieActivity()
            }
            is SeriesContentsListEvent.onClickOption ->
            {
                mCurrentSelectItem = event.item
                viewModelScope.launch {
                    _dialogBottomOption.emit(event.item)
                }
            }
            is SeriesContentsListEvent.onAddContentsInBookshelf ->
            {
                onDialogAddBookshelfClick(event.index)
            }
        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED) {
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                        {
                            if(data.second)
                            {
                                Log.i("isLoading = true")
                                _isLoading.emit(true)
                            }
                            else
                            {
                                Log.i("isLoading = false")
                                _isLoading.emit(false)
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.storyContentsListData.collect { data ->
                    data?.let {
                        settingDetailView(data)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.songContentsListData.collect { data ->
                    data?.let {
                        settingDetailView(data)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED) {
                apiViewModel.addBookshelfContentsData.collect{ data ->
                    data?.let {
                        updateBookshelfData(it)
                    }
                    checkSelectedItemAll(
                        isSelected = false
                    )
                    withContext(Dispatchers.Main)
                    {
                        delay(Common.DURATION_NORMAL)
                        _successMessage.emit(mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf))
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        if(result.isDuplicateLogin)
                        {
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else
                        {
                            if(code == RequestCode.CODE_CONTENTS_STORY_LIST ||
                                code == RequestCode.CODE_CONTENTS_SONG_LIST)
                            {
                                viewModelScope.launch {
                                    _toast.emit(result.message)
                                }
                                (mContext as AppCompatActivity).onBackPressed()
                            }
                            else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                            {
                                viewModelScope.launch {
                                    _isLoading.emit(false)
                                }

                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    _errorMessage.emit(result.message)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    override fun resume()
    {

    }

    override fun pause()
    {

    }

    override fun destroy()
    {

    }

    private fun requestDetailInformation()
    {
        if(mCurrentSeriesBaseResult.getSeriesType() == Common.CONTENT_TYPE_STORY)
        {
            apiViewModel.enqueueCommandStart(
                RequestCode.CODE_CONTENTS_STORY_LIST,
                mCurrentSeriesBaseResult.getDisplayID()
            )
        }
        else
        {
            apiViewModel.enqueueCommandStart(
                RequestCode.CODE_CONTENTS_SONG_LIST,
                mCurrentSeriesBaseResult.getDisplayID()
            )
        }
    }


    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult>)
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_ADD,
            mCurrentBookshelfAddResult!!.getID(),
            data
        )

    }

    private val lastStudyMovieIndex : Int
        get()
        {
            var result = 0
            for(i in 0 until mDetailItemInformationResult.getContentsList().size)
            {
                if(mDetailItemInformationResult.lastStudyContentID == mDetailItemInformationResult.getContentsList()[i].getID())
                {
                    if(mDetailItemInformationResult.isStillOnSeries)
                    {
                        result = mDetailItemInformationResult.getContentsList().size - i
                    }
                    else
                    {
                        result = i + 1
                    }
                }
            }
            return result
        }

    private fun settingDetailView(data : DetailItemInformationResult)
    {
        mDetailItemInformationResult = data
        var viewData: SeriesViewData

        if(mDetailItemInformationResult.isSingleSeries == false
            && mCurrentSeriesBaseResult.getSeriesType().equals(Common.CONTENT_TYPE_STORY))
        {
            viewModelScope.launch {
                _showToolbarInformationView.emit(true)
            }
        }

        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            viewData = SeriesViewData(
                type = mCurrentSeriesBaseResult.getSeriesType(),
                seriesLevel = mDetailItemInformationResult.seriesLevel,
                contentsSize = mDetailItemInformationResult.getContentsList().size,
                category = mCurrentSeriesBaseResult.categoryData,
                isSingleSeries = mDetailItemInformationResult.isSingleSeries,
                arLevel = mDetailItemInformationResult.seriesARLevel,
                introduction = mCurrentSeriesBaseResult.introduction
            )
        }
        else
        {
            viewData = SeriesViewData(
                type = mCurrentSeriesBaseResult.getSeriesType(),
                seriesLevel = mDetailItemInformationResult.seriesLevel,
                contentsSize = mDetailItemInformationResult.getContentsList().size,
                isSingleSeries = mDetailItemInformationResult.isSingleSeries,
                arLevel = mDetailItemInformationResult.seriesARLevel,
            )
        }

        viewModelScope.launch {
            _seriesDataView.emit(viewData)
        }

        viewModelScope.launch {
            _isSingleSeries.emit(mDetailItemInformationResult.isSingleSeries)
        }


        if(mDetailItemInformationResult.seriesID != "")
        {
            if(!mDetailItemInformationResult.isSingleSeries && mDetailItemInformationResult.isSingleSeries)
            {
                isStillOnSeries = true
            }
        }

        viewModelScope.launch {
            _isContentsLoading.emit(false)
        }

        initContentsList()
    }

    private fun initContentsList()
    {
        mCurrentContentsItemList.clear()
        mCurrentContentsItemList = mDetailItemInformationResult.getContentsList()
        for(i in mCurrentContentsItemList.indices)
        {
            val index = i + 1
            mCurrentContentsItemList[i].setIndex(index)
        }

        if(isStillOnSeries)
        {
            mCurrentContentsItemList.reverse()
        }

        viewModelScope.launch {
            _contentsList.emit(mCurrentContentsItemList)
        }

        checkLastWatchContents()
    }

    private fun checkLastWatchContents()
    {
        if(mDetailItemInformationResult.lastStudyContentID != "")
        {
            val loginInformationResult : LoginInformationResult =
                CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            val resultIndex = lastStudyMovieIndex
            viewModelScope.launch {
                _successMessage.emit(
                    "${loginInformationResult.getUserInformation().getName()}님은 현재 $resultIndex 까지 학습 했어요."
                )
            }
        }
    }

    /**
     * 컨텐츠의 책장 리스트에서 나의단어장으로 컨텐츠를 추가해서 갱신할때 사용하는 메소드 ( 추가됨으로써 서버쪽의 해당 책장의 정보를 갱신하기 위해 사용 )
     * 예) 책장 ID , 컨텐츠의 개수, 책장 컬러 등등
     * @param result 서버쪽에서 받은 결과 책장 정보
     */
    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in 0 until mMainInformationResult.getBookShelvesList().size)
        {
            if(mMainInformationResult.getBookShelvesList().get(i).getID().equals(result.getID()))
            {
                Log.f("update Index :$i")
                mMainInformationResult.getBookShelvesList().set(i, result)
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
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

    private fun addContentsListInBookshelf()
    {
        if(getSelectedItemList().size > 0)
        {
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList = getSelectedItemList()
            if(isStillOnSeries)
            {
                Log.f("Add List isStillOnSeries : " + mDetailItemInformationResult.seriesID)
                mSendBookshelfAddList.reverse()
            }
            viewModelScope.launch {
                _dialogBottomBookshelfContentsAdd.emit(mMainInformationResult.getBookShelvesList())
            }
        } else
        {
            viewModelScope.launch {
                _errorMessage.emit(mContext.resources.getString(R.string.message_not_add_selected_contents_bookshelf))
            }
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


    /** ====================== StartActivity ====================== */
    private fun startCurrentSelectMovieActivity()
    {
        mCurrentSelectItem?.let { item ->
            Log.f("Movie ID : " + item.getID())
            val sendItemList = java.util.ArrayList<ContentsBaseResult>()
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



    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSelectItem(index : Int)
    {
        // 현재 선택 상태를 반전
        val isSelected = mCurrentContentsItemList[index].isSelected()
        mCurrentContentsItemList[index].setSelected(!isSelected)

        // mCurrentContentsItemList를 ArrayList로 변환하여 방출
        viewModelScope.launch {
            _contentsList.emit(ArrayList<ContentsBaseResult>())
            _contentsList.emit(mCurrentContentsItemList)
        }

        Log.i("index : $index , isSelected : ${mCurrentContentsItemList[index].isSelected()}")

        sendSelectedItem()
    }

    private fun getSelectedItemList() : ArrayList<ContentsBaseResult>
    {
        return ArrayList(mCurrentContentsItemList.filter {
            it.isSelected()
        })
    }

    private fun sendSelectedItem()
    {
        val selectedItemCount = mCurrentContentsItemList.count { it.isSelected() }
        viewModelScope.launch {
            _itemSelectedCount.emit(selectedItemCount)
        }
    }

    private fun checkSelectedItemAll(isSelected : Boolean)
    {
        mCurrentContentsItemList.forEach {
            it.setSelected(isSelected)
        }

        // mCurrentContentsItemList를 ArrayList로 변환하여 방출
        viewModelScope.launch {
            _contentsList.emit(ArrayList<ContentsBaseResult>())
            _contentsList.emit(mCurrentContentsItemList)
        }

        viewModelScope.launch {
            if(isSelected)
            {
                _itemSelectedCount.emit(mCurrentContentsItemList.size)
            }
            else
            {
                _itemSelectedCount.emit(0)
            }
        }
    }
}