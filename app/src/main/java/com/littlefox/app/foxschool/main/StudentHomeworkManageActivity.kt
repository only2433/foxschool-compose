package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Intent
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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.app.foxschool.main.contract.StudentHomeworkContract
import com.littlefox.app.foxschool.main.presenter.StudentHomeworkManagePresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.lang.reflect.Field

/**
 * 학생용 숙제관리 화면
 * @author 김태은
 */
class StudentHomeworkManageActivity : BaseActivity(), MessageHandlerCallback, StudentHomeworkContract.View
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

    @BindView(R.id._homeworkViewpager)
    lateinit var _HomeworkViewPager : SwipeDisableViewPager

    private lateinit var mStudentHomeworkManagePresenter : StudentHomeworkManagePresenter
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
            setContentView(R.layout.activity_homework_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_homework)
        }

        ButterKnife.bind(this)
        mStudentHomeworkManagePresenter = StudentHomeworkManagePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mStudentHomeworkManagePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mStudentHomeworkManagePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mStudentHomeworkManagePresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mStudentHomeworkManagePresenter.activityResult(requestCode, resultCode, data)
    }
    /** LifeCycle end **/

    /** Init **/
    override fun initView()
    {
        settingLayoutColor()
        setTitleView(Common.PAGE_HOMEWORK_CALENDAR)
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
    }

    override fun initViewPager(mHomeworkPagerAdapter : HomeworkPagerAdapter)
    {
        _HomeworkViewPager.adapter = mHomeworkPagerAdapter
        _HomeworkViewPager.addOnPageChangeListener(mOnPageChangeListener)
        settingViewPagerController()
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

    private fun settingViewPagerController()
    {
        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL.toInt())
        try
        {
            val scroller : Field = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_HomeworkViewPager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ViewPager 페이지 변경
     */
    override fun setCurrentViewPage(position : Int, commentType : HomeworkCommentType?)
    {
        _HomeworkViewPager.currentItem = position
        setTitleView(position, commentType)
    }

    /**
     * 타이틀 영역 세팅 (화면명, 버튼)
     * 숙제관리 : X버튼 표시
     * 그 외 : <-버튼 표시
     */
    private fun setTitleView(position : Int, commentType : HomeworkCommentType? = null)
    {
        when(position)
        {
            Common.PAGE_HOMEWORK_CALENDAR ->
            {
                Log.f("[StudentHomeworkPage] PAGE_HOMEWORK_CALENDAR")
                _TitleText.text = resources.getString(R.string.text_homework_manage)
            }
            Common.PAGE_HOMEWORK_STATUS ->
            {
                Log.f("[StudentHomeworkPage] PAGE_HOMEWORK_STATUS")
                _TitleText.text = resources.getString(R.string.text_homework_status)
            }
            Common.PAGE_HOMEWORK_COMMENT ->
            {
                if (commentType == HomeworkCommentType.COMMENT_STUDENT)
                {
                    Log.f("[StudentHomeworkPage] PAGE_COMMENT_STUDENT")
                    _TitleText.text = resources.getString(R.string.text_homework_student_comment)
                }
                else if (commentType == HomeworkCommentType.COMMENT_TEACHER)
                {
                    Log.f("[StudentHomeworkPage] PAGE_COMMENT_TEACHER")
                    _TitleText.text = resources.getString(R.string.text_homework_teacher_comment)
                }
            }
        }

        if (position == Common.PAGE_HOMEWORK_CALENDAR)
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
        mStudentHomeworkManagePresenter.sendMessageEvent(message)
    }

    override fun onBackPressed()
    {
        CommonUtils.getInstance(this).hideKeyboard()
        if (_HomeworkViewPager.currentItem == Common.PAGE_HOMEWORK_CALENDAR)
        {
            super.onBackPressed()
        }
        else
        {
            mStudentHomeworkManagePresenter.onClickBackButton()
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
            R.id._backButtonRect -> mStudentHomeworkManagePresenter.onClickBackButton()
        }
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("Homework Page Change : $position")
            mStudentHomeworkManagePresenter.onPageChanged(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}