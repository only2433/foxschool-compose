package com.littlefox.app.foxschool.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.FlashcardSelectionPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.main.contract.FlashcardContract
import com.littlefox.app.foxschool.main.presenter.FlashcardPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

class FlashCardActivity : BaseActivity(), FlashcardContract.View, MessageHandlerCallback
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._flashcardBaseViewPager)
    lateinit var _FlashcardBaseViewPager : SwipeDisableViewPager

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._soundCheckButton)
    lateinit var _SoundCheckButton : ImageView

    @BindView(R.id._soundOffMessageText)
    lateinit var _SoundOffMessageText : TextView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._bottomViewLayout)
    lateinit var _BottomViewLayout : ScalableLayout

    @BindView(R.id._autoModeCheckButton)
    lateinit var _AutoModeCheckButton : ImageView

    @BindView(R.id._autoModeText)
    lateinit var _AutoModeText : TextView

    @BindView(R.id._autoModeTimeText)
    lateinit var _AutoModeTimeText : TextView

    @BindView(R.id._shuffleModeCheckButton)
    lateinit var _ShuffleModeCheckButton : ImageView

    @BindView(R.id._shuffleModeText)
    lateinit var _ShuffleModeText : TextView

    @BindView(R.id._coachmarkImage)
    lateinit var _CoachmarkImage : ImageView

    @BindView(R.id._autoModeStudyBackground)
    lateinit var _AutoModeStudyBackground : ImageView

    @BindView(R.id._autoModeStudyPlayIcon)
    lateinit var _AutoModeStudyPlayIcon : ImageView

    @BindView(R.id._autoModeStudyTimeText)
    lateinit var _AutoModeStudyTimeText : TextView

    private lateinit var mFlashcardPresenter : FlashcardPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mCurrentPageIndex : Int = 0
    private var isSoundOff : Boolean = false

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        setContentView(R.layout.activity_flash_card)
        ButterKnife.bind(this)

        mFlashcardPresenter = FlashcardPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mFlashcardPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mFlashcardPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mFlashcardPresenter.destroy()
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
        settingCoachMarkImage()
        _FlashcardBaseViewPager.isSaveFromParentEnabled = false
        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL.toInt())

        try
        {
            val scroller = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_FlashcardBaseViewPager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun initFont()
    {
        _SoundOffMessageText.typeface = Font.getInstance(this).getTypefaceRegular()
        _AutoModeText.typeface = Font.getInstance(this).getTypefaceRegular()
        _AutoModeTimeText.typeface = Font.getInstance(this).getTypefaceRegular()
        _AutoModeStudyTimeText.typeface = Font.getInstance(this).getTypefaceRegular()
        _ShuffleModeText.typeface = Font.getInstance(this).getTypefaceRegular()
    }
    /** ========== Init ========== */

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

    override fun handlerMessage(message : Message)
    {
        mFlashcardPresenter.sendMessageEvent(message)
    }

    override fun showPagerView(adapter : FlashcardSelectionPagerAdapter?)
    {
        Log.f("")
        _FlashcardBaseViewPager.adapter = adapter
        _FlashcardBaseViewPager.addOnPageChangeListener(mOnPageChangeListener)
    }

    private fun settingCoachMarkImage()
    {
        if(CommonUtils.getInstance(this).getPhoneDisplayRadio() == DisplayPhoneType.RADIO_20_9)
        {
            _CoachmarkImage.setImageResource(R.drawable.coachmark_flashcard_2192_1080)
        }
        else
        {
            _CoachmarkImage.setImageResource(R.drawable.coachmark_flashcard_1920_1080)
        }
    }

    override fun settingSoundButton(isEnable : Boolean)
    {
        Log.f("isEnable : $isEnable")
        if(isEnable)
        {
            _SoundCheckButton.setImageResource(R.drawable.btn_flashcard_sound_on)
            _SoundOffMessageText.visibility = View.GONE
            isSoundOff = false
        }
        else
        {
            _SoundCheckButton.setImageResource(R.drawable.btn_flashcard_sound_off)
            _SoundOffMessageText.visibility = View.VISIBLE
            isSoundOff = true
        }
    }

    override fun settingAutoPlayInterval(second : Int)
    {
        Log.f("second : $second")
        val timeText = "$second${resources.getString(R.string.text_second)}"
        _AutoModeTimeText.text = timeText
        _AutoModeStudyTimeText.text = timeText
    }

    override fun settingBaseControlView(status : FlashcardStatus)
    {
        Log.f("status : $status")
        settingBottomView(status)
        when(status)
        {
            FlashcardStatus.INTRO, FlashcardStatus.BOOKMARK_INTRO -> enableSoundButton(true)
            FlashcardStatus.STUDY, FlashcardStatus.BOOKMARK_STUDY -> enableSoundButton(false)
            FlashcardStatus.RESULT -> enableSoundButton(false)
        }
    }

    private fun settingBottomView(status : FlashcardStatus)
    {
        Log.f("status : $status")
        when(status)
        {
            FlashcardStatus.INTRO, FlashcardStatus.BOOKMARK_INTRO ->
            {
                _BottomViewLayout.visibility = View.VISIBLE
                _AutoModeCheckButton.visibility = View.VISIBLE
                _AutoModeStudyPlayIcon.visibility = View.GONE
                _AutoModeStudyTimeText.visibility = View.GONE
                _AutoModeStudyBackground.visibility = View.GONE
                _BottomViewLayout.moveChildView(_AutoModeText, 708f, 38f)
                _ShuffleModeCheckButton.visibility = View.VISIBLE
                _ShuffleModeText.visibility = View.VISIBLE
            }
            FlashcardStatus.STUDY, FlashcardStatus.BOOKMARK_STUDY ->
            {
                _BottomViewLayout.visibility = View.VISIBLE
                _AutoModeCheckButton.visibility = View.GONE
                _AutoModeTimeText.visibility = View.GONE
                _AutoModeStudyPlayIcon.visibility = View.VISIBLE
                _AutoModeStudyTimeText.visibility = View.VISIBLE
                _AutoModeStudyBackground.visibility = View.VISIBLE
                _BottomViewLayout.moveChildView(_AutoModeText, 790f, 38f)
                _ShuffleModeCheckButton.visibility = View.GONE
                _ShuffleModeText.visibility = View.GONE
            }
            FlashcardStatus.RESULT -> _BottomViewLayout.visibility = View.GONE
        }
    }

    private fun enableSoundButton(isEnable : Boolean)
    {
        if(isEnable)
        {
            _SoundCheckButton.visibility = View.VISIBLE
            if(isSoundOff)
            {
                settingSoundButton(false)
            }
            else
            {
                settingSoundButton(true)
            }
        }
        else
        {
            _SoundCheckButton.visibility = View.GONE
            _SoundOffMessageText.visibility = View.GONE
        }
    }

    override fun checkAutoplayBox(status : FlashcardStatus, isEnable : Boolean)
    {
        Log.f("status : $status, isEnable : $isEnable")
        when(status)
        {
            FlashcardStatus.INTRO, FlashcardStatus.BOOKMARK_INTRO ->
            {
                if(isEnable)
                {
                    _AutoModeCheckButton.setImageResource(R.drawable.flashcard_select_on)
                    _AutoModeTimeText.visibility = View.VISIBLE
                }
                else
                {
                    _AutoModeCheckButton.setImageResource(R.drawable.flashcard_select_off)
                    _AutoModeTimeText.visibility = View.INVISIBLE
                }
            }
            else -> {}
        }

        if(isEnable)
        {
            _AutoModeStudyPlayIcon.setImageResource(R.drawable.icon_autoplay_on)
        }
        else
        {
            _AutoModeStudyPlayIcon.setImageResource(R.drawable.icon_autoplay_off)
        }
    }

    override fun checkShuffleBox(isEnable : Boolean?)
    {
        Log.f("isEnable : $isEnable")
        if(isEnable!!)
        {
            _ShuffleModeCheckButton.setImageResource(R.drawable.flashcard_select_on)
        }
        else
        {
            _ShuffleModeCheckButton.setImageResource(R.drawable.flashcard_select_off)
        }
    }

    override fun showCoachMarkView()
    {
        _CoachmarkImage.visibility = View.VISIBLE
        _CoachmarkImage.setOnClickListener {
            _CoachmarkImage.visibility = View.GONE
            mFlashcardPresenter.onCoachMarkNeverSeeAgain()
        }
    }

    override fun showBottomViewLayout()
    {
        ViewAnimator.animate(_BottomViewLayout)
            .fadeIn()
            .duration(Common.DURATION_NORMAL)
            .onStart { hideBackButton() }
            .start()
    }

    override fun hideBottomViewLayout()
    {
        ViewAnimator.animate(_BottomViewLayout)
            .fadeOut()
            .duration(Common.DURATION_NORMAL)
            .onStart { showBackButton() }
            .start()
    }

    private fun showBackButton()
    {
        _BackButton.visibility = View.VISIBLE
        _SoundCheckButton.visibility = View.GONE
        _CloseButton.visibility = View.GONE
        if(isSoundOff)
        {
            _SoundOffMessageText.visibility = View.GONE
        }
    }

    private fun hideBackButton()
    {
        _BackButton.visibility = View.GONE
        _SoundCheckButton.visibility = View.VISIBLE
        _CloseButton.visibility = View.VISIBLE
        if(isSoundOff)
        {
            _SoundOffMessageText.visibility = View.VISIBLE
        }
    }

    override fun prevPageView()
    {
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex - 1, true)
    }

    override fun nextPageView()
    {
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex + 1, true)
    }

    override fun forceChangePageView(position : Int)
    {
        mCurrentPageIndex = position
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex, true)
    }

    @OnClick(R.id._soundCheckButton, R.id._closeButton, R.id._autoModeCheckButton, R.id._autoModeStudyBackground,
        R.id._shuffleModeCheckButton, R.id._autoModeTimeText, R.id._backButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._soundCheckButton -> mFlashcardPresenter.onClickSound()
            R.id._closeButton -> mFlashcardPresenter.onClickClose()
            R.id._autoModeCheckButton, R.id._autoModeStudyBackground -> mFlashcardPresenter.onCheckAutoPlay()
            R.id._shuffleModeCheckButton -> mFlashcardPresenter.onCheckShuffle()
            R.id._autoModeTimeText -> mFlashcardPresenter.onClickAutoPlayInterval()
            R.id._backButton -> mFlashcardPresenter.onClickHelpViewBack()
        }
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("position :$position")
            mCurrentPageIndex = position
            mFlashcardPresenter.onFlashCardPageSelected(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}