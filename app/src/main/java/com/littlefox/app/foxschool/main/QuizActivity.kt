package com.littlefox.app.foxschool.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.QuizSelectionPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.DURATION_NORMAL
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.QuizContract
import com.littlefox.app.foxschool.main.presenter.QuizPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.lang.reflect.Field

class QuizActivity : BaseActivity(), MessageHandlerCallback, QuizContract.View
{
    @BindView(R.id._quizTitlelayout)
    lateinit var _QuizTitleLayout : ImageView

    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._quizCloseButton)
    lateinit var _QuizCloseButton : ImageView

    @BindView(R.id._quizTaskBoxLayout)
    lateinit var _QuizTaskBoxLayout : ScalableLayout

    @BindView(R.id._quizTaskQuestionImage)
    lateinit var _QuizQuestionImage : ImageView

    @BindView(R.id._quizTimerTitle)
    lateinit var _QuizTimerTitle : TextView

    @BindView(R.id._quizTimerText)
    lateinit var _QuizTimerText : TextView

    @BindView(R.id._quizCountTitle)
    lateinit var _QuizAnswerCountTitle : TextView

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

    private lateinit var mQuizPresenter : QuizPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mCurrentPageIndex : Int = 0

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        ButterKnife.bind(this)
        mQuizPresenter = QuizPresenter(this)
        Log.f("")
    }

    override fun onResume()
    {
        super.onResume()
        mQuizPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mQuizPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mQuizPresenter.destroy()
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

        if(CommonUtils.getInstance(this).checkTablet && Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY)
        {
            val params = _QuizDisplayPager.layoutParams as RelativeLayout.LayoutParams
            params.topMargin = CommonUtils.getInstance(this).getPixel(80)
            _QuizDisplayPager.layoutParams = params
            val aniParams = _AniAnswerLayout.getChildLayoutParams(_AniAnswerView)
            _AniAnswerLayout.moveChildView(
                _AniAnswerView,
                aniParams.scale_Left,
                aniParams.scale_Top + 80
            )
        }
    }

    override fun initFont()
    {
        _QuizTitleText.typeface = Font.getInstance(this).getRobotoBold()
        _QuizTimerTitle.typeface = Font.getInstance(this).getRobotoMedium()
        _QuizTimerText.typeface = Font.getInstance(this).getRobotoMedium()
        _QuizAnswerCountTitle.typeface = Font.getInstance(this).getRobotoMedium()
        _QuizAnswerCountText.typeface = Font.getInstance(this).getRobotoMedium()
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

    override fun handlerMessage(message : Message)
    {
        mQuizPresenter.sendMessageEvent(message)
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

    /** 퀴즈 뷰페이저 표시 */
    override fun showPagerView(quizSelectionPagerAdapter : QuizSelectionPagerAdapter?)
    {
        _QuizDisplayPager.adapter = quizSelectionPagerAdapter
        _QuizDisplayPager.addOnPageChangeListener(mOnPageChangeListener)
    }

    /** 강제로 페이지 변경 */
    override fun forceChangePageView(pageIndex : Int)
    {
        _QuizDisplayPager.setCurrentItem(pageIndex, true)
    }

    /** 다음 페이지로 변경 */
    override fun nextPageView()
    {
        _QuizDisplayPager.setCurrentItem(mCurrentPageIndex + 1, true)
    }

    /** 상단 박스 표시 (플레이시간, 정답수) */
    override fun showTaskBoxLayout()
    {
        _QuizTaskBoxLayout.visibility = View.VISIBLE
    }

    /** 상단 박스 비표시 (플레이시간, 정답수) */
    override fun hideTaskBoxLayout()
    {
        _QuizTaskBoxLayout.visibility = View.GONE
    }

    /** 플레이 시간 텍스트 변경 */
    override fun showPlayTime(time : String?)
    {
        _QuizTimerText.text = time
    }

    /**  정답 수 텍스트 변경 */
    override fun showCorrectAnswerCount(message : String?)
    {
        _QuizAnswerCountText.text = message
    }

    /** O 이미지 표시 */
    override fun showCorrectAnswerView()
    {
        _AniAnswerView.visibility = View.VISIBLE
        _AniAnswerView.setImageResource(R.drawable.img_correct)
        rotateAnswerCheckView()
    }

    /** X 이미지 표시 */
    override fun showInCorrectAnswerView()
    {
        _AniAnswerView.visibility = View.VISIBLE
        _AniAnswerView.setImageResource(R.drawable.img_incorrect)
        rotateAnswerCheckView()
    }

    /** O, X 이미지 비표시 */
    override fun hideAnswerView()
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
            mQuizPresenter.onQuizPageSelected()
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}