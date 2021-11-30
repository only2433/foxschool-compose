package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
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
import com.littlefox.app.foxschool.adapter.TeacherHomeworkPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.app.foxschool.enumerate.HomeworkDetailType
import com.littlefox.app.foxschool.main.contract.TeacherHomeworkContract
import com.littlefox.app.foxschool.main.presenter.TeacherHomeworkManagePresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.lang.reflect.Field

/**
 * 선생님용 숙제관리 화면
 * @author 김태은
 */
class TeacherHomeworkManageActivity : BaseActivity(), MessageHandlerCallback, TeacherHomeworkContract.View
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

    private lateinit var mHomeworkManagePresenter : TeacherHomeworkManagePresenter
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
        mHomeworkManagePresenter = TeacherHomeworkManagePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mHomeworkManagePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mHomeworkManagePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mHomeworkManagePresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mHomeworkManagePresenter.activityResult(requestCode, resultCode, data)
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
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
    }

    override fun initViewPager(mHomeworkPagerAdapter : TeacherHomeworkPagerAdapter)
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
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(R.color.color_25b4cf))
        _TitleBaselayout.setBackgroundColor(resources.getColor(R.color.color_29c8e6))
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
    override fun setCurrentViewPage(position : Int, detailType : HomeworkDetailType?, commentType : HomeworkCommentType?)
    {
        _HomeworkViewPager.currentItem = position
        setTitleView(position, detailType, commentType)
    }

    /**
     * 타이틀 영역 세팅 (화면명, 버튼)
     * 숙제관리 : X버튼 표시
     * 그 외 : <-버튼 표시
     */
    private fun setTitleView(position : Int, detailType : HomeworkDetailType? = null, commentType : HomeworkCommentType? = null)
    {
        when(position)
        {
            Common.PAGE_HOMEWORK_CALENDAR ->
            {
                Log.f("[TeacherHomeworkPage] PAGE_HOMEWORK_CALENDAR")
                _TitleText.text = resources.getString(R.string.text_homework_manage)
            }
            Common.PAGE_HOMEWORK_STATUS ->
            {
                Log.f("[TeacherHomeworkPage] PAGE_HOMEWORK_STATUS")
                _TitleText.text = resources.getString(R.string.text_homework_status)
            }
            Common.PAGE_HOMEWORK_DETAIL ->
            {
                if (detailType == HomeworkDetailType.TYPE_HOMEWORK_CURRENT_STATUS_DETAIL)
                {
                    Log.f("[TeacherHomeworkPage] PAGE_TYPE_STATUS_DETAIL")
                    _TitleText.text = resources.getString(R.string.text_homework_status_detail)
                }
                else if (detailType == HomeworkDetailType.TYPE_HOMEWORK_CONTENT)
                {
                    Log.f("[TeacherHomeworkPage] PAGE_TYPE_HOMEWORK_DETAIL")
                    _TitleText.text = resources.getString(R.string.text_homework_contents)
                }
            }
            Common.PAGE_HOMEWORK_COMMENT ->
            {
                if (commentType == HomeworkCommentType.COMMENT_STUDENT)
                {
                    Log.f("[TeacherHomeworkPage] PAGE_COMMENT_STUDENT")
                    _TitleText.text = resources.getString(R.string.text_homework_student_comment)
                }
                else if (commentType == HomeworkCommentType.COMMENT_TEACHER)
                {
                    Log.f("[TeacherHomeworkPage] PAGE_COMMENT_TEACHER")
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
        Log.f("")
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    override fun hideLoading()
    {
        Log.f("")
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
        mHomeworkManagePresenter.sendMessageEvent(message)
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
            mHomeworkManagePresenter.onClickBackButton()
        }
    }

    @Optional
    @OnClick(R.id._closeButtonRect, R.id._backButtonRect)
    fun onClickView(view: View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
            R.id._backButtonRect -> mHomeworkManagePresenter.onClickBackButton()
        }
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("Homework Page Change : $position")
            mHomeworkManagePresenter.onPageChanged(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }
}