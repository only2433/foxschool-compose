package com.littlefox.app.foxschool.main;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.littlefox.library.view.animator.AnimationListener;
import com.littlefox.library.view.animator.ViewAnimator;
import com.littlefox.library.view.dialog.MaterialLoadingDialog;
import com.littlefox.library.view.extra.SwipeDisableViewPager;
import com.littlefox.library.view.scroller.FixedSpeedScroller;
import com.littlefox.logmonitor.Log;
import com.ssomai.android.scalablelayout.ScalableLayout;

import net.littlefox.lf_app_fragment.R;
import net.littlefox.lf_app_fragment.adapter.MainFragmentSelectionPagerAdapter;
import net.littlefox.lf_app_fragment.base.BaseActivity;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.CommonUtils;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.common.Font;
import net.littlefox.lf_app_fragment.handler.WeakReferenceHandler;
import net.littlefox.lf_app_fragment.handler.callback.MessageHandlerCallback;
import net.littlefox.lf_app_fragment.main.contract.MainContract;
import net.littlefox.lf_app_fragment.main.contract.presenter.MainPresenter;
import net.littlefox.lf_app_fragment.object.result.login.UserInformationResult;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends BaseActivity implements MessageHandlerCallback, MainContract.View
{
    @BindView(R.id._mainDrawLayout)
    DrawerLayout _MainDrawLayout;

    @BindView(R.id._mainContent)
    CoordinatorLayout _MainContentCoordinatorLayout;

    @Nullable
    @BindView(R.id._mainAppbarLayout)
    AppBarLayout _MainAppbarLayout;

    @BindView(R.id._mainBackgroundView)
    ImageView _MainBackgroundView;

    @BindView(R.id._mainBackgroundAnimationLayout)
    FrameLayout _MainBackgroundAnimationLayout;

    @Nullable
    @BindView(R.id._topMenuSetting)
    ImageView _TopMenuSetting;

    @Nullable
    @BindView(R.id._topMenuSearch)
    ImageView _TopMenuSearch;

    @Nullable
    @BindView(R.id._mainToolBar)
    Toolbar _MainToolbar;

    @BindView(R.id._mainTabLayout)
    TabLayout _MainTabsLayout;

    @BindView(R.id._mainViewPager)
    SwipeDisableViewPager _MainViewPager;

    @BindView(R.id._navigationBaseLayout)
    RelativeLayout _NavigationBaseLayout;

    /*
    DRAWER LAYOUT
     */
    @BindView(R.id._userStatusLayout)
    ScalableLayout _UserStatusLayout;

    @BindView(R.id._userThumbnailImage)
    ImageView _UserThumbnailImage;

    @BindView(R.id._userBadgeBackground)
    ImageView _UserBadgeBackground;

    @BindView(R.id._userTypeText)
    TextView _UserTypeText;

    @BindView(R.id._userNameText)
    TextView _UserNameText;

    @BindView(R.id._userSelectMenuButton)
    ImageView _UserSelectMenuButton;

    @BindView(R.id._userSelectMenuLayout)
    LinearLayout _UserSelectMenuLayout;

    @BindViews({R.id._parentUserLayout, R.id._childUser1Layout, R.id._childUser2Layout, R.id._childUser3Layout})
    List<ScalableLayout> _UserSelectMenuItemLayoutList;

    @BindViews({R.id._parentUserThumnnailImage, R.id._childUser1ThumnnailImage, R.id._childUser2ThumnnailImage, R.id._childUser3ThumnnailImage})
    List<ImageView> _UserSelectMenuItemThumnnailList;

    @BindViews({R.id._parentUserBadgeBackground, R.id._childUser1BadgeBackground, R.id._childUser2BadgeBackground, R.id._childUser3BadgeBackground})
    List<ImageView> _UserSelectMenuItemBadgeList;

    @BindViews({R.id._parentUserTypeText, R.id._childUser1TypeText, R.id._childUser2TypeText, R.id._childUser3TypeText})
    List<TextView> _UserSelectMenuItemTypeList;

    @BindViews({R.id._parentUserNameText, R.id._childUser1NameText, R.id._childUser2NameText, R.id._childUser3NameText})
    List<TextView> _UserSelectMenuItemNameList;

    @BindViews({R.id._parentUserCheckImage, R.id._childUser1CheckImage, R.id._childUser2CheckImage, R.id._childUser3CheckImage})
    List<ImageView> _UserSelectMenuItemCheckImageList;

    @BindView(R.id._addChildUserButtonLayout)
    ScalableLayout _AddChildUserButtonLayout;

    @BindView(R.id._addChildUserButton)
    ImageView _AddChildUserButton;

    @BindView(R.id._addChildUserText)
    TextView _AddChildUserText;

    @BindView(R.id._paymentInfoLayout)
    ScalableLayout _PaymentInfoLayout;

    @BindView(R.id._paymentRemainingDateText)
    TextView _PaymentRemainingDateText;

    @BindView(R.id._detailPaymentInfoButton)
    TextView _DetailPaymentInfoButton;

    @BindView(R.id._studyInfoLayout)
    ScalableLayout _StudyInfoLayout;

    @BindView(R.id._leaningLogMenuButton)
    ImageView _LeaningLogMenuButton;

    @BindView(R.id._leaningLogMenuText)
    TextView _LeaningLogMenuText;

    @BindView(R.id._attendanceMenuButton)
    ImageView _AttendanceMenuButton;

    @BindView(R.id._attendanceMenuText)
    TextView _AttendanceMenuText;

    @BindView(R.id._myInfoMenuButton)
    ImageView _MyInfoMenuButton;

    @BindView(R.id._myInfoMenuText)
    TextView _MyInfoMenuText;

    @BindView(R.id._menuItemScrollView)
    ScrollView _MenuItemScrollView;

    @BindView(R.id._menuItemLayout)
    LinearLayout _MenuItemLayout;

    @BindView(R.id._loginEnterButton)
    TextView _LoginEnterButton;

    @BindView(R.id._signEnterButton)
    TextView _SignEnterButton;

    @BindView(R.id._loginThumbnailImage)
    ImageView _LoginThumbnailImage;

    @BindView(R.id._loginMessageText)
    TextView _LoginMessageText;

    @BindView(R.id._menuLogoutLayout)
    ScalableLayout _MenuLogoutLayout;

    @BindView(R.id._menuLogoutText)
    TextView _MenuLogoutText;

    private static final int MESSAGE_FORCE_CLOSE_DRAWER_MENU            = 10;
    private static final int MESSAGE_SEND_CHANGE_USER                   = 11;

    private static final int MAX_USER_SIZE = 4;

    private static final int PARENT_USER_INDEX  = 0;
    private static final int CHILD_USER_1_INDEX = 1;
    private static final int CHILD_USER_2_INDEX = 2;
    private static final int CHILD_USER_3_INDEX = 3;

    private static final int[] TAB_INDICATOR_COLOR = {R.color.color_1a8ec7, R.color.color_198791, R.color.color_a55376, R.color.color_5c42a6, R.color.color_df8004};
    private static final int[] TAB_IMAGE_ICONS  = { R.drawable.choice_top_bar_icon_home, R.drawable.choice_top_bar_icon_story, R.drawable.choice_top_bar_icon_song, R.drawable.choice_top_bar_icon_my_books, R.drawable.choice_top_bar_icon_class};
    private static final int[] TAB_IMAGE_ICONS_TABLET  = { R.drawable.choice_top_bar_icon_home_tablet, R.drawable.choice_top_bar_icon_story_tablet, R.drawable.choice_top_bar_icon_song_tablet, R.drawable.choice_top_bar_icon_my_books_tablet, R.drawable.choice_top_bar_icon_class_tablet};

    private static final int[] TAB_BACKGROUND_COLOR = {R.color.color_20b1f9, R.color.color_26d0df, R.color.color_fe7fb5, R.color.color_8d65ff, R.color.color_ffa531};
    private static final int[] TAB_BACKGROUND_COLOR_TABLET = {R.color.color_fff55a, R.color.color_fff55a, R.color.color_fff55a, R.color.color_fff55a, R.color.color_fff55a};

    private MainPresenter mMainPresenter = null;
    private ImageView _SettingButton;
    private ImageView _SearchButton;
    private FixedSpeedScroller mFixedSpeedScroller;
    private MaterialLoadingDialog mMaterialLoadingDialog = null;
    private WeakReferenceHandler mWeakReferenceHandler = null;
    private int mCurrentUserPosition;
    private int mSelectMenuLayoutHeight = 0;
    private int mCurrentUserStatusSize = 0;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        /**
         * 특정 사양 낮은 기기에서 메모리에 문제가 생겨서 onCreate가 되는 상황이 발생. 예외코드
         */
        CommonUtils.getInstance(this).initFeature();

        if(Feature.IS_TABLET)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            setContentView(R.layout.activity_main_tablet);
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_main);
        }

        ButterKnife.bind(this);
        mWeakReferenceHandler = new WeakReferenceHandler(this);
        mMainPresenter = new MainPresenter(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.f("");
        mMainPresenter.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mMainPresenter.pause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mMainPresenter.destroy();
    }

    @Override
    public void initView()
    {
        setStatusBarColor(Common.PAGE_HOME);
        _MainDrawLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        _MainDrawLayout.setFocusableInTouchMode(false);
        _MainDrawLayout.addDrawerListener(mDrawerListener);

        _NavigationBaseLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        if(Feature.IS_TABLET == false)
        {
            settingToolbar();
        }
        else
        {
            final int TABLET_DRAWER_MENU_WIDTH = 650;
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) _NavigationBaseLayout.getLayoutParams();
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_DRAWER_MENU_WIDTH);
            _NavigationBaseLayout.setLayoutParams(params);
        }

    }

    @Override
    public void initFont()
    {
        _UserTypeText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _UserNameText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _PaymentRemainingDateText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _DetailPaymentInfoButton.setTypeface(Font.getInstance(this).getRobotoMedium());

        _LeaningLogMenuText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _AttendanceMenuText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _MyInfoMenuText.setTypeface(Font.getInstance(this).getRobotoMedium());

        for(int i = 0; i < MAX_USER_SIZE; i++)
        {
            _UserSelectMenuItemTypeList.get(i).setTypeface(Font.getInstance(this).getRobotoMedium());
            _UserSelectMenuItemNameList.get(i).setTypeface(Font.getInstance(this).getRobotoMedium());
        }
        _AddChildUserText.setTypeface(Font.getInstance(this).getRobotoMedium());

        _LoginMessageText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _SignEnterButton.setTypeface(Font.getInstance(this).getRobotoMedium());
        _LoginEnterButton.setTypeface(Font.getInstance(this).getRobotoMedium());
    }

    @Override
    public void onBackPressed()
    {
        if(_MainDrawLayout.isDrawerOpen(_NavigationBaseLayout))
        {
            _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
        }
        else
        {
            mMainPresenter.onBackPressed();
        }

    }

    @Override
    public void handlerMessage(Message message)
    {
        switch (message.what)
        {
            case MESSAGE_FORCE_CLOSE_DRAWER_MENU:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                break;
            case MESSAGE_SEND_CHANGE_USER:
                mMainPresenter.changeUser(mCurrentUserPosition);
                break;

        }
        mMainPresenter.sendMessageEvent(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mMainPresenter.acvitityResult(requestCode, resultCode, data);
    }

    @Override
    public void initViewPager(MainFragmentSelectionPagerAdapter mainFragmentSelectionPagerAdapter)
    {
        settingViewPagerInformation(mainFragmentSelectionPagerAdapter);
        settingViewPagerController();

        if(Feature.IS_TABLET == false)
        {
            checkToolbarAnimationLayoutSize();
        }

    }

    @Override
    public void showSuccessMessage(String message)
    {
        CommonUtils.getInstance(this).showSnackMessage(_MainContentCoordinatorLayout,
                message,
                getResources().getColor(R.color.color_18b5b2), Gravity.CENTER);
    }


    @Override
    public void showErrorMessage(String message)
    {
        CommonUtils.getInstance(this).showSnackMessage(_MainContentCoordinatorLayout,
                message,
                getResources().getColor(R.color.color_d8232a), Gravity.CENTER);
    }

    @Override
    public void settingUserInformation(UserInformationResult userInformationResult)
    {
        setMenuLoginStatus();

        if(Feature.IS_FREE_USER)
        {
            return;
        }
        for(int i = 0; i < _UserSelectMenuItemLayoutList.size(); i++)
        {
            _UserSelectMenuItemLayoutList.get(i).setVisibility(View.GONE);
        }
        mCurrentUserStatusSize = userInformationResult.getUserInformationList().size();
        for(int i = 0; i < mCurrentUserStatusSize; i++)
        {
            final int position = i;

            _UserSelectMenuItemLayoutList.get(i).setVisibility(View.VISIBLE);
            if(userInformationResult.getUserInformationList().get(i).getID().equals(userInformationResult.getCurrentUserID()))
            {
                mCurrentUserPosition = i;
                Glide.with(this)
                        .load(userInformationResult.getUserInformationList().get(i).getThumbnail())
                        .transition(withCrossFade())
                        .into(_UserThumbnailImage);

                if(userInformationResult.getUserInformationList().get(i).isCustomAvatar())
                {
                    _UserBadgeBackground.setVisibility(View.VISIBLE);
                }
                else
                {
                    _UserBadgeBackground.setVisibility(View.GONE);
                }

                if(i == 0)
                {
                    _UserTypeText.setText(getResources().getString(R.string.text_parent_user));
                    _DetailPaymentInfoButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    _UserTypeText.setText(getResources().getString(R.string.text_child_user)+String.valueOf(i));
                    _DetailPaymentInfoButton.setVisibility(View.GONE);
                }

                _UserNameText.setText(userInformationResult.getUserInformationList().get(i).getNickName());
                _UserSelectMenuItemTypeList.get(i).setTextColor(getResources().getColor(R.color.color_787a9f));
                _UserSelectMenuItemNameList.get(i).setTextColor(getResources().getColor(R.color.color_2e3192));
                _UserSelectMenuItemCheckImageList.get(i).setImageResource(R.drawable.check_on);
            }
            else
            {
                _UserSelectMenuItemTypeList.get(i).setTextColor(getResources().getColor(R.color.color_a7a7bf));
                _UserSelectMenuItemNameList.get(i).setTextColor(getResources().getColor(R.color.color_9192bf));
                _UserSelectMenuItemCheckImageList.get(i).setImageResource(R.drawable.check_off);
            }

            Glide.with(this)
                    .load(userInformationResult.getUserInformationList().get(i).getThumbnail())
                    .transition(withCrossFade())
                    .into(_UserSelectMenuItemThumnnailList.get(i));

            if(userInformationResult.getUserInformationList().get(i).isCustomAvatar())
            {
                _UserSelectMenuItemBadgeList.get(i).setVisibility(View.VISIBLE);
            }
            else
            {
                _UserSelectMenuItemBadgeList.get(i).setVisibility(View.GONE);
            }

            if(i == 0)
            {
                _UserSelectMenuItemTypeList.get(i).setText(getResources().getString(R.string.text_parent_user));
            }
            else
            {
                _UserSelectMenuItemTypeList.get(i).setText(getResources().getString(R.string.text_add_child_user)+String.valueOf(i));
            }

            _UserSelectMenuItemNameList.get(i).setText(userInformationResult.getUserInformationList().get(i).getNickName());

        }

        if(userInformationResult.getUserInformationList().size() == MAX_USER_SIZE
                || mCurrentUserPosition != 0)
        {
            _AddChildUserButtonLayout.setVisibility(View.GONE);
        }
        else
        {
            _AddChildUserButtonLayout.setVisibility(View.VISIBLE);
        }

        Log.f("Feature.IS_REMAIN_DAY_END_USER : "+Feature.IS_REMAIN_DAY_END_USER);
        if(Feature.IS_REMAIN_DAY_END_USER)
        {
            _PaymentRemainingDateText.setText(getResources().getString(R.string.text_free_using));
            _DetailPaymentInfoButton.setText(getResources().getString(R.string.text_subscribe));
        }
        else
        {
            if(Locale.getDefault().toString().contains(Locale.KOREA.toString())
                    || Locale.getDefault().toString().contains(Locale.JAPAN.toString())
                    || Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString())
                    || Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
            {
                _PaymentRemainingDateText.setText(String.format(getResources().getString(R.string.text_remaining_day), userInformationResult.getRemainingDay()));
            }
            else
            {
                String remainingDate = "";
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userInformationResult.getExpireDate());
                    remainingDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                _PaymentRemainingDateText.setText(String.format(getResources().getString(R.string.text_remaining_day_name))+" : "+remainingDate);

            }

            _DetailPaymentInfoButton.setText(getResources().getString(R.string.text_detail_view));
        }


        _UserSelectMenuLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout()
            {
                _UserSelectMenuLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);


                mSelectMenuLayoutHeight = _UserSelectMenuLayout.getHeight();
                Log.f("mSelectMenuLayoutHeight : "+mSelectMenuLayoutHeight);
            }
        });
    }

    @Override
    public void setCurrentPage(int page)
    {
        Log.f("page : "+page);
        _MainViewPager.setCurrentItem(page);
    }


    @Override
    public void showLoading()
    {
        mMaterialLoadingDialog = new MaterialLoadingDialog(this, CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE));
        mMaterialLoadingDialog.show();
    }

    @Override
    public void hideLoading()
    {
        if(mMaterialLoadingDialog != null)
        {
            mMaterialLoadingDialog.dismiss();
            mMaterialLoadingDialog = null;
        }
    }

    @Optional
    @OnClick({R.id._userSelectMenuButton, R.id._parentUserLayout, R.id._childUser1Layout, R.id._childUser2Layout, R.id._childUser3Layout
    ,  R.id._loginEnterButton,  R.id._myInfoMenuButton, R.id._addChildUserButtonLayout, R.id._attendanceMenuButton, R.id._signEnterButton, R.id._detailPaymentInfoButton, R.id._menuLogoutLayout,
    R.id._leaningLogMenuButton, R.id._topMenuSetting, R.id._topMenuSearch})
    public void onClickView(View view)
    {
        switch (view.getId())
        {
            case R.id._userSelectMenuButton:
                if(_UserSelectMenuLayout.getVisibility() == View.INVISIBLE)
                {
                    showUserSelectLayoutAnimation();
                }
                else
                {
                    hideUserSelectLayoutAnimation();
                }
                break;
            case R.id._parentUserLayout:
                if(mCurrentUserPosition != PARENT_USER_INDEX)
                {
                    mCurrentUserPosition = PARENT_USER_INDEX;
                    selectUserStatus(mCurrentUserPosition);
                    hideUserSelectLayoutAnimation();
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_FORCE_CLOSE_DRAWER_MENU, Common.DURATION_SHORT);
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_SEND_CHANGE_USER, Common.DURATION_CHANGE_USER);
                }
                break;
            case R.id._childUser1Layout:
                if(mCurrentUserPosition != CHILD_USER_1_INDEX)
                {
                    mCurrentUserPosition = CHILD_USER_1_INDEX;
                    selectUserStatus(mCurrentUserPosition);
                    hideUserSelectLayoutAnimation();
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_FORCE_CLOSE_DRAWER_MENU, Common.DURATION_SHORT);
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_SEND_CHANGE_USER, Common.DURATION_CHANGE_USER);
                }
                break;
            case R.id._childUser2Layout:
                if(mCurrentUserPosition != CHILD_USER_2_INDEX)
                {
                    mCurrentUserPosition = CHILD_USER_2_INDEX;
                    selectUserStatus(mCurrentUserPosition);
                    hideUserSelectLayoutAnimation();
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_FORCE_CLOSE_DRAWER_MENU, Common.DURATION_SHORT);
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_SEND_CHANGE_USER, Common.DURATION_CHANGE_USER);
                }
                break;
            case R.id._childUser3Layout:
                if(mCurrentUserPosition != CHILD_USER_3_INDEX)
                {
                    mCurrentUserPosition = CHILD_USER_3_INDEX;
                    hideUserSelectLayoutAnimation();
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_FORCE_CLOSE_DRAWER_MENU, Common.DURATION_SHORT);
                    mWeakReferenceHandler.sendEmptyMessageDelayed(MESSAGE_SEND_CHANGE_USER, Common.DURATION_CHANGE_USER);
                }
                break;

            case R.id._loginEnterButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuLogin();
                break;

            case R.id._myInfoMenuButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuMyInformation();
                break;
            case R.id._leaningLogMenuButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuLearningLog();
                break;

            case R.id._addChildUserButtonLayout:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuAddUser();
                break;

            case R.id._attendanceMenuButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuAttendance();
                break;

            case R.id._signEnterButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickPaidSignIn();
                break;

            case R.id._detailPaymentInfoButton:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuDetailPaymentInformation();
                break;

            case R.id._menuLogoutLayout:
                _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                mMainPresenter.onClickMenuLogout();
                break;
            case R.id._topMenuSetting:
                Log.f("");
                _MainDrawLayout.openDrawer(_NavigationBaseLayout);
                break;
            case R.id._topMenuSearch:
                Log.f("");
                mMainPresenter.onClickSearch();
                break;
        }
    }


    private void checkToolbarAnimationLayoutSize()
    {
        _MainToolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _MainToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Log.f("Toolbar Height :"+ _MainToolbar.getHeight()+", _MainTabsLayout height : "+_MainTabsLayout.getMeasuredHeight());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, _MainToolbar.getHeight());
                _MainBackgroundView.setLayoutParams(params);

                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, _MainToolbar.getHeight());
                _MainBackgroundAnimationLayout.setLayoutParams(params);
            }
        });
    }

    private void initMenuView()
    {
        final int[] MENU_TEXTVIEW_ID_LIST = {
                R.id._menuScheduleText, R.id._menuNewsText, R.id._menuTestimonialText, R.id._menuFAQsText,
                R.id._menu1on1AskText, R.id._menuAboutAppText
        };

        final int[] MENU_IMAGEVIEW_ID_LIST = {
                R.id._menuScheduleButtonRect, R.id._menuNewsButtonRect, R.id._menuTestimonialButtonRect, R.id._menuFAQsButtonRect,
                R.id._menu1on1AskButtonRect, R.id._menuAboutAppButtonRect
        };

        LinearLayout addLayout = null;
        _MenuItemLayout.removeAllViews();

        if(Feature.IS_FREE_USER)
        {
            if(Feature.IS_TABLET)
            {
                addLayout = (LinearLayout)View.inflate(this, R.layout.drawer_item_free_user_tablet, null);
            }
            else
            {
                addLayout = (LinearLayout)View.inflate(this, R.layout.drawer_item_free_user_phone, null);
            }
        }
        else
        {
            if(Feature.IS_TABLET)
            {
                addLayout = (LinearLayout)View.inflate(this, R.layout.drawer_item_paid_user_tablet, null);
            }
            else
            {
                addLayout = (LinearLayout)View.inflate(this, R.layout.drawer_item_paid_user_phone, null);
            }
        }
        _MenuItemLayout.addView(addLayout);


        for(int i = 0; i < MENU_TEXTVIEW_ID_LIST.length ; i++)
        {
            TextView textView = (TextView)addLayout.findViewById(MENU_TEXTVIEW_ID_LIST[i]);
            textView.setTypeface(Font.getInstance(this).getRobotoMedium());

            ImageView imageView = (ImageView)addLayout.findViewById(MENU_IMAGEVIEW_ID_LIST[i]);
            imageView.setOnClickListener(mMenuClickListener);
        }

        if(Feature.IS_FREE_USER == false)
        {
            TextView _MenuRestoreText  = (TextView)addLayout.findViewById(R.id._menuRestoreText);
            _MenuRestoreText.setTypeface(Font.getInstance(this).getRobotoMedium());
            ImageView _MenuRestoreButtonRect = (ImageView)addLayout.findViewById(R.id._menuRestoreButtonRect);
            _MenuRestoreButtonRect.setOnClickListener(mMenuClickListener);
        }

        TextView _MenuStoreText = (TextView)addLayout.findViewById(R.id._menuStoreText);
        _MenuStoreText.setTypeface(Font.getInstance(this).getRobotoMedium());
        ImageView _MenuStoreButtonIcon = (ImageView)addLayout.findViewById(R.id._menuStoreButtonIcon);
        ImageView _MenuStoreButtonRect = (ImageView)addLayout.findViewById(R.id._menuStoreButtonRect);
        _MenuStoreButtonRect.setOnClickListener(mMenuClickListener);

        if(Locale.getDefault().toString().contains(Locale.KOREA.toString()) == false)
        {
            _MenuStoreText.setVisibility(View.GONE);
            _MenuStoreButtonIcon.setVisibility(View.GONE);
            _MenuStoreButtonRect.setVisibility(View.GONE);
        }

        _MenuLogoutText.setTypeface(Font.getInstance(this).getRobotoMedium());


    }

    private void setMenuLoginStatus()
    {
        Log.i("Feature.IS_FREE_USER : "+Feature.IS_FREE_USER);

        initMenuView();

        if(Feature.IS_FREE_USER)
        {
            _LoginThumbnailImage.setVisibility(View.VISIBLE);
            _LoginMessageText.setVisibility(View.VISIBLE);
            _SignEnterButton.setVisibility(View.VISIBLE);
            _LoginEnterButton.setVisibility(View.VISIBLE);
            _StudyInfoLayout.setVisibility(View.GONE);
            _UserThumbnailImage.setVisibility(View.GONE);
            _UserTypeText.setVisibility(View.GONE);
            _UserNameText.setVisibility(View.GONE);
            _UserSelectMenuButton.setVisibility(View.GONE);
            _PaymentRemainingDateText.setVisibility(View.GONE);
            _DetailPaymentInfoButton.setVisibility(View.GONE);
            _MenuLogoutLayout.setVisibility(View.GONE);

        }
        else
        {
            _LoginThumbnailImage.setVisibility(View.GONE);
            _LoginMessageText.setVisibility(View.GONE);
            _SignEnterButton.setVisibility(View.GONE);
            _LoginEnterButton.setVisibility(View.GONE);
            _StudyInfoLayout.setVisibility(View.VISIBLE);
            _UserThumbnailImage.setVisibility(View.VISIBLE);
            _UserTypeText.setVisibility(View.VISIBLE);
            _UserNameText.setVisibility(View.VISIBLE);
            _UserSelectMenuButton.setVisibility(View.VISIBLE);
            _PaymentRemainingDateText.setVisibility(View.VISIBLE);
            _DetailPaymentInfoButton.setVisibility(View.VISIBLE);
            _MenuLogoutLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showUserSelectLayoutAnimation()
    {
        Log.f("height : "+mSelectMenuLayoutHeight);

        if(_UserSelectMenuLayout.getVisibility() == View.INVISIBLE)
        {
            ViewAnimator.animate(_UserSelectMenuLayout)
                    .translationY(-mSelectMenuLayoutHeight, 0.0f)
                    .duration(Common.DURATION_SHORT)
                    .onStart(new AnimationListener.Start()
                    {
                        @Override
                        public void onStart()
                        {

                            _UserSelectMenuLayout.setVisibility(View.VISIBLE);

                        }
                    })
                    .onStop(new AnimationListener.Stop()
                    {
                        @Override
                        public void onStop()
                        {
                            _UserSelectMenuButton.setImageResource(Feature.IS_TABLET? R.drawable.icon_arrow2_tablet : R.drawable.icon_arrow2);
                        }
                    })
                    .start();
        }
    }

    private void hideUserSelectLayoutAnimation()
    {
        Log.f("");

        if(_UserSelectMenuLayout.getVisibility() == View.VISIBLE)
        {
            ViewAnimator.animate(_UserSelectMenuLayout)
                    .translationY(0.0f, -mSelectMenuLayoutHeight)
                    .duration(Common.DURATION_SHORT)
                    .onStart(new AnimationListener.Start()
                    {
                        @Override
                        public void onStart() {
                            _UserSelectMenuLayout.setVisibility(View.VISIBLE);

                        }
                    })
                    .onStop(new AnimationListener.Stop() {
                        @Override
                        public void onStop() {
                            _UserSelectMenuLayout.setVisibility(View.INVISIBLE);
                            _UserSelectMenuButton.setImageResource(Feature.IS_TABLET ? R.drawable.icon_arrow1_tablet : R.drawable.icon_arrow1);

                        }
                    }).start();
        }
    }

    private void selectUserStatus(int index)
    {
        for(int i = 0; i < mCurrentUserStatusSize; i++)
        {
            if(i == index)
            {
                _UserSelectMenuItemTypeList.get(i).setTextColor(getResources().getColor(R.color.color_787a9f));
                _UserSelectMenuItemNameList.get(i).setTextColor(getResources().getColor(R.color.color_2e3192));
                _UserSelectMenuItemCheckImageList.get(i).setImageResource(R.drawable.check_on);
            }
            else
            {
                _UserSelectMenuItemTypeList.get(i).setTextColor(getResources().getColor(R.color.color_a7a7bf));
                _UserSelectMenuItemNameList.get(i).setTextColor(getResources().getColor(R.color.color_9192bf));
                _UserSelectMenuItemCheckImageList.get(i).setImageResource(R.drawable.check_off);
            }
        }
    }

    private void settingViewPagerInformation(MainFragmentSelectionPagerAdapter mainFragmentSelectionPagerAdapter)
    {
        Log.f("Screen Page Count : "+mainFragmentSelectionPagerAdapter.getCount());
        _MainViewPager.setAdapter(mainFragmentSelectionPagerAdapter);
        _MainViewPager.setOffscreenPageLimit(mainFragmentSelectionPagerAdapter.getCount());
        _MainViewPager.addOnPageChangeListener(mOnPageChangeListener);

        _MainTabsLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        _MainTabsLayout.setupWithViewPager(_MainViewPager);

        settingTabsLayout(mainFragmentSelectionPagerAdapter.getCount());
    }

    private void settingViewPagerController()
    {
        mFixedSpeedScroller = new FixedSpeedScroller(this, new LinearOutSlowInInterpolator());
        mFixedSpeedScroller.setDuration(Common.DURATION_NORMAL);
        try
        {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(_MainViewPager, mFixedSpeedScroller);

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void settingToolbar()
    {
        View customView;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setSupportActionBar(_MainToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(Feature.IS_TABLET)
        {
            customView = inflater.inflate(R.layout.topbar_main_menu_tablet, null);
        }
        else
        {
            customView = inflater.inflate(R.layout.topbar_main_menu, null);
        }


        _SettingButton = (ImageView)customView.findViewById(R.id._topMenuSetting);
        _SettingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO: 드로우 레이아웃을 연다.
                Log.f("");
                _MainDrawLayout.openDrawer(_NavigationBaseLayout);
            }
        });

        _SearchButton = (ImageView)customView.findViewById( R.id._topMenuSearch);
        _SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.f("");
                mMainPresenter.onClickSearch();
            }
        });

        getSupportActionBar().setCustomView(customView);
        Toolbar parent =(Toolbar) customView.getParent();
        parent.setContentInsetsAbsolute(0,0);
    }

    private void settingTabsLayout(int tabsSize)
    {
        LinearLayout.LayoutParams params = null;
        View viewGroup = null;
        for(int i = 0 ; i < tabsSize; i++)
        {
            final int index = i;
            if(Feature.IS_TABLET)
            {
                ImageView image = new ImageView(this);
                if(i == Common.PAGE_CLASS)
                {
                    params = new LinearLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(61), CommonUtils.getInstance(this).getHeightPixel(36));
                }
                else
                {
                    params = new LinearLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(40), CommonUtils.getInstance(this).getHeightPixel(36));
                }
                image.setLayoutParams(params);
                image.setImageResource(TAB_IMAGE_ICONS_TABLET[i]);
                _MainTabsLayout.getTabAt(i).setCustomView(image);
            }
            else
            {
                ImageView image = new ImageView(this);
                if(i == Common.PAGE_CLASS)
                {
                    params = new LinearLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(82), CommonUtils.getInstance(this).getHeightPixel(47));
                }
                else
                {
                    params = new LinearLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(72), CommonUtils.getInstance(this).getHeightPixel(68));
                }
                image.setLayoutParams(params);
                image.setImageResource(TAB_IMAGE_ICONS[i]);
                _MainTabsLayout.getTabAt(i).setCustomView(image);
            }
        }

        _MainTabsLayout.getTabAt(Common.PAGE_HOME).getCustomView().setSelected(true);


        LinearLayout tabStrip = ((LinearLayout)_MainTabsLayout.getChildAt(0));
        if(Feature.IS_FREE_USER)
        {

            tabStrip.getChildAt(Common.PAGE_MY_BOOKS).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        CommonUtils.getInstance(MainActivity.this).showSnackMessage(_MainContentCoordinatorLayout, getResources().getString(R.string.message_payment_service_login), getResources().getColor(R.color.color_d8232a));
                    }

                    return true;
                }
            });
        }
    }

    public void animateRevealColorFromCoordinates(ViewGroup viewRoot, final int color, int x, int y, int duration)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            return;
        }

        viewRoot.post(new Runnable() {
            @Override
            public void run()
            {
                float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

                Animator anim = null;

                anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);

                viewRoot.setBackgroundColor(getResources().getColor(color));
                anim.setDuration(duration);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.addListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        _MainBackgroundView.setBackgroundColor(getResources().getColor(color));
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
                anim.start();
            }
        });


    }

    private void setStatusBarColor(int index)
    {
        CommonUtils.getInstance(this).setStatusBar(getResources().getColor(TAB_INDICATOR_COLOR[index]));
    }

    private void setIndicatorBarColor(int index)
    {
        _MainTabsLayout.setSelectedTabIndicatorColor(
                Feature.IS_TABLET ?
                        getResources().getColor(TAB_BACKGROUND_COLOR_TABLET[index]) :
                        getResources().getColor(TAB_BACKGROUND_COLOR[index]));
    }



    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {

        }

        @Override
        public void onPageSelected(int position)
        {
            Log.f("position : "+ position);
            Rect rect = new Rect();
            _MainTabsLayout.getTabAt(position).getCustomView().getGlobalVisibleRect(rect);
            animateRevealColorFromCoordinates(_MainBackgroundAnimationLayout, TAB_BACKGROUND_COLOR[position], rect.centerX(), rect.centerY(), Feature.IS_TABLET ? Common.DURATION_SHORT_LONG : Common.DURATION_MENU_ANIMATION_PHONE);
            setStatusBarColor(position);
            setIndicatorBarColor(position);
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    };

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView)
        {
            Log.f("");
            hideUserSelectLayoutAnimation();
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    private View.OnClickListener mMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id._menuScheduleButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuPublishSchedule();
                    break;

                case R.id._menuNewsButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuNews();
                    break;

                case R.id._menuTestimonialButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuTestimonial();
                    break;

                case R.id._menuFAQsButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuFAQ();
                    break;

                case R.id._menu1on1AskButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenu1On1Ask();
                    break;

                case R.id._menuAboutAppButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuAppUseGuide();
                    break;

                case R.id._menuRestoreButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuRestore();
                    break;

                case R.id._menuStoreButtonRect:
                    _MainDrawLayout.closeDrawer(_NavigationBaseLayout);
                    mMainPresenter.onClickMenuStore();
                    break;
            }
        }
    };

}
