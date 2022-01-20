package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.opengl.Visibility
import android.os.Bundle
import android.os.Message
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import butterknife.*
import butterknife.Optional
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.MainContract
import com.littlefox.app.foxschool.main.presenter.MainPresenter
import com.littlefox.library.common.CommonUtils.setSharedPreference
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

import java.lang.reflect.Field
import java.util.*

class MainActivity() : BaseActivity(), MessageHandlerCallback, MainContract.View
{
    @BindView(R.id._mainDrawLayout)
    lateinit var _MainDrawLayout : DrawerLayout

    @BindView(R.id._mainContent)
    lateinit var _MainContentCoordinatorLayout : CoordinatorLayout

    @Nullable
    @BindView(R.id._mainAppbarLayout)
    lateinit var _MainAppbarLayout : AppBarLayout

    @BindView(R.id._mainBackgroundView)
    lateinit var _MainBackgroundView : ImageView

    @BindView(R.id._mainBackgroundAnimationLayout)
    lateinit var _MainBackgroundAnimationLayout : FrameLayout

    @Nullable
    @BindView(R.id._topMenuSetting)
    lateinit var _TopMenuSetting : ImageView

    @Nullable
    @BindView(R.id._topMenuSearch)
    lateinit var _TopMenuSearch : ImageView

    @Nullable
    @BindView(R.id._topMenuSchoolName)
    lateinit var _TopMenuSchoolName : TextView

    @Nullable
    @BindView(R.id._mainToolBar)
    lateinit var _MainToolbar : Toolbar

    @BindView(R.id._mainTabLayout)
    lateinit var _MainTabsLayout : TabLayout

    @BindView(R.id._mainViewPager)
    lateinit var _MainViewPager : SwipeDisableViewPager

    @BindView(R.id._navigationBaseLayout)
    lateinit var _NavigationBaseLayout : RelativeLayout

    /*
    DRAWER LAYOUT
     */
    @BindView(R.id._userStatusLayout)
    lateinit var _UserStatusLayout : ScalableLayout


    @BindView(R.id._userNameText)
    lateinit var _UserNameText : TextView

    @BindView(R.id._userClassText)
    lateinit var _UserClassText : TextView

    @BindView(R.id._userInfoButtonText)
    lateinit var _UserInfoButtonText : TextView


    @BindView(R.id._studyInfoLayout)
    lateinit var _StudyInfoLayout : ScalableLayout

    @BindView(R.id._leaningLogMenuButton)
    lateinit var _LeaningLogMenuButton : ImageView

    @BindView(R.id._leaningLogMenuText)
    lateinit var _LeaningLogMenuText : TextView

    @BindView(R.id._recordLogText)
    lateinit var _RecordLogText : TextView

    @BindView(R.id._homeworkManageMenuButton)
    lateinit var _HomeworkManageMenuButton : ImageView

    @BindView(R.id._homeworkManageText)
    lateinit var _HomeworkManageText : TextView

    @BindView(R.id._menuHomeworkManageNewIcon)
    lateinit var _MenuHomeworkManageNewIcon : ImageView

    @BindView(R.id._menuItemScrollView)
    lateinit var _MenuItemScrollView : ScrollView

    @BindView(R.id._menuItemLayout)
    lateinit var _MenuItemLayout : LinearLayout


    @BindView(R.id._menuLogoutLayout)
    lateinit var _MenuLogoutLayout : ScalableLayout

    @BindView(R.id._menuLogoutText)
    lateinit var _MenuLogoutText : TextView

    companion object
    {
        private val MESSAGE_FORCE_CLOSE_DRAWER_MENU : Int   = 10

        private val TAB_IMAGE_ICONS_STUDENT = intArrayOf(
            R.drawable.choice_top_bar_icon_story_student,
            R.drawable.choice_top_bar_icon_song_student,
            R.drawable.choice_top_bar_icon_my_books_student
        )
        private val TAB_IMAGE_ICONS_TEACHER = intArrayOf(
            R.drawable.choice_top_bar_icon_story_teacher,
            R.drawable.choice_top_bar_icon_song_teacher,
            R.drawable.choice_top_bar_icon_my_books_teacher
        )
        private val TAB_IMAGE_ICONS_TABLET = intArrayOf(
            R.drawable.choice_top_bar_icon_story_tablet,
            R.drawable.choice_top_bar_icon_song_tablet,
            R.drawable.choice_top_bar_icon_my_books_tablet
        )

    }

