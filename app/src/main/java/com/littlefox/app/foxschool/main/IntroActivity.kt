package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.IntroViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.IntroContract
import com.littlefox.app.foxschool.main.presenter.IntroPresenter
import com.littlefox.app.foxschool.view.ProgressBarAnimation
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels

@AndroidEntryPoint
class IntroActivity : BaseActivity(), MessageHandlerCallback, IntroContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._introBaseLayout)
    lateinit var _IntroBaseLayout : RelativeLayout

    @BindView(R.id._logoTextImage)
    lateinit var  _IntroLogoTextImage : ImageView

    @BindView(R.id._introProgressPercent)
    lateinit var _IntroProgressPercent : ProgressBar

    @BindView(R.id._introProgressText)
    lateinit var _IntroProgressText : TextView

    @BindView(R.id._progressLayout)
    lateinit var _ProgressLayout : ScalableLayout

    @BindView(R.id._frameAnimationLayout)
    lateinit var _FrameAnimationLayout : ScalableLayout

    @BindView(R.id._frameAnimationView)
    lateinit var _FrameAnimationView : ImageView

    @BindView(R.id._introItemSelectLayout)
    lateinit var _IntroItemSelectLayout : ScalableLayout

    @BindView(R.id._introMessageText)
    lateinit var _IntroTitleText : TextView

    @BindView(R.id._introduceText)
    lateinit var _IntroduceTextButton : TextView

    @BindView(R.id._loginText)
    lateinit var _LoginTextButton : TextView

    companion object
    {
        private const val SYSTEM_DIALOG_REASON_KEY : String         = "reason"
        private const val SYSTEM_DIALOG_REASON_HOME_KEY : String    = "homekey"
    }

    private lateinit var mIntroPresenter : IntroContract.Presenter
    private lateinit var mProgressBarAnimation : ProgressBarAnimation
    private var mFrameAnimationDrawable : AnimationDrawable? = null
    private var mHomeKeyIntentFilter : IntentFilter? = null

    private val viewModel: IntroViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).isTabletModel)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_intro_tablet)
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_intro)
        }
        ButterKnife.bind(this)
        mIntroPresenter = IntroPresenter(this, viewModel)
        mIntroPresenter.onAddActivityResultLaunchers(mLoginActivityResult)
        mHomeKeyIntentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    }

    override fun onResume()
    {
        super.onResume()
        mIntroPresenter.resume()
        registerReceiver(mBroadcastReceiver, mHomeKeyIntentFilter)
    }

    override fun onPause()
    {
        super.onPause()
        mIntroPresenter.pause()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onStop()
    {
        Log.f("")
        super.onStop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mIntroPresenter.destroy()
        stopFrameAnimation()
    }

    override fun initView()
    {
        _IntroLogoTextImage.setOnTouchListener(mLogoTouchListener)
    }

    override fun initFont()
    {
        _IntroTitleText.setTypeface(Font.getInstance(this).getTypefaceBold())
        _IntroProgressText.setTypeface(Font.getInstance(this).getTypefaceBold())
        _IntroduceTextButton.setTypeface(Font.getInstance(this).getTypefaceBold())
        _LoginTextButton.setTypeface(Font.getInstance(this).getTypefaceBold())


    }

    override fun handlerMessage(message : Message)
    {
        mIntroPresenter.sendMessageEvent(message)
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message, Gravity.CENTER)
    }
    override fun showErrorMessage(message : String) {}

    override fun onUserLeaveHint()
    {
        super.onUserLeaveHint()
        Log.f("")
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mIntroPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent : Intent?)
    {
        Log.i("")
        super.onNewIntent(intent)
    }

    override fun showToast(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    override fun showItemSelectView()
    {
        Log.f("")
        _IntroItemSelectLayout.setVisibility(View.VISIBLE)
        _ProgressLayout.setVisibility(View.GONE)
        _FrameAnimationLayout.setVisibility(View.GONE)
    }

    override fun showProgressView()
    {
        Log.f("")
        _IntroItemSelectLayout.setVisibility(View.GONE)
        _ProgressLayout.setVisibility(View.VISIBLE)
        _FrameAnimationLayout.setVisibility(View.VISIBLE)
        startFrameAnimation()
    }

    override fun setProgressPercent(fromPercent : Float, toPercent : Float)
    {
        mProgressBarAnimation = ProgressBarAnimation(_IntroProgressPercent, _IntroProgressText, fromPercent, toPercent)
        mProgressBarAnimation.duration = Common.DURATION_SHORT_LONG
        _IntroProgressPercent.startAnimation(mProgressBarAnimation)
    }

    private fun startFrameAnimation()
    {
        if(mFrameAnimationDrawable == null)
        {
            _FrameAnimationView.setBackgroundResource(R.drawable.frame_animation_intro)
            mFrameAnimationDrawable = _FrameAnimationView.background as AnimationDrawable
        }
        mFrameAnimationDrawable?.start()
    }

    private fun stopFrameAnimation()
    {
        mFrameAnimationDrawable?.stop()
        mFrameAnimationDrawable = null
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
    }

    @OnClick( R.id._introduceText, R.id._loginText)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._introduceText -> mIntroPresenter.onClickIntroduce()
            R.id._loginText -> mIntroPresenter.onClickLogin()
        }
    }

    private val mLogoTouchListener : View.OnTouchListener = object : View.OnTouchListener
    {
        override fun onTouch(view : View?, event : MotionEvent?) : Boolean
        {
            if(event?.action == MotionEvent.ACTION_DOWN)
            {
                mIntroPresenter.onActivateEasterEgg()
            }
            else if(event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_OUTSIDE)
            {
                mIntroPresenter.onDeactivateEasterEgg()
            }

            return true
        }

    }

    private val mBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context : Context, intent : Intent)
        {
            Log.i("", ">>> Home Event")
            val action : String? = intent.getAction()
            if(action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            {
                val reason : String = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)!!
                if(reason != null)
                {
                    if(reason == SYSTEM_DIALOG_REASON_HOME_KEY)
                    {
                        mIntroPresenter.onClickHomeButton()
                    }
                }
            }
        }
    }

    private val mLoginActivityResult = registerForActivityResult(StartActivityForResult())
    { result ->
        if(result.resultCode == RESULT_OK)
        {
            mIntroPresenter.onActivityResultLogin()
        }
    }
}