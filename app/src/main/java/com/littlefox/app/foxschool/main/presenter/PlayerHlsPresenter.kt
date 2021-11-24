package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Message
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorRequestData
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PageByPageData
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.PlayerDataBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.player.PlayItemResult
import com.littlefox.app.foxschool.adapter.PlayerListAdapter
import com.littlefox.app.foxschool.adapter.PlayerSpeedListAdapter
import com.littlefox.app.foxschool.adapter.listener.PlayerEventListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.AuthContentPlayCoroutine
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.StudyLogSaveCoroutine
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.PlayerHlsActivity
import com.littlefox.app.foxschool.main.contract.PlayerContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.*

import java.util.*

/**
 * Created by only340 on 2018-03-21.
 */
class PlayerHlsPresenter : PlayerContract.Presenter
{
    internal inner class UITimerTask : TimerTask()
    {
        override fun run()
        {
            if(mCurrentPlayerUserType === PlayerUserType.FULL_PLAY)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_UI_UPDATE)
            }
            else if(mCurrentPlayerUserType === PlayerUserType.PREVIEW)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_PREVIEW_UI_UPDATE)
            }
            mCurrentStudyLogMilliSeconds = mCurrentStudyLogMilliSeconds + (Common.DURATION_SHORTEST * PLAY_SPEED_LIST[mCurrentPlaySpeedIndex]).toInt()
        }
    }

    internal inner class WarningWatchMessageTask : TimerTask()
    {
        override fun run()
        {
            mCurrentWatchingTime += Common.SECOND
            if(mCurrentWatchingTime >= MAX_WARNING_WATCH_MOVIE_TIME)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_WARNING_WATCH_MOVIE)
            }
        }
    }

    internal inner class LockCountDownTask : TimerTask()
    {
        override fun run()
        {
            if(mCurrentLockTime == 0)
            {
                mCurrentLockTime = MAX_LOCKMODE_SECOND
            }
            else
            {
                --mCurrentLockTime
            }
            if(mCurrentLockTime == 0)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_LOCK_BUTTON_ACTIVATE)
            }
            else
            {
                mMainHandler.sendEmptyMessage(MESSAGE_LOCK_COUNT_TIME)
            }
        }
    }

    companion object
    {
        //1시간이 지나면 팝업을 띄워 확인 작업
        private const val MAX_WARNING_WATCH_MOVIE_TIME : Int    = 60 * 60 * Common.SECOND
        private const val MAX_LOCKMODE_SECOND : Int             = 3

        private const val MESSAGE_UI_UPDATE : Int                       = 100
        private const val MESSAGE_PREVIEW_UI_UPDATE : Int               = 101
        private const val MESSAGE_LOCK_BUTTON_ACTIVATE : Int            = 102
        private const val MESSAGE_WARNING_WATCH_MOVIE : Int             = 103
        private const val MESSAGE_LOCK_COUNT_TIME : Int                 = 104
        private const val MESSAGE_START_QUIZ : Int                      = 105
        private const val MESSAGE_START_TRANSLATE : Int                 = 106
        private const val MESSAGE_START_EBOOK : Int                     = 107
        private const val MESSAGE_START_VOCABULARY : Int                = 108
        private const val MESSAGE_START_GAME_STARWORDS : Int            = 109
        private const val MESSAGE_START_FLASHCARD : Int                 = 110
        private const val MESSAGE_REQUEST_CONTENTS_ADD : Int            = 111
        private const val MESSAGE_COMPLETE_CONTENTS_ADD : Int           = 112
        private const val MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG : Int  = 113
        private const val MESSAGE_REQUEST_VIDEO : Int                   = 114
        private const val MESSAGE_CHECK_MOVIE : Int                     = 115

        private const val DIALOG_TYPE_WARNING_WATCH_MOVIE : Int     = 10001
        private const val DIALOG_TYPE_WARNING_API_EXCEPTION : Int   = 10002

        private val PLAY_SPEED_LIST = floatArrayOf(0.7f, 0.85f, 1.0f, 1.15f, 1.3f)
        private const val DEFAULT_SPEED_INDEX : Int         = 2
        private const val FINE_TUNING_PAGE_TIME : Float     = 1f
    }

    private lateinit var mContext : Context
    private lateinit var mPlayerContractView : PlayerContract.View
    private lateinit var mPlayListAdapter : PlayerListAdapter
    private lateinit var mPlayerSpeedListAdapter : PlayerSpeedListAdapter
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mUIUpdateTimer : Timer? = null
    private var mWarningWatchTimer : Timer? = null
    private var mLockCountTimer : Timer? = null
    private var mVibrator : Vibrator? = null
    private var mCurrentPlayDuration : Long = 0L
    private var mCurrentPlayerStatus : PlayerStatus = PlayerStatus.STOP
    private var mCurrentPlayerUserType : PlayerUserType = PlayerUserType.PREVIEW
    private var isLockMode : Boolean = false
    private var mCurrentLockTime : Int = 0
    private var mFreeUserPreviewTime : Int  = 0

    private lateinit var mPlayInformationList : ArrayList<ContentsBaseResult>
    private lateinit var mPlayerIntentParamsObject : PlayerIntentParamsObject
    private lateinit var mAuthContentResult : PlayItemResult
    private var mAuthContentPlayCoroutine : AuthContentPlayCoroutine? = null
    private var mStudyLogSaveCoroutine : StudyLogSaveCoroutine? = null
    private var mBookshelfContentAddCoroutine : BookshelfContentAddCoroutine? = null
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
    private var mBottomBookAddDialog : BottomBookAddDialog ? = null;
    private var mBottomContentItemOptionDialog : BottomContentItemOptionDialog? = null;
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mPageByPageDataList : ArrayList<PageByPageData> = ArrayList<PageByPageData>()
    private var mCurrentRepeatPageIndex : Int = -1
    private var mCurrentPageIndex : Int = 0
    private var mCurrentPlaySpeedIndex : Int = DEFAULT_SPEED_INDEX
    private var mPlayer : SimpleExoPlayer? = null
    private var isVideoPrepared : Boolean = false
    private var mCoachingMarkUserDao : CoachmarkDao? = null
    protected var mJob: Job? = null;
    private lateinit var mLoginInformationResult : LoginInformationResult
    private lateinit var _PlayerView : PlayerView

    constructor(context : Context, videoView : PlayerView, orientation : Int)
    {
        mContext = context
        _PlayerView = videoView
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback?)
        mCurrentOrientation = orientation
        mPlayerContractView = mContext as PlayerContract.View
        mPlayerContractView.initView()
        mPlayerContractView.initFont()
        Log.f("onCreate")
        init()
        setupPlayVideo()
        initPlayList(mCurrentOrientation)
        initPlaySpeedList()
        mMainHandler.sendEmptyMessage(MESSAGE_CHECK_MOVIE)
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

    override fun resume()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            if(mCurrentPlayerStatus === PlayerStatus.PAUSE)
            {
                Log.f("Build.VERSION.SDK_INT ERROR : " + Build.VERSION.SDK_INT)
                isVideoPrepared = false
                mCurrentCaptionIndex = 0
                mCurrentPageIndex = 0
                mPlayerContractView.initCaptionText()
                startMovie()
            }
        }
        else
        {
            resumePlayer()
        }
    }

    override fun destroy()
    {
        Log.f("")
        enableTimer(false)
        releaseStudyLogSaveAsync()
        releaseAuthContentPlay()
        releasePlayer()
        if(mMainHandler.hasMessages(MESSAGE_UI_UPDATE))
        {
            mMainHandler.removeMessages(MESSAGE_UI_UPDATE)
        }
        if(mMainHandler.hasMessages(MESSAGE_PREVIEW_UI_UPDATE))
        {
            mMainHandler.removeMessages(MESSAGE_PREVIEW_UI_UPDATE)
        }
        mMainHandler.removeCallbacksAndMessages(null)

        mJob?.cancel()

    }


    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {

    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_UI_UPDATE -> updateUI()
            MESSAGE_PREVIEW_UI_UPDATE -> updatePreviewUI()
            MESSAGE_LOCK_BUTTON_ACTIVATE ->
            {
                enableLockCountTimer(false)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    mVibrator?.vibrate(VibrationEffect.createOneShot(Common.DURATION_NORMAL.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else
                {
                    mVibrator?.vibrate(Common.DURATION_NORMAL);
                }
                isLockMode = !isLockMode
                mPlayerContractView.enableLockMenu(isLockMode)
            }
            MESSAGE_WARNING_WATCH_MOVIE ->
            {
                enableTimer(false)
                mPlayer?.setPlayWhenReady(false)
                showTemplateAlertDialog(DIALOG_TYPE_WARNING_WATCH_MOVIE,
                        DialogButtonType.BUTTON_2,
                        mContext.resources.getString(R.string.message_longtime_play_warning))
            }
            MESSAGE_LOCK_COUNT_TIME -> mPlayerContractView.setLockCountTime(mCurrentLockTime)
            MESSAGE_START_QUIZ -> startQuizAcitiviy()
            MESSAGE_START_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_START_GAME_STARWORDS -> startGameStarwordsActivity()
            MESSAGE_START_FLASHCARD -> startFlashcardActivity()
            MESSAGE_REQUEST_CONTENTS_ADD ->
            {
                mPlayerContractView.showLoading()
                requestBookshelfContentsAddAsync(mSendBookshelfAddList)
            }
            MESSAGE_COMPLETE_CONTENTS_ADD ->
            if(msg.arg1 == Activity.RESULT_OK)
            {
                mPlayerContractView.showSuccessMessage(msg.obj as String)
            }
            else
            {
                mPlayerContractView.showErrorMessage(msg.obj as String)
            }
            MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG -> showBottomBookAddDialog()
            MESSAGE_REQUEST_VIDEO -> requestAuthContentPlay()
            MESSAGE_CHECK_MOVIE -> checkMovieTiming()
        }
    }

    private fun init()
    {
        mPlayerIntentParamsObject = (mContext as AppCompatActivity).getIntent().getParcelableExtra(Common.INTENT_PLAYER_DATA_PARAMS)!!
        mPlayInformationList = mPlayerIntentParamsObject.getPlayerInformationList()
        Log.f("list size : " + mPlayInformationList.size + ", isOptionDisable :" + mPlayInformationList[0].isOptionDisable())
        mCurrentPlayMovieIndex = 0

        mVibrator = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
        accessDataBase()
    }

    private fun initPlayList(orientation : Int)
    {
        Log.f("orientation : $orientation")
        mPlayListAdapter = PlayerListAdapter(mContext, orientation, mCurrentPlayMovieIndex, mPlayInformationList)
        mPlayListAdapter.setPlayerEventListener(mOnItemPlayerEventListener)
        mPlayerContractView.initPlayListView(mPlayListAdapter, mCurrentPlayMovieIndex)
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
        mPlayerContractView.initPlaySpeedListView(mPlayerSpeedListAdapter)
    }

    private val isPlaying : Boolean
        private get()
        {
            Log.f("playWhenReady : " + mPlayer!!.getPlayWhenReady() + ", state : " + mPlayer!!.getPlaybackState())
            return mPlayer!!.getPlayWhenReady() && mPlayer!!.getPlaybackState() == Player.STATE_READY
        }

    private fun accessDataBase()
    {
        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao();
    }

    private fun isStoryCoachmarkViewed(userID : String) : Boolean
    {
        var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID);
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
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID);

            if(data == null)
            {
                Log.f("data null ")
                data = CoachmarkEntity(userID,
                    true,
                    false,
                    false);
                mCoachingMarkUserDao?.insertItem(data);
            }
            else
            {
                Log.f("data update  ")
                data.isStoryCoachmarkViewed = true;
                mCoachingMarkUserDao?.updateItem(data);
            }
        }

    }

    private fun setSongCoachmarkViewed(userID : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID);
            if(data == null)
            {
                data = CoachmarkEntity(userID,
                    false,
                    true,
                    false);
                mCoachingMarkUserDao?.insertItem(data);
            }
            else
            {
                data.isSongCoachmarkViewed = true;
                mCoachingMarkUserDao?.updateItem(data);
            }
        }
    }

    private fun isSongCoachmarkViewed(userID : String) : Boolean
    {
        var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID);
        if(data == null)
        {
            return false;
        }
        else
        {
            if(data.isSongCoachmarkViewed)
            {
                return true;
            }
            else
            {
                return false;
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

    /**
     * 현재의 페이지 라인의 첫번째 인덱스를 리턴한다. (예: 1,2,3,4,5 면 1를 리턴, 6,7,8,9,10 이면 6을 리턴)
     * @return 현재 라인의 첫번째 인덱스
     */
    private fun getFirstIndexOfCurrentPageLine(pageIndex : Int) : Int
    {
        return pageIndex / Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE * Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE
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
            mCurrentPlayDuration = mPlayer!!.getCurrentPosition()
            mPlayer!!.setPlayWhenReady(false)
            enableTimer(false)
            mPlayerContractView.enablePlayMovie(false)
            mCurrentPlayerStatus = PlayerStatus.PAUSE
        }
    }

    private fun resumePlayer()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(mCurrentPlayerStatus === PlayerStatus.PAUSE)
        {
            mPlayer!!.seekTo(mCurrentPlayDuration)
            mPlayer!!.setPlayWhenReady(true)
            enableTimer(true)
            mPlayerContractView.enablePlayMovie(true)
            mCurrentPlayerStatus = PlayerStatus.PLAY
        }
    }

    private fun setVideoSpeed(speendIndex : Int)
    {
        var params : PlaybackParameters? = null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            params = PlaybackParameters(PLAY_SPEED_LIST[speendIndex])
            mPlayer?.setPlaybackParameters(params)
        }
    }

    private fun setupPlayVideo()
    {
        if(mPlayer == null)
        {
            mPlayer = ExoPlayerFactory.newSimpleInstance(mContext.applicationContext)
            _PlayerView.setPlayer(mPlayer)
        }

        mPlayer!!.addListener(object : Player.EventListener
        {
            override fun onLoadingChanged(isLoading : Boolean) {}

            override fun onPlayerStateChanged(playWhenReady : Boolean, playbackState : Int)
            {
                Log.f("playWhenReady : $playWhenReady, playbackState : $playbackState")
                Log.f("Max Duration : " + mPlayer!!.getDuration())
                when(playbackState)
                {
                    Player.STATE_IDLE -> { }
                    Player.STATE_BUFFERING -> if(playWhenReady)
                    {
                        mPlayerContractView.showMovieLoading()
                    }
                    Player.STATE_READY ->
                    {
                        if(playWhenReady)
                        {
                            mPlayerContractView.hideMovieLoading()
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
                            mPlayerContractView.hideMovieLoading()
                        }
                        setVideoCompleted()
                    }
                }
            }

            override fun onPlayerError(error : ExoPlaybackException)
            {
                Log.f("Play Error : " + error.message)
            }

            override fun onPlaybackParametersChanged(playbackParameters : PlaybackParameters)
            {
            }

            override fun onSeekProcessed()
            {
                Log.f("Max Duration : " + mPlayer!!.getDuration())
            }
        })
    }

    private val isAvailableCaption : Boolean
        get()
        {
            if(mAuthContentResult.getCaptionList().size > 0)
                return true
            else
                return false
        }


    private fun setVideoPrepared()
    {
        if(mPlayInformationList[mCurrentPlayMovieIndex].getType().equals(Common.CONTENT_TYPE_SONG))
        {
            setVideoSpeed(DEFAULT_SPEED_INDEX)
            mPlayerContractView.settingSpeedTextLayout(DEFAULT_SPEED_INDEX, true)
        }
        else
        {
            setVideoSpeed(mCurrentPlaySpeedIndex)
            mPlayerContractView.settingSpeedTextLayout(mCurrentPlaySpeedIndex, true)
            mPlayerContractView.enableSpeedButton()
        }
        if(mCurrentPlayerStatus === PlayerStatus.COMPELTE)
        {
            return
        }
        if(mCurrentPlayerStatus === PlayerStatus.PLAY)
        {
            if(mCurrentPlayerUserType === PlayerUserType.FULL_PLAY)
            {
                Log.f("init Play Payment User")
                Log.f("Max Duration : " + mPlayer!!.getDuration())
                Log.f("Max Progress : " + (mPlayer!!.getDuration() / Common.SECOND))
                mPlayerContractView.setCurrentMovieTime("00:00")
                mPlayerContractView.setRemainMovieTime(CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer!!.getDuration()))
                mPlayerContractView.setMaxProgress((mPlayer!!.getDuration().toInt() / Common.SECOND))
                if(isAvailableCaption)
                {
                    mPlayerContractView.settingCurrentPageLine(mPageByPageDataList[0].getCurrentIndex(), mPageByPageDataList.size)
                }
                mPlayerContractView.enableCurrentPage(-1)
                mPlayerContractView.showPaymentUserStartView()
            }
            else if(mCurrentPlayerUserType === PlayerUserType.PREVIEW)
            {
                Log.f("init Play Free User")
                Log.f("tempPreviewTime : " + mAuthContentResult.getPreviewTime())
                mFreeUserPreviewTime = mAuthContentResult.getPreviewTime()
                mPlayerContractView.showPreviewUserStartView()
            }
        }
        mPlayerContractView.hideMovieLoading()
        mPlayer!!.setPlayWhenReady(true)
        enableTimer(true)
        mPlayListAdapter.setEnableOption(true)
    }

    private fun setVideoCompleted()
    {
        mCurrentPlayerStatus = PlayerStatus.COMPELTE
        enableTimer(false)
        mPlayerContractView.setSeekProgress((mPlayer!!.getDuration().toInt() / Common.SECOND))
        mPlayerContractView.setCurrentMovieTime(CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer!!.getDuration()))
        sendStudyLogSaveAsync()
        var nextMovieIndex = mCurrentPlayMovieIndex
        nextMovieIndex++
        if(isRepeatOn)
        {
            if(mPlayInformationList.size > 1)
            {
                if(nextMovieIndex >= mPlayInformationList.size)
                    nextMovieIndex = 0
                else
                    nextMovieIndex = nextMovieIndex
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
            if(mCurrentPlayerUserType === PlayerUserType.FULL_PLAY)
            {
                Log.f("ALL FULL_PLAY Complete")
                mPlayerContractView.showPaymentUserEndView()
            }
            else if(mCurrentPlayerUserType === PlayerUserType.PREVIEW)
            {
                Log.f("PREVIEW PLAY Complete")
                mPlayerContractView.showPreviewUserEndView()
            }
        }
        else
        {
            mCurrentPlayMovieIndex = nextMovieIndex
            mMainHandler.sendEmptyMessage(MESSAGE_CHECK_MOVIE)
        }
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

    private fun checkMovieTiming()
    {
        mPlayListAdapter.setEnableOption(false)
        var isShowCoachingMark : Boolean = false
        val type : String = mPlayInformationList[mCurrentPlayMovieIndex].getType()
        mPlayerContractView.initMovieLayout()

        mJob = CoroutineScope(Dispatchers.Main).launch{

            CoroutineScope(Dispatchers.Default).async {
                isShowCoachingMark = isShowCoachmark(type)
            }.await()

            if(isShowCoachingMark)
            {
                Log.f("show coachmark")
                pausePlayer()
                mPlayerContractView.settingCoachmarkView(type)
            } else
            {
                Log.f("show coachmark")
                prepareMovie()
            }
        }
    }

    private fun isShowCoachmark(type : String) : Boolean
    {
        if(Feature.IS_FREE_USER || Feature.IS_REMAIN_DAY_END_USER)
        {
            Log.f("serviceInformationType : "+mPlayInformationList[mCurrentPlayMovieIndex].getServiceInformation()?.getServiceSupportType());
            if(mPlayInformationList[mCurrentPlayMovieIndex].getServiceInformation()?.getServiceSupportType().equals(Common.SERVICE_SUPPORTED_PAID))
            {
                return false
            }
            else
            {
                return isNeverSeeAgainCheck(type)
            }
        }
        else
            return isNeverSeeAgainCheck(type)
    }

    private fun isNeverSeeAgainCheck(type : String) : Boolean
    {
        var isNeverSeeAgain = false
        val userID : String = if(Feature.IS_FREE_USER) Common.FREE_USER_NAME else mLoginInformationResult.getUserInformation().getFoxUserID()
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

    private fun prepareMovie()
    {
        Log.f("mCurrentPlayMovieIndex : $mCurrentPlayMovieIndex")
        val title : String = CommonUtils.getInstance(mContext).getContentsName(mPlayInformationList[mCurrentPlayMovieIndex])
        isAuthorizationComplete = false
        mCurrentCaptionIndex = 0
        isVideoPrepared = false
        mCurrentPageIndex = 0
        mCurrentRepeatPageIndex = -1
        mCurrentStudyLogMilliSeconds = 0f
        mPlayerContractView.initCaptionText()
        mPlayerContractView.setMovieTitle(title)
        mPlayerContractView.showMovieLoading()
        mPlayerContractView.disableSpeedButton()
        mPlayListAdapter.setCurrentPlayIndex(mCurrentPlayMovieIndex)
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_VIDEO, Common.DURATION_NORMAL)
    }

    private fun releasePlayer()
    {
        _PlayerView.setPlayer(null)
        mPlayer?.release()
    }

    /**
     * 결제를 하기위해 메인으로 이동
     */
    private fun startMainActivityToPayment()
    {
        Log.f("")
        MainObserver.executeToEnterPaymentPage()
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.MAIN)
                .setAnimationMode(AnimationMode.REVERSE_NORMAL_ANIMATION)
                .setIntentFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .startActivity()
        (mContext as AppCompatActivity).finish()
    }

    private fun notifyPlayItemIndex()
    {
        Log.f("list size : " + mPlayInformationList.size + ", index : " + mCurrentPlayMovieIndex)
        if(mPlayInformationList.size == 1)
        {
            mPlayerContractView.PlayOneItemMovie()
            return
        }
        mPlayerContractView.PlayNormalIndexMovie()
        if(mCurrentPlayMovieIndex == 0)
        {
            mPlayerContractView.PlayFirstIndexMovie()
        }
        else if(mCurrentPlayMovieIndex == mPlayInformationList.size - 1)
        {
            mPlayerContractView.PlayLastIndexMovie()
        }
    }

    private fun startMovie()
    {
        val isCaptionEnable = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, DataType.TYPE_BOOLEAN) as Boolean
        val isPageByPageEnable = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, DataType.TYPE_BOOLEAN) as Boolean
        Log.f("mAuthContentResult.getVideoUrl() : " + mAuthContentResult.getMovieHlsUrl())
        mCurrentPlayerStatus = PlayerStatus.PLAY
        if(mAuthContentResult.getPreviewTime() !== 0)
        {
            mCurrentPlayerUserType = PlayerUserType.PREVIEW
        }
        else
        {
            mCurrentPlayerUserType = PlayerUserType.FULL_PLAY
        }
        notifyPlayItemIndex()
        settingCurrentMovieStudyOption()
        mPlayerContractView.enablePlayMovie(true)
        mPlayerContractView.checkSupportCaptionView(isAvailableCaption)
        mPlayerContractView.settingPlayerOption(isCaptionEnable, isPageByPageEnable)
        val source : MediaSource = buildMediaSource(Uri.parse(mAuthContentResult.getMovieHlsUrl()))
        mPlayer!!.prepare(source, true, false)
        _PlayerView.requestFocus()
        mPlayerContractView.scrollPosition(mCurrentPlayMovieIndex)
    }

    private fun settingCurrentMovieStudyOption()
    {
        var isEbookAvailable = true
        var isQuizAvailable = true
        var isVocabularyAvailable = true
        var isFlashcardAvailable = true
        var isStarwordsAvailable = true
        var isCrosswordAvailable = true
        var isTranslateAvailable = true
        val data : ContentsBaseResult = mPlayInformationList[mCurrentPlayMovieIndex]
        var isNextMovieHave = false
        if(mAuthContentResult.getNextContentData() != null && mPlayInformationList.size <= 1)
        {
            isNextMovieHave = true
        }
        if(data.getServiceInformation()?.getEbookSupportType().equals(Common.SERVICE_NOT_SUPPORTED)
                || CommonUtils.getInstance(mContext).checkTablet == false
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            isEbookAvailable = false
        }
        if(data.getServiceInformation()?.getQuizSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isQuizAvailable = false
        }
        if(data.getServiceInformation()?.getVocabularySupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isVocabularyAvailable = false
        }
        if(data.getServiceInformation()?.getFlashcardSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isFlashcardAvailable = false
        }
        if(data.getServiceInformation()?.getStarwordsSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isStarwordsAvailable = false
        }
        if(data.getServiceInformation()?.getCrosswordSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isCrosswordAvailable = false
        }
        if(data.getServiceInformation()?.getOriginalTextSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            isTranslateAvailable = false
        }
        if(mPlayInformationList[mCurrentPlayMovieIndex].isOptionDisable())
        {
            mPlayerContractView.disablePortraitOptionButton()
        }
        else
        {
            mPlayerContractView.enablePortraitOptionButton()
        }
        mPlayerContractView.settingPaymentEndView(
            isEbookAvailable = isEbookAvailable,
            isQuizAvailable = isQuizAvailable,
            isVocabularyAvailable = isVocabularyAvailable,
            isFlashcardAvailable = isFlashcardAvailable,
            isStarwordsAvailable = isStarwordsAvailable,
            isCrosswordAvailable = isCrosswordAvailable,
            isTranslateAvailable = isTranslateAvailable,
            isNextButtonVisible = isNextMovieHave
        )
    }

    private fun showTemplateAlertDialog(type : Int, buttonType : DialogButtonType, message : String)
    {
        val dialog = TemplateAlertDialog(mContext)
        dialog.setMessage(message)
        dialog.setDialogEventType(type)
        dialog.setButtonType(buttonType)
        dialog.setDialogListener(mDialogListener)
        dialog.show()
    }

    private fun showTemplateAlertDialog(type : Int, firstButtonText : String, secondButtonText : String, message : String)
    {
        val dialog = TemplateAlertDialog(mContext)
        dialog.setMessage(message)
        dialog.setDialogEventType(type)
        dialog.setButtonText(firstButtonText, secondButtonText)
        dialog.setDialogListener(mDialogListener)
        dialog.show()
    }

    private fun showBottomItemOptionDialog(result : ContentsBaseResult)
    {
        pausePlayer()
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(mContext,result)
        mBottomContentItemOptionDialog!!
                .setFullName()
                .setFullScreen()
                .disableBookshelf()
                .disableGame()
                .setItemOptionListener(mItemOptionListener)
                .setView()
                .setOnCancelListener(object : DialogInterface.OnCancelListener
        {
            override fun onCancel(dialog : DialogInterface)
            {
                Log.f("")
                resumePlayer()
                enableTimer(true)
            }
        })
        mBottomContentItemOptionDialog!!.show()
    }

    private fun hideBottomDialog()
    {
        mBottomContentItemOptionDialog?.cancel()
        mBottomBookAddDialog?.cancel()
    }

    private fun showBottomBookAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog!!.setCancelable(true)
        mBottomBookAddDialog!!.setBookshelfData(mMainInformationResult.getBookShelvesList())
        mBottomBookAddDialog!!.setFullScreen()
        mBottomBookAddDialog!!.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog!!.setOnCancelListener(object : DialogInterface.OnCancelListener
        {
            override fun onCancel(dialog : DialogInterface)
            {
                Log.f("")
                resumePlayer()
                enableTimer(true)
            }
        })
        mBottomBookAddDialog!!.show()
    }

    private fun enableLockCountTimer(isStart : Boolean)
    {
        if(isStart)
        {
            if(mLockCountTimer == null)
            {
                mLockCountTimer = Timer()
                mLockCountTimer?.schedule(LockCountDownTask(), 0, Common.DURATION_LONG)
            }
        }
        else
        {
            mLockCountTimer?.cancel()
            mLockCountTimer = null
            mCurrentLockTime = 0
            mPlayerContractView.setLockCountTime(mCurrentLockTime)
        }
    }

    private fun enableTimer(isStart : Boolean)
    {
        if(isStart)
        {
            if(mUIUpdateTimer == null)
            {
                mUIUpdateTimer = Timer()
                mUIUpdateTimer?.schedule(UITimerTask(), 0, Common.DURATION_SHORTEST)
            }
            if(mWarningWatchTimer == null)
            {
                mWarningWatchTimer = Timer()
                mWarningWatchTimer?.schedule(WarningWatchMessageTask(), 0, Common.DURATION_LONG)
            }
        }
        else
        {
            mUIUpdateTimer?.cancel()
            mUIUpdateTimer = null
            mWarningWatchTimer?.cancel()
            mWarningWatchTimer = null
        }
    }

    private fun updateUI()
    {
        if(isVideoPrepared == false)
        {
            return
        }
        mPlayerContractView.setSeekProgress((mPlayer!!.getCurrentPosition().toInt() / Common.SECOND))
        mPlayerContractView.setCurrentMovieTime(CommonUtils.getInstance(mContext).getMillisecondTime(mPlayer!!.getCurrentPosition()))
        if(isAvailableCaption)
        {
            if(mCurrentRepeatPageIndex != -1)
            {
                if(isEndTimeForCurrentPage)
                {
                    mPlayer!!.setPlayWhenReady(false)
                    mPlayerContractView.enablePlayMovie(false)
                    return
                }
            }
            else
            {
                if(isTimeForPageByPage == true)
                {
                    if(mCurrentPageIndex % Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE === 0)
                    {
                        mPlayerContractView.settingCurrentPageLine(mPageByPageDataList[mCurrentPageIndex].getCurrentIndex(), mPageByPageDataList.size)
                    }
                    if(mCurrentPageIndex == 0)
                    {
                        mPlayerContractView.activatePageView(true)
                    }
                    mPlayerContractView.enableCurrentPage(mCurrentPageIndex + 1)
                    mCurrentPageIndex++
                }
            }
        }
        if(isTimeForCaption == true)
        {
            mPlayerContractView.setCaptionText(mAuthContentResult.getCaptionList().get(mCurrentCaptionIndex).getText())
            mCurrentCaptionIndex++
        }
    }

    private fun updatePreviewUI()
    {
        try
        {
            if(isFreeUserLimitedTimeEnd && isPlaying)
            {
                Log.f("Preview End")
                mCurrentPlayerStatus = PlayerStatus.COMPELTE
                enableTimer(false)
                mPlayer!!.setPlayWhenReady(false)
                mPlayer!!.stop(true)
                mPlayerContractView.showPreviewUserEndView()
                return
            }
            val remainTime : Int = mFreeUserPreviewTime - mPlayer!!.getCurrentPosition().toInt() / Common.SECOND
            mPlayerContractView.setRemainPreviewTime(remainTime)
            if(isTimeForCaption == true)
            {
                mPlayerContractView.setCaptionText(mAuthContentResult.getCaptionList().get(mCurrentCaptionIndex).getText())
                mCurrentCaptionIndex++
            }
        }
        catch(e : Exception)
        {
            Log.f("message : " + e.message)
        }
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
                if(visibleTime <= mPlayer!!.getCurrentPosition().toFloat())
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
                if(visibleTime <= mPlayer!!.getCurrentPosition().toFloat())
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
            if(endTime <= mPlayer!!.getCurrentPosition().toFloat())
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
        private get()
        {
            var startTime = 0f
            var endTime = 0f
            if(mAuthContentResult.getCaptionList().size <= 0)
            {
                return -1
            }
            startTime = mAuthContentResult.getCaptionList().get(0).getStartTime()
            if(startTime > mPlayer!!.getCurrentPosition().toFloat())
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
                if(startTime <= mPlayer!!.getCurrentPosition().toFloat()
                    && endTime >= mPlayer!!.getCurrentPosition().toFloat())
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
                if(startTime >= mPlayer!!.getCurrentPosition().toFloat())
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
            if(startTime > mPlayer!!.getCurrentPosition().toFloat())
            {
                return -1
            }
            endTime = mPageByPageDataList[lastItemIndex].getEndTime()
            if(endTime < mPlayer!!.getCurrentPosition().toFloat())
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
                if(startTime <= mPlayer!!.getCurrentPosition().toFloat() && endTime >= mPlayer!!.getCurrentPosition().toFloat())
                {
                    Log.f("startTime : " + startTime + ", curretPosition : " + mPlayer!!.getCurrentPosition().toFloat() + ", endTime : " + endTime)
                    return i
                }
            }
            /**
             * 하지만 , 동요에서 간주는 데이터가 비어버려 데이터를 찾을 수 없으므로 그때는 startTime으로 다시 찾는다.
             */
            for(i in mPageByPageDataList.indices)
            {
                startTime = mPageByPageDataList[i].getStartTime()
                if(startTime >= mPlayer!!.getCurrentPosition().toFloat())
                {
                    Log.f("startTime : " + startTime + ", curretPosition : " + mPlayer!!.getCurrentPosition().toFloat())
                    return i
                }
            }
            return -1
        }



    private fun sendStudyLogSaveAsync()
    {
        if(Feature.IS_FREE_USER)
        {
            return
        }
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
        mStudyLogSaveCoroutine = StudyLogSaveCoroutine(mContext)
        mStudyLogSaveCoroutine?.setData(
            mPlayInformationList[mCurrentSaveLogIndex].getID(),
            autoPlay,
            studyLogSeconds,
            mPlayerIntentParamsObject.getHomeworkNumber())
        mStudyLogSaveCoroutine?.asyncListener = mAsyncListener
        mStudyLogSaveCoroutine?.execute()
    }

    private fun releaseStudyLogSaveAsync()
    {
        Log.f("")
        try
        {
            mStudyLogSaveCoroutine?.cancel()
            mStudyLogSaveCoroutine = null
        }
        catch(e : Exception)
        {
        }
    }

    private fun requestAuthContentPlay()
    {
        Log.f("")
        releaseAuthContentPlay()
        mAuthContentPlayCoroutine = AuthContentPlayCoroutine(mContext)
        mAuthContentPlayCoroutine?.setData(mPlayInformationList[mCurrentPlayMovieIndex].getID())
        mAuthContentPlayCoroutine?.asyncListener = mAsyncListener
        mAuthContentPlayCoroutine?.execute()
    }

    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult>)
    {
        Log.f("")

        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext)
        mBookshelfContentAddCoroutine?.setData(mCurrentBookshelfAddResult.getID(), data)
        mBookshelfContentAddCoroutine?.asyncListener = mAsyncListener
        mBookshelfContentAddCoroutine?.execute()
    }

    private fun releaseAuthContentPlay()
    {
        Log.f("")
        mAuthContentPlayCoroutine?.cancel()
        mAuthContentPlayCoroutine = null
    }

    /**
     * 프리로 보는 시간이 끝났다면 TRUE 아니면 FALSE 를 리턴
     * @return
     */
    private val isFreeUserLimitedTimeEnd : Boolean
        get()
        {
            if(mPlayer!!.getCurrentPosition().toInt() / Common.SECOND >= mFreeUserPreviewTime)
            {
                return true
            }
            else
                return false
        }

    private fun startQuizAcitiviy()
    {
        Log.f("")
        var quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mPlayInformationList[mSelectItemOptionIndex].getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mPlayInformationList[mSelectItemOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        var title = ""
        title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mPlayInformationList[mSelectItemOptionIndex]).toString()
        val myVocabularyResult = MyVocabularyResult(
                mPlayInformationList[mSelectItemOptionIndex].getID(),
                title,
                VocabularyType.VOCABULARY_CONTENTS)
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.VOCABULARY)
                .setData(myVocabularyResult)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(mPlayInformationList[mSelectItemOptionIndex].getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mPlayInformationList[mSelectItemOptionIndex].getID(),
            mPlayInformationList[mSelectItemOptionIndex].getName(),
            mPlayInformationList[mSelectItemOptionIndex].getSubName(),
            VocabularyType.VOCABULARY_CONTENTS
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
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

    override fun onCloseButton()
    {
        Log.f("isAuthorizationComplete : $isAuthorizationComplete, status : $mCurrentPlayerStatus")
        (mContext as AppCompatActivity).onBackPressed()
    }

    override fun onHandlePlayButton()
    {
        if(isPlaying)
        {
            Log.f("pause video")
            mPlayer!!.setPlayWhenReady(false)
            mPlayerContractView.enablePlayMovie(false)
        }
        else
        {
            Log.f("play video")
            if(mCurrentRepeatPageIndex != -1)
            {
                mCurrentRepeatPageIndex = -1
            }
            mPlayer!!.setPlayWhenReady(true)
            mPlayerContractView.enablePlayMovie(true)
        }
    }

    override fun onActivateLockButton()
    {
        Log.f("")
        enableLockCountTimer(true)
    }

    override fun onUnActivateLockButton()
    {
        Log.f("")
        enableLockCountTimer(false)
    }

    override fun onNextButton()
    {
        Log.f("")
        mCurrentPlayMovieIndex++
        enableTimer(false)
        sendStudyLogSaveAsync()
        mMainHandler.sendEmptyMessage(MESSAGE_CHECK_MOVIE)
    }

    override fun onPrevButton()
    {
        Log.f("")
        mCurrentPlayMovieIndex--
        enableTimer(false)
        sendStudyLogSaveAsync()
        mMainHandler.sendEmptyMessage(MESSAGE_CHECK_MOVIE)
    }

    override fun onReplayButton()
    {
        Log.f("")
        enableTimer(false)
        mPlayerContractView.initMovieLayout()
        prepareMovie()
    }

    override fun onNextMovieButton()
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
        mPlayerContractView.initMovieLayout()
        prepareMovie()
    }

    override fun onPageByPageIndex(index : Int)
    {
        mCurrentRepeatPageIndex = index - 1
        mCurrentPageIndex = mCurrentRepeatPageIndex
        mPlayerContractView.setCaptionText("")
        Log.f("repeatIndex : " + mCurrentRepeatPageIndex + ", startTime : " + mPageByPageDataList[mCurrentRepeatPageIndex].getStartTime().toInt())
        mPlayer!!.seekTo(mPageByPageDataList[mCurrentRepeatPageIndex].getStartTime().toLong())
        mPlayer!!.setPlayWhenReady(true)
        mCurrentCaptionIndex = currentCaptionIndex
        mPlayerContractView.enableCurrentPage(index)
        mPlayerContractView.enablePlayMovie(true)
    }

    override fun onCoachMarkNeverSeeAgain(type : String)
    {
        val userID : String = if(Feature.IS_FREE_USER) Common.FREE_USER_NAME else mLoginInformationResult.getUserInformation().getFoxUserID()
        Log.f("userID : $userID , type : $type")
        if(type == Common.CONTENT_TYPE_STORY)
        {
            setStoryCoachmarkViewed(userID)
        }
        else
        {
            setSongCoachmarkViewed(userID);
        }
        prepareMovie()
    }

    override fun onMovePrevPage(startPage : Int)
    {
        val index : Int = startPage - Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE - 1
        Log.f("index : $index")
        mPlayerContractView.settingCurrentPageLine(mPageByPageDataList[index].getCurrentIndex(), mPageByPageDataList.size)
        onPageByPageIndex(startPage - 1)
    }

    override fun onMoveNextPage(lastPage : Int)
    {
        val index = lastPage + 1
        Log.f("index : $index")
        mPlayerContractView.settingCurrentPageLine(mPageByPageDataList[lastPage].getCurrentIndex(), mPageByPageDataList.size)
        onPageByPageIndex(index)
    }

    override  fun onClickCaptionButton(isEnable : Boolean)
    {
        Log.f("isEnable : $isEnable")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, isEnable)
    }

    override fun onClickPageByPageButton(isEnable : Boolean)
    {
        Log.f("isEnable : $isEnable")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, isEnable)
    }

    override fun onStartTrackingSeek()
    {
        Log.f("")
        enableTimer(false)
        mCurrentRepeatPageIndex = -1
        mPlayerContractView.setCaptionText("")
    }

    override fun onStopTrackingSeek(progress : Int)
    {
        mPlayer!!.seekTo((progress * Common.SECOND).toLong())
        mCurrentCaptionIndex = currentCaptionIndex
        val pageIndex = currentPageIndex
        Log.f("progress : $progress, pageIndex : $mCurrentPageIndex")
        if(pageIndex != -1)
        {
            mCurrentPageIndex = pageIndex
            val pageFirstNumber = getFirstIndexOfCurrentPageLine(mCurrentPageIndex) + 1
            mPlayerContractView.settingCurrentPageLine(pageFirstNumber, mPageByPageDataList.size)
            mPlayerContractView.activatePageView(true)
            mPlayerContractView.enableCurrentPage(mCurrentPageIndex + 1)
        }
        else
        {
            mCurrentPageIndex = 0
            mPlayerContractView.activatePageView(false)
        }
        enableTimer(true)
    }

    override fun onChangeOrientation(orientation : Int)
    {
        Log.f("orientation : $orientation")
        var isShowCoachingMark : Boolean = false
        mCurrentOrientation = orientation
        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            hideBottomDialog()
        }
        initPlayList(orientation)
        mPlayerContractView.settingSpeedTextLayout(mCurrentPlaySpeedIndex, false)

        mJob = CoroutineScope(Dispatchers.Main).launch {

            CoroutineScope(Dispatchers.Default).async {
                isShowCoachingMark = isShowCoachmark(mPlayInformationList[mCurrentPlayMovieIndex].getType())
            }.await()

            if(isShowCoachingMark)
            {
                mPlayerContractView.settingCoachmarkView(mPlayInformationList[mCurrentPlayMovieIndex].getType())
            }
        }
    }

    override fun onPaymentButton()
    {
        Log.f("")
        startMainActivityToPayment()
    }

    override fun onRepeatButton()
    {
        isRepeatOn = !isRepeatOn
        Log.f("반복 재생  : $isRepeatOn")
        mPlayerContractView.enableRepeatView(isRepeatOn)
    }

    override fun onClickCurrentMovieOptionButton()
    {
        Log.f("")
        Log.f("option index : $mCurrentPlayMovieIndex")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        showBottomItemOptionDialog(mPlayInformationList[mSelectItemOptionIndex])
    }

    override fun onClickCurrentMovieEbookButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_EBOOK)
    }

    override fun onClickCurrentMovieQuizButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_QUIZ)
    }

    override fun onClickCurrentMovieVocabularyButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_VOCABULARY)
    }

    override fun onClickCurrentMovieTranslateButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_TRANSLATE)
    }

    override fun onClickCurrentMovieStarwordsButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_GAME_STARWORDS)
    }

    override fun onClickCurrentMovieFlashcardButton()
    {
        Log.f("")
        mSelectItemOptionIndex = mCurrentPlayMovieIndex
        mMainHandler.sendEmptyMessage(MESSAGE_START_FLASHCARD)
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String)
        {
        }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_AUTH_CONTENT_PLAY)
                {
                    mAuthContentResult = (`object` as PlayerDataBaseObject).getData()
                    settingPageByPageData()
                    Log.f("Data Success")
                    isAuthorizationComplete = true
                    Log.f("AuthContentPlay result OK")
                    startMovie()
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mPlayerContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)

                    val messsage = Message.obtain()
                    messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                    messsage.obj = mContext!!.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                    messsage.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL)
                    resumePlayer()
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                {
                    //중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if(code == Common.COROUTINE_CODE_AUTH_CONTENT_PLAY)
                    {
                        Log.f("Auth Content data error retry popup")
                        mPlayerContractView.hideMovieLoading()
                        showTemplateAlertDialog(DIALOG_TYPE_WARNING_API_EXCEPTION,
                                mContext.resources.getString(R.string.text_retry),
                                mContext.resources.getString(R.string.text_close),
                                result.getMessage())
                        if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                        {
                            val data = ErrorRequestData(
                                    CrashlyticsHelper.ERROR_CODE_VIDEO_REQUEST,
                                    mPlayInformationList[mCurrentPlayMovieIndex].getID(),
                                    result.getStatus(),
                                    result.getMessage(),
                                    Exception())
                            CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
                        }
                    }
                    else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        Log.f("FAIL ASYNC_CODE_BOOKSHELF_CONTENTS_ADD")
                        mPlayerContractView.hideLoading()
                        val messsage = Message.obtain()
                        messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                        messsage.obj = result.getMessage()
                        messsage.arg1 = Activity.RESULT_CANCELED
                        mMainHandler.sendMessageDelayed(messsage, Common.DURATION_SHORT)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String) {}

        override fun onRunningProgress(code : String, progress : Int) {}

        override fun onRunningAdvanceInformation(code : String, `object` : Any) {}

        override fun onErrorListener(code : String, message : String) {}
    }
    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickQuiz()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_QUIZ, Common.DURATION_SHORT)
        }

        override fun onClickTranslate()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_TRANSLATE, Common.DURATION_SHORT)
        }

        override fun onClickVocabulary()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_VOCABULARY, Common.DURATION_SHORT)
        }

        override fun onClickBookshelf()
        {
            Log.f("")
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList.add(mPlayInformationList[mSelectItemOptionIndex])
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG, Common.DURATION_SHORT)
        }

        override fun onClickEbook()
        {
        }

        override fun onClickGameStarwords()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_STARWORDS, Common.DURATION_SHORT)
        }

        override fun onClickGameCrossword()
        {
        }

        override fun onClickFlashCard()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FLASHCARD, Common.DURATION_SHORT)
        }

        override fun onClickRecordPlayer() { }

        override fun onErrorMessage(message : String)
        {
            Log.f("message : $message")
            resumePlayer()
            enableTimer(true)
            mPlayerContractView.showErrorMessage(message)
        }
    }
    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("index : $index")
            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index)
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_CONTENTS_ADD, Common.DURATION_SHORT)
        }
    }
    private val mOnItemPlayerEventListener : PlayerEventListener = object : PlayerEventListener
    {
        override fun onClickOption(index : Int)
        {
            Log.f("option index : $index")
            mSelectItemOptionIndex = index
            showBottomItemOptionDialog(mPlayInformationList!![mSelectItemOptionIndex])
        }

        override fun onSelectSpeed(index : Int)
        {
            Log.f("speed : " + PLAY_SPEED_LIST[index])
            mCurrentPlaySpeedIndex = index
            setVideoSpeed(mCurrentPlaySpeedIndex)
            mPlayerContractView.settingSpeedTextLayout(mCurrentPlaySpeedIndex, true)
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_PLAYER_SPEED_INDEX, index)
        }

        override fun onItemClick(index : Int)
        {
            Log.f("List select Index : $index")
            mCurrentPlayMovieIndex = index
            enableTimer(false)
            mMainHandler.sendEmptyMessage(MESSAGE_CHECK_MOVIE)
        }
    }
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int) {}

        override fun onChoiceButtonClick(messageButtonType : DialogButtonType, messageType : Int)
        {
            if(messageType == DIALOG_TYPE_WARNING_WATCH_MOVIE)
            {
                when(messageButtonType)
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
                        mPlayer!!.setPlayWhenReady(true)
                    }
                }
            }
            else if(messageType == DIALOG_TYPE_WARNING_API_EXCEPTION)
            {
                when(messageButtonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        //TODO: 재시도
                        Log.f("Auth Content data error retry")
                        mPlayerContractView.showMovieLoading()
                        requestAuthContentPlay()
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        Log.f("Auth Content data error end")
                        (mContext as PlayerHlsActivity).finish()
                    }
                }
            }
        }
    }



}