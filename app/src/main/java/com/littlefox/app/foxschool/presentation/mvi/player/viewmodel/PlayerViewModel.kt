package com.littlefox.app.foxschool.presentation.mvi.player.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.PlayerApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.app.foxschool.enumerate.MovieNavigationStatus
import com.littlefox.app.foxschool.enumerate.PlayerPageLineType
import com.littlefox.app.foxschool.enumerate.PlayerStatus
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorRequestData
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PageByPageData
import com.littlefox.app.foxschool.`object`.data.player.PageLineData
import com.littlefox.app.foxschool.`object`.data.player.PlayerEndViewData
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.player.PlayItemResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.player.PlayerAction
import com.littlefox.app.foxschool.presentation.mvi.player.PlayerEvent
import com.littlefox.app.foxschool.presentation.mvi.player.PlayerSideEffect
import com.littlefox.app.foxschool.presentation.mvi.player.PlayerState
import com.littlefox.app.foxschool.presentation.mvi.search.SearchSideEffect
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(val apiViewModel : PlayerApiViewModel): BaseMVIViewModel<PlayerState, PlayerEvent, SideEffect>(
    PlayerState()
)
{
    companion object
    {
        //1시간이 지나면 팝업을 띄워 확인 작업
        private const val MAX_WARNING_WATCH_MOVIE_TIME : Int    = 60 * 60 * Common.SECOND

        const val DIALOG_TYPE_WARNING_WATCH_MOVIE : Int         = 10001
        const val DIALOG_TYPE_WARNING_API_EXCEPTION : Int       = 10002
        const val DIALOG_TYPE_WARNING_RECORD_PERMISSION : Int   = 10003

        private val PLAY_SPEED_LIST = floatArrayOf(0.7f, 0.85f, 1.0f, 1.15f, 1.3f)
        private const val DEFAULT_SPEED_INDEX : Int         = 2
        private const val FINE_TUNING_PAGE_TIME : Float     = 1f
        private const val PAGE_MAX_VISIBLE_COUNT : Int          = 5
    }

    @SuppressLint("StaticFieldLeak")
    private lateinit var mContext : Context

    private var mCurrentPlayDuration : Long = 0L
    private var mCurrentPlayerStatus : PlayerStatus = PlayerStatus.STOP

    private lateinit var mPlayInformationList : ArrayList<ContentsBaseResult>
    private lateinit var mPlayerIntentParamsObject : PlayerIntentParamsObject
    private lateinit var mAuthContentResult : PlayItemResult

    private var mCurrentPlayMovieIndex : Int  = 0
    private var mSelectItemOptionIndex : Int  = 0
    private var mCurrentSaveLogIndex : Int  = 0
    private var mCurrentCaptionIndex : Int  = 0
    private var mCurrentWatchingTime : Int  = 0
    private var mCurrentOrientation : Int = 0
    private var isAuthorizationComplete : Boolean = false
    private var isRepeatOn : Boolean = false
    private var mCurrentStudyLogMilliSeconds : Float = 0f

    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mCurrentBookshelfAddResult : MyBookshelfResult

    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mPageByPageDataList : ArrayList<PageByPageData> = ArrayList<PageByPageData>()
    private var mCurrentRepeatPageIndex : Int = -1
    private var mCurrentPageIndex : Int = 0
    private var mCurrentPlaySpeedIndex : Int = DEFAULT_SPEED_INDEX
    private var mCurrentSelectItem: ContentsBaseResult? = null
    private var isVideoPrepared : Boolean = false
    private var mCoachingMarkUserDao : CoachmarkDao? = null
    private lateinit var mPlayer : ExoPlayer
    private lateinit var mLoginInformationResult : LoginInformationResult
    private var mUiUpdateTimerJob: Job? = null
    private var mWarningWatchTimerJob: Job? = null
    private var mCoachingMarkJob: Job? = null

    override fun init(context : Context)
    {
        mContext = context
        settingData()
        onHandleApiObserver()
        setupPlayVideo()
        initPlaySpeedList()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    override fun resume()
    {
        Log.f("status : $mCurrentPlayerStatus")
        resumePlayer()
    }

    override fun pause()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(mCurrentPlayerStatus != PlayerStatus.COMPELTE)
        {
            sendStudyLogSaveAsync()
        }
        pausePlayer()
    }

    override fun destroy()
    {
        Log.f("")
        enableTimer(false)
        releasePlayer()
    }

    override fun onBackPressed()
    {
        (mContext as AppCompatActivity).finish()
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect{ data ->
                    data?.let {
                        if(data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                        {
                            postSideEffect(
                                SideEffect.EnableLoading(isLoading = data.second)
                            )
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.authContentData.collect{ data ->
                    data?.let {
                        mAuthContentResult = data as PlayItemResult
                        settingPageByPageData()
                        Log.f("Data Success")
                        isAuthorizationComplete = true
                        Log.f("AuthContentPlay result OK")
                        startMovie()
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.addBookshelfContentsData.collect{ data ->
                    data?.let {
                        viewModelScope.launch(Dispatchers.Main){
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_NORMAL)
                            }
                            postSideEffect(
                                SideEffect.ShowSuccessMessage(
                                    mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                                )
                            )
                        }
                        resumePlayer()
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
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
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
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
                            if(code == RequestCode.CODE_AUTH_CONTENT_PLAY)
                            {
                                Log.f("Auth Content data error retry popup")
                                postEvent(
                                    PlayerEvent.EnableMovieLoading(false)
                                )
                                postSideEffect(
                                    PlayerSideEffect.ShowWarningAPIExceptionDialog(
                                        result.message
                                    )
                                )
                                if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                                {
                                    val errorData = ErrorRequestData(
                                        CrashlyticsHelper.ERROR_CODE_VIDEO_REQUEST,
                                        mPlayInformationList[mCurrentPlayMovieIndex].id,
                                        result.status,
                                        result.message,
                                        Exception())
                                    CrashlyticsHelper.getInstance(mContext).sendCrashlytics(errorData)
                                }
                            }
                            else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                            {
                                Log.f("FAIL ASYNC_CODE_BOOKSHELF_CONTENTS_ADD")
                                viewModelScope.launch(Dispatchers.Main){
                                    withContext(Dispatchers.IO){
                                        delay(Common.DURATION_NORMAL)
                                    }
                                    postSideEffect(
                                        SideEffect.ShowErrorMessage(result.message)
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
            is PlayerAction.ClickActionContentsType ->
            {
                checkBottomSelectItemType(action.type)
            }
            is PlayerAction.ClickControllerPlay ->
            {
                onHandlePlayButton()
            }
            is PlayerAction.ClickControllerPrev ->
            {
                onPrevButton()
            }
            is PlayerAction.ClickControllerNext ->
            {
                onNextButton()
            }
            is PlayerAction.ClickReplay ->
            {
                onReplayButton()
            }
            is PlayerAction.ClickLoadNextMovie ->
            {
                onNextMovieButton()
            }
            is PlayerAction.SelectSpeed ->
            {
                onSelectSpeed(action.index)
            }
            is PlayerAction.SelectItem ->
            {
                onSelectItem(action.index)
            }
            is PlayerAction.StartTrackingTouch ->
            {
                onStartTrackingTouch()
            }
            is PlayerAction.StopTrackingTouch ->
            {
                onStopTrackingTouch(action.progress)
            }
            is PlayerAction.ClickOption ->
            {
                onClickItemOption(action.item)
            }
            is PlayerAction.ClickPageByPageIndex ->
            {

            }
            is PlayerAction.ClickPageByPagePrev ->
            {

            }
            is PlayerAction.ClickPageByPageNext ->
            {

            }
            is PlayerAction.ChangeOrientationPortrait ->
            {

            }
            is PlayerAction.ChangeOrientationLandscape ->
            {

            }
        }
    }

    override suspend fun reduceState(current : PlayerState, event : PlayerEvent) : PlayerState
    {
       return when(event)
        {
           is PlayerEvent.EnableMovieLoading ->
           {
               current.copy(
                   isMovieLoading = event.isLoading
               )
           }
           is PlayerEvent.SetPlayer ->
            {
                current.copy(
                    player = event.player
                )
            }
           is PlayerEvent.SetTitle ->
           {
               current.copy(
                   title = event.title
               )
           }
           is PlayerEvent.NotifyContentsList ->
           {
               current.copy(
                   contentsList = event.list
               )
           }
           is PlayerEvent.SetPlayerEndViewData ->
           {
               current.copy(
                   playerEndViewData = event.data
               )
           }
           is PlayerEvent.UpdateCaptionText ->
           {
               current.copy(
                   captionText = event.text
               )
           }
           is PlayerEvent.UpdateCurrentMovieTime ->
           {
               current.copy(
                   currentMovieTime = event.time
               )
           }
           is PlayerEvent.SetMaxMovieTime ->
           {
               current.copy(
                   totalMovieTime = event.time
               )
           }
           is PlayerEvent.UpdateCurrentProgress ->
           {
               current.copy(
                   currentProgress = event.progress
               )
           }
           is PlayerEvent.SetMaxProgress ->
           {
               current.copy(
                   maxProgress = event.maxProgress
               )
           }
           is PlayerEvent.ReadyToPlayMovie ->
           {
               current.copy(
                   isReadyToPlayMovie = event.isReady
               )
           }
           is PlayerEvent.PlayMovie ->
           {
               current.copy(
                   playMovie = event.isPlaying
               )
           }
           is PlayerEvent.ShowPlayerEndView ->
           {
               current.copy(
                   showPlayerEndView = event.isShow
               )
           }
           is PlayerEvent.UpdateNavigationStatus ->
           {
               current.copy(
                   navigationStatus = event.status
               )
           }
           is PlayerEvent.UpdateCurrentPlayIndex ->
           {
               current.copy(
                   currentPlayIndex = event.index
               )
           }
           is PlayerEvent.UpdateCurrentPageLineData ->
           {
               current.copy(
                   currentPageLineData = event.data
               )
           }
           is PlayerEvent.UpdateCurrentSpeedIndex -> {
               current.copy(
                   currentSpeedIndex = event.index
               )
           }

           is PlayerEvent.UpdateCurrentPageIndex -> {
               current.copy(
                   currentPageIndex = event.index
               )
           }
           is PlayerEvent.SupportSpeedViewButton ->
           {
               current.copy(
                   supportSpeedViewButton = event.isEnable
               )
           }
           is PlayerEvent.SupportMovieOption ->
           {
               current.copy(
                   supportMovieOption = event.isEnable
               )
           }

           is PlayerEvent.SupportCaptionAndPage ->
           {
               current.copy(
                   supportCaptionAndPage = event.isSupport
               )
           }
           is PlayerEvent.ActivatePageView ->
           {
               current.copy(
                   activatePageView = event.isActivate
               )
           }
           is PlayerEvent.ActivateMovieOption ->
           {
               current.copy(
                   activateMovieOption = event.isActivate
               )
           }
       }
    }

    private val isPlaying : Boolean
        get()
        {
            Log.f("playWhenReady : " + mPlayer.getPlayWhenReady() + ", state : " + mPlayer.getPlaybackState())
            return mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY
        }


    private val isSupportCaption : Boolean
        get()
        {
            if(mAuthContentResult.getCaptionList().size > 0)
                return true
            else
                return false
        }

    /**
     * 캡션에 대한 정보 처리 타이밍인지 확인 하는 메소드
     *
     * @return
     */
    private val isTimeForCaption : Boolean
        get()
        {
            try
            {
                if(mCurrentCaptionIndex >= mAuthContentResult.getCaptionList().size
                    || mCurrentCaptionIndex == -1
                    || mAuthContentResult.getCaptionList().size <= 0)
                {
                    return false
                }
                val visibleTime : Float = mAuthContentResult.getCaptionList().get(mCurrentCaptionIndex).getStartTime()
                if(visibleTime <= mPlayer.getCurrentPosition().toFloat())
                {
                    return true
                }
            }
            catch(e : ArrayIndexOutOfBoundsException)
            {
                return false
            }
            return false
        }

    private val isTimeForPageByPage : Boolean
        get()
        {
            try
            {
                if(mCurrentPageIndex >= mPageByPageDataList.size
                    || mCurrentPageIndex == -1
                    || mPageByPageDataList.size <= 0)
                {
                    return false
                }
                val visibleTime : Float = mPageByPageDataList[mCurrentPageIndex].getStartTime()
                if(visibleTime <= mPlayer.getCurrentPosition().toFloat())
                {
                    return true
                }
            }
            catch(e : ArrayIndexOutOfBoundsException)
            {
                return false
            }
            return false
        }

    private val isEndTimeForCurrentPage : Boolean
        get()
        {
            val endTime = (mPageByPageDataList[mCurrentRepeatPageIndex].getEndTime() * FINE_TUNING_PAGE_TIME).toFloat()
            if(endTime <= mPlayer.getCurrentPosition().toFloat())
            {
                return true
            }
            else
                return false
        }

    /**
     * 데이터는 startTime 과  endTime 사이에 존재한다고 생각하여 그 중간에 있는 CaptionIndex를 가져와 리턴
     */
    /**
     * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
     */
    private val currentCaptionIndex : Int
        get()
        {
            var startTime = 0f
            var endTime = 0f
            if(mAuthContentResult.getCaptionList().size <= 0)
            {
                return -1
            }
            startTime = mAuthContentResult.getCaptionList()[0].getStartTime()
            if(startTime > mPlayer.getCurrentPosition().toFloat())
            {
                return 0
            }
            /**
             * 데이터는 startTime 과  endTime 사이에 존재한다고 생각하여 그 중간에 있는 CaptionIndex를 가져와 리턴
             */
            for(i in 0 until mAuthContentResult.getCaptionList().size)
            {
                startTime = mAuthContentResult.getCaptionList()[i].getStartTime()
                endTime = mAuthContentResult.getCaptionList()[i].getEndTime()
                if(mPlayer.getCurrentPosition().toFloat() in startTime..endTime)
                {
                    return i
                }
            }
            /**
             * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
             */
            for(i in 0 until mAuthContentResult.getCaptionList().size)
            {
                startTime = mAuthContentResult.getCaptionList()[i].getStartTime()
                if(startTime >= mPlayer.getCurrentPosition().toFloat())
                {
                    return i
                }
            }
            return -1
        }
    /**
     * 데이터는 startTime 과  endTime 사이에 존재한다고 생각하여 그 중간에 있는 CaptionIndex를 가져와 리턴
     */
    /**
     * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
     */
    private val currentPageIndex : Int
        get()
        {
            var startTime = 0f
            var endTime = 0f
            val lastItemIndex = mPageByPageDataList.size - 1
            if(lastItemIndex == -1)
            {
                return -1
            }
            startTime = mPageByPageDataList[0].getStartTime()
            if(startTime > mPlayer.getCurrentPosition().toFloat())
            {
                return -1
            }
            endTime = mPageByPageDataList[lastItemIndex].getEndTime()
            if(endTime < mPlayer.getCurrentPosition().toFloat())
            {
                return lastItemIndex
            }
            /**
             * 데이터는 startTime 과  endTime 사이에 존재한다고 생각하여 그 중간에 있는 CaptionIndex를 가져와 리턴
             */
            for(i in mPageByPageDataList.indices)
            {
                startTime = mPageByPageDataList[i].getStartTime()
                endTime = mPageByPageDataList[i].getEndTime()
                if(startTime <= mPlayer.getCurrentPosition().toFloat() && endTime >= mPlayer.getCurrentPosition().toFloat())
                {
                    Log.f("startTime : " + startTime + ", curretPosition : " + mPlayer.getCurrentPosition().toFloat() + ", endTime : " + endTime)
                    return i
                }
            }
            /**
             * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
             */
            for(i in mPageByPageDataList.indices)
            {
                startTime = mPageByPageDataList[i].getStartTime()
                if(startTime >= mPlayer.getCurrentPosition().toFloat())
                {
                    Log.f("startTime : " + startTime + ", curretPosition : " + mPlayer.getCurrentPosition().toFloat())
                    return i
                }
            }
            return -1
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
                        SearchSideEffect.ShowRecordPermissionDialog
                    )
                }
                else
                {
                    startRecordPlayerActivity()
                }
            }
            ActionContentsType.ADD_BOOKSHELF -> {
                Log.f("")
                mCurrentSelectItem?.let {
                    mSendBookshelfAddList.clear()
                    mSendBookshelfAddList.add(it)
                    postSideEffect(
                        PlayerSideEffect.ShowBookshelfContentsAddDialog(
                            mMainInformationResult.getBookShelvesList()
                        )
                    )
                }

            }
            else -> {}
        }
    }


    private fun settingData()
    {
        mPlayerIntentParamsObject = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_PLAYER_DATA_PARAMS)!!
        mPlayInformationList = mPlayerIntentParamsObject.getPlayerInformationList()
        postEvent(
            PlayerEvent.NotifyContentsList(
                mPlayInformationList
            )
        )
        Log.f("list size : ${mPlayInformationList.size} , isOptionDisable : ${mPlayInformationList[0].isOptionDisable} " + ", homeworkNumber : ${mPlayerIntentParamsObject.getHomeworkNumber()}")
        mCurrentPlayMovieIndex = 0

        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult

        // 숙제관리에서 넘어온 플레이의 경우 3점 메뉴버튼 표시하지 않도록 처리
        if (mPlayerIntentParamsObject.getHomeworkNumber() != 0)
        {
            mPlayInformationList[mCurrentPlayMovieIndex].isOptionDisable = true
        }

        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao()
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayVideo()
    {
        mPlayer = ExoPlayer.Builder(mContext).build()
        postEvent(
            PlayerEvent.SetPlayer(mPlayer)
        )

        mPlayer.addListener(object : Player.Listener
        {

            override fun onLoadingChanged(isLoading : Boolean) {}

            override fun onPlayerStateChanged(playWhenReady : Boolean, playbackState : Int)
            {
                Log.f("playWhenReady : $playWhenReady, playbackState : $playbackState")
                Log.f("Max Duration : " + mPlayer.getDuration())
                when(playbackState)
                {
                    Player.STATE_IDLE -> { }
                    Player.STATE_BUFFERING -> if(playWhenReady)
                    {
                        postEvent(
                            PlayerEvent.EnableMovieLoading(true)
                        )
                    }
                    Player.STATE_READY ->
                    {
                        if(playWhenReady)
                        {
                            postEvent(
                                PlayerEvent.EnableMovieLoading(false)
                            )
                        }
                        if(isVideoPrepared)
                        {
                            return
                        }
                        isVideoPrepared = true
                        setVideoPrepared()
                    }
                    Player.STATE_ENDED ->
                    {
                        Log.f("Play Complete")
                        if(playWhenReady)
                        {
                            postEvent(
                                PlayerEvent.EnableMovieLoading(false)
                            )
                        }
                        setVideoCompleted()
                    }
                }
            }

            override fun onPlayerError(error : PlaybackException)
            {
                Log.f("Play Error : " + error.message)
            }

            override fun onPlaybackParametersChanged(playbackParameters : PlaybackParameters) {}

        })
    }

    private fun initPlaySpeedList()
    {
        Log.f("")
        mCurrentPlaySpeedIndex = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_PLAYER_SPEED_INDEX, DataType.TYPE_INTEGER) as Int
        if(mCurrentPlaySpeedIndex == -1)
        {
            mCurrentPlaySpeedIndex = DEFAULT_SPEED_INDEX
        }
        postEvent(
            PlayerEvent.UpdateCurrentSpeedIndex(
                mCurrentPlaySpeedIndex
            )
        )
    }

    private fun setVideoPrepared()
    {
        if(mPlayInformationList[mCurrentPlayMovieIndex].type.equals(Common.CONTENT_TYPE_SONG))
        {
            setVideoSpeed(DEFAULT_SPEED_INDEX)
        }
        else
        {
            setVideoSpeed(mCurrentPlaySpeedIndex)
            postEvent(
                PlayerEvent.SupportSpeedViewButton(true)
            )
        }
        if(mCurrentPlayerStatus == PlayerStatus.COMPELTE)
        {
            return
        }
        if(mCurrentPlayerStatus == PlayerStatus.PLAY)
        {
            Log.f("init Play")
            Log.f("Max Duration : " + mPlayer.getDuration())
            Log.f("Max Progress : " + (mPlayer.getDuration() / Common.SECOND))

            postEvent(
                PlayerEvent.UpdateCurrentMovieTime("00:00"),
                PlayerEvent.SetMaxMovieTime(
                    CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.duration)
                ),
                PlayerEvent.SetMaxProgress(
                    mPlayer.duration.toInt() / Common.SECOND
                ),
                PlayerEvent.UpdateCurrentPageIndex(-1)
            )
            if(isSupportCaption)
            {
                settingCurrentLineData(
                    mPageByPageDataList[0].getCurrentIndex(),
                    mPageByPageDataList.size
                )
            }
        }
        postEvent(
            PlayerEvent.EnableMovieLoading(false),
            PlayerEvent.ReadyToPlayMovie(true),
            PlayerEvent.ActivateMovieOption(true)
        )
        mPlayer.playWhenReady = true
        enableTimer(true)
    }

    private fun setVideoCompleted()
    {
        mCurrentPlayerStatus = PlayerStatus.COMPELTE
        enableTimer(false)

        postEvent(
            PlayerEvent.UpdateCurrentProgress(
                mPlayer.duration.toInt() / Common.SECOND
            ),
            PlayerEvent.UpdateCurrentMovieTime(
                CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.duration)
            )
        )

        sendStudyLogSaveAsync()

        var nextMovieIndex = mCurrentPlayMovieIndex
        nextMovieIndex++
        if(isRepeatOn)
        {
            if(mPlayInformationList.size > 1)
            {
                if(nextMovieIndex >= mPlayInformationList.size)
                {
                    nextMovieIndex = 0
                }
            }
            else
            {
                nextMovieIndex = 0
            }
        }

        if(isRepeatOn)
        {
            Log.f("Repeat Movie Index : $nextMovieIndex, mCurrentPlayMovieIndex : $mCurrentPlayMovieIndex")
        }
        else
        {
            Log.f("Next Movie Index : $nextMovieIndex")
        }

        if(nextMovieIndex >= mPlayInformationList.size)
        {
            Log.f("ALL FULL_PLAY Complete")
            postEvent(
                PlayerEvent.ShowPlayerEndView(true)
            )
        }
        else
        {
            mCurrentPlayMovieIndex = nextMovieIndex
            viewModelScope.launch(Dispatchers.Main) {
                checkMovieTiming()
            }
        }
    }

    private fun checkMovieTiming()
    {
        Log.f("")

        var isShowCoachingMark : Boolean = false
        val type : String = mPlayInformationList[mCurrentPlayMovieIndex].type
        postEvent(
            PlayerEvent.ShowPlayerEndView(false)
        )
        prepareMovie()

        /*        mCoachingMarkJob = CoroutineScope(Dispatchers.Main).launch{
                    CoroutineScope(Dispatchers.Default).async {
                        isShowCoachingMark = isNeverSeeAgainCheck(type)
                    }.await()

                    if(isShowCoachingMark)
                    {
                        Log.f("show coachmark")
                        pausePlayer()

                        when(type)
                        {
                            Common.CONTENT_TYPE_STORY ->
                            {
                                _showStoryCoachmarkView.call()
                            }
                            else ->
                            {
                                _showSongCoachmarkView.call()
                            }
                        }
                    } else
                    {
                        Log.f("show coachmark")
                        prepareMovie()
                    }
                }*/
    }

    private fun prepareMovie()
    {
        Log.f("mCurrentPlayMovieIndex : $mCurrentPlayMovieIndex")
        val title : String = mPlayInformationList[mCurrentPlayMovieIndex].getContentsName()
        isAuthorizationComplete = false
        mCurrentCaptionIndex = 0
        isVideoPrepared = false
        mCurrentPageIndex = 0
        mCurrentRepeatPageIndex = -1
        mCurrentStudyLogMilliSeconds = 0f
        postEvent(
            PlayerEvent.ReadyToPlayMovie(false),
            PlayerEvent.UpdateCaptionText(""),
            PlayerEvent.SetTitle(title),
            PlayerEvent.EnableMovieLoading(true),
            PlayerEvent.SupportSpeedViewButton(false),
            PlayerEvent.ActivateMovieOption(false),
            PlayerEvent.UpdateCurrentPlayIndex(mCurrentPlayMovieIndex)
        )

        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }
            requestAuthContentPlay()
        }
    }

    private fun isNeverSeeAgainCheck(type : String) : Boolean
    {
        var isNeverSeeAgain = false
        val userID : String = mLoginInformationResult.getUserInformation().getFoxUserID()
        Log.f("userID : $userID, type : $type")
        if(type == Common.CONTENT_TYPE_STORY)
        {
            isNeverSeeAgain = isStoryCoachmarkViewed(userID)
            if(!isNeverSeeAgain)
            {
                return true
            }
        }
        else
        {
            isNeverSeeAgain = isSongCoachmarkViewed(userID)
            if(!isNeverSeeAgain)
            {
                return true
            }
        }
        return false
    }

    private fun isStoryCoachmarkViewed(userID : String) : Boolean
    {
        var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)
        if(data == null)
        {
            Log.f("data null story")
            return false
        }
        else
        {
            Log.f("data.isStoryCoachmarkViewed : " + data.isStoryCoachmarkViewed)
            if(data.isStoryCoachmarkViewed)
            {
                return  true
            } else
            {
                return  false
            }
        }
    }

    private fun isSongCoachmarkViewed(userID : String) : Boolean
    {
        var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)
        if(data == null)
        {
            return false
        }
        else
        {
            if(data.isSongCoachmarkViewed)
            {
                return true
            }
            else
            {
                return false
            }
        }
    }

    private fun setStoryCoachmarkViewed(userID : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)

            if(data == null)
            {
                Log.f("data null ")
                data = CoachmarkEntity(userID,
                    true,
                    false,
                    false)
                mCoachingMarkUserDao?.insertItem(data)
            }
            else
            {
                Log.f("data update  ")
                data.isStoryCoachmarkViewed = true
                mCoachingMarkUserDao?.updateItem(data)
            }
        }
    }

    private fun setSongCoachmarkViewed(userID : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)
            if(data == null)
            {
                data = CoachmarkEntity(userID,
                    false,
                    true,
                    false)
                mCoachingMarkUserDao?.insertItem(data)
            }
            else
            {
                data.isSongCoachmarkViewed = true
                mCoachingMarkUserDao?.updateItem(data)
            }
        }
    }



    private fun setVideoSpeed(speedIndex : Int)
    {
        var params : PlaybackParameters? = null
        params = PlaybackParameters(PLAY_SPEED_LIST[speedIndex])
        mPlayer.setPlaybackParameters(params)
    }


    private fun pausePlayer()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(mCurrentPlayerStatus === PlayerStatus.COMPELTE)
        {
            return
        }
        if(mPlayer != null && isPlaying)
        {
            mCurrentPlayDuration = mPlayer.getCurrentPosition()
            mPlayer.setPlayWhenReady(false)
            enableTimer(false)
            postEvent(
                PlayerEvent.PlayMovie(false)
            )
            mCurrentPlayerStatus = PlayerStatus.PAUSE
        }
    }

    private fun resumePlayer()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(mCurrentPlayerStatus === PlayerStatus.PAUSE)
        {
            mPlayer.seekTo(mCurrentPlayDuration)
            mPlayer.setPlayWhenReady(true)
            enableTimer(true)
            postEvent(
                PlayerEvent.PlayMovie(true)
            )
            mCurrentPlayerStatus = PlayerStatus.PLAY
        }
    }

    private fun releasePlayer()
    {
        postEvent(
            PlayerEvent.SetPlayer(null)
        )
        mPlayer.release()
    }

    private fun onClickItemOption(item: ContentsBaseResult)
    {
        Log.f("index : ${item.id}")
        mCurrentSelectItem = item
        postSideEffect(
            PlayerSideEffect.ShowBottomOptionDialog(
                item
            )
        )
    }

    private fun requestAuthContentPlay()
    {
        val resolutionValue : String = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IS_VIDEO_HIGH_RESOLUTION, "N")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_AUTH_CONTENT_PLAY,
            mPlayInformationList[mCurrentPlayMovieIndex].id,
            if(resolutionValue == "Y") true else false
        )
    }

    private fun requestBookshelfContentsAdd(data : ArrayList<ContentsBaseResult>)
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_ADD,
            mCurrentBookshelfAddResult.getID(),
            data
        )
    }

    private fun sendStudyLogSaveAsync()
    {
        Log.f("-------------- 학습기록 로그 호출 ------------------")
        var autoPlay = ""
        mCurrentSaveLogIndex = mCurrentPlayMovieIndex
        if(mPlayInformationList!!.size > 1 || isRepeatOn)
        {
            autoPlay = "list"
        }
        else
        {
            autoPlay = "single"
        }
        val studyLogSeconds = Math.round(mCurrentStudyLogMilliSeconds / Common.DURATION_LONG.toFloat())
        Log.f("mCurrentStudyLogMilliSeconds : $mCurrentStudyLogMilliSeconds, studyLogSeconds : $studyLogSeconds")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_PLAY_CONTENTS_LOG_SAVE,
            mPlayInformationList[mCurrentSaveLogIndex].id,
            autoPlay,
            studyLogSeconds.toString(),
            mPlayerIntentParamsObject.getHomeworkNumber()
        )
    }

    private fun updateUI()
    {
        if(isVideoPrepared == false)
        {
            return
        }

        postEvent(
            PlayerEvent.UpdateCurrentProgress(
                mPlayer.currentPosition.toInt() / Common.SECOND
            ),
            PlayerEvent.UpdateCurrentMovieTime(
                CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.currentPosition)
            )
        )

        if(isSupportCaption)
        {
            if(mCurrentRepeatPageIndex != -1)
            {
                if(isEndTimeForCurrentPage)
                {
                    mPlayer.setPlayWhenReady(false)
                    postEvent(
                        PlayerEvent.PlayMovie(false)
                    )
                    return
                }
            }
            else
            {
                if(isTimeForPageByPage == true)
                {
                    if(mCurrentPageIndex % Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE == 0)
                    {
                        settingCurrentLineData(
                            mPageByPageDataList[mCurrentPageIndex].getCurrentIndex(),
                            mPageByPageDataList.size
                        )
                    }
                    if(mCurrentPageIndex == 0)
                    {
                        postEvent(
                            PlayerEvent.ActivatePageView(true)
                        )
                    }
                    postEvent(
                        PlayerEvent.UpdateCurrentPageIndex(mCurrentPageIndex + 1)
                    )
                    mCurrentPageIndex++
                }
            }
        }
        if(isTimeForCaption == true)
        {
            postEvent(
                PlayerEvent.UpdateCaptionText(
                    mAuthContentResult.getCaptionList()[mCurrentCaptionIndex].getText()
                )
            )
            mCurrentCaptionIndex++
        }
    }


    private fun enableTimer(isStart: Boolean)
    {
        if(isStart)
        {
            mUiUpdateTimerJob = viewModelScope.launch(Dispatchers.Default) {
                while(true)
                {
                    withContext(Dispatchers.IO){
                        delay(Common.DURATION_SHORTEST)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        updateUI()
                    }
                    mCurrentStudyLogMilliSeconds += (Common.DURATION_SHORTEST * PLAY_SPEED_LIST[mCurrentPlaySpeedIndex]).toInt()
                }
            }

            mWarningWatchTimerJob = viewModelScope.launch(Dispatchers.Default) {

                while(true)
                {
                    withContext(Dispatchers.IO){
                        delay(Common.DURATION_LONG)
                    }
                    mCurrentWatchingTime += Common.SECOND
                    if(mCurrentWatchingTime >= MAX_WARNING_WATCH_MOVIE_TIME)
                    {
                        enableTimer(false)
                        mPlayer.setPlayWhenReady(false)
                        viewModelScope.launch(Dispatchers.Main) {
                            //TODO: 오랜시간 영상 시청 관련 다이얼로그 화면 노출
                        }
                    }
                }
            }
        }
        else
        {
            mUiUpdateTimerJob?.cancel()
            mUiUpdateTimerJob = null
            mWarningWatchTimerJob?.cancel()
            mWarningWatchTimerJob = null
        }
    }

    private fun settingPageByPageData()
    {
        mPageByPageDataList = ArrayList<PageByPageData>()
        var data : PageByPageData? = null
        var startCheckIndex = 0
        for(i in 1..mAuthContentResult.getPageByPageMaxCount())
        {
            data = PageByPageData(i)
            data.setStartTime(mAuthContentResult.getCaptionList().get(startCheckIndex).getStartTime())
            for(j in startCheckIndex until mAuthContentResult.getCaptionList().size)
            {
                if(data.getCurrentIndex() !== mAuthContentResult.getPageByPageMaxCount())
                {
                    if(mAuthContentResult.getCaptionList().get(j).getPageByPageIndex() > data.getCurrentIndex())
                    {
                        data.setEndTime(mAuthContentResult.getCaptionList().get(j - 1).getEndTime())
                        startCheckIndex = j
                        break
                    }
                }
                else
                {
                    data.setEndTime(mAuthContentResult.getCaptionList().get(mAuthContentResult.getCaptionList().size - 1).getEndTime())
                    startCheckIndex = mAuthContentResult.getCaptionList().size
                    break
                }
            }
            mPageByPageDataList.add(data)
            Log.f("index : " + data.getCurrentIndex().toString() + ", startTime : " + data.getStartTime().toString() + ", endTime : " + data.getEndTime().toString() + ", startCheckIndex : " + startCheckIndex)
        }
    }

    @OptIn(UnstableApi::class)
    private fun startMovie()
    {
        Log.f("mAuthContentResult.getVideoUrl() : " + mAuthContentResult.getMovieHlsUrl())
        mCurrentPlayerStatus = PlayerStatus.PLAY
        notifyPlayItemIndex()
        settingCurrentMovieStudyOption()
        val source : MediaSource = buildMediaSource(Uri.parse(mAuthContentResult.getMovieHlsUrl()))
        mPlayer.setMediaSource(source)
        mPlayer.prepare()
        postEvent(
            PlayerEvent.PlayMovie(true),
            PlayerEvent.SupportCaptionAndPage(isSupport = isSupportCaption),
            PlayerEvent.UpdateCurrentPlayIndex(mCurrentPlayMovieIndex)
        )
    }

    private fun notifyPlayItemIndex()
    {
        Log.f("list size : " + mPlayInformationList.size + ", index : " + mCurrentPlayMovieIndex)
        if(mPlayInformationList.size == 1)
        {
            postEvent(
                PlayerEvent.UpdateNavigationStatus(
                    MovieNavigationStatus.BOTH_INVISIBLE
                )
            )
            return
        }

        if(mCurrentPlayMovieIndex == 0)
        {
            postEvent(
                PlayerEvent.UpdateNavigationStatus(
                    MovieNavigationStatus.PREV_BUTTON_INVISIBLE
                )
            )
        }
        else if(mCurrentPlayMovieIndex == mPlayInformationList.size - 1)
        {
            postEvent(
                PlayerEvent.UpdateNavigationStatus(
                    MovieNavigationStatus.NEXT_BUTTON_INVISIBLE
                )
            )
        }
        else
        {
            postEvent(
                PlayerEvent.UpdateNavigationStatus(
                    MovieNavigationStatus.NORMAL
                )
            )
        }
    }

    private fun settingCurrentMovieStudyOption()
    {
        var playerEndViewData = PlayerEndViewData()
        val data : ContentsBaseResult = mPlayInformationList[mCurrentPlayMovieIndex]

        if(mAuthContentResult.getNextContentData() != null && mPlayInformationList.size <= 1)
        {
            playerEndViewData.isNextButtonVisible = true
        }
        if(mPlayerIntentParamsObject.getHomeworkNumber() != 0)
        {
            Log.i("HomeworkNumber : ${mPlayerIntentParamsObject.getHomeworkNumber()}")
            playerEndViewData.run {
                isEbookAvailable = false
                isQuizAvailable = false
                isVocabularyAvailable = false
                isFlashcardAvailable = false
                isStarwordsAvailable = false
                isCrosswordAvailable = false
                isTranslateAvailable = false
                isNextButtonVisible = false
            }
        }
        else
        {
            Log.i("serviceInfo : ${data.service_info.toString()}")
            if(data.service_info?.ebook.equals(Common.SERVICE_NOT_SUPPORTED) || !Feature.IS_SUPPORT_EBOOK)
            {
                playerEndViewData.isEbookAvailable = false
            }

            if(data.service_info?.quiz.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isQuizAvailable = false
            }
            if(data.service_info?.vocabulary.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isVocabularyAvailable = false
            }
            if(data.service_info?.flash_card.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isFlashcardAvailable = false
            }
            if(data.service_info?.starwords.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isStarwordsAvailable = false
            }
            if(data.service_info?.crossword.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isCrosswordAvailable = false
            }
            if(data.service_info?.original_text.equals(Common.SERVICE_NOT_SUPPORTED))
            {
                playerEndViewData.isTranslateAvailable = false
            }
        }

        postEvent(
            if(mPlayInformationList[mCurrentPlayMovieIndex].isOptionDisable)
            {
                PlayerEvent.SupportMovieOption(false)
            }
            else
            {
                PlayerEvent.SupportMovieOption(true)
            },
            PlayerEvent.SetPlayerEndViewData(playerEndViewData)
        )
    }

    /**
     * 현재의 페이지 라인의 첫번째 인덱스를 리턴한다. (예: 1,2,3,4,5 면 1를 리턴, 6,7,8,9,10 이면 6을 리턴)
     * @return 현재 라인의 첫번째 인덱스
     */
    private fun getFirstIndexOfCurrentPageLine(pageIndex : Int) : Int
    {
        return pageIndex / Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE * Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE
    }


    @OptIn(UnstableApi::class)
    private fun buildMediaSource(uri : Uri) : MediaSource
    {
        val userAgent = Util.getUserAgent(mContext, Common.PACKAGE_NAME)
        val mediaItem = MediaItem.fromUri(uri)
        return if(uri.lastPathSegment!!.contains("mp4"))
        {
            ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory().setUserAgent(userAgent)).createMediaSource(mediaItem)
        }
        else if(uri.lastPathSegment!!.contains("m3u8"))
        {
            HlsMediaSource.Factory(DefaultHttpDataSource.Factory().setUserAgent(userAgent)).createMediaSource(mediaItem)
        }
        else
        {
            ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory().setUserAgent(userAgent)).createMediaSource(mediaItem)
        }
    }


    private fun startEbookActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startQuizActivity()
    {
        Log.f("")
        var quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].id)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        val title = mPlayInformationList[mSelectItemOptionIndex].getVocabularyName()
        val myVocabularyResult = MyVocabularyResult(
            mPlayInformationList[mSelectItemOptionIndex].id,
            title,
            VocabularyType.VOCABULARY_CONTENTS)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mPlayInformationList[mSelectItemOptionIndex].id)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mPlayInformationList[mSelectItemOptionIndex].id,
            mPlayInformationList[mSelectItemOptionIndex].name,
            mPlayInformationList[mSelectItemOptionIndex].sub_name,
            VocabularyType.VOCABULARY_CONTENTS
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 녹음기 화면으로 이동
     */
    private fun startRecordPlayerActivity()
    {
        Log.f("")
        val recordIntentParamsObject = RecordIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex])

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun onStartTrackingTouch()
    {
        enableTimer(false)
        mCurrentRepeatPageIndex = -1
        postEvent(
            PlayerEvent.UpdateCaptionText("")
        )
    }

    private fun onStopTrackingTouch(progress: Int)
    {
        mPlayer.seekTo((progress * Common.SECOND).toLong())
        mCurrentCaptionIndex = currentCaptionIndex
        val pageIndex = currentPageIndex
        Log.f("progress : $progress, pageIndex : $mCurrentPageIndex")
        if(pageIndex != -1)
        {
            mCurrentPageIndex = pageIndex
            val pageFirstNumber = getFirstIndexOfCurrentPageLine(mCurrentPageIndex) + 1
            settingCurrentLineData(
                pageFirstNumber,
                mPageByPageDataList.size
            )
            postEvent(
                PlayerEvent.ActivatePageView(true),
                PlayerEvent.UpdateCurrentPageIndex(mCurrentPageIndex + 1)
            )
        }
        else
        {
            mCurrentPageIndex = 0
            postEvent(
                PlayerEvent.ActivatePageView(false)
            )
        }
        enableTimer(true)
    }

    private fun onHandlePlayButton()
    {
        if(isPlaying)
        {
            Log.f("pause video")
            mPlayer.setPlayWhenReady(false)
            postEvent(
                PlayerEvent.PlayMovie(false)
            )
        }
        else
        {
            Log.f("play video")
            if(mCurrentRepeatPageIndex != -1)
            {
                mCurrentRepeatPageIndex = -1
            }
            mPlayer.setPlayWhenReady(true)
            postEvent(
                PlayerEvent.PlayMovie(true)
            )
        }
    }

    private fun onNextButton()
    {
        Log.f("")
        mCurrentPlayMovieIndex++
        enableTimer(false)
        sendStudyLogSaveAsync()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    private fun onPrevButton()
    {
        mCurrentPlayMovieIndex--
        enableTimer(false)
        sendStudyLogSaveAsync()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    private fun onReplayButton()
    {
        Log.f("")
        enableTimer(false)
        postEvent(
            PlayerEvent.ShowPlayerEndView(false)
        )
        prepareMovie()
    }

    private fun onNextMovieButton()
    {
        Log.f("")
        if(mAuthContentResult.getNextContentData() == null)
        {
            return
        }
        else
        {
            mPlayInformationList[0] = mAuthContentResult.getNextContentData()!!
        }
        enableTimer(false)
        postEvent(
            PlayerEvent.ShowPlayerEndView(false)
        )
        prepareMovie()
    }

    private fun onSelectSpeed(index : Int)
    {
        Log.f("speed : " + PLAY_SPEED_LIST[index])
        mCurrentPlaySpeedIndex = index
        setVideoSpeed(mCurrentPlaySpeedIndex)
        postEvent(
            PlayerEvent.UpdateCurrentSpeedIndex(mCurrentPlaySpeedIndex)
        )
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_PLAYER_SPEED_INDEX, index)
    }
    private fun onSelectItem(position: Int)
    {
        Log.f("List select Index : $position")
        mCurrentPlayMovieIndex = position
        enableTimer(false)
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    private fun settingCurrentLineData(startIndex: Int, maxPageCount: Int)
    {
        Log.f("startIndex : $startIndex,  maxPageCount : $maxPageCount")
        var index = 0
        val maxItemCountInLine = getCurrentPageCountInLine(startIndex, maxPageCount)
        val pageLineType : PlayerPageLineType
        val pageTagList : MutableList<Int> = mutableListOf(-1, -1, -1, -1, -1)
        for(i in startIndex until startIndex + maxItemCountInLine)
        {
            pageTagList[index] = i
            index++
        }

        if(startIndex == 1)
        {
            pageLineType = PlayerPageLineType.FIRST_LINE
        }
        else
        {
            pageLineType = if(((maxItemCountInLine < PAGE_MAX_VISIBLE_COUNT) || (startIndex + maxItemCountInLine - 1 == maxPageCount)))
            {
                PlayerPageLineType.LAST_LINE
            } else
            {
                PlayerPageLineType.NORMAL
            }
        }

        postEvent(
            PlayerEvent.UpdateCurrentPageLineData(
                PageLineData(
                    pageLineType,
                    pageTagList
                )
            )
        )
    }

    private fun getCurrentPageCountInLine(startIndex : Int, maxPageCount : Int) : Int
    {
        val pageIndex = startIndex - 1
        if(pageIndex + PAGE_MAX_VISIBLE_COUNT < maxPageCount)
        {
            return PAGE_MAX_VISIBLE_COUNT
        }
        else
        {
            return maxPageCount - pageIndex
        }
    }
}