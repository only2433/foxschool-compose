package com.littlefox.app.foxschool.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.FlashcardSelectionPagerAdapter
import com.littlefox.app.foxschool.api.viewmodel.factory.FlashcardFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.IntroFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomFlashcardIntervalSelectDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.IntervalSelectListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.main.contract.FlashcardContract
import com.littlefox.app.foxschool.main.presenter.FlashcardPresenter
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FlashCardActivity : BaseActivity()
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


    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mCurrentPageIndex : Int = 0
    private var isSoundOff : Boolean = false
    private var mBottomFlashcardIntervalSelectDialog : BottomFlashcardIntervalSelectDialog? = null
    private var mBottomBookAddDialog : BottomBookAddDialog? = null
    private var mTempleteAlertDialog : TemplateAlertDialog? = null
    private var mCurrentIntervalSecond: Int = 0

    private val factoryViewModel: FlashcardFactoryViewModel by viewModels()

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        setContentView(R.layout.activity_flash_card)
        ButterKnife.bind(this)

        initView()
        initFont()
        setupObserverViewModel()

        factoryViewModel.init(this)
    }

    override fun onResume()
    {
        super.onResume()
        factoryViewModel.resume()
    }

    override fun onPause()
    {
        super.onPause()
        factoryViewModel.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        factoryViewModel.destroy()
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

    override fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this) {loading ->
            if(loading)
            {
                showLoading()
            } else
            {
                hideLoading()
            }
        }

        factoryViewModel.toast.observe(this) {message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        factoryViewModel.successMessage.observe(this) {message ->
            showSuccessMessage(message)
        }

        factoryViewModel.errorMessage.observe(this) {message ->
            showErrorMessage(message)
        }

        factoryViewModel.showPagerView.observe(this) {adapter ->
            showPagerView(adapter)
        }

        factoryViewModel.settingSoundButton.observe(this) {enable ->
            settingSoundButton(enable)
        }

        factoryViewModel.settingAutoPlayInterval.observe(this) {interval ->
            settingAutoPlayInterval(interval)
        }

        factoryViewModel.settingBaseControlView.observe(this) {status ->
            settingBaseControlView(status)
        }

        factoryViewModel.checkAutoplayBox.observe(this) {data ->
            checkAutoplayBox(data.first, data.second)
        }

        factoryViewModel.checkShuffleBox.observe(this) {enable ->
            checkShuffleBox(enable)
        }

        factoryViewModel.showCoachMarkView.observe(this) {
            showCoachMarkView()
        }

        factoryViewModel.prevPageView.observe(this) {
            prevPageView()
        }

        factoryViewModel.nextPageView.observe(this) {
            nextPageView()
        }

        factoryViewModel.enableBottomViewLayout.observe(this) {enable ->
            if(enable)
            {
                showBottomViewLayout()
            } else
            {
                hideBottomViewLayout()
            }
        }

        factoryViewModel.forceChangePageView.observe(this) {position ->
            forceChangePageView(position)
        }

        factoryViewModel.dialogEmptyBookmark.observe(this) {
            showBookmarkEmptyDialog()
        }

        factoryViewModel.dialogReplayWarningBookmark.observe(this) {
            showBookmarkWarningDialog(FlashcardFactoryViewModel.DIALOG_BOOKMARK_INIT)
        }

        factoryViewModel.dialogCloseWarningBookmark.observe(this) {
            showBookmarkWarningDialog(FlashcardFactoryViewModel.DIALOG_CLOSE_APP)
        }

        factoryViewModel.dialogBottomVocabularyContentAdd.observe(this) {list ->
            showBottomVocabularyAddDialog(list)
        }
    }
    /** ========== Init ========== */


    fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }



    fun showPagerView(adapter : FlashcardSelectionPagerAdapter?)
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

    fun settingSoundButton(isEnable : Boolean)
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

    fun settingAutoPlayInterval(second : Int)
    {
        Log.f("second : $second")
        mCurrentIntervalSecond = second
        val timeText = "$second${resources.getString(R.string.text_second)}"
        _AutoModeTimeText.text = timeText
        _AutoModeStudyTimeText.text = timeText
    }

    fun settingBaseControlView(status : FlashcardStatus)
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

    fun checkAutoplayBox(status : FlashcardStatus, isEnable : Boolean)
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

    fun checkShuffleBox(isEnable : Boolean?)
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

    fun showCoachMarkView()
    {
        _CoachmarkImage.visibility = View.VISIBLE
        _CoachmarkImage.setOnClickListener {
            _CoachmarkImage.visibility = View.GONE
            factoryViewModel.onCoachMarkNeverSeeAgain()
        }
    }

    fun showBottomViewLayout()
    {
        ViewAnimator.animate(_BottomViewLayout)
            .fadeIn()
            .duration(Common.DURATION_NORMAL)
            .onStart { hideBackButton() }
            .start()
    }

    fun hideBottomViewLayout()
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

    fun prevPageView()
    {
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex - 1, true)
    }

    fun nextPageView()
    {
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex + 1, true)
    }

    fun forceChangePageView(position : Int)
    {
        mCurrentPageIndex = position
        _FlashcardBaseViewPager.setCurrentItem(mCurrentPageIndex, true)
    }

    /**
     * 자동재생 시간 선택 다이얼로그 표시
     */
    private fun showBottomIntervalDialog()
    {
        mBottomFlashcardIntervalSelectDialog = BottomFlashcardIntervalSelectDialog(this, mCurrentIntervalSecond).apply {
            setOnIntervalSelectListener(mIntervalSelectListener)
            show()
        }
    }

    /**
     * 책장에 담기 다이얼로그 표시
     */
    private fun showBottomVocabularyAddDialog(list: ArrayList<MyVocabularyResult>)
    {
        mBottomBookAddDialog = BottomBookAddDialog(this).apply {
            setCancelable(true)
            setLandScapeMode()
            setVocabularyData(list)
            setBookSelectListener(mBookAddListener)
            show()
        }
    }

    /**
     * 찜단어가 사라진다 라는 Alert 메세지 다이얼로그
     */
    private fun showBookmarkWarningDialog( dialogType : Int)
    {
        Log.f("")
        mTempleteAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_warning_bookmark_init))
            setDialogEventType(dialogType)
            setButtonType(DialogButtonType.BUTTON_2)
            setGravity(Gravity.LEFT)
            setDialogListener(mDialogListener)
            show()
        }
    }

    /**
     * 찜단어가 없다라는 메세지 다이얼로그
     */
    private fun showBookmarkEmptyDialog()
    {
        Log.f("")
        mTempleteAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_warning_bookmark_empty))
            setButtonType(DialogButtonType.BUTTON_1)
            setGravity(Gravity.LEFT)
            show()
        }
    }


    @OnClick(R.id._soundCheckButton, R.id._closeButton, R.id._autoModeCheckButton, R.id._autoModeStudyBackground,
        R.id._shuffleModeCheckButton, R.id._autoModeTimeText, R.id._backButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._soundCheckButton -> factoryViewModel.onClickSound()
            R.id._closeButton -> factoryViewModel.onClickClose()
            R.id._autoModeCheckButton, R.id._autoModeStudyBackground -> factoryViewModel.onCheckAutoPlay()
            R.id._shuffleModeCheckButton -> factoryViewModel.onCheckShuffle()
            R.id._autoModeTimeText -> showBottomIntervalDialog()
            R.id._backButton -> factoryViewModel.onClickHelpViewBack()
        }
    }

    /**
     * 자동 넘기기 시간 선택 다이얼로그 Listener
     */
    private val mIntervalSelectListener : IntervalSelectListener = object : IntervalSelectListener
    {
        override fun onClickIntervalSecond(second : Int)
        {
            factoryViewModel.onClickIntervalSecond(second)
        }

    }
    /**
     * 책장에 추가 다이얼로그 Listener
     */
    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            factoryViewModel.onClickVocabularyBook(index)
        }

    }

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            factoryViewModel.onDialogChoiceClick(buttonType, eventType)
        }

    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("position :$position")
            mCurrentPageIndex = position
            factoryViewModel.onFlashCardPageSelected(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}