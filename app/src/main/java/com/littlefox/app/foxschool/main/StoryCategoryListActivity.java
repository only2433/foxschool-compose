package com.littlefox.app.foxschool.main;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.transition.Explode;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.littlefox.library.view.animator.AnimationListener;
import com.littlefox.library.view.animator.ViewAnimator;
import com.littlefox.logmonitor.Log;
import com.ssomai.android.scalablelayout.ScalableLayout;

import net.littlefox.lf_app_fragment.R;
import net.littlefox.lf_app_fragment.adapter.SeriesCardViewAdapter;
import net.littlefox.lf_app_fragment.base.BaseActivity;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.CommonUtils;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.common.Font;
import net.littlefox.lf_app_fragment.handler.callback.MessageHandlerCallback;
import net.littlefox.lf_app_fragment.main.contract.StoryCategoryListContract;
import net.littlefox.lf_app_fragment.main.contract.presenter.StoryCategoryListPresenter;
import net.littlefox.lf_app_fragment.view.decoration.GridSpacingItemDecoration;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class StoryCategoryListActivity extends BaseActivity implements MessageHandlerCallback, StoryCategoryListContract.View
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
    @BindView(R.id._backgroundView)
    ImageView _BackgroundView;

    @Nullable
    @BindView(R.id._backgroundAnimationLayout)
    FrameLayout _BackgroundAnimationLayout;

    @BindView(R.id._detailThumbnailImage)
    ImageView _DetailThumbnailImage;

    @BindView(R.id._detailInformationText)
    TextView _DetailInformationText;

    @Nullable
    @BindView(R.id._detailToolbar)
    Toolbar _DetailToolbar;

    @BindView(R.id._categoryInformationList)
    RecyclerView _CategoryInformationListView;

    @BindView(R.id._loadingProgressLayout)
    ScalableLayout _LoadingProgressLayout;

    @Nullable
    @BindView(R.id._titleText)
    TextView _TitleText;

    private static final int COLUMN_COUNT = Feature.IS_TABLET ? 3 : 2;
    private static final int COLUMN_MARGIN  = Feature.IS_TABLET ? 40 : 24;

    private StoryCategoryListPresenter mStoryCategoryListPresenter = null;
    private boolean isCollapsed = false;
    private TextView _TopbarTitleText;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        if(Feature.IS_TABLET)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            setContentView(R.layout.activity_story_category_list_tablet);
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_story_category_list);
        }

        ButterKnife.bind(this);

        mStoryCategoryListPresenter = new StoryCategoryListPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStoryCategoryListPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStoryCategoryListPresenter.pause();
    }

    @Override
    public void onBackPressed()
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStoryCategoryListPresenter.destroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void initView()
    {
        if(Feature.IS_TABLET)
        {
            final int TOP_MARGIN = 50;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _CategoryInformationListView.getLayoutParams();
            params.topMargin = CommonUtils.getInstance(this).getPixel(TOP_MARGIN);
            _CategoryInformationListView.setLayoutParams(params);
        }
    }

    @Override
    public void initFont()
    {

    }

    @Override
    public void handlerMessage(Message message)
    {
        mStoryCategoryListPresenter.sendMessageEvent(message);
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
        menuBackIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onBackPressed();
            }
        });

        _TopbarTitleText = (TextView)customView.findViewById( R.id._topMenuTitle);
        _TopbarTitleText.setTypeface(Font.getInstance(this).getRobotoBold());
        _TopbarTitleText.setText(title);
        _TopbarTitleText.setVisibility(View.INVISIBLE);

        ImageView infomationImageView = (ImageView)customView.findViewById(R.id._topMenuInfo);
        infomationImageView.setVisibility(View.GONE);
    }

    @Override
    public void settingBackgroundView(String thumbnailUrl, String topbarColor)
    {
        final int DEFAULT_THUMBNAIL_HEIGHT = 607;
        CoordinatorLayout.LayoutParams coordinatorLayoutParams = null;
        CollapsingToolbarLayout.LayoutParams collapsingToolbarLayoutParams = null;

        _DetailCollapsingToolbarLayout.setContentScrimColor(Color.parseColor(topbarColor));
        coordinatorLayoutParams = (CoordinatorLayout.LayoutParams)_DetailAppbarLayout.getLayoutParams();
        coordinatorLayoutParams.height = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT);
        _DetailAppbarLayout.setLayoutParams(coordinatorLayoutParams);
        _DetailAppbarLayout.addOnOffsetChangedListener(mOffsetChangedListener);

        collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT));
        _DetailThumbnailImage.setLayoutParams(collapsingToolbarLayoutParams);

        if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString()))
        {
            collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(420), CommonUtils.getInstance(this).getHeightPixel(84));
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).getDisplayWidthPixel() - CommonUtils.getInstance(this).getPixel(420);
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)- CommonUtils.getInstance(this).getHeightPixel(84);
        }
        else
        {
            collapsingToolbarLayoutParams = new CollapsingToolbarLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(280), CommonUtils.getInstance(this).getHeightPixel(84));
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).getDisplayWidthPixel() - CommonUtils.getInstance(this).getPixel(280);
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)- CommonUtils.getInstance(this).getHeightPixel(84);

        }
        Log.i("leftMargin : "+collapsingToolbarLayoutParams.leftMargin+", topMargin : "+collapsingToolbarLayoutParams.topMargin );

        _DetailInformationText.setLayoutParams(collapsingToolbarLayoutParams);
        _DetailInformationText.setTypeface(Font.getInstance(this).getRobotoMedium());
        _DetailInformationText.setVisibility(View.INVISIBLE);

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
    }

    @Override
    public void settingBackgroundViewTablet(String thumbnailUrl, final String topbarColor)
    {
        _BackgroundAnimationLayout.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                //썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
                final Rect rect = new Rect();
                _DetailThumbnailImage.getGlobalVisibleRect(rect);
                animateRevealColorFromCoordinates(_BackgroundAnimationLayout, Color.parseColor(topbarColor), rect.right, 0, Common.DURATION_NORMAL);

            }
        }, Common.DURATION_SHORT);

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
    public void showCategoryCardListView(SeriesCardViewAdapter seriesCardViewAdapter)
    {
        _CategoryInformationListView.setVisibility(View.VISIBLE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COLUMN_COUNT);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });

        _CategoryInformationListView.setLayoutManager(gridLayoutManager);
        _CategoryInformationListView.addItemDecoration(new GridSpacingItemDecoration(this, COLUMN_COUNT, CommonUtils.getInstance(this).getPixel(COLUMN_MARGIN)));
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this,R.anim.listview_layoutanimation);
        _CategoryInformationListView.setLayoutAnimation(animationController);
        _CategoryInformationListView.setAdapter(seriesCardViewAdapter);

    }

    @Override
    public void showLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void showSeriesCountView(int count)
    {
        _DetailInformationText.setVisibility(View.VISIBLE);
        _DetailInformationText.setText(String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count)));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initPairTransition()
    {
        Log.f("");
        ViewCompat.setTransitionName(_DetailThumbnailImage, Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE);
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

    @Optional
    @OnClick({R.id._backButtonRect})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id._backButtonRect:
                super.onBackPressed();
                break;

        }
    }


}
