package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.RecorderStatus
import com.littlefox.app.foxschool.main.contract.RecordPlayerContract
import com.littlefox.app.foxschool.main.presenter.RecordPlayerPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.progress.CircularProgressBar
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 녹음기 화면
 * @author 김태은
 */
class RecordPlayerActivity : BaseActivity(), MessageHandlerCallback, RecordPlayerContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._coachmarkImage)
    lateinit var _CoachmarkImage : ImageView

    @BindView(R.id._recordContentsLayout)
    lateinit var _RecordContentsLayout : ScalableLayout

    @BindView(R.id._recordInfoText)
    lateinit var _RecordInfoText : TextView

    @BindView(R.id._recordInfoButton)
    lateinit var _RecordInfoButton : ImageView

    @BindView(R.id._recordTitleText)
    lateinit var _RecordTitleText : TextView

    @BindView(R.id._recordTimerText)
    lateinit var _RecordTimerText : TextView

    @BindView(R.id._recordingMicBackground)
    lateinit var _RecordingMicBackground : ImageView

    @BindView(R.id._recordingProgressView)
    lateinit var _RecordingProgressView : CircularProgressBar

    @BindView(R.id._recordProgressCircleImageView)
    lateinit var _RecordProgressCircleImageView : ScalableLayout

    @BindView(R.id._recordProgressImageView)
    lateinit var _RecordProgressImageView : ImageView

    @BindView(R.id._recordingFrameImage)
    lateinit var _RecordingFrameImage : ImageView

    @BindView(R.id._recordMicImage)
    lateinit var _RecordMicImage : ImageView

    @BindView(R.id._recordSpeakerImage)
    lateinit var _RecordSpeakerImage : ImageView

    @BindView(R.id._recordStartText)
    lateinit var _RecordStartText : TextView

    @BindView(R.id._recordControllerLayout)
    lateinit var _RecordControllerLayout : ScalableLayout

    @BindView(R.id._recordResetButton)
    lateinit var _RecordResetButton : ImageView

    @BindView(R.id._recordResetButtonRect)
    lateinit var _RecordResetButtonRect : ImageView

    @BindView(R.id._recordStartButton)
    lateinit var _RecordStartButton : ImageView

    @BindView(R.id._recordPauseButton)
    lateinit var _RecordPauseButton : ImageView

    @BindView(R.id._recordPlayButton)
    lateinit var _RecordPlayButton : ImageView

    @BindView(R.id._recordStopButton)
    lateinit var _RecordStopButton : ImageView

    @BindView(R.id._recordStopButtonRect)
    lateinit var _RecordStopButtonRect : ImageView

    @BindView(R.id._recordUploadButton)
    lateinit var _RecordUploadButton : ImageView

    @BindView(R.id._recordUploadButtonRect)
    lateinit var _RecordUploadButtonRect : ImageView

    @BindView(R.id._audioSeekerBarView)
    lateinit var _AudioSeekerBarView : ScalableLayout

    @BindView(R.id._audioCurrentPlayTime)
    lateinit var _AudioCurrentPlayTime : TextView

    @BindView(R.id._audioSeekbarPlayBar)
    lateinit var _AudioSeekbarPlayBar : SeekBar

    @BindView(R.id._audioPlayFullTime)
    lateinit var _AudioPlayFullTime : TextView


    companion object
    {
        private const val MESSAGE_ENABLE_STOP_MEDIA_RECORDER : Int = 100
    }

    private lateinit var mRecordPlayerPresenter : RecordPlayerPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mFrameAnimationDrawable : AnimationDrawable? = null
    private var isEnableStopMediaRecorder : Boolean = true // 녹음 정지버튼 클릭 가능 여부

    private val mMainHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_ENABLE_STOP_MEDIA_RECORDER -> isEnableStopMediaRecorder = true
            }
        }
    }

    /** ========== LifeCycle ========== */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_record_player_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_record_player)
        }
        ButterKnife.bind(this)

        mRecordPlayerPresenter = RecordPlayerPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mRecordPlayerPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mRecordPlayerPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mRecordPlayerPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    override fun initView()
    {
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.text_recorder)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
        _RecordProgressCircleImageView.visibility = View.GONE
        if (CommonUtils.getInstance(this).checkTablet)
        {
            _RecordingProgressView.setProgressWidth(CommonUtils.getInstance(this).getPixel(6))
        }
        else
        {
            _RecordingProgressView.setProgressWidth(CommonUtils.getInstance(this).getPixel(10))
        }
        _RecordingProgressView.setProgressColor(resources.getColor(R.color.color_19a0f7))
        _RecordingProgressView.useRoundedCorners(false)
    }

    override fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(this).getRobotoBold())
        _RecordInfoText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _RecordTitleText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _RecordTimerText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _RecordStartText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _AudioCurrentPlayTime.setTypeface(Font.getInstance(this).getRobotoRegular())
        _AudioPlayFullTime.setTypeface(Font.getInstance(this).getRobotoRegular())
    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _TitleBaselayout.setBackgroundColor(resources.getColor(backgroundColor))
    }

    /** ========== Init end ========== */

    override fun handlerMessage(message : Message)
    {
        when(message.what)
        {
            MESSAGE_ENABLE_STOP_MEDIA_RECORDER -> isEnableStopMediaRecorder = true
        }
        mRecordPlayerPresenter.sendMessageEvent(message)
    }

    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    override fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    /**
     * 컨텐츠 제목 세팅
     */
    override fun setRecordTitle(contents : ContentsBaseResult)
    {
        _RecordTitleText.setText(CommonUtils.getInstance(this).getContentsName(contents))
    }

    /**
     * 코치마크 뷰 세팅
     */
    override fun setCoachMarkView()
    {
        if (_CoachmarkImage.isShown)
        {
            _CoachmarkImage.visibility = View.GONE
            mRecordPlayerPresenter.onCoachMarkNeverSeeAgain()
        }
        else
        {
            _CoachmarkImage.visibility = View.VISIBLE
        }
    }

    /**
     * 녹음 상태에 따른 화면 세팅
     */
    override fun setRecorderStatus(status : RecorderStatus)
    {
        _RecordStartText.visibility = View.GONE
        setControllerView(status)
        setRecordingView(status)
    }

    /**
     * 녹음 상태에 따른 뷰 세팅
     */
    private fun setRecordingView(status : RecorderStatus)
    {
        Log.f("setRecordingView : $status")
        when(status)
        {
            RecorderStatus.RECORD_STOP ->
            {
                // 녹음 정지 (녹음 초기상태)
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_cacaca))

                _RecordingProgressView.setProgressColor(resources.getColor(R.color.color_a4abb3))
                _RecordingProgressView.cancelAnimation()
                _RecordingProgressView.animate().cancel()
                _RecordProgressImageView.setImageResource(R.drawable.progress_circle_off)
                _RecordProgressCircleImageView.clearAnimation()
                _RecordProgressCircleImageView.animate().cancel()
                stopFrameAnimation()

                _RecordMicImage.visibility = View.VISIBLE
                _RecordStartText.visibility = View.VISIBLE
                _AudioSeekerBarView.visibility = View.GONE
                _RecordSpeakerImage.visibility = View.GONE
                _RecordingProgressView.visibility = View.GONE
                _RecordProgressImageView.visibility = View.GONE
                _RecordProgressCircleImageView.visibility = View.GONE
            }
            RecorderStatus.RECORD_START ->
            {
                // 녹음 시작
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_444444))

                _RecordingProgressView.setProgressColor(resources.getColor(R.color.color_19a0f7))
                _RecordProgressImageView.setImageResource(R.drawable.progress_circle_on)
                startFrameAnimation()

                _RecordingProgressView.visibility = View.VISIBLE
                _RecordProgressImageView.visibility = View.VISIBLE
                _RecordProgressCircleImageView.visibility = View.VISIBLE
            }
            RecorderStatus.RECORD_PAUSE ->
            {
                // 녹음 일시정지
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_cacaca))

                _RecordingProgressView.setProgressColor(resources.getColor(R.color.color_a4abb3))
                _RecordingProgressView.cancelAnimation()
                _RecordProgressImageView.setImageResource(R.drawable.progress_circle_off)
                stopFrameAnimation()
            }
            RecorderStatus.AUDIO_STOP ->
            {
                // 오디오 준비 (오디오 초기상태)
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_cacaca))

                _RecordingProgressView.setProgressColor(resources.getColor(R.color.color_a4abb3))
                _RecordingProgressView.cancelAnimation()
                _RecordingProgressView.animate().cancel()
                _RecordProgressCircleImageView.clearAnimation()
                _RecordProgressCircleImageView.animate().cancel()
                stopFrameAnimation()

                _RecordMicImage.visibility = View.GONE
                _AudioSeekerBarView.visibility = View.VISIBLE
                _RecordSpeakerImage.visibility = View.VISIBLE
                _RecordingProgressView.visibility = View.GONE
                _RecordProgressImageView.visibility = View.GONE
                _RecordProgressCircleImageView.visibility = View.GONE

                setSeekBar()
            }
            RecorderStatus.AUDIO_PLAY ->
            {
                // 오디오 재생
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_444444))
                startFrameAnimation()
            }
            RecorderStatus.AUDIO_PAUSE ->
            {
                // 오디오 일시정지
                _RecordTimerText.setTextColor(this.resources.getColor(R.color.color_cacaca))
                stopFrameAnimation()
            }
        }
    }

    /**
     * 녹음 상태에 따른 컨트롤러 뷰 세팅
     */
    private fun setControllerView(status : RecorderStatus)
    {
        Log.f("setControllerView : $status")
        _RecordResetButton.visibility = View.VISIBLE
        _RecordResetButtonRect.visibility = View.VISIBLE
        _RecordStartButton.clearAnimation()
        _RecordStartButton.animate().cancel()

        when(status)
        {
            RecorderStatus.RECORD_STOP ->
            {
                // 녹음 정지 (녹음 초기상태)
                _RecordStartButton.visibility = View.VISIBLE
                _RecordPauseButton.visibility = View.GONE
                _RecordPlayButton.visibility = View.GONE

                _RecordResetButton.visibility = View.GONE
                _RecordResetButtonRect.visibility = View.GONE
                _RecordStopButton.visibility = View.GONE
                _RecordStopButtonRect.visibility = View.GONE
                _RecordUploadButton.visibility = View.GONE
                _RecordUploadButtonRect.visibility = View.GONE
            }
            RecorderStatus.RECORD_START ->
            {
                // 녹음 시작
                _RecordStartButton.visibility = View.GONE
                _RecordPauseButton.visibility = View.VISIBLE
                _RecordPlayButton.visibility = View.GONE

                _RecordStopButton.visibility = View.VISIBLE
                _RecordStopButtonRect.visibility = View.VISIBLE
                _RecordUploadButton.visibility = View.GONE
                _RecordUploadButtonRect.visibility = View.GONE
            }
            RecorderStatus.RECORD_PAUSE ->
            {
                // 녹음 일시정지
                _RecordStartButton.visibility = View.VISIBLE
                _RecordPauseButton.visibility = View.GONE
                _RecordPlayButton.visibility = View.GONE

                _RecordStopButton.visibility = View.VISIBLE
                _RecordStopButtonRect.visibility = View.VISIBLE
                _RecordUploadButton.visibility = View.GONE
                _RecordUploadButtonRect.visibility = View.GONE

                // 녹음버튼 반짝반짝 애니메이션
                val anim = CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 1.0f, 0.5f)
                anim!!.repeatCount = Animation.INFINITE
                anim.repeatMode = Animation.REVERSE
                _RecordStartButton.startAnimation(anim)
            }
            RecorderStatus.AUDIO_STOP, RecorderStatus.AUDIO_PAUSE ->
            {
                // 오디오 정지 (오디오 초기상태), 일시정지
                _RecordStartButton.visibility = View.GONE
                _RecordPauseButton.visibility = View.GONE
                _RecordPlayButton.visibility = View.VISIBLE

                _RecordStopButton.visibility = View.GONE
                _RecordStopButtonRect.visibility = View.GONE
                _RecordUploadButton.visibility = View.VISIBLE
                _RecordUploadButtonRect.visibility = View.VISIBLE
            }
            RecorderStatus.AUDIO_PLAY ->
            {
                // 오디오 재생
                _RecordStartButton.visibility = View.GONE
                _RecordPauseButton.visibility = View.VISIBLE
                _RecordPlayButton.visibility = View.GONE

                _RecordStopButton.visibility = View.GONE
                _RecordStopButtonRect.visibility = View.GONE
                _RecordUploadButton.visibility = View.VISIBLE
                _RecordUploadButtonRect.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 오디오 재생 바 초기 세팅
     */
    private fun setSeekBar()
    {
        _AudioSeekbarPlayBar.thumbOffset = CommonUtils.getInstance(this).getPixel(0)
        _AudioSeekbarPlayBar.progress = 0
        _AudioSeekbarPlayBar.secondaryProgress = 0
        _AudioSeekbarPlayBar.setOnSeekBarChangeListener(mOnSeekBarListener)
    }

    /**
     * 타이머 텍스트 변경
     */
    override fun setTimerText(time : String)
    {
        _RecordTimerText.setText(time)
    }

    /**
     * 오디오 재생 바 세팅
     */
    override fun setAudioPlayTime(currentTime : Int, maxTime : Int)
    {
        _AudioSeekbarPlayBar.progress = currentTime / Common.SECOND
        _AudioSeekbarPlayBar.max = maxTime / Common.SECOND
        _RecordTimerText.setText(CommonUtils.getInstance(this).getMillisecondTime(currentTime.toLong()))
        _AudioCurrentPlayTime.setText(CommonUtils.getInstance(this).getMillisecondTime(currentTime.toLong()))
        _AudioPlayFullTime.setText(CommonUtils.getInstance(this).getMillisecondTime(maxTime.toLong()))
    }

    /**
     * 녹음 프레임 애니메이션 시작
     */
    private fun startFrameAnimation()
    {
        Log.f("")
        if(mFrameAnimationDrawable == null)
        {
            _RecordingFrameImage.setBackgroundResource(R.drawable.frame_animation_speek_wave)
            mFrameAnimationDrawable = _RecordingFrameImage.getBackground() as AnimationDrawable
        }
        _RecordingFrameImage.setVisibility(View.VISIBLE)
        mFrameAnimationDrawable!!.start()
    }

    /**
     * 녹음 프레임 애니메이션 정지
     */
    private fun stopFrameAnimation()
    {
        Log.f("")
        try
        {
            if(mFrameAnimationDrawable != null)
            {
                mFrameAnimationDrawable!!.stop()
                mFrameAnimationDrawable = null
                _RecordingFrameImage.setVisibility(View.GONE)
            }
        } catch(e : Exception)
        {
        }
    }

    /**
     * 프로그레스 애니메이션 시작/재시작
     */
    override fun startRecordingAnimation(duration : Long, percent : Int)
    {
        Log.f("startRecordingAnimation")
        _RecordingProgressView.setMaxProgress(duration.toInt())
        val position = 360f * (percent.toFloat() * 0.01f)
        _RecordProgressCircleImageView.startAnimation(CommonUtils.getInstance(this).getRotateAnimation(duration, position, 360f))
    }

    /**
     * 프로그레스 애니메이션 멈춤 (현재 위치 고정)
     */
    override fun stopRecordingAnimation(percent : Int)
    {
        Log.f("stopRecordingAnimation")
        _RecordingProgressView.setProgress(percent)
        _RecordProgressCircleImageView.clearAnimation()
        val position = 360f * (percent.toFloat() * 0.01f)
        _RecordProgressCircleImageView.startAnimation(CommonUtils.getInstance(this).getRotateAnimation(0, position, position))
    }

    /**
     * 녹음파일 업로드 완료
     */
    override fun setUploadButtonEnable(isEnable : Boolean)
    {
        _RecordUploadButtonRect.isEnabled = isEnable
        _RecordUploadButton.isEnabled = isEnable
        _RecordUploadButton.alpha = if (isEnable) 1.0f else 0.5f
    }

    /**
     * 녹음 시작 버튼 클릭 이벤트
     */
    private fun onClickRecordStart()
    {
        Log.f("onClickRecordStart")
        // 녹음 시작 버튼 누른 후 딜레이를 통해 일정시간 이후에 정지, 일시정지가 가능하도록 처리
        // 너무 짧게 녹음되면 파일이 제대로 생성되지 못하고, 나중에 파일을 결합하는 과정에서 문제가 발생함
        isEnableStopMediaRecorder = false
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_ENABLE_STOP_MEDIA_RECORDER, Common.DURATION_LONG)
        mRecordPlayerPresenter.onClickRecordStart()
    }

    /**
     * 녹음 일시정지 버튼 클릭 이벤트
     */
    private fun onClickRecordPause()
    {
        if (isEnableStopMediaRecorder)
        {
            Log.f("onClickRecordPause")
            mRecordPlayerPresenter.onClickRecordPause()
        }
    }

    /**
     * 녹음 정지 버튼 클릭 이벤트
     */
    private fun onClickRecordStop()
    {
        if (isEnableStopMediaRecorder)
        {
            Log.f("onClickRecordStop")
            mRecordPlayerPresenter.onClickRecordStop()
        }
    }

    /**
     * 녹음 다시하기 버튼 클릭 이벤트
     */
    private fun onClickRecordReset()
    {
        Log.f("onClickRecordReset")
        if (isEnableStopMediaRecorder)
        {
            mRecordPlayerPresenter.onClickRecordReset()
        }
    }

    /**
     * 녹음 재생하기 버튼 클릭 이벤트
     */
    private fun onClickRecordPlay()
    {
        Log.f("onClickRecordPlay")
        mRecordPlayerPresenter.onClickRecordPlay()
    }

    /**
     * 녹음 업로드 버튼 클릭 이벤트
     */
    private fun onClickRecordUpload()
    {
        Log.f("onClickRecordUpload")
        mRecordPlayerPresenter.onClickRecordUpload()
    }

    @Optional
    @OnClick(R.id._closeButtonRect, R.id._recordInfoButton, R.id._coachmarkImage, R.id._recordResetButtonRect, R.id._recordStopButtonRect,
        R.id._recordStartButton, R.id._recordPauseButton, R.id._recordPlayButton, R.id._recordUploadButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> mRecordPlayerPresenter.onClickClose()
            R.id._recordInfoButton, R.id._coachmarkImage -> setCoachMarkView()
            R.id._recordStartButton -> onClickRecordStart()
            R.id._recordPauseButton -> onClickRecordPause()
            R.id._recordStopButtonRect -> onClickRecordStop()
            R.id._recordResetButtonRect -> onClickRecordReset()
            R.id._recordPlayButton -> onClickRecordPlay()
            R.id._recordUploadButtonRect -> onClickRecordUpload()
        }
    }

    private val mOnSeekBarListener : SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar : SeekBar, progress : Int, fromUser : Boolean) { }

        override fun onStartTrackingTouch(seekBar : SeekBar)
        {
            mRecordPlayerPresenter.enableTimer(false)
        }

        override fun onStopTrackingTouch(seekBar : SeekBar)
        {
            mRecordPlayerPresenter.onSeekTo((seekBar.progress * Common.SECOND))
            mRecordPlayerPresenter.enableTimer(isStart = true)
        }
    }
}