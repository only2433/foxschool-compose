package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.record.RecordInfoData
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.MILLI_SECOND
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.RecordPlayerContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.record.RecordFileUploadHelper
import com.littlefox.app.foxschool.record.VoiceRecorderHelper
import com.littlefox.app.foxschool.record.listener.VoiceRecordEventListener
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.common.FileUtils
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class RecordPlayerPresenter : RecordPlayerContract.Presenter
{
    companion object
    {
        private const val MAX_RECORDING_TIME : Long             = 600100 // 10분

        private const val MESSAGE_RECORD_TIME_CHECK : Int       = 101
        private const val MESSAGE_PLAY_TIME_CHECK : Int         = 102
        private const val MESSAGE_RECORD_UPLOAD_SUCCESS : Int   = 103
        private const val MESSAGE_RECORD_UPLOAD_FAIL : Int      = 104
        private const val MESSAGE_START_RECORD_HISTORY : Int    = 105
        private const val MESSAGE_RECORD_FILE_MERGED : Int      = 106
        private const val MESSAGE_DUPLICATE_ERROR : Int         = 107
        private const val MESSAGE_AUTH_ERROR : Int              = 108

        private const val DIALOG_RECORD_RESET : Int             = 10001
        private const val DIALOG_WARNING_RECORD_RESET : Int     = 10001
        private const val DIALOG_WARNING_RECORD_EXIT : Int      = 10002
        private const val DIALOG_FILE_UPLOAD_COMPLETE : Int     = 10003

       // val PATH_SDCARD = Environment.getExternalStorageDirectory().absolutePath
       // val PATH_ROOT = "$PATH_SDCARD/LittleFox/Log"
    }

    private lateinit var mContext : Context
    private lateinit var mRecordPlayerContractView : RecordPlayerContract.View
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private lateinit var mRecordInformation : RecordIntentParamsObject
    private lateinit var mUserInformationResult : LoginInformationResult
    private var mCoachingMarkUserDao : CoachmarkDao?    = null
    private var mCurrentUserID : String                 = ""

    // 녹음기 상태
    private var mRecorderStatus : RecorderStatus = RecorderStatus.RECORD_STOP

    // 녹음 기능 관련 변수
    private var mVoiceRecorderHelper : VoiceRecorderHelper? = null
    private val mCurrentPlayerStatus : PlayerStatus = PlayerStatus.STOP
    private var mRecordingPathList : ArrayList<String> = ArrayList()
    private var PATH_MP3_ROOT : String  = ""
    private var mFileName : String      = ""

    // 녹음 시간 타이머 관련 변수
    private var mUIUpdateTimer : Timer? = null
    private var mRecordTotalTime : Long = 0 // milli second
    private var mRecordAddTime : Int = 0


    // 녹음파일 재생 관련 변수
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null

    /**
     * 학습도중 홈버튼을 눌러서 밖으로 빠져 나가게 될때.
     */
    private var isExitToPushHome = false

    // 녹음 파일 업로드
    private var mRecordFileUploadHelper : RecordFileUploadHelper? = null

    private var mCurrentRecordFileNumber : Int = 0

    internal inner class UITimerTask : TimerTask()
    {
        override fun run()
        {
            if (mRecorderStatus == RecorderStatus.RECORD_START)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_RECORD_TIME_CHECK)
            }
            else if (mRecorderStatus == RecorderStatus.AUDIO_PLAY)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_PLAY_TIME_CHECK)
            }
        }
    }

    constructor(context : Context)
    {
        mContext = context
        mRecordPlayerContractView = (mContext as RecordPlayerContract.View).apply {
            initView()
            initFont()
        }
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        mRecordInformation = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_RECORD_PLAYER_DATA)!!
        PATH_MP3_ROOT = mContext.cacheDir.toString() + "/mp3/"
        mFileName = mRecordInformation.getID() // 파일명 : 컨텐츠 ID

        mRecordPlayerContractView.setRecordTitle(mRecordInformation)
        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao()
        mUserInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
        mCurrentUserID = mUserInformationResult.getUserInformation().getFoxUserID()
        CoroutineScope(Dispatchers.Main).launch {
            if(isShowCoachMark(mCurrentUserID))
            {
                mRecordPlayerContractView.setCoachMarkView()
            }
            else
            {
                readyToRecord()
            }
        }
    }

    override fun resume()
    {
        Log.f("status : $mCurrentPlayerStatus")
        if(isExitToPushHome)
        {
            isExitToPushHome = false
        }
    }

    override fun pause()
    {
        Log.f("")
        if (mRecorderStatus == RecorderStatus.RECORD_START)
        {
            isExitToPushHome = true
        }
        onClickRecordPause()
    }

    override fun destroy()
    {
        Log.f("")
        if(mMainHandler.hasMessages(MESSAGE_RECORD_TIME_CHECK))
        {
            mMainHandler.removeMessages(MESSAGE_RECORD_TIME_CHECK)
        }
        if(mMainHandler.hasMessages(MESSAGE_PLAY_TIME_CHECK))
        {
            mMainHandler.removeMessages(MESSAGE_PLAY_TIME_CHECK)
        }
        // TODO 통신 제거
        mMainHandler.removeCallbacksAndMessages(null)
        releaseRecord()
        releaseAudio()
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_RECORD_TIME_CHECK ->
            {
                if (mRecordTotalTime >=  MAX_RECORDING_TIME)
                {
                    // 녹음 진행시간이 최대 녹음시간을 넘어가면 녹음 정지
                    Log.f("MESSAGE_RECORD_TIME_CHECK || recordStop")
                    onClickRecordStop()
                }
                else
                {
                    setTimerText()
                    mRecordTotalTime += MILLI_SECOND
                }
            }
            MESSAGE_PLAY_TIME_CHECK ->
            {
                setAudioTimerText(isEnd = false)
            }
            MESSAGE_RECORD_UPLOAD_SUCCESS ->
            {
                mRecordingPathList.clear()
                showFileUploadCompleteDialog()
                mRecordPlayerContractView.setUploadButtonEnable(false)
            }
            MESSAGE_RECORD_UPLOAD_FAIL ->
            {
                mRecordPlayerContractView.showErrorMessage(mContext.resources.getString(R.string.message_record_upload_fail));
                mRecordPlayerContractView.setUploadButtonEnable(true)
            }
            MESSAGE_START_RECORD_HISTORY ->
            {
                startRecordHistoryActivity()
                (mContext as AppCompatActivity).finish() // 녹음기 화면 종료
            }
            MESSAGE_RECORD_FILE_MERGED ->
            {
                mRecordPlayerContractView.setRecorderStatus(RecorderStatus.AUDIO_STOP)
            }
            MESSAGE_DUPLICATE_ERROR ->
            {
                // 중복 로그인 시 재시작
                (mContext as AppCompatActivity).finish()
                Toast.makeText(mContext, msg.obj as String, Toast.LENGTH_LONG).show()
                IntentManagementFactory.getInstance().initAutoIntroSequence()
            }
            MESSAGE_AUTH_ERROR ->
            {
                Log.f("== isAuthenticationBroken ==")
                (mContext as AppCompatActivity).finish()
                Toast.makeText(mContext, msg.obj as String, Toast.LENGTH_LONG).show()
                IntentManagementFactory.getInstance().initScene()
            }
        }
    }

    /**
     * 첫 가이드 화면 표시해야 하는지 체크
     */
    private suspend fun isShowCoachMark(userID : String) : Boolean
    {
        val data : CoachmarkEntity? = CoroutineScope(Dispatchers.IO).async {
            mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)
        }.await()

        if(data == null)
        {
            Log.f("data null story")
            return true
        }
        else
        {
            Log.f("userID : $mCurrentUserID, data.isRecordCoachmarkViewed : ${data.isRecordCoachmarkViewed}")
            if(data.isRecordCoachmarkViewed)
            {
                return false
            }
            return true
        }
    }

    /**
     * 첫 가이드 이미지 표시된 후 DB 값 세팅 (다시 보지 않도록)
     */
    private fun setRecordCoachMarkViewed(userID : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)

            if(data == null)
            {
                Log.f("data null ")
                data = CoachmarkEntity(userID,
                    isStoryCoachmarkViewed = false,
                    isSongCoachmarkViewed = false,
                    isRecordCoachmarkViewed = true,
                    isFlashcardCoachmarkViewed = false)
                mCoachingMarkUserDao?.insertItem(data)
            }
            else
            {
                Log.f("data update  ")
                data.isRecordCoachmarkViewed = true
                mCoachingMarkUserDao?.updateItem(data)
            }
        }.start()
    }

    override fun enableTimer(isStart : Boolean)
    {
        if(isStart)
        {
            if(mUIUpdateTimer == null)
            {
                mUIUpdateTimer = Timer()
                mUIUpdateTimer?.schedule(UITimerTask(), 0, Common.DURATION_SHORTEST)
            }
        }
        else
        {
            mUIUpdateTimer?.cancel()
            mUIUpdateTimer = null
        }
    }

    /**
     * 녹음 기능 준비
     */
    private fun readyToRecord()
    {
        // 기존에 사용하던 흔적이 있는 경우 사용하던 폴더 제거 후 다시 생성
        val isDirectoryExists = File(PATH_MP3_ROOT).exists()
        if (isDirectoryExists)
        {
            Log.f("기존 파일 삭제 : " + PATH_MP3_ROOT)
            FileUtils.deleteAllFileInPath(PATH_MP3_ROOT)
        }

        val isDirectoryMakeComplete = FileUtils.createDirectory(PATH_MP3_ROOT)
        if(isDirectoryMakeComplete == false)
        {
            Log.f("파일 디렉토리 만들수 없음")
        }

        mVoiceRecorderHelper = VoiceRecorderHelper(mContext)
        mVoiceRecorderHelper!!.setVoiceRecordEventListener(mVoiceRecordEventListener)
    }

    private fun getRecordFileDuration(filePath : String)
    {

        mMediaPlayer = MediaPlayer()
        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            mMediaPlayer!!.let {
                it.setDataSource(filePath)
                it.prepareAsync()
                it.setOnPreparedListener(object : MediaPlayer.OnPreparedListener
                {
                    override fun onPrepared(mediaPlayer : MediaPlayer)
                    {
                        Log.f("mediaPlayer.duration : "+mediaPlayer.duration+", mRecordAddTime : "+mRecordAddTime)
                        mRecordAddTime += mediaPlayer.duration
                        mRecordTotalTime = mRecordAddTime.toLong()
                        setTimerText()
                    }
                })
            }
        } catch(e : Exception)
        {
            Log.f("Exception : "+e)
        }
    }

    /**
     * 오디오 재생 기능 준비
     */
    private fun readyToPlay()
    {
        mMediaPlayer = MediaPlayer()
        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            mMediaPlayer!!.let {
                it.setDataSource("$PATH_MP3_ROOT$mFileName.mp3")
                it.prepareAsync()
                it.setOnPreparedListener(object : MediaPlayer.OnPreparedListener
                {
                    override fun onPrepared(mediaPlayer : MediaPlayer)
                    {
                        Log.f("")
                        mRecordPlayerContractView.hideLoading()
                        setAudioTimerText(isEnd = false)
                    }
                })
                it.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                    override fun onCompletion(mediaPlayer : MediaPlayer)
                    {
                        Log.f("---- End ----")
                        mRecorderStatus = RecorderStatus.AUDIO_PAUSE
                        setAudioPause()
                        setAudioTimerText(isEnd = true)
                        enableTimer(false)
                    }
                })
            }

            Log.f("Recording File Size : ${File("$PATH_MP3_ROOT$mFileName.mp3").length()}")
        } catch(e : Exception)
        {
            Log.f("Exception : "+e)
        }
    }

    /**
     * 화면에 녹음시간 update
     */
    private fun setTimerText()
    {
        mRecordPlayerContractView.setTimerText(CommonUtils.getInstance(mContext).getMillisecondTime(mRecordTotalTime))
    }

    /**
     * 화면에 오디오 재생시간 update
     */
    private fun setAudioTimerText(isEnd : Boolean)
    {
        if (isEnd)
        {
            mRecordPlayerContractView.setAudioPlayTime(mMediaPlayer!!.duration, mMediaPlayer!!.duration)
        }
        else
        {
            mRecordPlayerContractView.setAudioPlayTime(mMediaPlayer!!.currentPosition, mMediaPlayer!!.duration)
        }
    }

    /**
     * 녹음 기능 제거
     */
    private fun releaseRecord()
    {
        if(mVoiceRecorderHelper != null)
        {
            mVoiceRecorderHelper!!.releaseRecording()
        }
        mRecordingPathList.clear()
    }

    /**
     * 오디오 플레이어 제거
     */
    private fun releaseAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    /**
     * 녹음 시작
     */
    private fun setRecorderStart()
    {
        Log.f("")
        // 기존에 녹음한 파일이 있는 경우, 새로운 파일로 녹음 시작 (일시정지 한 다음 다시 녹화하는 경우)
        // 다음 파일의 최대값은 파일 총 사이즈에서 진행된 만큼 뺀 값으로 세팅
        // 리스트에 파일의 PATH를 저장 후 파일 합칠 때 사용
        mCurrentRecordFileNumber = mRecordingPathList.size
        mVoiceRecorderHelper!!.startRecording((MAX_RECORDING_TIME - mRecordTotalTime).toInt(), PATH_MP3_ROOT + "${mFileName}_${mCurrentRecordFileNumber}.mp3")
        mRecordingPathList.add(PATH_MP3_ROOT + "${mFileName}_${mCurrentRecordFileNumber}.mp3")

        mRecordPlayerContractView.setRecorderStatus(RecorderStatus.RECORD_START)
        val percent = (mRecordTotalTime.toFloat() / MAX_RECORDING_TIME.toFloat()) * 100
        mRecordPlayerContractView.startRecordingAnimation(MAX_RECORDING_TIME - mRecordTotalTime, percent.toInt())
        enableTimer(true)
    }

    /**
     * 녹음 일시정지
     */
    private fun setRecorderPause()
    {
        Log.f("")
        mVoiceRecorderHelper!!.stopRecording()
        enableTimer(false)
        mMainHandler.removeMessages(MESSAGE_RECORD_TIME_CHECK)

        mRecordPlayerContractView.setRecorderStatus(RecorderStatus.RECORD_PAUSE)
        val percent = (mRecordTotalTime.toFloat() / MAX_RECORDING_TIME.toFloat()) * 100
        mRecordPlayerContractView.stopRecordingAnimation(percent.toInt())
        setTimerText()
    }

    /**
     * 녹음 정지
     */
    private fun setRecorderStop()
    {
        Log.f("")
        if(mRecordingPathList.size > 0) // 녹음한게 있는 경우
        {
            mVoiceRecorderHelper!!.stopRecording()
            enableTimer(false)
            mMainHandler.removeMessages(MESSAGE_RECORD_TIME_CHECK)
            mRecordPlayerContractView.setRecorderStatus(RecorderStatus.RECORD_MERGE)
            mRecordPlayerContractView.stopRecordingAnimation(0)
            CoroutineScope(Dispatchers.IO).launch {
                mVoiceRecorderHelper!!.mergeMediaFiles(mRecordingPathList, "$PATH_MP3_ROOT$mFileName.mp3")
                mMainHandler.sendEmptyMessage(MESSAGE_RECORD_FILE_MERGED)
            }
        }
    }

    /**
     * 녹음 초기화
     */
    private fun setRecorderReset()
    {
        Log.f("")
        mVoiceRecorderHelper!!.stopRecording()
        enableTimer(false)
        mMainHandler.removeMessages(MESSAGE_RECORD_TIME_CHECK)
        mRecordingPathList.clear()

        mRecordPlayerContractView.setRecorderStatus(RecorderStatus.RECORD_STOP)
        mRecordTotalTime = 0
        mRecordAddTime = 0
        mRecordPlayerContractView.stopRecordingAnimation(0)
        setTimerText()
        mRecordPlayerContractView.setUploadButtonEnable(true)

        FileUtils.deleteAllFileInPath(PATH_MP3_ROOT)
    }

    /**
     * 오디오 재생
     */
    private fun setAudioPlay()
    {
        Log.f("")
        mRecordPlayerContractView.setRecorderStatus(RecorderStatus.AUDIO_PLAY)

        if(mMediaPlayer != null)
        {
            enableTimer(true)
            mMediaPlayer?.start()
            return
        }
        else readyToPlay()
    }

    /**
     * 오디오 재생 일시정지
     */
    private fun setAudioPause()
    {
        Log.f("")
        mRecordPlayerContractView.setRecorderStatus(RecorderStatus.AUDIO_PAUSE)
        if (mMediaPlayer!!.isPlaying) mMediaPlayer!!.pause()
    }

    /**
     * ===============================
     *           다이얼로그
     * ===============================
     */
    /**
     * 녹음 초기화 확인 다이얼로그
     */
    private fun showRecordResetDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.getString(R.string.message_record_reset))
            setDialogEventType(DIALOG_RECORD_RESET)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
        }

    }

    /**
     * 녹음 초기화 경고 다이얼로그
     */
    private fun showRecordResetWarningDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.getString(R.string.message_warning_record_reset))
            setDialogEventType(DIALOG_WARNING_RECORD_RESET)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
        }

    }

    /**
     * 화면 나가기 경고 다이얼로그
     */
    private fun showExitScreenWarningDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.getString(R.string.message_warning_record_exit))
            setDialogEventType(DIALOG_WARNING_RECORD_EXIT)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(mContext.getString(R.string.text_cancel), mContext.getString(R.string.text_leave))
            setDialogListener(mDialogListener)
            show()
        }
    }

    /**
     * 녹음파일 업로드 완료 다이얼로그
     */
    private fun showFileUploadCompleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.getString(R.string.message_record_upload_complete))
            setDialogEventType(DIALOG_FILE_UPLOAD_COMPLETE)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
        }
    }

    /**
     * ===================================
     *          onClick Events
     * ===================================
     */
    /**
     * 첫 가이드 다시 보지 않기
     */
    override fun onCoachMarkNeverSeeAgain()
    {
        Log.f("")
        CoroutineScope(Dispatchers.Main).launch {
            if(isShowCoachMark(mCurrentUserID))
            {
                setRecordCoachMarkViewed(mCurrentUserID)
            }
        }

        // 초기 상태의 경우 녹음기 준비
        if (mRecorderStatus == RecorderStatus.RECORD_STOP)
        {
            readyToRecord()
        }
    }

    /**
     * 닫기 버튼 클릭 이벤트
     */
    override fun onClickClose()
    {
        Log.f("")
        // 일단 화면 일시정지
        if (mRecorderStatus == RecorderStatus.RECORD_START)
        {
            // 녹음중인 경우
            mRecorderStatus = RecorderStatus.RECORD_PAUSE
            setRecorderPause()
        }
        else if (mRecorderStatus == RecorderStatus.AUDIO_PLAY)
        {
            // 오디오 재생인 경우
            mRecorderStatus = RecorderStatus.AUDIO_PAUSE
            setAudioPause()
        }

        if (mRecordingPathList.size > 0) // 녹음을 했지만 파일을 저장하지 않은 상태에서 닫기 버튼 클릭 시
        {
            showExitScreenWarningDialog()
        }
        else
        {
            releaseAudio()
            enableTimer(false)
            mMainHandler.removeMessages(MESSAGE_RECORD_TIME_CHECK)
            mMainHandler.removeMessages(MESSAGE_PLAY_TIME_CHECK)
            (mContext as AppCompatActivity).finish()
        }
    }

    /**
     * 녹음 시작 버튼 클릭 이벤트
     */
    override fun onClickRecordStart()
    {
        Log.f("Recording Selected : START || RecorderStatus : $mRecorderStatus")
        mRecorderStatus = RecorderStatus.RECORD_START
        setRecorderStart()
    }

    /**
     * 녹음 일시정지 버튼 클릭 이벤트
     */
    override fun onClickRecordPause()
    {
        Log.f("Recording Selected : PAUSE || RecorderStatus : $mRecorderStatus")
        if (mRecorderStatus == RecorderStatus.RECORD_START)
        {
            // 녹음중인 경우
            mRecorderStatus = RecorderStatus.RECORD_PAUSE
            setRecorderPause()
        }
        else if (mRecorderStatus == RecorderStatus.AUDIO_PLAY)
        {
            // 오디오 재생인 경우
            mRecorderStatus = RecorderStatus.AUDIO_PAUSE
            setAudioPause()
        }
    }

    /**
     * 녹음 정지 버튼 클릭 이벤트
     */
    override fun onClickRecordStop()
    {
        Log.f("Recording Selected : STOP || RecorderStatus : $mRecorderStatus")
        if (mRecorderStatus == RecorderStatus.RECORD_START || mRecorderStatus == RecorderStatus.RECORD_PAUSE)
        {
            // 녹음중인 경우
            mRecorderStatus = RecorderStatus.AUDIO_STOP
            setRecorderStop()
        }
    }

    /**
     * 녹음 다시하기 버튼 클릭 이벤트
     */
    override fun onClickRecordReset()
    {
        Log.f("Recording Selected : RESET || RecorderStatus : $mRecorderStatus")
        // 녹음 초기화
        if (mRecorderStatus == RecorderStatus.RECORD_START || mRecorderStatus == RecorderStatus.RECORD_PAUSE)
        {
            // 일단 녹음 일시정지
            mRecorderStatus = RecorderStatus.RECORD_PAUSE
            setRecorderPause()

            // 녹음 진행중일 때 누르면 초기화 경고 다이얼로그 표시
            showRecordResetWarningDialog()
        }
        else if (mRecorderStatus == RecorderStatus.AUDIO_PLAY || mRecorderStatus == RecorderStatus.AUDIO_PAUSE || mRecorderStatus == RecorderStatus.AUDIO_STOP)
        {
            // 일단 오디오 일시정지
            mRecorderStatus = RecorderStatus.AUDIO_PAUSE
            setAudioPause()

            // 녹음 초기화 확인 다이얼로그 표시
            showRecordResetDialog()
        }
        else
        {
            mRecorderStatus = RecorderStatus.RECORD_STOP
            setRecorderReset()
            readyToRecord()
        }
    }

    /**
     * 녹음 재생 버튼 클릭 이벤트
     */
    override fun onClickRecordPlay()
    {
        Log.f("Recording Selected : PLAY || RecorderStatus : $mRecorderStatus")
        mRecorderStatus = RecorderStatus.AUDIO_PLAY
        setAudioPlay()
    }

    /**
     * 녹음 업로드 버튼 클릭 이벤트
     */
    override fun onClickRecordUpload()
    {
        Log.f("Recording Selected : UPLOAD || RecorderStatus : $mRecorderStatus")
        mRecordPlayerContractView.showLoading()
        requestRecordFileUpload()
    }

    /**
     * 오디오 재생 위치 이동
     */
    override fun onSeekTo(time : Int)
    {
        mMediaPlayer!!.seekTo(time)
        if(mRecorderStatus == RecorderStatus.AUDIO_PLAY)
        {
            mMediaPlayer?.start()
        }
    }

    /**
     * 녹음기록 화면으로 이동
     */
    private fun startRecordHistoryActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_HISTORY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 녹음파일 업로드
     */
    private fun requestRecordFileUpload()
    {
        val data = RecordInfoData(
            filePath = PATH_MP3_ROOT,
            fileName = "$mFileName.mp3",
            contentsID = mRecordInformation.getID(),
            recordTime = mMediaPlayer!!.duration,
            homeworkNo = mRecordInformation.getHomeworkNumber()
        )

        if (mRecordFileUploadHelper == null)
        {
            mRecordFileUploadHelper = RecordFileUploadHelper(mContext)
            mRecordFileUploadHelper!!.setAsyncListener(mAsyncListener)
        }
        mRecordFileUploadHelper!!.setData(data).build()
    }



    /**
     * 녹음기 이벤트 Listener
     */
    private val mVoiceRecordEventListener : VoiceRecordEventListener = object : VoiceRecordEventListener
    {
        /** 녹음 시작 */
        override fun onStartRecord() { }

        /** 녹음 시간 퍼센트 화면에 노출 */
        override fun onRecordProgress(percent : Int) { }

        /** 녹음 완료 */
        override fun onCompleteRecord()
        {
            getRecordFileDuration(PATH_MP3_ROOT + "${mFileName}_${mCurrentRecordFileNumber}.mp3")
        }

        /** 녹음 실패 */
        override fun inFailure(status : Int, message : String)
        {
            Log.f("status : $status, message : $message")
        }

        /** 파일 결합 완료 */
        override fun onCompleteFileMerged()
        {
            Log.f("Success merging media files.")
            readyToPlay()
        }
    }

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            if (eventType == DIALOG_RECORD_RESET)
            {
                // 녹음 초기화 확인 다이얼로그 (오디오 재생 화면)
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 녹음 초기화 취소 (일시정지 상태로 전환)
                        Log.f("RecordReset Selected : NO || Recorder : PAUSE")
                        if (mRecorderStatus == RecorderStatus.RECORD_PAUSE)
                        {
                            setRecorderPause()  // 녹음상태 였을 때
                        }
                        else if (mRecorderStatus == RecorderStatus.AUDIO_PAUSE)
                        {
                            setAudioPause() // 재생상태 였을 때
                        }
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 녹음 초기화
                        Log.f("RecordReset Selected : YES || Recorder : RESET")
                        mRecorderStatus = RecorderStatus.RECORD_STOP
                        setRecorderReset()
                        readyToRecord()
                    }
                }
            }
            else if (eventType == DIALOG_WARNING_RECORD_RESET)
            {
                // 녹음 초기화 경고 다이얼로그 (녹음 화면)
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 녹음 초기화 취소 (일시정지 상태로 전환)
                        Log.f("RecordReset Selected : NO || Recorder : PAUSE")
                        mRecorderStatus = RecorderStatus.RECORD_PAUSE
                        setRecorderPause()
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 녹음 초기화
                        Log.f("RecordReset Selected : YES || Recorder : RESET")
                        mRecorderStatus = RecorderStatus.RECORD_STOP
                        setRecorderReset()
                        readyToRecord()
                    }
                }
            }
            else if (eventType == DIALOG_WARNING_RECORD_EXIT)
            {
                // 화면 나가기 알림 다이얼로그
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 화면 나가기 취소
                        Log.f("ScreenExit Selected : NO || Recorder : PAUSE")
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 화면 나가기
                        Log.f("ScreenExit Selected : YES || Recorder : RESET")
                        (mContext as AppCompatActivity).finish()
                    }
                }
            }
            else if (eventType == DIALOG_FILE_UPLOAD_COMPLETE)
            {
                // 녹음파일 업로드 완료 다이얼로그
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 현재 화면에 머무르기 (동작없음)
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 녹음 기록 화면으로 이동하기
                        FileUtils.deleteAllFileInPath(PATH_MP3_ROOT)
                        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_RECORD_HISTORY, Common.DURATION_SHORT)
                    }
                }
            }
        }
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            mRecordPlayerContractView.hideLoading()

            val result : BaseResult = mObject as BaseResult
            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if (code.equals(Common.COROUTINE_CODE_CLASS_RECORD_FILE))
                {
                    Log.f("Record File Upload Complete")
                    FileUtils.deleteAllFileInPath(PATH_MP3_ROOT)
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_RECORD_UPLOAD_SUCCESS, Common.DURATION_SHORT)
                }
            }
            else
            {
                if (result.isDuplicateLogin)
                {
                    val message : Message = Message.obtain().apply {
                        what = MESSAGE_DUPLICATE_ERROR
                        obj = result.getMessage()
                    }
                    mMainHandler.sendMessage(message)
                }
                else if (result.isAuthenticationBroken)
                {
                    val message : Message = Message.obtain().apply {
                        what = MESSAGE_AUTH_ERROR
                        obj = result.getMessage()
                    }
                    mMainHandler.sendMessage(message)
                }
                else
                {
                    if (code.equals(Common.COROUTINE_CODE_CLASS_RECORD_FILE))
                    {
                        Log.f("Record File Upload Fail")
                        mMainHandler.sendEmptyMessage(MESSAGE_RECORD_UPLOAD_FAIL)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}