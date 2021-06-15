package com.littlefox.app.foxschool.main

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import butterknife.*
import butterknife.Optional
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.login.UserInformationResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.MainContract
import com.littlefox.app.foxschool.main.presenter.MainPresenter
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.extra.SwipeDisableViewPager
import com.littlefox.library.view.scroller.FixedSpeedScroller
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

import java.lang.reflect.Field
import java.text.ParseException
import java.text.SimpleDateFormat
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

    @BindView(R.id._topMenuSetting)
    lateinit var _TopMenuSetting : ImageView

    @BindView(R.id._topMenuSearch)
    lateinit var _TopMenuSearch : ImageView

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

    @BindView(R.id._userThumbnailImage)
    lateinit var _UserThumbnailImage : ImageView

    @BindView(R.id._userBadgeBackground)
    lateinit var _UserBadgeBackground : ImageView

    @BindView(R.id._userTypeText)
    lateinit var _UserTypeText : TextView

    @BindView(R.id._userNameText)
    lateinit var _UserNameText : TextView

    @BindView(R.id._userSelectMenuButton)
    lateinit var _UserSelectMenuButton : ImageView

    @BindView(R.id._userSelectMenuLayout)
    lateinit  var _UserSelectMenuLayout : LinearLayout

    @BindViews(
        R.id._parentUserLayout,
        R.id._childUser1Layout,
        R.id._childUser2Layout,
        R.id._childUser3Layout
    )
    lateinit var _UserSelectMenuItemLayoutList : List<@JvmSuppressWildcards ScalableLayout>

    @BindViews(
        R.id._parentUserThumnnailImage,
        R.id._childUser1ThumnnailImage,
        R.id._childUser2ThumnnailImage,
        R.id._childUser3ThumnnailImage
    )
    lateinit var _UserSelectMenuItemThumnnailList : List<@JvmSuppressWildcards ImageView>

    @BindViews(
        R.id._parentUserBadgeBackground,
        R.id._childUser1BadgeBackground,
        R.id._childUser2BadgeBackground,
        R.id._childUser3BadgeBackground
    )
    lateinit var _UserSelectMenuItemBadgeList : List<@JvmSuppressWildcards ImageView>

    @BindViews(
        R.id._parentUserTypeText,
        R.id._childUser1TypeText,
        R.id._childUser2TypeText,
        R.id._childUser3TypeText
    )
    lateinit var _UserSelectMenuItemTypeList : List<@JvmSuppressWildcards TextView>

    @BindViews(
        R.id._parentUserNameText,
        R.id._childUser1NameText,
        R.id._childUser2NameText,
        R.id._childUser3NameText
    )
    lateinit var _UserSelectMenuItemNameList : List<@JvmSuppressWildcards TextView>

    @BindViews(
        R.id._parentUserCheckImage,
        R.id._childUser1CheckImage,
        R.id._childUser2CheckImage,
        R.id._childUser3CheckImage
    )
    lateinit var _UserSelectMenuItemCheckImageList : List<@JvmSuppressWildcards ImageView>

    @BindView(R.id._addChildUserButtonLayout)
    lateinit var _AddChildUserButtonLayout : ScalableLayout

    @BindView(R.id._addChildUserButton)
    lateinit var _AddChildUserButton : ImageView

    @BindView(R.id._addChildUserText)
    lateinit var _AddChildUserText : TextView

    @BindView(R.id._paymentInfoLayout)
    lateinit var _PaymentInfoLayout : ScalableLayout

    @BindView(R.id._paymentRemainingDateText)
    lateinit var _PaymentRemainingDateText : TextView

    @BindView(R.id._studyInfoLayout)
    lateinit var _StudyInfoLayout : ScalableLayout

    @BindView(R.id._leaningLogMenuButton)
    lateinit var _LeaningLogMenuButton : ImageView

    @BindView(R.id._leaningLogMenuText)
    lateinit var _LeaningLogMenuText : TextView

    @BindView(R.id._attendanceMenuButton)
    lateinit var _AttendanceMenuButton : ImageView

    @BindView(R.id._attendanceMenuText)
    lateinit var _AttendanceMenuText : TextView

    @BindView(R.id._myInfoMenuButton)
    lateinit var _MyInfoMenuButton : ImageView

    @BindView(R.id._myInfoMenuText)
    lateinit var _MyInfoMenuText : TextView

    @BindView(R.id._menuItemScrollView)
    lateinit var _MenuItemScrollView : ScrollView

    @BindView(R.id._menuItemLayout)
    lateinit var _MenuItemLayout : LinearLayout

    @BindView(R.id._loginEnterButton)
    lateinit var _LoginEnterButton : TextView

    @BindView(R.id._signEnterButton)
    lateinit var _SignEnterButton : TextView

    @BindView(R.id._loginThumbnailImage)
    lateinit var _LoginThumbnailImage : ImageView

    @BindView(R.id._loginMessageText)
    lateinit var _LoginMessageText : TextView

    @BindView(R.id._menuLogoutLayout)
    lateinit var _MenuLogoutLayout : ScalableLayout

    @BindView(R.id._menuLogoutText)
    lateinit var _MenuLogoutText : TextView

    companion object
    {
        private val MESSAGE_FORCE_CLOSE_DRAWER_MENU : Int   = 10
        private val MESSAGE_SEND_CHANGE_USER : Int          = 11
        private val MAX_USER_SIZE : Int                     = 4
        private val PARENT_USER_INDEX : Int                 = 0
        private val CHILD_USER_1_INDEX : Int                = 1
        private val CHILD_USER_2_INDEX : Int                = 2
        private val CHILD_USER_3_INDEX : Int                = 3
        private val TAB_INDICATOR_COLOR = intArrayOf(
            R.color.color_1fb77c,
            R.color.color_1fb77c,
            R.color.color_1fb77c
        )
        private val TAB_IMAGE_ICONS = intArrayOf(
            R.drawable.choice_top_bar_icon_story,
            R.drawable.choice_top_bar_icon_song,
            R.drawable.choice_top_bar_icon_my_books
        )
        private val TAB_IMAGE_ICONS_TABLET = intArrayOf(
            R.drawable.choice_top_bar_icon_story_tablet,
            R.drawable.choice_top_bar_icon_song_tablet,
            R.drawable.choice_top_bar_icon_my_books_tablet
        )
        private val TAB_BACKGROUND_COLOR = intArrayOf(
            R.color.color_23cc8a,
            R.color.color_23cc8a,
            R.color.color_23cc8a
        )
        private val TAB_BACKGROUND_COLOR_TABLET = intArrayOf(
            R.color.color_fff55a,
            R.color.color_fff55a,
            R.color.color_fff55a
        )
    }

    private lateinit var mMainPresenter : MainPresenter
    private lateinit var _SettingButton : ImageView
    private lateinit var _SearchButton : ImageView
    private lateinit var mFixedSpeedScroller : FixedSpeedScroller
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mWeakReferenceHandler : WeakReferenceHandler
    private var mCurrentUserPosition = 0
    private var mSelectMenuLayoutHeight = 0
    private var mCurrentUserStatusSize = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        /**
         * 특정 사양 낮은 기기에서 메모리에 문제가 생겨서 onCreate가 되는 상황이 발생. 예외코드
         */
        CommonUtils.getInstance(this).initFeature()
        if(Feature.IS_TABLET)
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
        setStatusBarColor(Common.PAGE_STORY)
        _MainDrawLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        _MainDrawLayout.setFocusableInTouchMode(false)
        _MainDrawLayout.addDrawerListener(mDrawerListener)
        _NavigationBaseLayout.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(v : View, event : MotionEvent) : Boolean
            {
                return true
            }
        })

        if(Feature.IS_TABLET === false)
        {
            settingToolbar()
        } else
        {
            val TABLET_DRAWER_MENU_WIDTH = 650
            val params : DrawerLayout.LayoutParams = _NavigationBaseLayout.getLayoutParams() as DrawerLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_DRAWER_MENU_WIDTH)
            _NavigationBaseLayout.setLayoutParams(params)
        }
    }

    override fun initFont()
    {
        _UserTypeText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _UserNameText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _PaymentRemainingDateText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _LeaningLogMenuText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _AttendanceMenuText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _MyInfoMenuText.setTypeface(Font.getInstance(this).getRobotoMedium())
        for(i in 0 until MAX_USER_SIZE)
        {
            _UserSelectMenuItemTypeList[i].setTypeface(Font.getInstance(this).getRobotoMedium())
            _UserSelectMenuItemNameList[i].setTypeface(Font.getInstance(this).getRobotoMedium())
        }
        _AddChildUserText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _LoginMessageText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _SignEnterButton.setTypeface(Font.getInstance(this).getRobotoMedium())
        _LoginEnterButton.setTypeface(Font.getInstance(this).getRobotoMedium())
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
            MESSAGE_SEND_CHANGE_USER -> mMainPresenter.changeUser(mCurrentUserPosition)
        }
        mMainPresenter.sendMessageEvent(message)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mMainPresenter.acvitityResult(requestCode, resultCode, data)
    }

    override fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter)
    {
        settingViewPagerInformation(mainFragmentSelectionPagerAdapter)
        settingViewPagerController()
        if(Feature.IS_TABLET === false)
        {
            checkToolbarAnimationLayoutSize()
        }
    }

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSnackMessage(
            _MainContentCoordinatorLayout,
            message,
            getResources().getColor(R.color.color_18b5b2),
            Gravity.CENTER
        )
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showSnackMessage(
            _MainContentCoordinatorLayout,
            message,
            getResources().getColor(R.color.color_d8232a),
            Gravity.CENTER
        )
    }

    override fun settingUserInformation(userInformationResult : UserInformationResult)
    {
        setMenuLoginStatus()
        if(Feature.IS_FREE_USER)
        {
            return
        }
        for(i in _UserSelectMenuItemLayoutList.indices)
        {
            _UserSelectMenuItemLayoutList[i].setVisibility(View.GONE)
        }
        mCurrentUserStatusSize = userInformationResult.getUserInformationList().size
        for(i in 0 until mCurrentUserStatusSize)
        {
            val position = i
            _UserSelectMenuItemLayoutList[i].setVisibility(View.VISIBLE)
            if(userInformationResult.getUserInformationList().get(i).getID()
                    .equals(userInformationResult.getCurrentUserID())
            )
            {
                mCurrentUserPosition = i
                Glide.with(this)
                    .load(userInformationResult.getUserInformationList().get(i).getThumbnail())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(_UserThumbnailImage)
                if(userInformationResult.getUserInformationList().get(i).isCustomAvatar)
                {
                    _UserBadgeBackground.visibility = View.VISIBLE
                } else
                {
                    _UserBadgeBackground.visibility = View.GONE
                }
                if(i == 0)
                {
                    _UserTypeText.setText(getResources().getString(R.string.text_parent_user))
                } else
                {
                    _UserTypeText.setText(getResources().getString(R.string.text_child_user).toString() + i.toString())
                }
                _UserNameText.setText(userInformationResult.getUserInformationList().get(i).getNickName())
                _UserSelectMenuItemTypeList[i].setTextColor(getResources().getColor(R.color.color_787a9f))
                _UserSelectMenuItemNameList[i].setTextColor(getResources().getColor(R.color.color_2e3192))
                _UserSelectMenuItemCheckImageList[i].setImageResource(R.drawable.check_on)
            } else
            {
                _UserSelectMenuItemTypeList[i].setTextColor(getResources().getColor(R.color.color_a7a7bf))
                _UserSelectMenuItemNameList[i].setTextColor(getResources().getColor(R.color.color_9192bf))
                _UserSelectMenuItemCheckImageList[i].setImageResource(R.drawable.check_off)
            }
            Glide.with(this)
                .load(userInformationResult.getUserInformationList().get(i).getThumbnail())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(_UserSelectMenuItemThumnnailList[i])
            if(userInformationResult.getUserInformationList().get(i).isCustomAvatar)
            {
                _UserSelectMenuItemBadgeList.get(i).visibility = View.VISIBLE
            } else
            {
                _UserSelectMenuItemBadgeList.get(i).visibility = View.GONE
            }
            if(i == 0)
            {
                _UserSelectMenuItemTypeList[i].setText(getResources().getString(R.string.text_parent_user))
            } else
            {
                _UserSelectMenuItemTypeList[i].setText(getResources().getString(R.string.text_add_child_user).toString() + i.toString())
            }
            _UserSelectMenuItemNameList[i].setText(userInformationResult.getUserInformationList().get(i).getNickName())
        }
        if((userInformationResult.getUserInformationList().size === MAX_USER_SIZE
                    || mCurrentUserPosition != 0))
        {
            _AddChildUserButtonLayout.setVisibility(View.GONE)
        }
        else
        {
            _AddChildUserButtonLayout.setVisibility(View.VISIBLE)
        }
        Log.f("Feature.IS_REMAIN_DAY_END_USER : " + Feature.IS_REMAIN_DAY_END_USER)
        if(Feature.IS_REMAIN_DAY_END_USER)
        {
            _PaymentRemainingDateText.setText(getResources().getString(R.string.text_free_using))
        } else
        {
            if((Locale.getDefault().toString().contains(Locale.KOREA.toString())
                        || Locale.getDefault().toString().contains(Locale.JAPAN.toString())
                        || Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString())
                        || Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
            )
            {
                _PaymentRemainingDateText.setText(
                    java.lang.String.format(
                        getResources().getString(R.string.text_remaining_day),
                        userInformationResult.getRemainingDay()
                    )
                )
            } else
            {
                var remainingDate = ""
                try
                {
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userInformationResult.getExpireDate())
                    remainingDate = SimpleDateFormat("MM/dd/yyyy").format(date)
                } catch(e : ParseException)
                {
                    e.printStackTrace()
                }
                _PaymentRemainingDateText.setText(String.format(getResources().getString(R.string.text_remaining_day_name)) + " : " + remainingDate)
            }
        }
        _UserSelectMenuLayout.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener
            {
                override fun onGlobalLayout()
                {
                    _UserSelectMenuLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    mSelectMenuLayoutHeight = _UserSelectMenuLayout.getHeight()
                    Log.f("mSelectMenuLayoutHeight : $mSelectMenuLayoutHeight")
                }
            })
    }

    override fun setCurrentPage(page : Int)
    {
        Log.f("page : $page")
        _MainViewPager.setCurrentItem(page)
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

    @Optional
    @OnClick(
        R.id._userSelectMenuButton,
        R.id._parentUserLayout,
        R.id._childUser1Layout,
        R.id._childUser2Layout,
        R.id._childUser3Layout,
        R.id._loginEnterButton,
        R.id._myInfoMenuButton,
        R.id._addChildUserButtonLayout,
        R.id._attendanceMenuButton,
        R.id._signEnterButton,
        R.id._menuLogoutLayout,
        R.id._leaningLogMenuButton,
        R.id._topMenuSetting,
        R.id._topMenuSearch
    )
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._userSelectMenuButton ->
            if(_UserSelectMenuLayout.getVisibility() == View.INVISIBLE)
            {
                showUserSelectLayoutAnimation()
            } else
            {
                hideUserSelectLayoutAnimation()
            }
            R.id._parentUserLayout ->
            if(mCurrentUserPosition != PARENT_USER_INDEX)
            {
                mCurrentUserPosition = PARENT_USER_INDEX
                selectUserStatus(mCurrentUserPosition)
                hideUserSelectLayoutAnimation()
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_FORCE_CLOSE_DRAWER_MENU,
                    Common.DURATION_SHORT
                )
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_SEND_CHANGE_USER,
                    Common.DURATION_CHANGE_USER
                )
            }
            R.id._childUser1Layout ->
            if(mCurrentUserPosition != CHILD_USER_1_INDEX)
            {
                mCurrentUserPosition = CHILD_USER_1_INDEX
                selectUserStatus(mCurrentUserPosition)
                hideUserSelectLayoutAnimation()
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_FORCE_CLOSE_DRAWER_MENU,
                    Common.DURATION_SHORT
                )
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_SEND_CHANGE_USER,
                    Common.DURATION_CHANGE_USER
                )
            }
            R.id._childUser2Layout ->
            if(mCurrentUserPosition != CHILD_USER_2_INDEX)
            {
                mCurrentUserPosition = CHILD_USER_2_INDEX
                selectUserStatus(mCurrentUserPosition)
                hideUserSelectLayoutAnimation()
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_FORCE_CLOSE_DRAWER_MENU,
                    Common.DURATION_SHORT
                )
                mWeakReferenceHandler.sendEmptyMessageDelayed(
                    MESSAGE_SEND_CHANGE_USER,
                    Common.DURATION_CHANGE_USER
                )
            }
            R.id._childUser3Layout ->
            if(mCurrentUserPosition != CHILD_USER_3_INDEX)
            {
                mCurrentUserPosition = CHILD_USER_3_INDEX
                hideUserSelectLayoutAnimation()
                mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_FORCE_CLOSE_DRAWER_MENU, Common.DURATION_SHORT)
                mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_SEND_CHANGE_USER, Common.DURATION_CHANGE_USER)
            }
            R.id._loginEnterButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuLogin()
            }
            R.id._myInfoMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuMyInformation()
            }
            R.id._leaningLogMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuLearningLog()
            }
            R.id._addChildUserButtonLayout ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuAddUser()
            }
            R.id._attendanceMenuButton ->
            {
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                mMainPresenter.onClickMenuAttendance()
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
        _MainToolbar!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            {
                _MainToolbar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Log.f("Toolbar Height :" + _MainToolbar!!.height + ", _MainTabsLayout height : " + _MainTabsLayout.getMeasuredHeight())
                var params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, _MainToolbar.height)
                _MainBackgroundView!!.layoutParams = params
                params = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    _MainToolbar.height
                )
                _MainBackgroundAnimationLayout.setLayoutParams(params)
            }
        })
    }

    private fun initMenuView()
    {
        val MENU_TEXTVIEW_ID_LIST = intArrayOf(
            R.id._menuScheduleText,
            R.id._menuNewsText,
            R.id._menuTestimonialText,
            R.id._menuFAQsText,
            R.id._menu1on1AskText,
            R.id._menuAboutAppText
        )
        val MENU_IMAGEVIEW_ID_LIST = intArrayOf(
            R.id._menuScheduleButtonRect,
            R.id._menuNewsButtonRect,
            R.id._menuTestimonialButtonRect,
            R.id._menuFAQsButtonRect,
            R.id._menu1on1AskButtonRect,
            R.id._menuAboutAppButtonRect
        )
        var addLayout : LinearLayout? = null
        _MenuItemLayout.removeAllViews()
        if(Feature.IS_FREE_USER)
        {
            if(Feature.IS_TABLET)
            {
                addLayout = View.inflate(this, R.layout.drawer_item_free_user_tablet, null) as LinearLayout
            } else
            {
                addLayout = View.inflate(this, R.layout.drawer_item_free_user_phone, null) as LinearLayout
            }
        } else
        {
            if(Feature.IS_TABLET)
            {
                addLayout = View.inflate(this, R.layout.drawer_item_paid_user_tablet, null) as LinearLayout
            } else
            {
                addLayout = View.inflate(this, R.layout.drawer_item_paid_user_phone, null) as LinearLayout
            }
        }
        _MenuItemLayout.addView(addLayout)
        for(i in MENU_TEXTVIEW_ID_LIST.indices)
        {
            val textView : TextView = addLayout.findViewById<View>(MENU_TEXTVIEW_ID_LIST[i]) as TextView
            textView.setTypeface(Font.getInstance(this).getRobotoMedium())
            val imageView = addLayout.findViewById<View>(MENU_IMAGEVIEW_ID_LIST[i]) as ImageView
            imageView.setOnClickListener(mMenuClickListener)
        }
        _MenuLogoutText.setTypeface(Font.getInstance(this).getRobotoMedium())
    }

    private fun setMenuLoginStatus()
    {
        Log.i("Feature.IS_FREE_USER : " + Feature.IS_FREE_USER)
        initMenuView()
        if(Feature.IS_FREE_USER)
        {
            _LoginThumbnailImage.visibility = View.VISIBLE
            _LoginMessageText.setVisibility(View.VISIBLE)
            _SignEnterButton.setVisibility(View.VISIBLE)
            _LoginEnterButton.setVisibility(View.VISIBLE)
            _StudyInfoLayout.setVisibility(View.GONE)
            _UserThumbnailImage.visibility = View.GONE
            _UserTypeText.setVisibility(View.GONE)
            _UserNameText.setVisibility(View.GONE)
            _UserSelectMenuButton.visibility = View.GONE
            _PaymentRemainingDateText.setVisibility(View.GONE)
            _MenuLogoutLayout.setVisibility(View.GONE)
        } else
        {
            _LoginThumbnailImage.visibility = View.GONE
            _LoginMessageText.setVisibility(View.GONE)
            _SignEnterButton.setVisibility(View.GONE)
            _LoginEnterButton.setVisibility(View.GONE)
            _StudyInfoLayout.setVisibility(View.VISIBLE)
            _UserThumbnailImage.visibility = View.VISIBLE
            _UserTypeText.setVisibility(View.VISIBLE)
            _UserNameText.setVisibility(View.VISIBLE)
            _UserSelectMenuButton.visibility = View.VISIBLE
            _PaymentRemainingDateText.setVisibility(View.VISIBLE)
            _MenuLogoutLayout.setVisibility(View.VISIBLE)
        }
    }

    private fun showUserSelectLayoutAnimation()
    {
        Log.f("height : $mSelectMenuLayoutHeight")
        if(_UserSelectMenuLayout.getVisibility() == View.INVISIBLE)
        {
            ViewAnimator.animate(_UserSelectMenuLayout)
                .translationY(-mSelectMenuLayoutHeight.toFloat(), 0.0f)
                .duration(Common.DURATION_SHORT)
                .onStart {_UserSelectMenuLayout.setVisibility(View.VISIBLE)}
                .onStop {_UserSelectMenuButton.setImageResource(if(Feature.IS_TABLET) R.drawable.icon_arrow2_tablet else R.drawable.icon_arrow2)}
                .start()
        }
    }

    private fun hideUserSelectLayoutAnimation()
    {
        Log.f("")
        if(_UserSelectMenuLayout.getVisibility() == View.VISIBLE)
        {
            ViewAnimator.animate(_UserSelectMenuLayout)
                .translationY(0.0f, -mSelectMenuLayoutHeight.toFloat())
                .duration(Common.DURATION_SHORT)
                .onStart {_UserSelectMenuLayout.setVisibility(View.VISIBLE)}.onStop {
                    _UserSelectMenuLayout.setVisibility(View.INVISIBLE)
                    _UserSelectMenuButton.setImageResource(if(Feature.IS_TABLET) R.drawable.icon_arrow1_tablet else R.drawable.icon_arrow1)
                }.start()
        }
    }

    private fun selectUserStatus(index : Int)
    {
        for(i in 0 until mCurrentUserStatusSize)
        {
            if(i == index)
            {
                _UserSelectMenuItemTypeList[i].setTextColor(getResources().getColor(R.color.color_787a9f))
                _UserSelectMenuItemNameList[i].setTextColor(getResources().getColor(R.color.color_2e3192))
                _UserSelectMenuItemCheckImageList[i].setImageResource(R.drawable.check_on)
            } else
            {
                _UserSelectMenuItemTypeList[i].setTextColor(getResources().getColor(R.color.color_a7a7bf))
                _UserSelectMenuItemNameList[i].setTextColor(getResources().getColor(R.color.color_9192bf))
                _UserSelectMenuItemCheckImageList[i].setImageResource(R.drawable.check_off)
            }
        }
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
        if(Feature.IS_TABLET)
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
            if(Feature.IS_TABLET)
            {
                val image = ImageView(this)
                params = LinearLayout.LayoutParams(
                    CommonUtils.getInstance(this).getPixel(40),
                    CommonUtils.getInstance(this).getHeightPixel(36))
                image.layoutParams = params
                image.setImageResource(TAB_IMAGE_ICONS_TABLET[i])
                _MainTabsLayout.getTabAt(i)?.setCustomView(image)
            } else
            {
                val image = ImageView(this)
                params = LinearLayout.LayoutParams(
                    CommonUtils.getInstance(this).getPixel(72),
                    CommonUtils.getInstance(this).getHeightPixel(68))
                image.layoutParams = params
                image.setImageResource(TAB_IMAGE_ICONS[i])
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
                    if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        CommonUtils.getInstance(this@MainActivity).showSnackMessage(
                            _MainContentCoordinatorLayout,
                            getResources().getString(R.string.message_payment_service_login),
                            getResources().getColor(R.color.color_d8232a)
                        )
                    }
                    return true
                }
            })
        }
    }

    fun animateRevealColorFromCoordinates(viewRoot : ViewGroup, color : Int, x : Int, y : Int, duration : Long)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return
        }
        viewRoot.post(object : Runnable
        {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run()
            {
                val finalRadius = Math.hypot(viewRoot.getWidth().toDouble(), viewRoot.getHeight().toDouble()).toFloat()
                var anim : Animator? = null
                anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0f, finalRadius)
                viewRoot.setBackgroundColor(getResources().getColor(color))
                anim.setDuration(duration)
                anim.setInterpolator(AccelerateDecelerateInterpolator())
                anim.addListener(object : Animator.AnimatorListener
                {
                    override fun onAnimationStart(animation : Animator)
                    {
                    }

                    override fun onAnimationEnd(animation : Animator)
                    {
                        _MainBackgroundView.setBackgroundColor(getResources().getColor(color))
                    }

                    override fun onAnimationCancel(animation : Animator)
                    {
                    }

                    override fun onAnimationRepeat(animation : Animator)
                    {
                    }
                })
                anim.start()
            }
        })
    }

    private fun setStatusBarColor(index : Int)
    {
        CommonUtils.getInstance(this).setStatusBar(getResources().getColor(TAB_INDICATOR_COLOR[index]))
    }

    private fun setIndicatorBarColor(index : Int)
    {
        _MainTabsLayout.setSelectedTabIndicatorColor(
            if(Feature.IS_TABLET)
                getResources().getColor(TAB_BACKGROUND_COLOR_TABLET[index])
            else
                getResources().getColor(TAB_BACKGROUND_COLOR[index]
            )
        )
    }

    private val mOnPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener
    {
        override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) {}

        override fun onPageSelected(position : Int)
        {
            Log.f("position : $position")
            val rect = Rect()
            _MainTabsLayout.getTabAt(position)?.getCustomView()?.getGlobalVisibleRect(rect)
            animateRevealColorFromCoordinates(
                _MainBackgroundAnimationLayout,
                TAB_BACKGROUND_COLOR[position],
                rect.centerX(),
                rect.centerY(),
                if(Feature.IS_TABLET)
                    Common.DURATION_SHORT_LONG
                else
                    Common.DURATION_MENU_ANIMATION_PHONE
            )
            setStatusBarColor(position)
            setIndicatorBarColor(position)
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
            hideUserSelectLayoutAnimation()
        }

        override fun onDrawerStateChanged(newState : Int) {}
    }

    private val mMenuClickListener : View.OnClickListener = object : View.OnClickListener
    {
        override fun onClick(view : View)
        {
            when(view.id)
            {
                R.id._menuScheduleButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuPublishSchedule()
                }
                R.id._menuNewsButtonRect ->
                {
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout)
                    mMainPresenter.onClickMenuNews()
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
            }
        }
    }


}