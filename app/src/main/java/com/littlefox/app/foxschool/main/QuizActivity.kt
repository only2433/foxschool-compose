package com.littlefox.app.foxschool.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.QuizSelectionPagerAdapter
import com.littlefox.app.foxschool.api.viewmodel.factory.IntroFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.QuizFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.DURATION_NORMAL
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import com.littlefox.app.foxschool.main.contract.QuizContract
import com.littlefox.app.foxschool.main.presenter.QuizPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field

@AndroidEntryPoint
class QuizActivity : BaseActivity()
{
    @BindView(R.id._quizTitlelayout)
    lateinit var _QuizTitleLayout : ImageView

    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._quizCloseButton)
    lateinit var _QuizCloseButton : ImageView

    @BindView(R.id._quizTaskBoxLayout)
    lateinit var _QuizTaskBoxLayout : ScalableLayout

    @BindView(R.id._quizTimerIcon)
    lateinit var _QuizTimerIcon : ImageView

    @BindView(R.id._quizTaskQuestionImage)
    lateinit var _QuizQuestionImage : ImageView

    @BindView(R.id._quizTimerTitle)
    lateinit var _QuizTimerTitle : TextView

    @BindView(R.id._quizTimerText)
    lateinit var _QuizTimerText : TextView

    @BindView(R.id._quizCountTitle)
    lateinit var  _QuizAnswerCountTitle : TextView

    @BindView(R.id._quizCountText)
    lateinit var _QuizAnswerCountText : TextView

    @BindView(R.id._quizBaseFragment)
    lateinit var _QuizDisplayPager : SwipeDisableViewPager

    @BindView(R.id._quizAniIcon)
    lateinit var _AniAnswerView : ImageView

    @BindView(R.id._quizTitle)
    lateinit var _QuizTitleText : TextView

    @BindView(R.id._quizAniLayout)
    lateinit var _AniAnswerLayout : ScalableLayout

    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mCurrentPageIndex : Int = 0

    private val factoryViewModel: QuizFactoryViewModel by viewModels()

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        ButterKnife.bind(this)
        initView()
        initFont()
        setupObserverViewModel()
        factoryViewModel.init(this)
        Log.f("")
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
        settingLayoutColor()
        settingTaskBoxLayout()
        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.duration = DURATION_NORMAL.toInt()

        try
        {
            val scroller : Field = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_QuizDisplayPager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun initFont()
    {
        _QuizTitleText.typeface = Font.getInstance(this).getTypefaceBold()
        _QuizTimerTitle.typeface = Font.getInstance(this).getTypefaceMedium()
        _QuizTimerText.typeface = Font.getInstance(this).getTypefaceMedium()
        _QuizAnswerCountTitle.typeface = Font.getInstance(this).getTypefaceMedium()
        _QuizAnswerCountText.typeface = Font.getInstance(this).getTypefaceMedium()
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
        factoryViewModel.forceChangePageView.observe(this) {page ->
            forceChangePageView(page)
        }
        factoryViewModel.nextPageView.observe(this) {
            nextPageView()
        }
        factoryViewModel.enableTaskBoxLayout.observe(this) {enable ->
            if(enable)
            {
                showTaskBoxLayout()
            } else
            {
                hideTaskBoxLayout()
            }
        }
        factoryViewModel.showPlayTime.observe(this) {data ->
            showPlayTime(data)
        }
        factoryViewModel.showCorrectAnswerCount.observe(this) {data ->
            showCorrectAnswerCount(data)
        }
        factoryViewModel.showCorrectAnswerView.observe(this) {
            showCorrectAnswerView()
        }
        factoryViewModel.showInCorrectAnswerView.observe(this) {
            showInCorrectAnswerView()
        }
        factoryViewModel.hideAnswerView.observe(this) {
            hideAnswerView()
        }
        factoryViewModel.dialogWarningText.observe(this) {message ->
            showMessageAlertDialog(message)
        }

    }

    /** ========== Init ========== */

    /** 상단바 색상 설정 */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _QuizTitleLayout.setBackgroundColor(resources.getColor(backgroundColor))
    }

    private fun settingTaskBoxLayout()
    {
        // 22:9 or 20:9 TaskBox size 조절
        if(CommonUtils.getInstance(this).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            _QuizTaskBoxLayout.run {
                setScaleSize(2402f, 120f)
                moveChildView(_QuizTimerIcon, 1754f, 36f)
                moveChildView(_QuizTimerTitle, 1815f, 0f)
                moveChildView(_QuizTimerText, 1917f, 0f)
                moveChildView(_QuizQuestionImage, 2077f, 38f)
                moveChildView(_QuizAnswerCountTitle, 2140f, 0f)
                moveChildView(_QuizAnswerCountText, 2252f, 0f)
            }
        }
    }
    private fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    private fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    /** 퀴즈 뷰페이저 표시 */
    private fun showPagerView(quizSelectionPagerAdapter : QuizSelectionPagerAdapter?)
    {
        Log.f("")
        _QuizDisplayPager.adapter = quizSelectionPagerAdapter
        _QuizDisplayPager.addOnPageChangeListener(mOnPageChangeListener)
    }

    /** 강제로 페이지 변경 */
    private fun forceChangePageView(pageIndex : Int)
    {
        _QuizDisplayPager.setCurrentItem(pageIndex, true)
    }

    /** 다음 페이지로 변경 */
    private fun nextPageView()
    {
        _QuizDisplayPager.setCurrentItem(mCurrentPageIndex + 1, true)
    }

    /** 상단 박스 표시 (플레이시간, 정답수) */
    private fun showTaskBoxLayout()
    {
        _QuizTaskBoxLayout.visibility = View.VISIBLE
    }

    /** 상단 박스 비표시 (플레이시간, 정답수) */
    private  fun hideTaskBoxLayout()
    {
        _QuizTaskBoxLayout.visibility = View.GONE
    }

    /** 플레이 시간 텍스트 변경 */
    private fun showPlayTime(time : String?)
    {
        _QuizTimerText.text = time
    }

    /**  정답 수 텍스트 변경 */
    private fun showCorrectAnswerCount(message : String?)
    {
        _QuizAnswerCountText.text = message
    }

    /** O 이미지 표시 */
    private fun showCorrectAnswerView()
    {
        _AniAnswerView.visibility = View.VISIBLE
        _AniAnswerView.setImageResource(R.drawable.img_correct)
        rotateAnswerCheckView()
    }

    /** X 이미지 표시 */
    private fun showInCorrectAnswerView()
    {
        _AniAnswerView.visibility = View.VISIBLE
        _AniAnswerView.setImageResource(R.drawable.img_incorrect)
        rotateAnswerCheckView()
    }

    /** O, X 이미지 비표시 */
    private fun hideAnswerView()
    {
        _AniAnswerView.visibility = View.GONE
    }

    private fun rotateAnswerCheckView()
    {
        _AniAnswerView.rotationY = 0f
        val animatorRotation = ObjectAnimator.ofFloat(_AniAnswerView, "rotationY", 360f)
        animatorRotation.duration = 500
        animatorRotation.start()
    }

    private fun showMessageAlertDialog(message : String)
    {
        TemplateAlertDialog(this).apply {
            setMessage(message)
            setDialogEventType(TemplateAlertDialog.DIALOG_EVENT_DEFAULT)
            setButtonType(DialogButtonType.BUTTON_1)
            show()
        }
    }

    @OnClick(R.id._quizCloseButton)
    fun onSelectClick(view : View)
    {
        when(view.id)
        {
            R.id._quizCloseButton ->
            {
                Log.f("Quiz Close Button Click")
                finish()
            }
        }
    }

    private val mOnPageChangeListener : OnPageChangeListener = object : OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            mCurrentPageIndex = position
            factoryViewModel.onQuizPageSelected()
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}