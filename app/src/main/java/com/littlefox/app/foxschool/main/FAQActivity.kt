package com.littlefox.app.foxschool.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.api.viewmodel.factory.ForumFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.ForumType
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FAQActivity : BaseActivity()
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._forumViewpager)
    lateinit var _ForumViewpager : SwipeDisableViewPager

    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private val factoryViewModel : ForumFactoryViewModel by viewModels()

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_forum_tablet)
        } 
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_forum)
        }
        ButterKnife.bind(this)

        initView()
        initFont()
        setupObserverViewModel()
        factoryViewModel.init(this, ForumType.FAQ)
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
        _TitleText.text = resources.getString(R.string.text_faqs)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
    }

    override fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this) { loading ->
            if(loading)
            {
                showLoading()
            }
            else
            {
                hideLoading()
            }
        }

        factoryViewModel.toast.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        factoryViewModel.successMessage.observe(this) { message ->
            showSuccessMessage(message)
        }

        factoryViewModel.errorMessage.observe(this) { message ->
            showErrorMessage(message)
        }

        factoryViewModel.initViewPager.observe(this) { adapter ->
            initViewPager(adapter)
        }

        factoryViewModel.setCurrentPage.observe(this) { page ->
            setCurrentViewPage(page)
        }

        factoryViewModel.setBackButton.observe(this) { enable ->
            setBackButton(enable)
        }
    }

    fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter?)
    {
        _ForumViewpager.adapter = mainFragmentSelectionPagerAdapter
        _ForumViewpager.addOnPageChangeListener(mOnPageChangeListener)

        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL.toInt())

        try
        {
            val scroller = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_ForumViewpager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
    }
    /** ========== Init ========== */

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

    fun setCurrentViewPage(position : Int)
    {
        _ForumViewpager.currentItem = position
    }

    fun setBackButton(isVisible : Boolean)
    {
        if(isVisible)
        {
            _BackButton.visibility = View.VISIBLE
            _BackButtonRect.visibility = View.VISIBLE
            _CloseButton.visibility = View.GONE
            _CloseButtonRect.visibility = View.GONE
        }
        else
        {
            _BackButton.visibility = View.GONE
            _BackButtonRect.visibility = View.GONE
            _CloseButton.visibility = View.VISIBLE
            _CloseButtonRect.visibility = View.VISIBLE
        }
    }


    fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    /**
     * 디바이스 back 버튼 이벤트
     */
    override fun onBackPressed()
    {
        if (_ForumViewpager.currentItem == Common.PAGE_FORUM_LIST)
        {
            super.onBackPressed()
        }
        else
        {
            _ForumViewpager.currentItem  = Common.PAGE_FORUM_LIST
        }
    }

    @OnClick(R.id._backButtonRect, R.id._closeButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> _ForumViewpager.currentItem = Common.PAGE_FORUM_LIST
            R.id._closeButtonRect -> super.onBackPressed()
        }
    }

    private val mOnPageChangeListener : OnPageChangeListener = object : OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            factoryViewModel.onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}