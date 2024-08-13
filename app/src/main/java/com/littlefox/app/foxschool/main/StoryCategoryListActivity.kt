package com.littlefox.app.foxschool.main

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.transition.Explode
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.SeriesCardViewAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.main.contract.StoryCategoryListContract
import com.littlefox.app.foxschool.main.presenter.StoryCategoryListPresenter
import com.littlefox.app.foxschool.view.decoration.GridSpacingItemDecoration
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class StoryCategoryListActivity : BaseActivity(), MessageHandlerCallback, StoryCategoryListContract.View
{
    @BindView(R.id._mainContent)
    lateinit var _MainContentLayout : CoordinatorLayout

    @Nullable
    @BindView(R.id._detailAppbarLayout)
    lateinit var _DetailAppbarLayout : AppBarLayout

    @Nullable
    @BindView(R.id._detailCollapsingToolbarLayout)
    lateinit var _DetailCollapsingToolbarLayout : CollapsingToolbarLayout

    @Nullable
    @BindView(R.id._backgroundView)
    lateinit var _BackgroundView : ImageView

    @Nullable
    @BindView(R.id._backgroundAnimationLayout)
    lateinit var _BackgroundAnimationLayout : FrameLayout

    @BindView(R.id._detailThumbnailImage)
    lateinit var _DetailThumbnailImage : ImageView

    @BindView(R.id._detailInformationText)
    lateinit var _DetailInformationText : TextView

    @Nullable
    @BindView(R.id._detailToolbar)
    lateinit var _DetailToolbar : Toolbar

    @BindView(R.id._categoryInformationList)
    lateinit var _CategoryInformationListView : RecyclerView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : ScalableLayout

    @Nullable
    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    private var COLUMN_COUNT : Int = 0
    private var COLUMN_MARGIN : Int = 0

    private lateinit var mStoryCategoryListPresenter : StoryCategoryListPresenter
    private lateinit var _TopbarTitleText : TextView
    private var isCollapsed : Boolean = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_story_category_list_tablet)
        } else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_story_category_list)
        }
        ButterKnife.bind(this)
        mStoryCategoryListPresenter = StoryCategoryListPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mStoryCategoryListPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mStoryCategoryListPresenter.pause()
    }

    override fun onBackPressed()
    {
        if(isCollapsed)
        {
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
            finish()
        } else
        {
            super.onBackPressed()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mStoryCategoryListPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
    }

    override fun initView()
    {
        if(CommonUtils.getInstance(this).checkTablet)
        {
            COLUMN_COUNT = 3
            COLUMN_MARGIN = 40
            val TOP_MARGIN = 50
            val params : LinearLayout.LayoutParams = _CategoryInformationListView.getLayoutParams() as LinearLayout.LayoutParams
            params.topMargin = CommonUtils.getInstance(this).getPixel(TOP_MARGIN)
            _CategoryInformationListView.setLayoutParams(params)
        }
        else
        {
            COLUMN_COUNT = 2
            COLUMN_MARGIN = 24
        }
    }

    override fun initFont() {}
    override fun showSuccessMessage(message : String) {}
    override fun showErrorMessage(message : String) {}

    override fun handlerMessage(message : Message)
    {
        mStoryCategoryListPresenter.sendMessageEvent(message)
    }

    override fun initTransition(transitionType : TransitionType)
    {

        when(transitionType)
        {
            TransitionType.PAIR_IMAGE -> initPairTransition()
            TransitionType.SLIDE_VIEW -> initSlideTransition()
        }
    }

    override fun setStatusBar(statusColor : String)
    {
        CommonUtils.getInstance(this).setStatusBar(Color.parseColor(statusColor))
    }

    override fun settingTitleView(title : String)
    {
        val customView : View
        setSupportActionBar(_DetailToolbar)
        getSupportActionBar()!!.run {
            setDisplayShowHomeEnabled(true)
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        customView = LayoutInflater.from(this).inflate(R.layout.topbar_detail_menu, null)
        getSupportActionBar()!!.setCustomView(customView)

        _DetailToolbar!!.setContentInsetsAbsolute(0, 0)
        val menuBackIcon = customView.findViewById<View>(R.id._topMenuBack) as ImageView
        menuBackIcon.setOnClickListener {onBackPressed()}

        _TopbarTitleText = (customView.findViewById<View>(R.id._topMenuTitle) as TextView).apply {
            setTypeface(Font.getInstance(context).getTypefaceBold())
            setText(title)
            setVisibility(View.INVISIBLE)
        }

        val infomationImageView = customView.findViewById<View>(R.id._topMenuInfo) as ImageView
        infomationImageView.visibility = View.GONE
    }

    override fun settingBackgroundView(thumbnailUrl : String, topbarColor : String)
    {
        val DEFAULT_THUMBNAIL_HEIGHT = 607
        var coordinatorLayoutParams : CoordinatorLayout.LayoutParams? = null
        var collapsingToolbarLayoutParams : CollapsingToolbarLayout.LayoutParams? = null
        _DetailCollapsingToolbarLayout.setContentScrimColor(Color.parseColor(topbarColor))

        coordinatorLayoutParams = _DetailAppbarLayout.getLayoutParams() as CoordinatorLayout.LayoutParams
        coordinatorLayoutParams.height = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)

        _DetailAppbarLayout.setLayoutParams(coordinatorLayoutParams)
        _DetailAppbarLayout.addOnOffsetChangedListener(mOffsetChangedListener)

        collapsingToolbarLayoutParams = CollapsingToolbarLayout.LayoutParams(
            CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
            CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT)
        )
        _DetailThumbnailImage.layoutParams = collapsingToolbarLayoutParams
        if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString()))
        {
            collapsingToolbarLayoutParams = CollapsingToolbarLayout.LayoutParams(
                CommonUtils.getInstance(this).getPixel(420),
                CommonUtils.getInstance(this).getHeightPixel(84)
            )
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).displayWidthPixel - CommonUtils.getInstance(this).getPixel(420)
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT) - CommonUtils.getInstance(this).getHeightPixel(84)
        }
        else
        {
            collapsingToolbarLayoutParams = CollapsingToolbarLayout.LayoutParams(
                CommonUtils.getInstance(this).getPixel(280),
                CommonUtils.getInstance(this).getHeightPixel(84)
            )
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).displayWidthPixel - CommonUtils.getInstance(this).getPixel(280)
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT) - CommonUtils.getInstance(this).getHeightPixel(84)
        }
        Log.i("leftMargin : " + collapsingToolbarLayoutParams.leftMargin + ", topMargin : " + collapsingToolbarLayoutParams.topMargin)
        _DetailInformationText.setLayoutParams(collapsingToolbarLayoutParams)
        _DetailInformationText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _DetailInformationText.setVisibility(View.INVISIBLE)
        val options = RequestOptions()
        options.override(Target.SIZE_ORIGINAL)
        Glide.with(this).load(thumbnailUrl).apply(options).listener(object : RequestListener<Drawable?>
            {
                override fun onLoadFailed(e : GlideException?, model : Any?, target : Target<Drawable?>?, isFirstResource : Boolean) : Boolean
                {
                    return false
                }

                override fun onResourceReady(resource : Drawable?, model : Any?, target : Target<Drawable?>?, dataSource : DataSource?, isFirstResource : Boolean) : Boolean
                {
                    return false
                }
            }).into(_DetailThumbnailImage)
    }

    override fun settingTitleViewTablet(title : String)
    {
        _TitleText.setText(title)
    }

    override fun settingBackgroundViewTablet(thumbnailUrl : String, topbarColor : String)
    {
        _BackgroundAnimationLayout.postDelayed(Runnable { //썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
            val rect = Rect()
            _DetailThumbnailImage!!.getGlobalVisibleRect(rect)
            animateRevealColorFromCoordinates(
                _BackgroundAnimationLayout,
                Color.parseColor(topbarColor),
                rect.right,
                0,
                Common.DURATION_NORMAL
            )
        }, Common.DURATION_SHORT)
        val options = RequestOptions()
        options.override(Target.SIZE_ORIGINAL)
        Glide.with(this).load(thumbnailUrl).apply(options).listener(object : RequestListener<Drawable?>
            {
                override fun onLoadFailed(e : GlideException?, model : Any?, target : Target<Drawable?>?, isFirstResource : Boolean) : Boolean
                {
                    return false
                }

                override fun onResourceReady(resource : Drawable?, model : Any?, target : Target<Drawable?>?, dataSource : DataSource?, isFirstResource : Boolean) : Boolean
                {
                    return false
                }
            }).into(_DetailThumbnailImage)
    }

    override fun showCategoryCardListView(seriesCardViewAdapter : SeriesCardViewAdapter)
    {

        val gridLayoutManager = GridLayoutManager(this, COLUMN_COUNT)
        gridLayoutManager.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup()
        {
            override fun getSpanSize(position : Int) : Int
            {
                return 1
            }
        })
        _CategoryInformationListView.let {
            it.setVisibility(View.VISIBLE)
            it.setLayoutManager(gridLayoutManager)
            it.addItemDecoration(
                GridSpacingItemDecoration(this,
                    COLUMN_COUNT,
                    CommonUtils.getInstance(this).getPixel(COLUMN_MARGIN),
                    CommonUtils.getInstance(this).checkTablet)
            )
            val animationController : LayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
            it.setLayoutAnimation(animationController)
            it.setAdapter(seriesCardViewAdapter)
        }

    }

    override fun showLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE)
    }

    override fun hideLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE)
    }

    override fun showSeriesCountView(count : Int)
    {
        _DetailInformationText.setVisibility(View.VISIBLE)
        _DetailInformationText.setText(
            java.lang.String.format(
                getString(R.string.text_count_stories),
                CommonUtils.getInstance(this).getDecimalNumber(count)
            )
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun initPairTransition()
    {
        Log.f("")
        ViewCompat.setTransitionName(_DetailThumbnailImage, Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun initSlideTransition()
    {
        Log.f("")
        val explode = Explode().apply {
            excludeTarget(android.R.id.statusBarBackground, true)
            duration = Common.DURATION_SHORT
        }
        getWindow().run {
            setEnterTransition(explode)
            setExitTransition(explode)
            setAllowEnterTransitionOverlap(true)
            setAllowReturnTransitionOverlap(true)
        }
    }

    private fun showTitleText()
    {
        ViewAnimator.animate(_TopbarTitleText)
            .alpha(0.0f, 1.0f)
            .duration(Common.DURATION_SHORTEST)
            .onStart {_TopbarTitleText.setVisibility(View.VISIBLE)}
            .start()
    }

    private fun hideTitleText()
    {
        ViewAnimator.animate(_TopbarTitleText)
            .alpha(1.0f, 0.0f)
            .duration(Common.DURATION_SHORTEST)
            .onStop {_TopbarTitleText.setVisibility(View.INVISIBLE)}
            .start()
    }

    fun animateRevealColorFromCoordinates(viewRoot : ViewGroup, color : Int, x : Int, y : Int, aniDuration : Long)
    {
        val finalRadius = Math.hypot(viewRoot.getWidth().toDouble(), viewRoot.getHeight().toDouble()).toFloat()
        viewRoot.setBackgroundColor(color)
        (ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0f, finalRadius)).apply {
            duration = aniDuration
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : Animator.AnimatorListener
            {
                override fun onAnimationStart(animation : Animator) {}

                override fun onAnimationEnd(animation : Animator)
                {
                    _BackgroundView.setBackgroundColor(color)
                }

                override fun onAnimationCancel(animation : Animator) {}

                override fun onAnimationRepeat(animation : Animator) {}
            })
            start()
        }

    }

    private val mOffsetChangedListener : AppBarLayout.OnOffsetChangedListener = object : AppBarLayout.OnOffsetChangedListener
    {
        override fun onOffsetChanged(appBarLayout : AppBarLayout, verticalOffset : Int)
        {
            if(Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0)
            {
                if(isCollapsed == false)
                {
                    Log.f("Collapsed")
                    isCollapsed = true
                    showTitleText()
                }
            }
            else if(verticalOffset == 0)
            { // Expanded
                if(isCollapsed == true)
                {
                    Log.f("Expanded")
                    isCollapsed = false //Expanded TODO: 탑 타이틀 사라지기
                    hideTitleText()
                }
            }
        }
    }

    @Optional
    @OnClick(R.id._backButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
        }
    }


}