    private lateinit var mMainPresenter : MainPresenter
    private lateinit var _SchoolNameText : TextView
    private lateinit var _SettingButton : ImageView
    private lateinit var _SearchButton : ImageView
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mWeakReferenceHandler : WeakReferenceHandler
    private var mSelectMenuLayoutHeight = 0
    private var mCurrentUserStatusSize = 0
    private var mLoginInformationResult : LoginInformationResult? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)


        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_main_tablet)
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main)
        }
        ButterKnife.bind(this)
        mWeakReferenceHandler = WeakReferenceHandler(this)
        mMainPresenter = MainPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        Log.f("")
        mMainPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mMainPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mMainPresenter.destroy()
    }

    override fun initView()
    {
        setStatusBarColor()
        setIndicatorBarColor()
        _MainDrawLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        _MainDrawLayout.isFocusableInTouchMode = false
        _MainDrawLayout.addDrawerListener(mDrawerListener)
        _NavigationBaseLayout.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(v : View, event : MotionEvent) : Boolean
            {
                return true
            }
        })

        if(CommonUtils.getInstance(this).checkTablet == false)
        {
            settingToolbar()
        }
        else
        {
            val TABLET_DRAWER_MENU_WIDTH = 650
            val params : DrawerLayout.LayoutParams = _NavigationBaseLayout.layoutParams as DrawerLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_DRAWER_MENU_WIDTH)
            _NavigationBaseLayout.layoutParams = params
        }
    }

    override fun initFont()
    {
        _UserNameText.typeface = Font.getInstance(this).getRobotoMedium()
        _UserClassText.typeface = Font.getInstance(this).getRobotoMedium()
        _UserInfoButtonText.typeface = Font.getInstance(this).getRobotoMedium()
        _LeaningLogMenuText.typeface = Font.getInstance(this).getRobotoMedium()
        _RecordLogText.typeface = Font.getInstance(this).getRobotoMedium()
        _HomeworkManageText.typeface = Font.getInstance(this).getRobotoMedium()
        if (CommonUtils.getInstance(this).checkTablet)
        {
            _TopMenuSchoolName.typeface = Font.getInstance(this).getRobotoBold()
        }
    }

    override fun onBackPressed()
    {
        if(_MainDrawLayout.isDrawerOpen(_NavigationBaseLayout))
        {
            _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
        } else
        {
            mMainPresenter.onBackPressed()
        }
    }

    override fun handlerMessage(message : Message)
    {
        when(message.what)
        {
            MESSAGE_FORCE_CLOSE_DRAWER_MENU -> _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
        }
        mMainPresenter.sendMessageEvent(message)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mMainPresenter.activityResult(requestCode, resultCode, data)
    }

    override fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter)
    {
        settingViewPagerInformation(mainFragmentSelectionPagerAdapter)
        settingViewPagerController()
        if(CommonUtils.getInstance(this).checkTablet == false)
        {
            checkToolbarAnimationLayoutSize()
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
        CommonUtils.getInstance(this).showSuccessSnackMessage(
            _MainContentCoordinatorLayout,
            message,
            Gravity.CENTER
        )
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(
            _MainContentCoordinatorLayout,
            message,
            Gravity.CENTER
        )
    }

    override fun showDownloadMessage(message : String)
    {
        CommonUtils.getInstance(this).showSnackMessage(
            _MainContentCoordinatorLayout,
            message,
            resources.getColor(R.color.color_white))
    }

    /**
     * 사용자 데이터 화면에 세팅
     */
    override fun settingUserInformation(loginInformationResult : LoginInformationResult?, isUpdateHomework : Boolean, isUpdateNews : Boolean)
    {
        mLoginInformationResult = loginInformationResult
        Log.f("isUpdateHomework : "+isUpdateHomework+", isUpdateNews : "+isUpdateNews)
        initMenuView(isUpdateHomework, isUpdateNews)

        settingUserLayout()
        settingLayoutColor()
        settingSchoolName()
    }

    /**
     * 사용자 영역 세팅 (이름, 반)
     */
    private fun settingUserLayout()
    {
        var name = mLoginInformationResult?.getUserInformation()?.getName()
        if (CommonUtils.getInstance(this).isTeacherMode)
        {
            name += " 선생님"
        }
        else
        {
            // 학생인 경우에만 class 데이터 존재
            val mClass = "${mLoginInformationResult?.getSchoolInformation()?.getGrade()}학년 ${mLoginInformationResult?.getSchoolInformation()?.getClassName()}"
            _UserClassText.text = mClass
            name += " 학생"
        }
        _UserNameText.text = name
    }

    /**
     * 상단바 & 메뉴화면 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _MainBackgroundView.setBackgroundColor(resources.getColor(backgroundColor))
        _UserStatusLayout.setBackgroundColor(resources.getColor(backgroundColor))
        _UserInfoButtonText.setTextColor(resources.getColor(backgroundColor))

        if (CommonUtils.getInstance(this).isTeacherMode)
        {
            _UserInfoButtonText.setBackgroundResource(R.drawable.round_box_empty_light_blue_60)
        }
        else
        {
            _UserInfoButtonText.setBackgroundResource(R.drawable.round_box_empty_green_60)
        }
    }

    /**
     * 학교명 설정
     */
    private fun settingSchoolName()
    {
        var schoolName = ""
        var schoolType = ""

        if(CommonUtils.getInstance(this).isTeacherMode)
        {
            schoolName = mLoginInformationResult!!.getTeacherInformation().getOrganizationName()
            schoolType = mLoginInformationResult!!.getTeacherInformation().getOrganizationTypeName()
        }
        else
        {
            schoolName = mLoginInformationResult!!.getSchoolInformation().getOrganizationName()
            schoolType = mLoginInformationResult!!.getSchoolInformation().getOrganizationTypeName()
        }

        schoolName += " $schoolType"

        // 학교명 16자 초과 시 말줄임표 처리
        if (schoolName.length > 16)
        {
            schoolName = schoolName.substring(0, 16)
            schoolName += "..."
        }

        if (CommonUtils.getInstance(this).checkTablet)
        {
            _TopMenuSchoolName.setText(schoolName)
        }
        else
        {
            _SchoolNameText.setText(schoolName)
        }
    }

    override fun setCurrentPage(page : Int)
    {
        Log.f("page : $page")
        _MainViewPager.currentItem = page
    }

    @Optional
    @OnClick(
        R.id._userInfoButtonText, R.id._leaningLogMenuButton, R.id._menuLogoutLayout, R.id._homeworkManageMenuButton, R.id._recordLogMenuButton,
        R.id._topMenuSetting, R.id._topMenuSearch
    )
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._userInfoButtonText ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuMyInformation()
            }
            R.id._leaningLogMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuLearningLog()
            }
            R.id._recordLogMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickRecordHistory()
            }
            R.id._homeworkManageMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuHomeworkManage()
            }
            R.id._menuLogoutLayout ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuLogout()
            }
            R.id._topMenuSetting ->
            {
                Log.f("")
                _MainDrawLayout.openDrawer(_NavigationBaseLayout)
            }
            R.id._topMenuSearch ->
            {
                Log.f("")
                mMainPresenter.onClickSearch()
            }
        }
    }

    private fun checkToolbarAnimationLayoutSize()
    {
        _MainToolbar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            {
                _MainToolbar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Log.f("Toolbar Height :${_MainToolbar.height}, _MainTabsLayout height : ${_MainTabsLayout.measuredHeight}")
                var params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, _MainToolbar.height)
                _MainBackgroundView.layoutParams = params
                params = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    _MainToolbar.height
                )
                _MainBackgroundAnimationLayout.layoutParams = params
            }
        })
    }

    /**
     * 메뉴화면 생성 (팍스스쿨 소식 ~ 로그아웃)
     */
    private fun initMenuView(isUpdateHomework : Boolean, isUpdateNews : Boolean)
    {
        val MENU_TEXTVIEW_ID_LIST = intArrayOf(
            R.id._menuFoxschoolNewsText,
            R.id._menuFAQsText,
            R.id._menu1on1AskText,
            R.id._menuAboutAppText,
            R.id._menuTeacherManualText,
            R.id._menuHomeNewspaperText
        )
        val MENU_IMAGEVIEW_ID_LIST = intArrayOf(
            R.id._menuFoxschoolNewsButtonRect,
            R.id._menuFAQsButtonRect,
            R.id._menu1on1AskButtonRect,
            R.id._menuAboutAppButtonRect,
            R.id._menuTeacherManualButtonRect,
            R.id._menuHomeNewspaperButtonRect
        )
        var addLayout : LinearLayout? = null
        _MenuItemLayout.removeAllViews()

        if(CommonUtils.getInstance(this).checkTablet)
        {
            addLayout = View.inflate(this, R.layout.drawer_item_user_tablet, null) as LinearLayout
        } else
        {
            addLayout = View.inflate(this, R.layout.drawer_item_user_phone, null) as LinearLayout
        }

        _MenuItemLayout.addView(addLayout)
        for(i in MENU_TEXTVIEW_ID_LIST.indices)
        {
            val textView : TextView = addLayout.findViewById<View>(MENU_TEXTVIEW_ID_LIST[i]) as TextView
            textView.typeface = Font.getInstance(this).getRobotoMedium()
            val imageView = addLayout.findViewById<View>(MENU_IMAGEVIEW_ID_LIST[i]) as ImageView
            imageView.setOnClickListener(mMenuClickListener)

            // 학생의 경우 교사 메뉴얼, 가정 통신문 항목 숨김
            if (CommonUtils.getInstance(this).isTeacherMode == false)
            {
                if ((i > MENU_TEXTVIEW_ID_LIST.size - 3))
                {
                    textView.visibility = View.GONE
                    imageView.visibility = View.GONE
                }
            }
        }

        val iconUpdateNewsView = addLayout.findViewById<View>(R.id._menuFoxschoolNewsNewIcon) as ImageView
        if(isUpdateNews)
        {
            iconUpdateNewsView.visibility = View.VISIBLE
        }
        else
        {
            iconUpdateNewsView.visibility = View.GONE
        }

        if(isUpdateHomework)
        {
            _MenuHomeworkManageNewIcon.visibility = View.VISIBLE
        }
        else
        {
            _MenuHomeworkManageNewIcon.visibility = View.GONE
        }

        // 학생의 경우 교사 메뉴얼, 가정 통신문 항목 숨김
        if (CommonUtils.getInstance(this).isTeacherMode == false)
        {
            val iconTeacherManual = addLayout.findViewById<View>(R.id._menuTeacherManualIcon) as ImageView
            val iconNewspaper = addLayout.findViewById<View>(R.id._menuHomeNewspaperIcon) as ImageView
            iconTeacherManual.visibility = View.GONE
            iconNewspaper.visibility = View.GONE
        }

        _MenuLogoutText.typeface = Font.getInstance(this).getRobotoMedium()
    }

    private fun settingViewPagerInformation(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter)
    {
        Log.f("Screen Page Count : " + mainFragmentSelectionPagerAdapter.getCount())
        _MainViewPager.setAdapter(mainFragmentSelectionPagerAdapter)
        _MainViewPager.setOffscreenPageLimit(mainFragmentSelectionPagerAdapter.getCount())
        _MainViewPager.addOnPageChangeListener(mOnPageChangeListener)
        _MainTabsLayout.setTabGravity(TabLayout.GRAVITY_FILL)
        _MainTabsLayout.setupWithViewPager(_MainViewPager)
        settingTabsLayout(mainFragmentSelectionPagerAdapter.getCount())
    }

    private fun settingViewPagerController()
    {
        mFixedSpeedScroller = FixedSpeedScroller(this, LinearOutSlowInInterpolator())
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL.toInt())
        try
        {
            val scroller : Field = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[_MainViewPager] = mFixedSpeedScroller
        } catch(e : Exception)
        {
            e.printStackTrace()
        }
    }

    private fun settingToolbar()
    {
        val customView : View
        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        setSupportActionBar(_MainToolbar)
        getSupportActionBar()?.setDisplayShowHomeEnabled(false)
        getSupportActionBar()?.setDisplayShowCustomEnabled(true)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            customView = inflater.inflate(R.layout.topbar_main_menu_tablet, null)
        } else
        {
            customView = inflater.inflate(R.layout.topbar_main_menu, null)
        }

        _SettingButton = customView.findViewById<View>(R.id._topMenuSetting) as ImageView
        _SettingButton.setOnClickListener(View.OnClickListener { // TODO: 드로우 레이아웃을 연다.
            Log.f("")
            _MainDrawLayout.openDrawer(_NavigationBaseLayout)
        })
        _SchoolNameText = customView.findViewById<View>(R.id._topMenuSchoolName) as TextView
        _SchoolNameText.typeface = Font.getInstance(this).getRobotoBold()
        _SearchButton = customView.findViewById<View>(R.id._topMenuSearch) as ImageView
        _SearchButton.setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(view : View)
            {
                Log.f("")
                mMainPresenter.onClickSearch()
            }
        })
        getSupportActionBar()?.setCustomView(customView)
        val parent = customView.parent as Toolbar
        parent.setContentInsetsAbsolute(0, 0)
    }

    private fun settingTabsLayout(tabsSize : Int)
    {
        var params : LinearLayout.LayoutParams? = null
        val viewGroup : View? = null
        for(i in 0 until tabsSize)
        {
            val index = i
            if(CommonUtils.getInstance(this).checkTablet)
            {
                val image = ImageView(this)
                params = LinearLayout.LayoutParams(
                    CommonUtils.getInstance(this).getPixel(40),
                    CommonUtils.getInstance(this).getHeightPixel(36))
                image.layoutParams = params
                image.setImageResource(TAB_IMAGE_ICONS_TABLET[i])
                _MainTabsLayout.getTabAt(i)?.setCustomView(image)
            }
            else
            {
                val image = ImageView(this)
                params = LinearLayout.LayoutParams(
                    CommonUtils.getInstance(this).getPixel(72),
                    CommonUtils.getInstance(this).getHeightPixel(68))
                image.layoutParams = params
                if (CommonUtils.getInstance(this).isTeacherMode)
                {
                    image.setImageResource(TAB_IMAGE_ICONS_TEACHER[i])
                }
                else
                {
                    image.setImageResource(TAB_IMAGE_ICONS_STUDENT[i])
                }
                _MainTabsLayout.getTabAt(i)?.setCustomView(image)
            }
        }
        _MainTabsLayout.getTabAt(Common.PAGE_STORY)?.getCustomView()?.setSelected(true)
        val tabStrip : LinearLayout = (_MainTabsLayout.getChildAt(0) as LinearLayout)
        if(Feature.IS_FREE_USER)
        {
            tabStrip.getChildAt(Common.PAGE_MY_BOOKS).setOnTouchListener(object : View.OnTouchListener
            {
                override fun onTouch(v : View, event : MotionEvent) : Boolean
                {
                    if(event.action == MotionEvent.ACTION_UP)
                    {
                        CommonUtils.getInstance(this@MainActivity).showSnackMessage(
                            _MainContentCoordinatorLayout,
                            resources.getString(R.string.message_payment_service_login),
                            resources.getColor(R.color.color_d8232a)
                        )
                    }
                    return true
                }
            })
        }
    }

    private fun setStatusBarColor()
    {
        val color : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(color))
    }

    private fun setIndicatorBarColor()
    {
        val color : Int = CommonUtils.getInstance(this).getTopBarIndicatorColor()
        _MainTabsLayout.setSelectedTabIndicatorColor(resources.getColor(color))
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) {}

        override fun onPageSelected(position : Int)
        {
            Log.f("position : $position")
        }

        override fun onPageScrollStateChanged(state : Int)
        {
        }
    }

    private val mDrawerListener : DrawerLayout.DrawerListener = object : DrawerLayout.DrawerListener
    {
        override fun onDrawerSlide(drawerView : View, slideOffset : Float) {}

        override fun onDrawerOpened(drawerView : View) {}

        override fun onDrawerClosed(drawerView : View)
        {
            Log.f("")
        }

        override fun onDrawerStateChanged(newState : Int) {}
    }

    private val mMenuClickListener : View.OnClickListener = object : View.OnClickListener
    {
        override fun onClick(view : View)
        {
            when(view.id)
            {
                R.id._menuFoxschoolNewsButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickFoxschoolNews()
                }
                R.id._menuFAQsButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuFAQ()
                }
                R.id._menu1on1AskButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenu1On1Ask()
                }
                R.id._menuAboutAppButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuAppUseGuide()
                }
                R.id._menuTeacherManualButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuTeacherManual()
                }
                R.id._menuHomeNewspaperButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuHomeNewsPaper()
                }
            }
        }
    }
}