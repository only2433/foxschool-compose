package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.api.viewmodel.factory.StudentHomeworkFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field

/**
 * 학생용 숙제관리 화면
 * @author 김태은
 */
@AndroidEntryPoint
class StudentHomeworkManageActivity : BaseActivity()
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

    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private val factoryViewModel : StudentHomeworkFactoryViewModel by viewModels()

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

        initView()
        initFont()
        setupObserverViewModel()

        factoryViewModel.init(this)
        factoryViewModel.onAddResultLaunchers(mStatusActivityResult)
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
    /** LifeCycle end **/

    /** Init **/
    private fun initView()
    {
        settingLayoutColor()
        setTitleView(Common.PAGE_HOMEWORK_CALENDAR)
    }

    private fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
    }

    private fun initViewPager(mHomeworkPagerAdapter : HomeworkPagerAdapter)
    {
        _HomeworkViewPager.adapter = mHomeworkPagerAdapter
        _HomeworkViewPager.addOnPageChangeListener(mOnPageChangeListener)
        settingViewPagerController()
    }

    private fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this, Observer<Boolean> {loading ->
            if (loading)
            {
                showLoading()
            }
            else
            {
                hideLoading()
            }
        })

        factoryViewModel.toast.observe(this, Observer<String> {message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        factoryViewModel.successMessage.observe(this, Observer<String> {message ->
            CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message, Gravity.CENTER)
        })

        factoryViewModel.errorMessage.observe(this, Observer<String> {message ->
            CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message, Gravity.CENTER)
        })

        factoryViewModel.settingViewPager.observe(this, Observer<HomeworkPagerAdapter> {adapter ->
            initViewPager(adapter)
        })

        factoryViewModel.currentViewPage.observe(this, Observer<Pair<Int, HomeworkCommentType?>> {pair ->
            // ViewPager 페이지 변경
            val position = pair.first
            val commentType = pair.second

            _HomeworkViewPager.currentItem = position
            setTitleView(position, commentType)
        })

        factoryViewModel.showRecordPermissionDialog.observe(this, Observer {
            // 마이크 권한 허용 요청 다이얼로그 - 녹음기 기능 사용을 위해
            mTemplateAlertDialog = TemplateAlertDialog(this).apply {
                setMessage(resources.getString(R.string.message_record_permission))
                setButtonType(DialogButtonType.BUTTON_2)
                setButtonText(resources.getString(R.string.text_cancel), resources.getString(R.string.text_change_permission))
                setDialogListener(mPermissionDialogListener)
                show()
            }
        })
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
                _TitleText.text = resources.getString(R.string.text_homework_manage_title)
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

    override fun onBackPressed()
    {
        CommonUtils.getInstance(this).hideKeyboard()
        if (_HomeworkViewPager.currentItem == Common.PAGE_HOMEWORK_CALENDAR)
        {
            super.onBackPressed()
        }
        else
        {
            factoryViewModel.onClickBackButton()
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
            R.id._backButtonRect -> factoryViewModel.onClickBackButton()
        }
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) { }

        override fun onPageSelected(position : Int)
        {
            Log.f("Homework Page Change : $position")
            factoryViewModel.onPageChanged(position)
        }

        override fun onPageScrollStateChanged(state : Int) { }
    }

    private val mPermissionDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, messageType : Int)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    // [취소] 컨텐츠 사용 불가 메세지 표시
                    factoryViewModel.onRecordPermissionCancel()
                }
                DialogButtonType.BUTTON_2 ->
                {
                    // [권한 변경하기] 앱 정보 화면으로 이동
                    factoryViewModel.onRecordPermissionChange()
                }
            }
        }
    }

    private val mStatusActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        factoryViewModel.onActivityResultStatus()
    }
}