package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.MyInformationPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.MyInformationContract
import com.littlefox.app.foxschool.main.presenter.MyInformationPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.lang.reflect.Field

/**
 * 나의 정보 화면
 * @author 김태은
 */
class MyInformationActivity : BaseActivity(), MessageHandlerCallback, MyInformationContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._myInfoViewpager)
    lateinit var _MyInfoViewpager : SwipeDisableViewPager

    private lateinit var mMyInformationPresenter : MyInformationPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_my_info_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_my_info)
        }

        ButterKnife.bind(this)
        mMyInformationPresenter = MyInformationPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mMyInformationPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mMyInformationPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mMyInformationPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** LifeCycle end **/

    /** Init **/
    override fun initView()
    {
        settingLayoutColor()
        setTitleView(Common.PAGE_MY_INFO)
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
    }

    override fun initViewPager(myInformationPagerAdapter : MyInformationPagerAdapter)
    {
        _MyInfoViewpager.adapter = myInformationPagerAdapter
        _MyInfoViewpager.addOnPageChangeListener(mOnPageChangeListener)
        settingViewPagerController()
    }

    private fun settingViewPagerController()
    {
        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL.toInt())
        try
        {
            val scroller : Field = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_MyInfoViewpager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
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
    /** Init end **/

    /**
     * ViewPager 페이지 변경
     */
    override fun setCurrentViewPage(position : Int)
    {
        if (position == Common.PAGE_MY_INFO)
        {
            _MyInfoViewpager.currentItem = Common.PAGE_MY_INFO
        }
        else
        {
            _MyInfoViewpager.currentItem = 1
        }
        setTitleView(position)
    }

    /**
     * 타이틀 영역 세팅
     * 나의 정보 화면 : X버튼 표시
     * 나의 정보 수정, 비밀번호 변경 화면 : <-버튼 표시
     */
    private fun setTitleView(position : Int)
    {
        when(position)
        {
            Common.PAGE_MY_INFO -> _TitleText.text = resources.getString(R.string.text_my_info)
            Common.PAGE_MY_INFO_CHANGE -> _TitleText.text = resources.getString(R.string.text_my_info_change)
            Common.PAGE_PASSWORD_CHANGE -> _TitleText.text = resources.getString(R.string.text_change_password)
        }

        if (position == Common.PAGE_MY_INFO)
        {
            _CloseButton.visibility = View.VISIBLE
            _CloseButtonRect.visibility = View.VISIBLE
            _BackButton.visibility = View.GONE
            _BackButtonRect.visibility = View.GONE
        }
        else
        {
            _CloseButton.visibility = View.GONE
            _CloseButtonRect.visibility = View.GONE
            _BackButton.visibility = View.VISIBLE
            _BackButtonRect.visibility = View.VISIBLE
        }
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

    override fun handlerMessage(message : Message)
    {
        mMyInformationPresenter.sendMessageEvent(message)
    }

    override fun onBackPressed()
    {
        CommonUtils.getInstance(this).hideKeyboard()
        if (_MyInfoViewpager.currentItem == Common.PAGE_MY_INFO)
        {
            super.onBackPressed()
        }
        else
        {
            _MyInfoViewpager.currentItem = Common.PAGE_MY_INFO
            setTitleView(Common.PAGE_MY_INFO)
        }
    }

    override fun dispatchTouchEvent(ev : MotionEvent) : Boolean
    {
        if(ev.action == MotionEvent.ACTION_UP)
        {
            val view = currentFocus
            if(view != null)
            {
                val consumed = super.dispatchTouchEvent(ev)
                val viewTmp = currentFocus
                val viewNew : View = viewTmp ?: view
                if(viewNew == view)
                {
                    val rect = Rect()
                    val coordinates = IntArray(2)
                    view.getLocationOnScreen(coordinates)
                    rect[coordinates[0], coordinates[1], coordinates[0] + view.width] =
                        coordinates[1] + view.height
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    if(rect.contains(x, y))
                    {
                        return consumed
                    }
                } else if(viewNew is EditText)
                {
                    Log.f("consumed : $consumed")
                    return consumed
                }
                CommonUtils.getInstance(this).hideKeyboard()
                viewNew.clearFocus()
                return consumed
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Optional
    @OnClick(R.id._closeButtonRect, R.id._backButtonRect)
    fun onClickView(view: View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
            R.id._backButtonRect -> mMyInformationPresenter.onClickBackButton()
        }
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("MyInformation Page Change : $position")
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}