package com.littlefox.app.foxschool.presentation.mvi.series_contents_list.viewmodel

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
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
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
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListAction
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListEvent
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListSideEffect
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListState
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SeriesContentsListViewModel @Inject constructor(private val apiViewModel : SeriesContentsListApiViewModel): BaseMVIViewModel<SeriesContentsListState, SeriesContentsListEvent, SideEffect>(
    SeriesContentsListState()
)
{
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
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                        {
                            if(data.second)
                            {
                                Log.i("isLoading = true")
                                postSideEffect(
                                    SideEffect.EnableLoading(true)
                                )
                            }
                            else
                            {
                                Log.i("isLoading = false")
                                postSideEffect(
                                    SideEffect.EnableLoading(false)
                                )
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.storyContentsListData.collect { data ->
                    data?.let {
                        Log.i("data size : ${data.getContentsList().size}")
                        settingDetailView(data)
                        initContentsList()
                        checkLastWatchContents()

                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.songContentsListData.collect { data ->
                    data?.let {
                        settingDetailView(data)
                        initContentsList()
                        checkLastWatchContents()
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.addBookshelfContentsData.collect{ data ->
                    data?.let {
                        updateBookshelfData(it)
                        checkSelectedItemAll(
                            isSelected = false
                        )
                        withContext(Dispatchers.Main)
                        {
                            delay(Common.DURATION_NORMAL)
                            postSideEffect(
                                SideEffect.ShowSuccessMessage(
                                    mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                                )
                            )
                        }
                    }

                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        if(result.isDuplicateLogin)
                        {
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
                            )
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
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
                            )
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
                            if(code == RequestCode.CODE_CONTENTS_STORY_LIST ||
                                code == RequestCode.CODE_CONTENTS_SONG_LIST)
                            {
                                postSideEffect(
                                    SideEffect.ShowToast(
                                        result.message
                                    )
                                )
                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    (mContext as AppCompatActivity).finish()
                                }
                            }
                            else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                            {
                                postSideEffect(
                                    SideEffect.EnableLoading(false)
                                )
                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    postSideEffect(
                                        SideEffect.ShowErrorMessage(
                                            result.message
                                        )
                                    )
                                }
                            }
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
            is SeriesContentsListAction.ClickBottomBarMenu ->
            {
                when(action.menu)
                {
                    ContentsListBottomBarMenu.SELECT_ALL ->
                    {
                        checkSelectedItemAll(
                            isSelected = true
                        )
                    }
                    ContentsListBottomBarMenu.SELECT_PLAY ->
                    {
                        startSelectedListMovieActivity()
                    }
                    ContentsListBottomBarMenu.BOOKSHELF_ADD ->
                    {
                        showAddContentsDialog()
                    }
                    ContentsListBottomBarMenu.CANCEL ->
                    {
                        checkSelectedItemAll(
                            isSelected = false
                        )
                    }
                    else -> {}
                }
            }
            is SeriesContentsListAction.ClickBottomContentsType ->
            {
                viewModelScope.launch {
                    withContext(Dispatchers.Main)
                    {
                        delay(Common.DURATION_NORMAL)
                    }
                    checkBottomSelectItemType(action.type)
                }
            }
            is SeriesContentsListAction.SelectedItem ->
            {
                onSelectItem(action.index)
            }
            is SeriesContentsListAction.ClickThumbnail ->
            {
                mCurrentSelectItem = action.item
                startCurrentSelectMovieActivity()
            }
            is SeriesContentsListAction.ClickOption ->
            {
                mCurrentSelectItem = action.item
                postSideEffect(
                    SeriesContentsListSideEffect.ShowBottomOptionDialog(
                        action.item
                    )
                )
            }
            is SeriesContentsListAction.AddContentsInBookshelf ->
            {
                executeAddContentsInBookshelf(action.index)
            }
        }
    }

    override suspend fun reduceState(current : SeriesContentsListState, event : SeriesContentsListEvent) : SeriesContentsListState
    {
        return when(event)
        {
            is SeriesContentsListEvent.SetTitle ->
            {
                current.copy(
                    title = event.title
                )
            }
            is SeriesContentsListEvent.NotifyContentsList ->
            {
                current.copy(
                    contentsList = event.list
                )
            }
            is SeriesContentsListEvent.SetSeriesData ->
            {
                current.copy(
                    seriesViewData = event.data
                )
            }
            is SeriesContentsListEvent.SetBackgroundViewData ->
            {
                current.copy(
                    backgroundViewData = event.data
                )
            }
            is SeriesContentsListEvent.SelectItemCount ->
            {
                current.copy(
                    selectItemCount = event.count
                )
            }
            is SeriesContentsListEvent.EnableSingleSeries ->
            {
                current.copy(
                    isSingleSeries = event.isSingleSeries
                )
            }
            is SeriesContentsListEvent.EnableContentsLoading ->
            {
                current.copy(
                    isContentsLoading = event.isLoading
                )
            }
            is SeriesContentsListEvent.EnableInformationTooltip ->
            {
                current.copy(
                    isShowInformationTooltip = event.isHaveInformationTooltip
                )
            }


        }
    }

    private fun prepareUI()
    {
        postEvent(
            SeriesContentsListEvent.SetTitle(
                mCurrentSeriesBaseResult.getSeriesName()
            )
        )
        postSideEffect(
            SeriesContentsListSideEffect.SetStatusBarColor(
                mCurrentSeriesBaseResult.statusBarColor
            )
        )


        val data = TopThumbnailViewData(
            thumbnail = mCurrentSeriesBaseResult.getThumbnailUrl(),
            titleColor = mCurrentSeriesBaseResult.titleColor,
            transitionType = mCurrentSeriesBaseResult.getTransitionType()
        )

        postEvent(
            SeriesContentsListEvent.SetBackgroundViewData(
                data
            ),
            SeriesContentsListEvent.EnableContentsLoading(
                true
            )
        )
    }

    private val lastStudyMovieIndex : Int
        get()
        {
            var result = 0
            for(i in 0 until mDetailItemInformationResult.getContentsList().size)
            {
                if(mDetailItemInformationResult.lastStudyContentID == mDetailItemInformationResult.getContentsList()[i].id)
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

    private fun settingDetailView(data : DetailItemInformationResult)
    {
        mDetailItemInformationResult = data
        var viewData: SeriesViewData

        if(mDetailItemInformationResult.isSingleSeries == false
            && mCurrentSeriesBaseResult.getSeriesType().equals(Common.CONTENT_TYPE_STORY))
        {
            postEvent(
                SeriesContentsListEvent.EnableInformationTooltip(true)
            )
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

        postEvent(
            SeriesContentsListEvent.SetSeriesData(viewData),
            SeriesContentsListEvent.EnableSingleSeries(mDetailItemInformationResult.isSingleSeries)
        )

        if(mDetailItemInformationResult.seriesID != "")
        {
            if(!mDetailItemInformationResult.isSingleSeries && mDetailItemInformationResult.isSingleSeries)
            {
                isStillOnSeries = true
            }
        }

        postEvent(
            SeriesContentsListEvent.EnableContentsLoading(false)
        )
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
        postEvent(
            SeriesContentsListEvent.NotifyContentsList(mCurrentContentsItemList)
        )
    }

    private fun checkLastWatchContents()
    {
        if(mDetailItemInformationResult.lastStudyContentID != "")
        {
            val loginInformationResult : LoginInformationResult =
                CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            val resultIndex = lastStudyMovieIndex
            postSideEffect(
                SideEffect.ShowSuccessMessage(
                    "${loginInformationResult.getUserInformation().getName()}님은 현재 $resultIndex 까지 학습 했어요."
                )
            )
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
                    postSideEffect(
                        SeriesContentsListSideEffect.ShowRecordPermissionDialog
                    )
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
                    postSideEffect(
                        SeriesContentsListSideEffect.ShowBookshelfContentsAddDialog(
                            mMainInformationResult.getBookShelvesList()
                        )
                    )
                }
            }
            else -> {}
        }
    }

    private fun showAddContentsDialog()
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
            postSideEffect(
                SeriesContentsListSideEffect.ShowBookshelfContentsAddDialog(
                    mMainInformationResult.getBookShelvesList()
                )
            )
        } else
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(
                    mContext.resources.getString(R.string.message_not_add_selected_contents_bookshelf)
                )
            )
        }
    }


    private fun executeAddContentsInBookshelf(index : Int)
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
    private fun startSelectedListMovieActivity()
    {
        val sendItemList = getSelectedItemList()

        if(sendItemList.isNotEmpty())
        {
            val playerIntentParamsObject = PlayerIntentParamsObject(sendItemList)
            if(isStillOnSeries)
            {
                sendItemList.reverse()
            }
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerIntentParamsObject)
                .startActivity()
        }
        else
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(
                    mContext.resources.getString(R.string.message_not_selected_contents_list)
                )
            )
        }
    }

    private fun startCurrentSelectMovieActivity()
    {
        mCurrentSelectItem?.let { item ->
            Log.f("Movie  : " + item.toString())
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSelectItem(index : Int)
    {
        // 현재 선택 상태를 반전
        mCurrentContentsItemList.forEachIndexed{ position, item ->
            if(position == index)
            {
                item.isSelected = !item.isSelected
            }
        }
        // mCurrentContentsItemList를 ArrayList로 변환하여 방출
        postEvent(
            SeriesContentsListEvent.NotifyContentsList(mCurrentContentsItemList)
        )
        Log.i("index : $index , isSelected : ${mCurrentContentsItemList[index].isSelected}")
        sendSelectedItem()
    }

    private fun getSelectedItemList() : ArrayList<ContentsBaseResult>
    {
        return ArrayList(mCurrentContentsItemList.filter {
            it.isSelected
        })
    }

    private fun sendSelectedItem()
    {
        val selectedItemCount = mCurrentContentsItemList.count { it.isSelected }
        postEvent(
            SeriesContentsListEvent.SelectItemCount(selectedItemCount)
        )
    }

    private fun checkSelectedItemAll(isSelected : Boolean)
    {
        mCurrentContentsItemList.forEach {
            it.isSelected = isSelected
        }

        // mCurrentContentsItemList를 ArrayList로 변환하여 방출
        postEvent(
            SeriesContentsListEvent.NotifyContentsList(mCurrentContentsItemList),
            if(isSelected)
            {
                SeriesContentsListEvent.SelectItemCount(mCurrentContentsItemList.size)
            }
            else
            {
                SeriesContentsListEvent.SelectItemCount(0)
            }
        )
    }
}