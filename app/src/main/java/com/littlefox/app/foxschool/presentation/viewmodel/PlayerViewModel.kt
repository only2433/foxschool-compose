package com.littlefox.app.foxschool.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.PlayerApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel.Companion
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Event
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.app.foxschool.enumerate.MovieNavigationStatus
import com.littlefox.app.foxschool.enumerate.PlayerStatus
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorRequestData
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PageByPageData
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
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.player.PlayerEvent
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(val apiViewModel : PlayerApiViewModel) : BaseViewModel()
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
    }

    private val _player = SingleLiveEvent<SimpleExoPlayer?>()
    val player: LiveData<SimpleExoPlayer?> get() = _player

    private val _contentsList = SingleLiveEvent<ArrayList<ContentsBaseResult>>()
    val contentsList: LiveData<ArrayList<ContentsBaseResult>> get() = _contentsList

    private val _isMovieLoading = SingleLiveEvent<Boolean>()
    val isMovieLoading: LiveData<Boolean> get() = _isMovieLoading

    private val _settingSpeedTextLayout = SingleLiveEvent<Pair<Int, Boolean>>()
    val settingSpeedTextLayout: LiveData<Pair<Int, Boolean>> get() = _settingSpeedTextLayout

    private val _showStoryCoachmarkView = SingleLiveEvent<Void>()
    val showStoryCoachmarkView: LiveData<Void> get() = _showStoryCoachmarkView

    private val _showSongCoachmarkView = SingleLiveEvent<Void>()
    val showSongCoachmarkView: LiveData<Void> get() = _showSongCoachmarkView

    private val _setMovieTitle = SingleLiveEvent<String>()
    val setMovieTitle: LiveData<String> get() = _setMovieTitle

    private val _setCaptionText = SingleLiveEvent<String>()
    val setCaptionText: LiveData<String> get() = _setCaptionText

    private val _setRemainMovieTime = SingleLiveEvent<String>()
    val setRemainMovieTime: LiveData<String> get() = _setRemainMovieTime

    private val _setCurrentMovieTime = SingleLiveEvent<String>()
    val setCurrentMovieTime: LiveData<String> get() = _setCurrentMovieTime

    private val _setSeekProgress = SingleLiveEvent<Int>()
    val setSeekProgress: LiveData<Int> get() = _setSeekProgress

    private val _setMaxProgress = SingleLiveEvent<Int>()
    val setMaxProgress: LiveData<Int> get() = _setMaxProgress

    private val _isCompleteToReadyMovie = SingleLiveEvent<Boolean>()
    val isReadyToMovie: LiveData<Boolean> get() = _isCompleteToReadyMovie

    private val _enablePlayMovie = SingleLiveEvent<Boolean>()
    val enablePlayMovie: LiveData<Boolean> get() = _enablePlayMovie

    private val _showPlayerEndView = SingleLiveEvent<Boolean>()
    val showPlayerEndView: LiveData<Boolean> get() = _showPlayerEndView

    private val _settingPlayerEndView = SingleLiveEvent<PlayerEndViewData>()
    val settingPlayerEndView: LiveData<PlayerEndViewData> get() = _settingPlayerEndView

    private val _movieNavigationStatus = SingleLiveEvent<MovieNavigationStatus>()
    val movieNavigationStatus: LiveData<MovieNavigationStatus> get() = _movieNavigationStatus

    private val _supportCaptionAndPage = SingleLiveEvent<Boolean>()
    val supportCaptionAndPage: LiveData<Boolean> get() = _supportCaptionAndPage

    private val _scrollPosition = SingleLiveEvent<Int>()
    val scrollPosition: LiveData<Int> get() = _scrollPosition

    private val _setCurrentPageLine = SingleLiveEvent<Pair<Int, Int>>()
    val setCurrentPageLine: LiveData<Pair<Int, Int>> get() = _setCurrentPageLine

    private val _setCurrentPage = SingleLiveEvent<Int>()
    val setCurrentPage: LiveData<Int> get() = _setCurrentPage

    private val _currentPlayIndex = SingleLiveEvent<Int>()
    val currentPlayIndex: LiveData<Int> get() = _currentPlayIndex

    private val _activatePageView = SingleLiveEvent<Boolean>()
    val activatePageView: LiveData<Boolean> get() = _activatePageView

    private val _enableSpeedButton = SingleLiveEvent<Boolean>()
    val enableSpeedButton: LiveData<Boolean> get() = _enableSpeedButton

    private val _enablePortraitOptionButton = SingleLiveEvent<Boolean>()
    val enablePortraitOptionButton: LiveData<Boolean> get() = _enablePortraitOptionButton

    private val _availableMovieOptionButton = SingleLiveEvent<Boolean>()
    val availableMovieOptionButton: LiveData<Boolean> get() = _availableMovieOptionButton

    private val _currentPlaySpeedIndex = SingleLiveEvent<Int>()
    val currentPlaySpeedIndex: LiveData<Int> get() = _currentPlaySpeedIndex

    private val _dialogWarningWatchingMovie = SingleLiveEvent <Void>()
    val dialogWarningWatchingMovie: LiveData<Void> get() = _dialogWarningWatchingMovie

    private val _dialogWarningAPIException = SingleLiveEvent <String>()
    val dialogWarningAPIException: LiveData<String> get() = _dialogWarningAPIException

    private val _dialogWarningRecordPermission = SingleLiveEvent <Void>()
    val dialogWarningRecordPermission: LiveData<Void> get() = _dialogWarningRecordPermission

    private val _dialogBottomOption = SingleLiveEvent <ContentsBaseResult>()
    val dialogBottomOption: LiveData<ContentsBaseResult> get() = _dialogBottomOption

    private val _dialogBottomBookshelfContentAdd = SingleLiveEvent<ArrayList<MyBookshelfResult>>()
    val dialogBottomBookshelfContentAdd: LiveData<ArrayList<MyBookshelfResult>> get() = _dialogBottomBookshelfContentAdd


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

    private var isVideoPrepared : Boolean = false
    private var mCoachingMarkUserDao : CoachmarkDao? = null
    private lateinit var mPlayer : SimpleExoPlayer
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

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.onBackPressed ->
            {
                (mContext as AppCompatActivity).finish()
            }

            is PlayerEvent.onStartTrackingTouch ->
            {
                onStartTrackingTouch()
            }

            is PlayerEvent.onStopTrackingTouch ->
            {
                onStopTrackingTouch(event.progress)
            }

            is PlayerEvent.onClickControllerPlay ->
            {
                onHandlePlayButton()
            }
            is PlayerEvent.onClickControllerPrev ->
            {
                onPrevButton()
            }
            is PlayerEvent.onClickControllerNext ->
            {
                onNextButton()
            }
            is PlayerEvent.onClickReplay ->
            {
                onReplayButton()
            }
            is PlayerEvent.onSelectSpeed ->
            {
                onSelectSpeed(event.index)
            }
            is PlayerEvent.onSelectItem ->
            {
                onSelectItem(event.index)
            }
        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect{ data ->
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
                            _successMessage.value = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
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
                            if(code == RequestCode.CODE_AUTH_CONTENT_PLAY)
                            {
                                Log.f("Auth Content data error retry popup")
                                _isMovieLoading.value = false
                                _dialogWarningAPIException.value = result.message
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
                                    _errorMessage.value = result.message
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
        Log.f("status : $mCurrentPlayerStatus")
        resumePlayer()
    }

    override fun pause()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(mCurrentPlayerStatus !== PlayerStatus.COMPELTE)
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


    private fun settingData()
    {
        mPlayerIntentParamsObject = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_PLAYER_DATA_PARAMS)!!
        mPlayInformationList = mPlayerIntentParamsObject.getPlayerInformationList()
        _contentsList.value = mPlayInformationList
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

    private fun setupPlayVideo()
    {
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext.applicationContext)
        _player.value = mPlayer

        mPlayer.addListener(object : Player.EventListener
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
                        _isMovieLoading.value = true
                    }
                    Player.STATE_READY ->
                    {
                        if(playWhenReady)
                        {
                            _isMovieLoading.value = false
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
                            _isMovieLoading.value = false
                        }
                        setVideoCompleted()
                    }
                }
            }

            override fun onPlayerError(error : ExoPlaybackException)
            {
                Log.f("Play Error : " + error.message)
            }

            override fun onPlaybackParametersChanged(playbackParameters : PlaybackParameters) {}

            override fun onSeekProcessed()
            {
                Log.f("Max Duration : " + mPlayer.getDuration())
            }
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
        _currentPlaySpeedIndex.value = mCurrentPlaySpeedIndex
    }

    private fun setVideoPrepared()
    {
        if(mPlayInformationList[mCurrentPlayMovieIndex].type.equals(Common.CONTENT_TYPE_SONG))
        {
            setVideoSpeed(DEFAULT_SPEED_INDEX)
            _settingSpeedTextLayout.value = Pair(DEFAULT_SPEED_INDEX, true)
        }
        else
        {
            setVideoSpeed(mCurrentPlaySpeedIndex)
            _settingSpeedTextLayout.value = Pair(mCurrentPlaySpeedIndex, true)
            _enableSpeedButton.value = true
        }
        if(mCurrentPlayerStatus === PlayerStatus.COMPELTE)
        {
            return
        }
        if(mCurrentPlayerStatus === PlayerStatus.PLAY)
        {
            Log.f("init Play")
            Log.f("Max Duration : " + mPlayer.getDuration())
            Log.f("Max Progress : " + (mPlayer.getDuration() / Common.SECOND))

            _setCurrentMovieTime.value = "00:00"
            _setRemainMovieTime.value = CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.getDuration())
            _setMaxProgress.value = (mPlayer.getDuration().toInt() / Common.SECOND)
            if(isSupportCaption)
            {
                _setCurrentPageLine.value = Pair(mPageByPageDataList[0].getCurrentIndex(), mPageByPageDataList.size)
            }
            _setCurrentPage.value = -1
        }
        _isMovieLoading.value = false
        _isCompleteToReadyMovie.value = true
        _enablePortraitOptionButton.value = true
        mPlayer.setPlayWhenReady(true)
        enableTimer(true)
    }

    private fun setVideoCompleted()
    {
        mCurrentPlayerStatus = PlayerStatus.COMPELTE
        enableTimer(false)
        _setSeekProgress.value = mPlayer.getDuration().toInt() / Common.SECOND
        _setCurrentMovieTime.value = CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.getDuration())
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
            _showPlayerEndView.value = true
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
        _showPlayerEndView.value = false

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
        _setCaptionText.value = ""
        _setMovieTitle.value = title
        _isCompleteToReadyMovie.value = false
        _isMovieLoading.value = true
        _enableSpeedButton.value = false
        _enablePortraitOptionButton.value = false
        _currentPlayIndex.value = mCurrentPlayMovieIndex

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
            _enablePlayMovie.value = false
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
            _enablePlayMovie.value = true
            mCurrentPlayerStatus = PlayerStatus.PLAY
        }
    }

    private fun releasePlayer()
    {
        _player.value = null
        mPlayer.release()
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
        _setSeekProgress.value = mPlayer.getCurrentPosition().toInt() / Common.SECOND
        _setCurrentMovieTime.value = CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.getCurrentPosition())

        if(isSupportCaption)
        {
            if(mCurrentRepeatPageIndex != -1)
            {
                if(isEndTimeForCurrentPage)
                {
                    mPlayer.setPlayWhenReady(false)
                    _enablePlayMovie.value = false
                    return
                }
            }
            else
            {
                if(isTimeForPageByPage == true)
                {
                    if(mCurrentPageIndex % Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE == 0)
                    {
                        _setCurrentPageLine.value = Pair(mPageByPageDataList[mCurrentPageIndex].getCurrentIndex(), mPageByPageDataList.size)
                    }
                    if(mCurrentPageIndex == 0)
                    {
                        _activatePageView.value = true
                    }
                    _setCurrentPage.value = mCurrentPageIndex + 1
                    mCurrentPageIndex++
                }
            }
        }
        if(isTimeForCaption == true)
        {
            _setCaptionText.value = mAuthContentResult.getCaptionList()[mCurrentCaptionIndex].getText()
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

    private fun startMovie()
    {
        Log.f("mAuthContentResult.getVideoUrl() : " + mAuthContentResult.getMovieHlsUrl())
        mCurrentPlayerStatus = PlayerStatus.PLAY
        notifyPlayItemIndex()
        settingCurrentMovieStudyOption()
        val source : MediaSource = buildMediaSource(Uri.parse(mAuthContentResult.getMovieHlsUrl()))
        mPlayer.prepare(source, true, false)
       // _PlayerView.requestFocus() TODO
        _enablePlayMovie.value = true
        _supportCaptionAndPage.value = isSupportCaption
        _scrollPosition.value = mCurrentPlayMovieIndex
    }

    private fun notifyPlayItemIndex()
    {
        Log.f("list size : " + mPlayInformationList.size + ", index : " + mCurrentPlayMovieIndex)
        if(mPlayInformationList.size == 1)
        {
            _movieNavigationStatus.value = MovieNavigationStatus.BOTH_INVISIBLE
            return
        }

        if(mCurrentPlayMovieIndex == 0)
        {
            _movieNavigationStatus.value = MovieNavigationStatus.PREV_BUTTON_INVISIBLE
        }
        else if(mCurrentPlayMovieIndex == mPlayInformationList.size - 1)
        {
            _movieNavigationStatus.value = MovieNavigationStatus.NEXT_BUTTON_INVISIBLE
        }
        else
        {
            _movieNavigationStatus.value = MovieNavigationStatus.NORMAL
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

        if(mPlayInformationList[mCurrentPlayMovieIndex].isOptionDisable)
        {
            _availableMovieOptionButton.value = false
        }
        else
        {
            _availableMovieOptionButton.value = true
        }
        _settingPlayerEndView.value = playerEndViewData
    }

    /**
     * 현재의 페이지 라인의 첫번째 인덱스를 리턴한다. (예: 1,2,3,4,5 면 1를 리턴, 6,7,8,9,10 이면 6을 리턴)
     * @return 현재 라인의 첫번째 인덱스
     */
    private fun getFirstIndexOfCurrentPageLine(pageIndex : Int) : Int
    {
        return pageIndex / Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE * Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE
    }


    private fun buildMediaSource(uri : Uri) : MediaSource
    {
        val userAgent = Util.getUserAgent(mContext, Common.PACKAGE_NAME)
        return if(uri.lastPathSegment!!.contains("mp4"))
        {
            ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent)).createMediaSource(uri)
        }
        else if(uri.lastPathSegment!!.contains("m3u8"))
        {
            HlsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent)).createMediaSource(uri)
        }
        else
        {
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(mContext, userAgent)).createMediaSource(uri)
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

    private fun startQuizAcitiviy()
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
        _setCaptionText.value = ""
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
            _setCurrentPageLine.value = Pair(pageFirstNumber, mPageByPageDataList.size)
            _activatePageView.value = true
            _setCurrentPage.value = mCurrentPageIndex + 1
        }
        else
        {
            mCurrentPageIndex = 0
            _activatePageView.value = false
        }
        enableTimer(true)
    }

    private fun onHandlePlayButton()
    {
        if(isPlaying)
        {
            Log.f("pause video")
            mPlayer.setPlayWhenReady(false)
            _enablePlayMovie.value = false
        }
        else
        {
            Log.f("play video")
            if(mCurrentRepeatPageIndex != -1)
            {
                mCurrentRepeatPageIndex = -1
            }
            mPlayer.setPlayWhenReady(true)
            _enablePlayMovie.value = true
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
        _showPlayerEndView.value = false
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
        _showPlayerEndView.value = false
        prepareMovie()
    }

    private fun onSelectSpeed(index : Int)
    {
        Log.f("speed : " + PLAY_SPEED_LIST[index])
        mCurrentPlaySpeedIndex = index
        _currentPlaySpeedIndex.value = mCurrentPlaySpeedIndex
        setVideoSpeed(mCurrentPlaySpeedIndex)
        _settingSpeedTextLayout.value = Pair(mCurrentPlaySpeedIndex, true)
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

}