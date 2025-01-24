package com.littlefox.app.foxschool.api.viewmodel.factory

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.PlayerListAdapter
import com.littlefox.app.foxschool.adapter.PlayerSpeedListAdapter
import com.littlefox.app.foxschool.adapter.listener.PlayerEventListener
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.PlayerApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.PlayerHlsActivity
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
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PlayerFactoryViewModel @Inject constructor(private val apiViewModel : PlayerApiViewModel) : BaseFactoryViewModel()
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

    private val _initPlayListView = SingleLiveEvent<Pair<PlayerListAdapter, Int>>()
    val initPlayListView: LiveData<Pair<PlayerListAdapter, Int>> = _initPlayListView

    private val _initPlaySpeedListView = SingleLiveEvent<PlayerSpeedListAdapter>()
    val initPlaySpeedListView: LiveData<PlayerSpeedListAdapter> = _initPlaySpeedListView

    private val _showMovieLoading = SingleLiveEvent<Void>()
    val showMovieLoading: LiveData<Void> = _showMovieLoading

    private val _hideMovieLoading = SingleLiveEvent<Void>()
    val hideMovieLoading: LiveData<Void> = _hideMovieLoading

    private val _initMovieLayout = SingleLiveEvent<Void>()
    val initMovieLayout: LiveData<Void> = _initMovieLayout

    private val _settingSpeedTextLayout = SingleLiveEvent<Pair<Int, Boolean>>()
    val settingSpeedTextLayout: LiveData<Pair<Int, Boolean>> = _settingSpeedTextLayout

    private val _settingCoachmarkView = SingleLiveEvent<String>()
    val settingCoachmarkView: LiveData<String> = _settingCoachmarkView

    private val _initCaptionText = SingleLiveEvent<Void>()
    val initCaptionText: LiveData<Void> = _initCaptionText

    private val _setMovieTitle = SingleLiveEvent<String>()
    val setMovieTitle: LiveData<String> = _setMovieTitle

    private val _setCaptionText = SingleLiveEvent<String>()
    val setCaptionText: LiveData<String> = _setCaptionText

    private val _setRemainMovieTime = SingleLiveEvent<String>()
    val setRemainMovieTime: LiveData<String> = _setRemainMovieTime

    private val _setCurrentMovieTime = SingleLiveEvent<String>()
    val setCurrentMovieTime: LiveData<String> = _setCurrentMovieTime

    private val _setSeekProgress = SingleLiveEvent<Int>()
    val setSeekProgress: LiveData<Int> = _setSeekProgress

    private val _setMaxProgress = SingleLiveEvent<Int>()
    val setMaxProgress: LiveData<Int> = _setMaxProgress

    private val _enablePlayMovie = SingleLiveEvent<Boolean>()
    val enablePlayMovie: LiveData<Boolean> = _enablePlayMovie

    private val _showPlayerStartView = SingleLiveEvent<Void>()
    val showPlayerStartView: LiveData<Void> = _showPlayerStartView

    private val _showPlayerEndView = SingleLiveEvent<Void>()
    val showPlayerEndView: LiveData<Void> = _showPlayerEndView

    private val _settingPlayerEndView = SingleLiveEvent<PlayerEndViewData>()
    val settingPlayerEndView: LiveData<PlayerEndViewData> = _settingPlayerEndView

    private val _playFirstIndexMovie = SingleLiveEvent<Void>()
    val playFirstIndexMovie: LiveData<Void> = _playFirstIndexMovie

    private val _playNormalIndexMovie = SingleLiveEvent<Void>()
    val playNormalIndexMovie: LiveData<Void> = _playNormalIndexMovie

    private val _playLastIndexMovie = SingleLiveEvent<Void>()
    val playLastIndexMovie: LiveData<Void> = _playLastIndexMovie

    private val _playOneItemMovie = SingleLiveEvent<Void>()
    val playOneItemMovie: LiveData<Void> = _playOneItemMovie

    private val _checkSupportCaptionView = SingleLiveEvent<Boolean>()
    val checkSupportCaptionView: LiveData<Boolean> = _checkSupportCaptionView

    private val _settingCaptionOption = SingleLiveEvent<Pair<Boolean, Boolean>>()
    val settingCaptionOption: LiveData<Pair<Boolean, Boolean>> = _settingCaptionOption

    private val _enableRepeatView = SingleLiveEvent<Boolean>()
    val enableRepeatView: LiveData<Boolean> = _enableRepeatView

    private val _scrollPosition = SingleLiveEvent<Int>()
    val scrollPosition: LiveData<Int> = _scrollPosition

    private val _setCurrentPageLine = SingleLiveEvent<Pair<Int, Int>>()
    val setCurrentPageLine: LiveData<Pair<Int, Int>> = _setCurrentPageLine

    private val _setCurrentPage = SingleLiveEvent<Int>()
    val setCurrentPage: LiveData<Int> = _setCurrentPage

    private val _activatePageView = SingleLiveEvent<Boolean>()
    val activatePageView: LiveData<Boolean> = _activatePageView

    private val _enableSpeedButton = SingleLiveEvent<Boolean>()
    val enableSpeedButton: LiveData<Boolean> = _enableSpeedButton

    private val _enablePortraitOptionButton = SingleLiveEvent<Boolean>()
    val enablePortraitOptionButton: LiveData<Boolean> = _enablePortraitOptionButton

    private val _availableMovieOptionButton = SingleLiveEvent<Boolean>()
    val availableMovieOptionButton: LiveData<Boolean> = _availableMovieOptionButton

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
    private lateinit var mPlayListAdapter : PlayerListAdapter
    private lateinit var mPlayerSpeedListAdapter : PlayerSpeedListAdapter

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
    private var mCurrentPlaybackState: Int = PlaybackState.STATE_NONE

    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mCurrentBookshelfAddResult : MyBookshelfResult

    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mPageByPageDataList : ArrayList<PageByPageData> = ArrayList<PageByPageData>()
    private var mCurrentRepeatPageIndex : Int = -1
    private var mCurrentPageIndex : Int = 0
    private var mCurrentPlaySpeedIndex : Int = DEFAULT_SPEED_INDEX

    private var isVideoPrepared : Boolean = false
    private var mCoachingMarkUserDao : CoachmarkDao? = null
    private lateinit var mPlayer : ExoPlayer
    private lateinit var mLoginInformationResult : LoginInformationResult
    @SuppressLint("StaticFieldLeak")
    private lateinit var _PlayerView : PlayerView

    private var mUiUpdateTimerJob: Job? = null
    private var mWarningWatchTimerJob: Job? = null
    private var mCoachingMarkJob: Job? = null

    fun init(context : Context, playerView: PlayerView, orientation : Int)
    {
        mContext = context
        _PlayerView = playerView
        mCurrentOrientation = orientation
        Log.f("onCreate")
        init()
        setupViewModelObserver()
        setupPlayVideo()
        initPlayList(mCurrentOrientation)
        initPlaySpeedList()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
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

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
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
                        if(code == RequestCode.CODE_AUTH_CONTENT_PLAY)
                        {
                            Log.f("Auth Content data error retry popup")
                            _hideMovieLoading.call()
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

    private fun init()
    {
        mPlayerIntentParamsObject = (mContext as AppCompatActivity).getIntent().getParcelableExtra(Common.INTENT_PLAYER_DATA_PARAMS)!!
        mPlayInformationList = mPlayerIntentParamsObject.getPlayerInformationList()
        Log.f("list size : ${mPlayInformationList.size} , isOptionDisable : ${mPlayInformationList[0].isOptionDisable} " +
                ", homeworkNumber : ${mPlayerIntentParamsObject.getHomeworkNumber()}")
        mCurrentPlayMovieIndex = 0

        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
        accessDataBase()

        // 숙제관리에서 넘어온 플레이의 경우 3점 메뉴버튼 표시하지 않도록 처리
        if (mPlayerIntentParamsObject.getHomeworkNumber() != 0)
        {
            mPlayInformationList[mCurrentPlayMovieIndex].isOptionDisable = true
        }
    }

    private fun setupPlayVideo()
    {
        mPlayer = ExoPlayer.Builder(mContext).build()
        _PlayerView.setPlayer(mPlayer)

        mPlayer.addListener(object : Player.Listener
        {
            override fun onPlayWhenReadyChanged(playWhenReady : Boolean, reason : Int)
            {
                super.onPlayWhenReadyChanged(playWhenReady, reason)

                Log.f("playWhenReady : $playWhenReady, playbackState : $mCurrentPlaybackState")
                Log.f("Max Duration : " + mPlayer.getDuration())
                when(mCurrentPlaybackState)
                {
                    Player.STATE_IDLE -> { }
                    Player.STATE_BUFFERING -> if(playWhenReady)
                    {
                        _showMovieLoading.call()
                    }
                    Player.STATE_READY ->
                    {
                        if(playWhenReady)
                        {
                            _hideMovieLoading.call()
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
                            _hideMovieLoading.call()
                        }
                        setVideoCompleted()
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState : Int)
            {
                super.onPlaybackStateChanged(playbackState)
                mCurrentPlaybackState = playbackState
            }

            override fun onPlaybackParametersChanged(playbackParameters : PlaybackParameters) {}

            override fun onPlayerError(error : PlaybackException)
            {
                super.onPlayerError(error)
                Log.i("Play Error : ${error.toString()}")
            }
        })
    }

    private fun initPlayList(orientation : Int)
    {
        Log.f("orientation : $orientation")
        mPlayListAdapter = PlayerListAdapter(mContext, orientation, mCurrentPlayMovieIndex, mPlayInformationList)
        mPlayListAdapter.setPlayerEventListener(mOnItemPlayerEventListener)
        mPlayListAdapter.setEnableOption(true)
        _initPlayListView.value = Pair(mPlayListAdapter, mCurrentPlayMovieIndex)
    }

    private fun initPlaySpeedList()
    {
        Log.f("")
        mCurrentPlaySpeedIndex = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_PLAYER_SPEED_INDEX, DataType.TYPE_INTEGER) as Int
        if(mCurrentPlaySpeedIndex == -1)
        {
            mCurrentPlaySpeedIndex = DEFAULT_SPEED_INDEX
        }
        mPlayerSpeedListAdapter = PlayerSpeedListAdapter(mContext, mCurrentPlaySpeedIndex)
        mPlayerSpeedListAdapter.setPlayerEventListener(mOnItemPlayerEventListener)
        _initPlaySpeedListView.value = mPlayerSpeedListAdapter
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
        _initCaptionText.call()
        _setMovieTitle.value = title
        _showMovieLoading.call()
        _enableSpeedButton.value = false
        _enablePortraitOptionButton.value = false
        mPlayListAdapter.setCurrentPlayIndex(mCurrentPlayMovieIndex)

        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }
            requestAuthContentPlay()
        }
    }

    @OptIn(UnstableApi::class)
    private fun startMovie()
    {
        val isCaptionEnable = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, DataType.TYPE_BOOLEAN) as Boolean
        val isPageByPageEnable = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("mAuthContentResult.getVideoUrl() : " + mAuthContentResult.getMovieHlsUrl())
        mCurrentPlayerStatus = PlayerStatus.PLAY
        notifyPlayItemIndex()
        settingCurrentMovieStudyOption()
        val source : MediaSource = buildMediaSource(Uri.parse(mAuthContentResult.getMovieHlsUrl()))
        mPlayer.setMediaSource(source)
        mPlayer.prepare()
        _PlayerView.requestFocus()

        _enablePlayMovie.value = true
        _checkSupportCaptionView.value = isAvailableCaption
        _settingCaptionOption.value = Pair(isCaptionEnable, isPageByPageEnable)
        _scrollPosition.value = mCurrentPlayMovieIndex
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
            if(isAvailableCaption)
            {
                _setCurrentPageLine.value = Pair(mPageByPageDataList[0].getCurrentIndex(), mPageByPageDataList.size)
            }
            _setCurrentPage.value = -1
            _showPlayerStartView.call()
        }
        _hideMovieLoading.call()
        _enablePortraitOptionButton.value = true
        mPlayer.setPlayWhenReady(true)
        enableTimer(true)
        mPlayListAdapter.setEnableOption(true)
    }

    private fun releasePlayer()
    {
        _PlayerView.setPlayer(null)
        mPlayer?.release()
    }

    private val isPlaying : Boolean
        get()
        {
            Log.f("playWhenReady : " + mPlayer.getPlayWhenReady() + ", state : " + mPlayer.getPlaybackState())
            return mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY
        }

    private val isAvailableCaption : Boolean
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
        private get()
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
        private get()
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
        private get()
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
            startTime = mAuthContentResult.getCaptionList().get(0).getStartTime()
            if(startTime > mPlayer.getCurrentPosition().toFloat())
            {
                return 0
            }
            /**
             * 데이터는 startTime 과  endTime 사이에 존재한다고 생각하여 그 중간에 있는 CaptionIndex를 가져와 리턴
             */
            for(i in 0 until mAuthContentResult.getCaptionList().size)
            {
                startTime = mAuthContentResult.getCaptionList().get(i).getStartTime()
                endTime = mAuthContentResult.getCaptionList().get(i).getEndTime()
                if(startTime <= mPlayer.getCurrentPosition().toFloat()
                    && endTime >= mPlayer.getCurrentPosition().toFloat())
                {
                    return i
                }
            }
            /**
             * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
             */
            for(i in 0 until mAuthContentResult.getCaptionList().size)
            {
                startTime = mAuthContentResult.getCaptionList().get(i).getStartTime()
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
        private get()
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

    private fun notifyPlayItemIndex()
    {
        Log.f("list size : " + mPlayInformationList.size + ", index : " + mCurrentPlayMovieIndex)
        if(mPlayInformationList.size == 1)
        {
            _playOneItemMovie.call()
            return
        }
        _playNormalIndexMovie.call()
        if(mCurrentPlayMovieIndex == 0)
        {
            _playFirstIndexMovie.call()
        }
        else if(mCurrentPlayMovieIndex == mPlayInformationList.size - 1)
        {
            _playLastIndexMovie.call()
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
            if(data.service_info?.ebook.equals(Common.SERVICE_NOT_SUPPORTED)
                || Feature.IS_SUPPORT_EBOOK == false)
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
            _showPlayerEndView.call()
        }
        else
        {
            mCurrentPlayMovieIndex = nextMovieIndex
            viewModelScope.launch(Dispatchers.Main) {
                checkMovieTiming()
            }
        }
    }


    private fun updateUI()
    {
        if(isVideoPrepared == false)
        {
            return
        }
        _setSeekProgress.value = mPlayer.getCurrentPosition().toInt() / Common.SECOND
        _setCurrentMovieTime.value = CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer.getCurrentPosition())

        if(isAvailableCaption)
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
                    if(mCurrentPageIndex % Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE === 0)
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
            _setCaptionText.value = mAuthContentResult.getCaptionList().get(mCurrentCaptionIndex).getText()
            mCurrentCaptionIndex++
        }
    }

    private fun checkMovieTiming()
    {
        Log.f("")
        mPlayListAdapter.setEnableOption(false)
        var isShowCoachingMark : Boolean = false
        val type : String = mPlayInformationList[mCurrentPlayMovieIndex].type
        _initMovieLayout.call()

        mCoachingMarkJob = CoroutineScope(Dispatchers.Main).launch{
            CoroutineScope(Dispatchers.Default).async {
                isShowCoachingMark = isNeverSeeAgainCheck(type)
            }.await()

            if(isShowCoachingMark)
            {
                Log.f("show coachmark")
                pausePlayer()
                _settingCoachmarkView.value = type
            } else
            {
                Log.f("show coachmark")
                prepareMovie()
            }
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

    private fun accessDataBase()
    {
        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao()
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

    private fun setVideoSpeed(speedIndex : Int)
    {
        var params : PlaybackParameters? = null
        params = PlaybackParameters(PLAY_SPEED_LIST[speedIndex])
        mPlayer.setPlaybackParameters(params)
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
                    mCurrentStudyLogMilliSeconds = mCurrentStudyLogMilliSeconds + (Common.DURATION_SHORTEST * PLAY_SPEED_LIST[mCurrentPlaySpeedIndex]).toInt()
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

    /**
     * 현재의 페이지 라인의 첫번째 인덱스를 리턴한다. (예: 1,2,3,4,5 면 1를 리턴, 6,7,8,9,10 이면 6을 리턴)
     * @return 현재 라인의 첫번째 인덱스
     */
    private fun getFirstIndexOfCurrentPageLine(pageIndex : Int) : Int
    {
        return pageIndex / Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE * Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE
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

    private fun onClickEbookButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startEbookActivity()
        }
    }

    private fun onClickQuizButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startQuizAcitiviy()
        }

    }

    private fun onClickVocabularyButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startVocabularyActivity()
        }
    }

    private fun onClickTranslateButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startOriginTranslateActivity()
        }
    }

    private fun onClickStarwordsButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameStarwordsActivity()
        }
    }

    private fun onClickCrosswordButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameCrosswordActivity()
        }
    }

    private fun onClickFlashcardButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startFlashcardActivity()
        }
    }

    private fun onClickRecordPlayerButton()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
        {
            _dialogWarningRecordPermission.call()
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

    fun onClickItemOption(action: ActionContentsType)
    {
        when(action)
        {
            ActionContentsType.QUIZ ->
            {
                onClickQuizButton()
            }
            ActionContentsType.EBOOK ->
            {
                onClickEbookButton()
            }
            ActionContentsType.VOCABULARY ->
            {
                onClickVocabularyButton()
            }
            ActionContentsType.TRANSLATE ->
            {
                onClickTranslateButton()
            }
            ActionContentsType.STARWORDS ->
            {
                onClickStarwordsButton()
            }
            ActionContentsType.CROSSWORD ->
            {
                onClickCrosswordButton()
            }
            ActionContentsType.FLASHCARD ->
            {
                onClickFlashcardButton()
            }
            ActionContentsType.RECORD_PLAYER ->
            {
                onClickRecordPlayerButton()
            }
            else -> {}
        }
    }

    fun onCloseButton()
    {
        Log.f("isAuthorizationComplete : $isAuthorizationComplete, status : $mCurrentPlayerStatus")
        (mContext as AppCompatActivity).onBackPressed()
    }

    fun onHandlePlayButton()
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

    fun onNextButton()
    {
        Log.f("")
        mCurrentPlayMovieIndex++
        enableTimer(false)
        sendStudyLogSaveAsync()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    fun onPrevButton()
    {
        mCurrentPlayMovieIndex--
        enableTimer(false)
        sendStudyLogSaveAsync()
        viewModelScope.launch(Dispatchers.Main) {
            checkMovieTiming()
        }
    }

    fun onReplayButton()
    {
        Log.f("")
        enableTimer(false)
        _initMovieLayout.call()
        prepareMovie()
    }

    fun onStartTrackingSeek()
    {
        Log.f("")
        enableTimer(false)
        mCurrentRepeatPageIndex = -1
        _setCaptionText.value = ""
    }

    fun onStopTrackingSeek(progress : Int)
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

    fun onChangeOrientation(orientation : Int)
    {
        Log.f("orientation : $orientation")
        var isShowCoachingMark : Boolean = false
        mCurrentOrientation = orientation
        initPlayList(orientation)
        _settingSpeedTextLayout.value = Pair(mCurrentPlaySpeedIndex, false)

        mCoachingMarkJob = viewModelScope.launch(Dispatchers.Main) {
            viewModelScope.async(Dispatchers.Default) {
                isShowCoachingMark = isNeverSeeAgainCheck(mPlayInformationList[mCurrentPlayMovieIndex].type)
            }.await()

            if(isShowCoachingMark)
            {
                _settingCoachmarkView.value = mPlayInformationList[mCurrentPlayMovieIndex].type
            }
        }
    }

    fun onPageByPageIndex(index : Int)
    {
        mCurrentRepeatPageIndex = index - 1
        mCurrentPageIndex = mCurrentRepeatPageIndex

        Log.f("repeatIndex : " + mCurrentRepeatPageIndex + ", startTime : " + mPageByPageDataList[mCurrentRepeatPageIndex].getStartTime().toInt())
        mPlayer.seekTo(mPageByPageDataList[mCurrentRepeatPageIndex].getStartTime().toLong())
        mPlayer.setPlayWhenReady(true)
        mCurrentCaptionIndex = currentCaptionIndex
        _setCaptionText.value = ""
        _setCurrentPage.value = index
        _enablePlayMovie.value = true
    }

    fun onRepeatButton()
    {
        isRepeatOn = !isRepeatOn
        Log.f("반복 재생  : $isRepeatOn")
        _enableRepeatView.value = isRepeatOn
    }

    fun onClickMovieOptionButton()
    {
        Log.f("option index : $mCurrentPlayMovieIndex")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        pausePlayer()
        _dialogBottomOption.value = mPlayInformationList[mSelectItemOptionIndex]
    }

    fun onClickAddBookshelf()
    {
        Log.f("")
        mSendBookshelfAddList.clear()
        mSendBookshelfAddList.add(mPlayInformationList[mSelectItemOptionIndex])
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            _dialogBottomBookshelfContentAdd.value = mMainInformationResult.getBookShelvesList()
        }

    }

    fun onNextMovieButton()
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
        _initMovieLayout.call()
        prepareMovie()
    }

    fun onClickBottomOptionDialogCancel()
    {
        Log.f("")
        resumePlayer()
        enableTimer(true)
    }

    fun onCoachMarkNeverSeeAgain(type : String)
    {
        val userID : String = mLoginInformationResult.getUserInformation().getFoxUserID()
        Log.f("userID : $userID , type : $type")
        if(type == Common.CONTENT_TYPE_STORY)
        {
            setStoryCoachmarkViewed(userID)
        }
        else
        {
            setSongCoachmarkViewed(userID)
        }
        prepareMovie()
    }

    /**
     * 해당 페이지 라인의 처음 페이지 (예: 1,2,3,4,5 라면 1 , 6,7,8,9,10 이면 6)
     * @param startPage
     */
    fun onMovePrevPage(startPage : Int)
    {
        val index : Int = startPage - Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE - 1
        Log.f("index : $index")
        _setCurrentPageLine.value = Pair(mPageByPageDataList[index].getCurrentIndex(), mPageByPageDataList.size)
        onPageByPageIndex(startPage - 1)
    }

    /**
     * 해당 페이지 라인의 마지막 페이지 (예: 1,2,3,4,5 라면 5 , 6,7,8,9,10 이면 10)
     * @param lastPage
     */
    fun onMoveNextPage(lastPage : Int)
    {
        val index = lastPage + 1
        Log.f("index : $index")
        _setCurrentPageLine.value = Pair(mPageByPageDataList[index].getCurrentIndex(), mPageByPageDataList.size)
        onPageByPageIndex(index)
    }

    fun onClickCaptionButton(isEnable : Boolean)
    {
        Log.f("isEnable : $isEnable")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, isEnable)
    }

    fun onClickPageByPageButton(isEnable : Boolean)
    {
        Log.f("isEnable : $isEnable")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, isEnable)
    }

    fun onDialogAddBookshelfClick(index : Int)
    {
        Log.f("index : $index")
        mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index)
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            requestBookshelfContentsAdd(mSendBookshelfAddList)
        }
    }


    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        if(eventType == DIALOG_TYPE_WARNING_WATCH_MOVIE)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    Log.f("Warning watch movie End")
                    (mContext as PlayerHlsActivity).finish()
                }
                DialogButtonType.BUTTON_2 ->
                {
                    Log.f("Warning watch movie Continue")
                    mCurrentWatchingTime = 0
                    enableTimer(true)
                    mPlayer.setPlayWhenReady(true)
                }
            }
        }
        else if(eventType == DIALOG_TYPE_WARNING_API_EXCEPTION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    Log.f("Auth Content data error retry")
                    _showMovieLoading.call()
                    requestAuthContentPlay()
                }
                DialogButtonType.BUTTON_2 ->
                {
                    Log.f("Auth Content data error end")
                    (mContext as PlayerHlsActivity).finish()
                }
            }
        }
        else if(eventType == DIALOG_TYPE_WARNING_RECORD_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    // [취소] 컨텐츠 사용 불가 메세지 표시
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

    private val mOnItemPlayerEventListener : PlayerEventListener = object : PlayerEventListener
    {
        override fun onClickOption(index : Int)
        {
            Log.f("option index : $index")
            mSelectItemOptionIndex = index
            pausePlayer()
            _dialogBottomOption.value = mPlayInformationList[mSelectItemOptionIndex]
        }

        override fun onSelectSpeed(index : Int)
        {
            Log.f("speed : " + PLAY_SPEED_LIST[index])
            mCurrentPlaySpeedIndex = index
            setVideoSpeed(mCurrentPlaySpeedIndex)
            _settingSpeedTextLayout.value = Pair(mCurrentPlaySpeedIndex, true)
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_PLAYER_SPEED_INDEX, index)
        }

        override fun onItemClick(position : Int)
        {
            Log.f("List select Index : $position")
            mCurrentPlayMovieIndex = position
            enableTimer(false)
            viewModelScope.launch(Dispatchers.Main) {
                checkMovieTiming()
            }
        }
    }

}