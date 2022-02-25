package com.littlefox.app.foxschool.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*


import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.library.view.dialog.ProgressWheel
import com.littlefox.logmonitor.Log
import java.io.IOException
import java.util.*

class AudioPlayDialog : Dialog
{
    internal inner class UITimerTask : TimerTask()
    {
        override fun run()
        {
            mDialogHandler.sendEmptyMessage(MESSAGE_UI_UPDATE)
        }

    }

    var mDialogHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_UI_UPDATE -> updateUI()
            }
        }
    }

    companion object
    {
        private const val TEST_TITLE : String = "Where Am I? 4"
        private const val TEST_THUMB_URL : String = "https://img.littlefox.co.kr/static/contents/img/C0006514/notext_ipad.jpg?1559537329"
        private const val TEST_AUDIO_URL : String = "https://cdn.littlefox.co.kr/contents/ebook/mp3/whereami04_movie.mp3"
        private const val DIALOG_WIDTH : Int = 1020
        private const val MESSAGE_UI_UPDATE : Int  = 100
    }

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._thumbnailImage)
    lateinit var _ThumbnailImage : ImageView

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._playerCurrentPlayTime)
    lateinit var _PlayerCurrentPlayTime : TextView

    @BindView(R.id._seekbarPlayBar)
    lateinit var _SeekbarPlayBar : SeekBar

    @BindView(R.id._playerRemainPlayTime)
    lateinit var _PlayerRemainPlayTime : TextView

    @BindView(R.id._progressWheelView)
    lateinit var _ProgressWheelView : ProgressWheel

    @BindView(R.id._playButton)
    lateinit var _PlayButton : ImageView

    private val mContext : Context
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null
    private var mUIUpdateTimer : Timer? = null

    private var mTitle : String         = ""
    private var mThumbnailUrl : String  = ""
    private var mAudioPath : String     = ""
    private var isPrepareComplete : Boolean = false

    constructor(context : Context, title : String, thumbnailUrl : String, audioPath : String) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_audio_play)
        ButterKnife.bind(this)
        mContext = context

        mTitle = title
        mThumbnailUrl = thumbnailUrl
        mAudioPath = audioPath

        Log.f("title : "+ mTitle)
        Log.f("thumbnail : "+ mThumbnailUrl)
        Log.f("audio Path : "+ mAudioPath)
    }

    protected override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        var params : WindowManager.LayoutParams = getWindow()!!.attributes.apply {
            width = CommonUtils.getInstance(mContext).getPixel(DIALOG_WIDTH)
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
            windowAnimations = R.style.DialogScaleAnimation
        }
        getWindow()!!.attributes = params
        getWindow()!!.decorView.setBackgroundColor(Color.TRANSPARENT)

        initView()
        initFont()
        initSeekbar()
        startAudio()
    }

    private fun initView()
    {
        Glide.with(mContext)
            .load(mThumbnailUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(_ThumbnailImage)
        _TitleText.setText(mTitle)
    }

    private fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _PlayerCurrentPlayTime.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _PlayerRemainPlayTime.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
    }

    private fun initSeekbar()
    {
        _SeekbarPlayBar.run {
            thumbOffset = CommonUtils.getInstance(mContext).getPixel(0)
            progress = 0
            secondaryProgress = 0
            setOnSeekBarChangeListener(mOnSeekBarListener)
        }
    }

    override fun dismiss()
    {
        releaseAudio()
        enableTimer(false)
        mDialogHandler.removeMessages(MESSAGE_UI_UPDATE)
        super.dismiss()
    }

    private fun startAudio()
    {
        Log.f("")
        if(mMediaPlayer != null)
            mMediaPlayer?.reset()
        else
            mMediaPlayer = MediaPlayer()

        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(
                        AudioAttributes.USAGE_MEDIA)
                        .build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mMediaPlayer?.setDataSource(mAudioPath)
            mMediaPlayer?.prepareAsync()
            mMediaPlayer?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener
            {
                override fun onPrepared(mediaPlayer : MediaPlayer)
                {
                    Log.f("")
                    isPrepareComplete = true
                    setRemainDuration()
                    _ProgressWheelView.visibility = View.GONE
                    _PlayButton.visibility = View.VISIBLE
                    enableTimer(isStart = true)
                    mMediaPlayer?.start()
                }
            })
            mMediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(mediaPlayer : MediaPlayer)
                {
                    Log.f("---- End ----")
                    if(isPrepareComplete)
                    {
                        isPrepareComplete = false
                        setMaxDuration()
                        enableTimer(false)
                    }
                    else
                    {
                        Toast.makeText(mContext,
                            mContext.resources.getString(R.string.message_warning_record_empty),
                            Toast.LENGTH_SHORT).show()

                        releaseAudio()
                        dismiss()
                    }
                }
            })
        }catch(e : Exception)
        {
            Log.f("Exception : "+e)
        }
    }

    private fun releaseAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    private fun updateUI()
    {
        _SeekbarPlayBar.progress = mMediaPlayer!!.currentPosition / Common.SECOND
        _PlayerCurrentPlayTime.setText(CommonUtils.getInstance(mContext).getMillisecondTime(mMediaPlayer!!.currentPosition.toLong()))

    }

    private fun setMaxDuration()
    {
        _SeekbarPlayBar.progress = mMediaPlayer!!.duration / Common.SECOND
        _PlayerCurrentPlayTime.setText(CommonUtils.getInstance(mContext).getMillisecondTime(mMediaPlayer!!.duration.toLong()))
    }

    private fun setRemainDuration()
    {
        _SeekbarPlayBar.max = ((mMediaPlayer!!.duration / Common.SECOND))
        _PlayerRemainPlayTime.setText(CommonUtils.getInstance(mContext).getMillisecondTime(mMediaPlayer!!.duration.toLong()))
    }

    private fun enableTimer(isStart : Boolean)
    {
        if(isStart)
        {
            if(mUIUpdateTimer == null)
            {
                mUIUpdateTimer = Timer()
                mUIUpdateTimer?.schedule(UITimerTask(), 0, Common.DURATION_SHORTEST)
                enablePlayIcon(isPlaying = true)
            }
        }
        else
        {
            mUIUpdateTimer?.cancel()
            mUIUpdateTimer = null
            enablePlayIcon(isPlaying = false)
        }
    }

    private fun enablePlayIcon(isPlaying : Boolean)
    {
        if(isPlaying)
        {
            _PlayButton.setImageResource(R.drawable.icon_audio_pause)
        }
        else
        {
            _PlayButton.setImageResource(R.drawable.icon_audio_play)
        }
    }

    @OnClick(R.id._closeButtonRect, R.id._playButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect ->
            {
                dismiss()
            }
            R.id._playButton ->
            {
                if(mMediaPlayer?.isPlaying == true)
                {
                    mMediaPlayer?.pause()
                    enableTimer(isStart = false)
                }
                else
                {
                    mMediaPlayer?.start()
                    enableTimer(isStart = true)
                }
            }
        }
    }

    private val mOnSeekBarListener : SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar : SeekBar, progress : Int, fromUser : Boolean)
        {

        }

        override fun onStartTrackingTouch(seekBar : SeekBar)
        {
            enableTimer(false)
        }

        override fun onStopTrackingTouch(seekBar : SeekBar)
        {
            mMediaPlayer!!.seekTo((seekBar.progress * Common.SECOND))

            if(mMediaPlayer?.isPlaying == false)
                mMediaPlayer?.start()
            enableTimer(isStart = true)
        }
    }
}
