package com.littlefox.app.foxschool.main;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.transition.Explode;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.littlefox.library.view.animator.AnimationListener;
import com.littlefox.library.view.animator.ViewAnimator;
import com.littlefox.library.view.dialog.MaterialLoadingDialog;
import com.littlefox.library.view.dialog.ProgressWheel;
import com.littlefox.logmonitor.Log;
import com.ssomai.android.scalablelayout.ScalableLayout;

import net.littlefox.lf_app_fragment.R;
import net.littlefox.lf_app_fragment.adapter.DetailListItemAdapter;
import net.littlefox.lf_app_fragment.base.BaseActivity;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.CommonUtils;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.common.Font;
import net.littlefox.lf_app_fragment.common.LittlefoxLocale;
import net.littlefox.lf_app_fragment.dialog.TempleteAlertDialog;
import net.littlefox.lf_app_fragment.handler.callback.MessageHandlerCallback;
import net.littlefox.lf_app_fragment.main.contract.SeriesContentsListContract;
import net.littlefox.lf_app_fragment.main.contract.presenter.SeriesContentsListPresenter;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class SeriesContentsListActivity extends BaseActivity implements MessageHandlerCallback, SeriesContentsListContract.View
{
    @BindView(R.id._mainContent)
    CoordinatorLayout _MainContentLayout;

    @Nullable
    @BindView(R.id._detailAppbarLayout)
    AppBarLayout _DetailAppbarLayout;

    @Nullable
    @BindView(R.id._detailCollapsingToolbarLayout)
    CollapsingToolbarLayout _DetailCollapsingToolbarLayout;

    @Nullable
    @BindView(R.id._menuInformationLayout)
    ScalableLayout _MenuInformationLayout;

    @Nullable
    @BindView(R.id._backgroundView)
    ImageView _BackgroundView;

    @Nullable
    @BindView(R.id._backgroundAnimationLayout)
    FrameLayout _BackgroundAnimationLayout;

    @Nullable
    @BindView(R.id._backButtonRect)
    ImageView _BackButtonRect;

    @Nullable
    @BindView(R.id._backButton)
    ImageView _BackButton;

    @BindView(R.id._detailThumbnailImage)
    ImageView _DetailThumbnailImage;

    @BindView(R.id._detailInformationText)
    TextView _DetailInformationText;

    @Nullable
    @BindView(R.id._detailToolbar)
    Toolbar _DetailToolbar;

    @BindView(R.id._detailInformationList)
    RecyclerView _DetailInformationList;

    @Nullable
    @BindView(R.id._arDataText)
    TextView _ArDataText;

    @Nullable
    @BindView(R.id._detailARHelpButton)
    ImageView _DetailARHelpButton;

    @BindView(R.id._loadingProgressLayout)
    ScalableLayout _LoadingProgressLayout;

    @Nullable
    @BindView(R.id._floatingMenuBarLayout)
    ScalableLayout _FloatingMenuBarLayout;

    @Nullable
    @BindView(R.id._fabToolbar)
    FABToolbarLayout _FabToolbarLayout;

    @Nullable
    @BindView(R.id._floatingMenuButton)
    FloatingActionButton _FloatingMenuButton;

    @BindView(R.id._menuSelectAllImage)
    ImageView _MenuSelectAllImage;

    @BindView(R.id._menuSelectAllText)
    TextView _MenuSelectAllText;

    @BindView(R.id._menuSelectPlayImage)
    ImageView _MenuSelectPlayImage;

    @BindView(R.id._menuSelectPlayText)
    TextView _MenuSelectPlayText;

    @BindView(R.id._menuSelectCountText)
    TextView _MenuSelectCountText;

    @BindView(R.id._menuAddBookshelfImage)
    ImageView _MenuAddBookshelfImage;

    @BindView(R.id._menuAddBookshelfText)
    TextView _MenuAddBookshelfText;

    @BindView(R.id._menuCancelImage)
    ImageView _MenuCancelImage;

    @BindView(R.id._menuCancelText)
    TextView _MenuCancelText;

    @Nullable
    @BindView(R.id._titleText)
    TextView _TitleText;

    @Nullable
    @BindView(R.id._detailInformationIntroduceText)
    TextView _DetailInformationIntroduceText;

    @Nullable
    @BindView(R.id._progressWheelView)
    ProgressWheel _ProgressWheelView;

    private static final int DEFAULT_THUMBNAIL_HEIGHT = 607;

    private TextView _TopbarTitleText;
    private ImageView _TopbarInformationView;
    private boolean isCollapsed = false;
    private boolean isListSettingComplete = false;
    private MaterialLoadingDialog mMaterialLoadingDialog = null;
    private SeriesContentsListPresenter mSeriesContentsListPresenter;
    private TempleteAlertDialog mTempleteAlertDialog = null;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {

        //TODO: 단어장 리스트, 스탑이미지, 디테일리스트 가이드 보자

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        if(Feature.IS_TABLET)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            setContentView(R.layout.activity_series_contents_list_tablet);
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_series_contents_list);
        }

        ButterKnife.bind(this);

        mSeriesContentsListPresenter = new SeriesContentsListPresenter(this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.f("");
        mSeriesContentsListPresenter.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mSeriesContentsListPresenter.pause();
    }

    @Override
    public void onBackPressed()
    {
        if(Feature.IS_TABLET == false)
        {
            if(_FabToolbarLayout.isToolbar())
            {
                _FabToolbarLayout.hide();
            }
            else
            {
                if(isCollapsed)
                {
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    finish();
                }
                else
                {
                    super.onBackPressed();
                }
            }
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mSeriesContentsListPresenter.destroy();
    }



    @SuppressLint("RestrictedApi")
    @Override
    public void initView()
    {
        Log.i("Feature.IS_FREE_USER : "+Feature.IS_FREE_USER);


        if(Feature.IS_FREE_USER || Feature.IS_REMAIN_DAY_END_USER)
        {
            if(Feature.IS_TABLET == false)
            {
                _FloatingMenuButton.setVisibility(View.GONE);
            }

            _FloatingMenuBarLayout.setVisibility(View.GONE);
        }

        if(Feature.IS_TABLET)
        {
            final int TABLET_LIST_WIDTH = Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY ? 860 : 960;
            final int LEFT_MARGIN = 60;
            final int TOP_MARGIN = 24;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _DetailInformationList.getLayoutParams();
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_LIST_WIDTH);
            params.leftMargin = CommonUtils.getInstance(this).getPixel(LEFT_MARGIN);
            params.topMargin = CommonUtils.getInstance(this).getPixel(TOP_MARGIN);
            _DetailInformationList.setLayoutParams(params);

            final int PROGRESS_MARGIN_LEFT = Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY ? 1185 : 1095 ;

            _LoadingProgressLayout.moveChildView(_ProgressWheelView, PROGRESS_MARGIN_LEFT, 0);
        }

    }

    @Override
    public void initFont()
    {
        _MenuSelectAllText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _MenuSelectPlayText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _MenuSelectCountText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _MenuAddBookshelfText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _MenuCancelText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _DetailInformationText.setTypeface(Font.getInstance(this).getRobotoMedium());
        if(Feature.IS_TABLET)
        {
            _TitleText.setTypeface(Font.getInstance(this).getRobotoMedium());
            _ArDataText.setTypeface(Font.getInstance(this).getRobotoMedium());
            _DetailInformationIntroduceText.setTypeface(Font.getInstance(this).getRobotoMedium());
        }
    }

    @Override
    public void initTransition(int transitionType)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return;
        }

        switch (transitionType)
        {
            case Common.TRANSITION_PAIR_IMAGE:
                initPairTransition();
                break;
            case Common.TRANSITION_SLIDE_VIEW:
                initSlideTransition();
                break;
        }
    }

    @Override
    public void setStatusBar(String statusColor)
    {
        CommonUtils.getInstance(this).setStatusBar(Color.parseColor(statusColor));
    }

    @Override
    public void settingTitleView(String title)
    {
        View customView;
        setSupportActionBar(_DetailToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        customView = LayoutInflater.from(this).inflate(R.layout.topbar_detail_menu, null);
        getSupportActionBar().setCustomView(customView);
        _DetailToolbar.setContentInsetsAbsolute(0,0);

        ImageView menuBackIcon = (ImageView)customView.findViewById(R.id._topMenuBack);
        menuBackIcon.setOnClickListener(mMenuItemClickListener);

        _TopbarTitleText = (TextView)customView.findViewById(R.id._topMenuTitle);
        _TopbarTitleText.setTypeface(Font.getInstance(this).getRobotoBold());
        _TopbarTitleText.setText(title);
        _TopbarTitleText.setVisibility(View.INVISIBLE);

        _TopbarInformationView = (ImageView)customView.findViewById( R.id._topMenuInfo);
        _TopbarInformationView.setOnClickListener(mMenuItemClickListener);
        _TopbarInformationView.setVisibility(View.GONE);
    }

    @Override
    public void settingBackgroundView(String thumbnailUrl, String topbarColor)
    {

        CoordinatorLayout.LayoutParams coordinatorLayoutParams = null;
        CollapsingToolbarLayout.LayoutParams collapsingToolbarLayoutParams = null;

        _DetailCollapsingToolbarLayout.setContentScrimColor(Color.parseColor(topbarColor));
        coordinatorLayoutParams = (CoordinatorLayout.LayoutParams)_DetailAppbarLayout.getLayoutParams();
        coordinatorLayoutParams.height = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT);
        _DetailAppbarLayout.setLayoutParams(coordinatorLayoutParams);
        _DetailAppbarLayout.addOnOffsetChangedListener(mOffsetChangedListener);

        collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT));
        _DetailThumbnailImage.setLayoutParams(collapsingToolbarLayoutParams);

        _DetailInformationText.setVisibility(View.INVISIBLE);
        _DetailARHelpButton.setVisibility(View.INVISIBLE);


        RequestOptions options = new RequestOptions();
        options.override(Target.SIZE_ORIGINAL);

        Glide.with(this).load(thumbnailUrl).apply(options).listener(new RequestListener<Drawable>()
        {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }

        }).into(_DetailThumbnailImage);

    }

    @Override
    public void settingTitleViewTablet(String title)
    {
        _TitleText.setText(title);
        _TopbarInformationView = (ImageView)findViewById(R.id._infoButton);
        _TopbarInformationView.setVisibility(View.INVISIBLE);
        _TopbarInformationView.setOnClickListener(mMenuItemClickListener);
    }



    @Override
    public void settingBackgroundViewTablet(String thumbnailUrl, final String topbarColor, int animationType)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animationType == Common.TRANSITION_PAIR_IMAGE)
		{
			getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener()
			{
				@Override
				public void onTransitionStart(Transition transition) { }

				@Override
				public void onTransitionEnd(Transition transition)
				{
					Log.f("");
					_BackgroundAnimationLayout.post(new Runnable()
					{
						@Override
						public void run()
						{
							// 썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
							final Rect rect = new Rect();
							_DetailThumbnailImage.getGlobalVisibleRect(rect);
							animateRevealColorFromCoordinates(_BackgroundAnimationLayout, Color.parseColor(topbarColor),
									rect.right, 0, Common.DURATION_NORMAL);

						}
					});
				}

				@Override
				public void onTransitionCancel(Transition transition)
				{
				}

				@Override
				public void onTransitionPause(Transition transition)
				{
				}

				@Override
				public void onTransitionResume(Transition transition)
				{
				}
			});
		}
		else
		{
			Log.f("");
			_BackgroundAnimationLayout.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					// 썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
					final Rect rect = new Rect();
					_DetailThumbnailImage.getGlobalVisibleRect(rect);
					animateRevealColorFromCoordinates(_BackgroundAnimationLayout, Color.parseColor(topbarColor),
							rect.right, 0, Common.DURATION_NORMAL);

				}
			}, Common.DURATION_SHORT);
		}

        _DetailARHelpButton.setVisibility(View.INVISIBLE);
        _ArDataText.setVisibility(View.INVISIBLE);

        RequestOptions options = new RequestOptions();
        options.override(Target.SIZE_ORIGINAL);

        Glide.with(this).load(thumbnailUrl).apply(options).listener(new RequestListener<Drawable>()
        {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
            {
                Log.f("");
                return false;
            }


        }).into(_DetailThumbnailImage);
    }

    @Override
    public void showFloatingToolbarLayout()
    {
        if(Feature.IS_TABLET == false)
        {
            if(_FabToolbarLayout.isToolbar() == false)
            {
                _FabToolbarLayout.show();
            }
        }
    }

    @Override
    public void hideFloatingToolbarLayout()
    {
        if(Feature.IS_TABLET == false)
        {
            Log.f("");
            if(_FabToolbarLayout.isToolbar() == true)
            {
                _MenuSelectCountText.setVisibility(View.GONE);
                _FabToolbarLayout.hide();
            }
        }
        else
        {
            _MenuSelectCountText.setVisibility(View.GONE);
        }
    }

    @Override
    public void setFloatingToolbarPlayCount(int count)
    {
        Log.f("count : "+count);
        _MenuSelectCountText.setVisibility(View.VISIBLE);
        if(count < 10)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_1);
            _FloatingMenuBarLayout.moveChildView(
                    _MenuSelectCountText,
                    Feature.IS_TABLET ? 1787 : 410,
                    Feature.IS_TABLET ? 175 : 10,
                    Feature.IS_TABLET ? 30 : 40,
                    Feature.IS_TABLET ? 30 : 40);

        }
        else if(count < 100)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_2);
            _FloatingMenuBarLayout.moveChildView(
                    _MenuSelectCountText,
                    Feature.IS_TABLET ? 1787 : 410,
                    Feature.IS_TABLET ? 175 : 10,
                    Feature.IS_TABLET ? 40 : 50,
                    Feature.IS_TABLET ? 30 : 40);
        }
        else
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_3);
            _FloatingMenuBarLayout.moveChildView(
                    _MenuSelectCountText,
                    Feature.IS_TABLET ? 1787 : 410,
                    Feature.IS_TABLET ? 175 : 10,
                    Feature.IS_TABLET ? 50 : 60,
                    Feature.IS_TABLET ? 30 : 40);
        }
        _MenuSelectCountText.setText(String.valueOf(count));
    }

    @Override
    public void showContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE);
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

    @Override
    public void showErrorMessage(String message)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainContentLayout, message);
    }

    @Override
    public void showSuccessMessage(String message)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainContentLayout, message);
    }

    @Override
    public void showSeriesDataView(String seriesType, int level, int count, boolean isSingleSeries, String arLevelData)
    {
        Log.f("level : "+ level+", count : "+ count);

        String arLevelDataText = "";
        CollapsingToolbarLayout.LayoutParams collapsingToolbarLayoutParams;
        int informationTextWidth = 0;
        if(LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.ENGLISH.toString())
                || LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.JAPANESE.toString()))
        {
            if(level == 0 || isSingleSeries)
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(420);
            }
            else
            {
                if(LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.ENGLISH.toString()))
                {
                    informationTextWidth = CommonUtils.getInstance(this).getPixel(690);
                }
                else
                {
                    informationTextWidth = CommonUtils.getInstance(this).getPixel(620);
                }

            }

            collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(informationTextWidth, CommonUtils.getInstance(this).getHeightPixel(84));
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).getDisplayWidthPixel() - informationTextWidth;
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)- CommonUtils.getInstance(this).getHeightPixel(84);
        }
        else
        {
            if(level == 0 || isSingleSeries)
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(310);
            }
            else
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(550);
            }
            collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(informationTextWidth, CommonUtils.getInstance(this).getHeightPixel(84));
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).getDisplayWidthPixel() - informationTextWidth;
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)- CommonUtils.getInstance(this).getHeightPixel(84);
        }

        _DetailInformationText.setLayoutParams(collapsingToolbarLayoutParams);
        _DetailInformationText.setGravity(Gravity.CENTER);

        CollapsingToolbarLayout.LayoutParams arButtonParams = new CollapsingToolbarLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(50), CommonUtils.getInstance(this).getHeightPixel(60));
        arButtonParams.leftMargin = CommonUtils.getInstance(this).getDisplayWidthPixel() - CommonUtils.getInstance(this).getPixel(70);
        arButtonParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)- CommonUtils.getInstance(this).getHeightPixel(70);
        _DetailARHelpButton.setLayoutParams(arButtonParams);

        _DetailInformationText.setVisibility(View.VISIBLE);


        if(level == 0)
        {
            if(seriesType.equals(Common.CONTENT_TYPE_SONG))
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_songs), CommonUtils.getInstance(this).getDecimalNumber(count)));

            }
            else
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count)));
            }
        }
        else
        {
            if(isSingleSeries)
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_level), level)+" | "+
                        String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count)));
            }
            else
            {
                if(LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.ENGLISH.toString())
                || LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.JAPANESE.toString()))
                {
                    arLevelDataText = "ATOS "+arLevelData;
                }
                else
                {
                    arLevelDataText = "AR "+arLevelData;
                }

                _DetailInformationText.setText(String.format(getString(R.string.text_count_level), level)+" | "+
                        String.format(getString(R.string.text_count_series_stories), CommonUtils.getInstance(this).getDecimalNumber(count))+" | "+ arLevelDataText);

                _DetailInformationText.setGravity(Gravity.CENTER_VERTICAL);
                _DetailInformationText.setPadding(CommonUtils.getInstance(this).getPixel(30),0,0,0);
                _DetailARHelpButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void showSeriesDataViewTablet(String seriesType, int level, int count, String categoryData, boolean isSingleSeries, String arLevelData)
    {
        Log.f("");
        String categoryInformation = "";
        if(categoryData.equals("") == false)
        {
            categoryInformation = " | " + categoryData.replace("|", " | ");
        }

        String arLevelDataText = "";



        if(level == 0)
        {
            if(seriesType.equals(Common.CONTENT_TYPE_SONG))
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_songs), CommonUtils.getInstance(this).getDecimalNumber(count)));
            }
            else
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count)));
            }
        }
        else
        {
            if(isSingleSeries)
            {
                _DetailInformationText.setText(
                        String.format(getString(R.string.text_count_level), level)+" | "+
                                String.format(getString(R.string.text_count_stories, CommonUtils.getInstance(this).getDecimalNumber(count))+
                                        categoryInformation));
            }
            else
            {
                _DetailInformationText.setText(
                        String.format(getString(R.string.text_count_level), level)+" | "+
                                String.format(getString(R.string.text_count_series_stories, CommonUtils.getInstance(this).getDecimalNumber(count))+
                                        categoryInformation));

                _ArDataText.setVisibility(View.VISIBLE);
                _DetailARHelpButton.setVisibility(View.VISIBLE);

                _MenuInformationLayout.setScaleSize(640,1200);
                if(LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.ENGLISH.toString())
                        || LittlefoxLocale.getInstance().getDeviceLocale().contains(Locale.JAPANESE.toString()))
                {
                    arLevelDataText = "ATOS "+arLevelData;
                    _MenuInformationLayout.moveChildView(_ArDataText, 20, 1050, 120, 70);
                    _MenuInformationLayout.moveChildView(_DetailARHelpButton, 150, 1063, 44, 44);


                }
                else
                {
                    arLevelDataText = "AR "+arLevelData;
                    _MenuInformationLayout.moveChildView(_ArDataText, 20, 1050, 80, 70);
                    _MenuInformationLayout.moveChildView(_DetailARHelpButton, 110, 1063, 44, 44);
                }

                _ArDataText.setText(arLevelDataText);
            }
        }
    }

    @Override
    public void showSeriesInformationIntroduceTablet(String text)
    {
        _DetailInformationIntroduceText.setText(text);
    }

    @Override
    public void showSeriesInformationView()
    {
        _TopbarInformationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showStoryDetailListView(DetailListItemAdapter storyDetailItemAdapter)
    {
        isListSettingComplete = true;
        _DetailInformationList.setVisibility(View.VISIBLE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        _DetailInformationList.setLayoutManager(linearLayoutManager);

        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this,R.anim.listview_layoutanimation);
        _DetailInformationList.setLayoutAnimation(animationController);
        _DetailInformationList.setAdapter(storyDetailItemAdapter);
    }

    @Override
    public void showLastWatchSeriesInformation(String seriesName, String nickName, int position, final boolean isLastMovie)
    {
        Log.f("nickName : "+nickName+", position : "+position +" Locale : "+ Locale.getDefault().toString()+", isLastMovie : "+isLastMovie);
        Snackbar snackbar = null;
        String message = "";
        Spanned htmlText = null;
        final int currentMovieIndex = position;
        SpannableStringBuilder snackbarText = new SpannableStringBuilder();
        int firstSpannablePosition = 0;
        int secondSpannablePosition = 0;

        if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
        {
            snackbarText.append(nickName+"님은 현재 ");
            firstSpannablePosition = snackbarText.length();
            snackbarText.append(currentMovieIndex+"편");
            secondSpannablePosition = snackbarText.length();
            snackbarText.append(" 까지 학습했어요.");
            snackbarText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        else if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString()))
        {
            snackbarText.append("Last viewed story in this series: "+ seriesName+" ");
            firstSpannablePosition = snackbarText.length();
            snackbarText.append(String.valueOf(currentMovieIndex));
            secondSpannablePosition = snackbarText.length();
            snackbarText.append(".");
            snackbarText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if(Locale.getDefault().toString().contains(Locale.JAPAN.toString()))
        {
            snackbarText.append(nickName+"さんは");
            firstSpannablePosition = snackbarText.length();
            snackbarText.append(currentMovieIndex+"話");
            secondSpannablePosition = snackbarText.length();
            snackbarText.append("までみました。");
            snackbarText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        else if(Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString()))
        {
            snackbarText.append("你已学习了 ");
            firstSpannablePosition = snackbarText.length();
            snackbarText.append(currentMovieIndex+"篇");
            secondSpannablePosition = snackbarText.length();
            snackbarText.append("。");
            snackbarText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        else if(Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
        {
            snackbarText.append("你已學習了 ");
            firstSpannablePosition = snackbarText.length();
            snackbarText.append(currentMovieIndex+"篇");
            secondSpannablePosition = snackbarText.length();
            snackbarText.append("。");
            snackbarText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), firstSpannablePosition, secondSpannablePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }



        if(isLastMovie)
        {
            snackbar = Snackbar.make(_MainContentLayout, message, Snackbar.LENGTH_LONG);
        }
        else
        {
            snackbar = Snackbar.make(_MainContentLayout, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.text_auto_play, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Log.f("");
                            if (isLastMovie == false)
                            {
                                mSeriesContentsListPresenter.onClickNextMovieAfterLastMovie(currentMovieIndex);
                            }
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.color_fd1c51));
        }

        View view = snackbar.getView();
        view.setAlpha(0.9f);
        view.setBackgroundColor(getResources().getColor(R.color.color_000000));

        TextView textView = (TextView)view.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f,  getResources().getDisplayMetrics()), 1.0f);
        textView.setTextColor(getResources().getColor(R.color.color_ffffff));
        textView.setText(snackbarText);
        textView.setTypeface(Font.getInstance(this).getRobotoRegular());
        textView.setMaxLines(3);


        TextView actionText = (TextView)view.findViewById(com.google.android.material.R.id.snackbar_action);
        actionText.setTypeface(Font.getInstance(this).getRobotoMedium());

        snackbar.show();

    }

    public void animateRevealColorFromCoordinates(ViewGroup viewRoot, final int color, int x, int y, int duration)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            _BackgroundView.setBackgroundColor(color);
            return;
        }

        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = null;

        anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(color);

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
                _BackgroundView.setBackgroundColor(color);
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

    private void showARInfomrationDialog()
    {
        mTempleteAlertDialog = new TempleteAlertDialog(this);
        mTempleteAlertDialog.setMessage(getResources().getString(R.string.message_ar_information));
        mTempleteAlertDialog.setButtonType(TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1);
        mTempleteAlertDialog.setGravity(Gravity.LEFT);
        mTempleteAlertDialog.show();
    }

    @Optional
    @OnClick({R.id._backButtonRect, R.id._floatingMenuButton, R.id._menuSelectAllImage, R.id._menuSelectAllText, R.id._menuSelectPlayImage,
    R.id._menuSelectPlayText, R.id._menuAddBookshelfImage , R.id._menuAddBookshelfText, R.id._menuCancelImage, R.id._menuCancelText, R.id._detailARHelpButton})
    public void onClickView(View view)
    {
        switch (view.getId())
        {
            case R.id._backButtonRect:
                super.onBackPressed();
                break;
            case R.id._floatingMenuButton:
                if(isListSettingComplete)
                {
                    _FabToolbarLayout.show();
                }
                break;
            case R.id._menuSelectAllImage:
            case R.id._menuSelectAllText:
                if(isListSettingComplete)
                {
                    mSeriesContentsListPresenter.onClickSelectAll();
                }

                break;
            case R.id._menuSelectPlayImage:
            case R.id._menuSelectPlayText:
                if(isListSettingComplete)
                {
                    mSeriesContentsListPresenter.onClickSelectPlay();
                }

                break;
            case R.id._menuAddBookshelfImage:
            case R.id._menuAddBookshelfText:
                if(isListSettingComplete)
                {
                    mSeriesContentsListPresenter.onClickAddBookshelf();
                }

                break;
            case R.id._menuCancelImage:
            case R.id._menuCancelText:
                if(isListSettingComplete)
                {
                    _MenuSelectCountText.setVisibility(View.GONE);
                    mSeriesContentsListPresenter.onClickCancel();
                    if(Feature.IS_TABLET == false)
                    {
                        _FabToolbarLayout.hide();
                    }
                }
                break;
            case R.id._detailARHelpButton:
                showARInfomrationDialog();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initPairTransition()
    {
        Log.f("");
        ViewCompat.setTransitionName(_DetailThumbnailImage, Common.STORY_DETAIL_LIST_HEADER_IMAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSlideTransition()
    {
        Log.f("");
        Explode explode = new Explode();

        explode.excludeTarget(android.R.id.statusBarBackground, true);
        explode.setDuration(Common.DURATION_SHORT);

        getWindow().setEnterTransition(explode);
        getWindow().setExitTransition(explode);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);
    }

    private void showTitleText()
    {
        ViewAnimator
                .animate(_TopbarTitleText)
                .alpha(0.0f, 1.0f)
                .duration(Common.DURATION_SHORTEST)
                .onStart(new AnimationListener.Start() {
                    @Override
                    public void onStart() {
                        _TopbarTitleText.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void hideTitleText()
    {
        ViewAnimator
                .animate(_TopbarTitleText)
                .alpha(1.0f, 0.0f)
                .duration(Common.DURATION_SHORTEST)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        _TopbarTitleText.setVisibility(View.INVISIBLE);
                    }
                })
                .start();
    }


    private AppBarLayout.OnOffsetChangedListener mOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener()
    {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
        {
            if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0)
            {
                if(isCollapsed == false)
                {
                    Log.f("Collapsed");
                    isCollapsed = true;
                    showTitleText();
                }
            }
            else if (verticalOffset == 0)
            {
                // Expanded
                if(isCollapsed == true)
                {
                    Log.f("Expanded");
                    isCollapsed = false;
                    //Expanded TODO: 탑 타이틀 사라지기
                    hideTitleText();
                }
            }
        }
    };

    private View.OnClickListener mMenuItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id._topMenuBack:
                    SeriesContentsListActivity.super.onBackPressed();
                    break;
                case R.id._infoButton:
                case R.id._topMenuInfo:
                    mSeriesContentsListPresenter.onClickSeriesInformation();
                    break;
            }
        }
    };


    @Override
    public void handlerMessage(Message message)
    {
        mSeriesContentsListPresenter.sendMessageEvent(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mSeriesContentsListPresenter.acvitityResult(requestCode, resultCode, data);
    }
}
