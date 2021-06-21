package com.littlefox.app.foxschool.main

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.transition.Explode
import android.transition.Transition
import android.util.TypedValue
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
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter

import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.main.contract.SeriesContentsListContract
import com.littlefox.app.foxschool.main.presenter.SeriesContentsListPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.dialog.ProgressWheel
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

import java.util.*

class SeriesContentsListActivity : BaseActivity(), MessageHandlerCallback, SeriesContentsListContract.View
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
    @BindView(R.id._menuInformationLayout)
    lateinit var _MenuInformationLayout : ScalableLayout

    @Nullable
    @BindView(R.id._backgroundView)
    lateinit var _BackgroundView : ImageView

    @Nullable
    @BindView(R.id._backgroundAnimationLayout)
    lateinit var _BackgroundAnimationLayout : FrameLayout

    @Nullable
    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @Nullable
    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._detailThumbnailImage)
    lateinit var _DetailThumbnailImage : ImageView

    @BindView(R.id._detailInformationText)
    lateinit var _DetailInformationText : TextView

    @Nullable
    @BindView(R.id._detailToolbar)
    lateinit var _DetailToolbar : Toolbar

    @BindView(R.id._detailInformationList)
    lateinit var _DetailInformationList : RecyclerView

    @Nullable
    @BindView(R.id._arDataText)
    lateinit var _ArDataText : TextView

    @Nullable
    @BindView(R.id._detailARHelpButton)
    lateinit var _DetailARHelpButton : ImageView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : ScalableLayout

    @Nullable
    @BindView(R.id._floatingMenuBarLayout)
    lateinit var _FloatingMenuBarLayout : ScalableLayout

    @Nullable
    @BindView(R.id._fabToolbar)
    lateinit var _FabToolbarLayout : FABToolbarLayout

    @Nullable
    @BindView(R.id._floatingMenuButton)
    lateinit var _FloatingMenuButton : FloatingActionButton

    @BindView(R.id._menuSelectAllImage)
    lateinit var _MenuSelectAllImage : ImageView

    @BindView(R.id._menuSelectAllText)
    lateinit var _MenuSelectAllText : TextView

    @BindView(R.id._menuSelectPlayImage)
    lateinit var _MenuSelectPlayImage : ImageView

    @BindView(R.id._menuSelectPlayText)
    lateinit var _MenuSelectPlayText : TextView

    @BindView(R.id._menuSelectCountText)
    lateinit var _MenuSelectCountText : TextView

    @BindView(R.id._menuAddBookshelfImage)
    lateinit var _MenuAddBookshelfImage : ImageView

    @BindView(R.id._menuAddBookshelfText)
    lateinit var _MenuAddBookshelfText : TextView

    @BindView(R.id._menuCancelImage)
    lateinit var _MenuCancelImage : ImageView

    @BindView(R.id._menuCancelText)
    lateinit var _MenuCancelText : TextView

    @Nullable
    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @Nullable
    @BindView(R.id._detailInformationIntroduceText)
    lateinit var _DetailInformationIntroduceText : TextView

    @Nullable
    @BindView(R.id._progressWheelView)
    lateinit var _ProgressWheelView : ProgressWheel

    companion object
    {
        private const val DEFAULT_THUMBNAIL_HEIGHT = 607
    }

    private lateinit var _TopbarTitleText : TextView
    private lateinit var _TopbarInformationView : ImageView
    private lateinit var mSeriesContentsListPresenter : SeriesContentsListPresenter
    private lateinit var mTempleteAlertDialog : TempleteAlertDialog

    private var isCollapsed = false
    private var isListSettingComplete = false
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {

        //TODO: 단어장 리스트, 스탑이미지, 디테일리스트 가이드 보자
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(Feature.IS_TABLET)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_series_contents_list_tablet)
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_series_contents_list)
        }
        ButterKnife.bind(this)
        mSeriesContentsListPresenter = SeriesContentsListPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        Log.f("")
        mSeriesContentsListPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mSeriesContentsListPresenter.pause()
    }

    override fun onBackPressed()
    {
        if(Feature.IS_TABLET === false)
        {
            if(_FabToolbarLayout.isToolbar())
            {
                _FabToolbarLayout.hide()
            } else
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
        } else
        {
            super.onBackPressed()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mSeriesContentsListPresenter.destroy()
    }

    override fun handlerMessage(message : Message)
    {
        mSeriesContentsListPresenter.sendMessageEvent(message)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mSeriesContentsListPresenter.acvitityResult(requestCode, resultCode, data)
    }


    @SuppressLint("RestrictedApi")
    override fun initView()
    {
        Log.i("Feature.IS_FREE_USER : " + Feature.IS_FREE_USER)
        if(Feature.IS_FREE_USER || Feature.IS_REMAIN_DAY_END_USER)
        {
            if(Feature.IS_TABLET === false)
            {
                _FloatingMenuButton.setVisibility(View.GONE)
            }
            _FloatingMenuBarLayout.setVisibility(View.GONE)
        }
        if(Feature.IS_TABLET)
        {
            val TABLET_LIST_WIDTH : Int = if(Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY) 860 else 960
            val LEFT_MARGIN : Int = 60
            val TOP_MARGIN : Int = 24
            val params : LinearLayout.LayoutParams = _DetailInformationList.getLayoutParams() as LinearLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_LIST_WIDTH)
            params.leftMargin = CommonUtils.getInstance(this).getPixel(LEFT_MARGIN)
            params.topMargin = CommonUtils.getInstance(this).getPixel(TOP_MARGIN)
            _DetailInformationList.setLayoutParams(params)

            val PROGRESS_MARGIN_LEFT = if(Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY) 1185 else 1095
            _LoadingProgressLayout.moveChildView(_ProgressWheelView, PROGRESS_MARGIN_LEFT.toFloat(), 0f)
        }
    }

    override fun initFont()
    {
        _MenuSelectAllText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _MenuSelectPlayText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _MenuSelectCountText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _MenuAddBookshelfText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _MenuCancelText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _DetailInformationText.setTypeface(Font.getInstance(this).getRobotoMedium())
        if(Feature.IS_TABLET)
        {
            _TitleText.setTypeface(Font.getInstance(this).getRobotoMedium())
            _ArDataText.setTypeface(Font.getInstance(this).getRobotoMedium())
            _DetailInformationIntroduceText.setTypeface(Font.getInstance(this).getRobotoMedium())
        }
    }

    override fun initTransition(transitionType : TransitionType)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return
        }
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
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayShowCustomEnabled(true)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        customView = LayoutInflater.from(this).inflate(R.layout.topbar_detail_menu, null)
        getSupportActionBar()!!.setCustomView(customView)
        _DetailToolbar.setContentInsetsAbsolute(0, 0)

        val menuBackIcon = customView.findViewById<View>(R.id._topMenuBack) as ImageView
        menuBackIcon.setOnClickListener(mMenuItemClickListener)

        _TopbarTitleText = customView.findViewById<View>(R.id._topMenuTitle) as TextView
        _TopbarTitleText.setTypeface(Font.getInstance(this).getRobotoBold())
        _TopbarTitleText.setText(title)
        _TopbarTitleText.setVisibility(View.INVISIBLE)

        _TopbarInformationView = customView.findViewById<View>(R.id._topMenuInfo) as ImageView
        _TopbarInformationView.setOnClickListener(mMenuItemClickListener)
        _TopbarInformationView.visibility = View.GONE
    }

    override fun settingBackgroundView(thumbnailUrl : String, topbarColor : String)
    {
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
        _DetailInformationText.setVisibility(View.INVISIBLE)
        _DetailARHelpButton.visibility = View.INVISIBLE
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
        _TopbarInformationView = findViewById(R.id._infoButton) as ImageView
        _TopbarInformationView.visibility = View.INVISIBLE
        _TopbarInformationView.setOnClickListener(mMenuItemClickListener)
    }

    override fun settingBackgroundViewTablet(thumbnailUrl : String, topbarColor : String, animationType : TransitionType)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animationType == TransitionType.PAIR_IMAGE)
        {
            getWindow().getSharedElementEnterTransition().addListener(object : Transition.TransitionListener
                {
                    override fun onTransitionStart(transition : Transition) {}

                    override fun onTransitionEnd(transition : Transition)
                    {
                        Log.f("")
                        _BackgroundAnimationLayout.post(Runnable { // 썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
                            val rect = Rect()
                            _DetailThumbnailImage.getGlobalVisibleRect(rect)
                            animateRevealColorFromCoordinates(
                                _BackgroundAnimationLayout,
                                Color.parseColor(topbarColor),
                                rect.right,
                                0,
                                Common.DURATION_NORMAL
                            )
                        })
                    }

                    override fun onTransitionCancel(transition : Transition)
                    {
                    }

                    override fun onTransitionPause(transition : Transition)
                    {
                    }

                    override fun onTransitionResume(transition : Transition)
                    {
                    }
                })
        } else
        {
            Log.f("")
            _BackgroundAnimationLayout.postDelayed(Runnable { // 썸네일 이동 애니메이션이 끝난 후 배경색 애니메이션을 동작하게 해야해서 일정시간 딜레이를 줌.
                val rect = Rect()
                _DetailThumbnailImage!!.getGlobalVisibleRect(rect)
                animateRevealColorFromCoordinates(_BackgroundAnimationLayout, Color.parseColor(topbarColor), rect.right, 0, Common.DURATION_NORMAL)
            }, Common.DURATION_SHORT)
        }
        _DetailARHelpButton!!.visibility = View.INVISIBLE
        _ArDataText.setVisibility(View.INVISIBLE)
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

    override fun showFloatingToolbarLayout()
    {
        if(Feature.IS_TABLET === false)
        {
            if(_FabToolbarLayout.isToolbar() == false)
            {
                _FabToolbarLayout.show()
            }
        }
    }

    override fun hideFloatingToolbarLayout()
    {
        if(Feature.IS_TABLET === false)
        {
            Log.f("")
            if(_FabToolbarLayout.isToolbar() == true)
            {
                _MenuSelectCountText.setVisibility(View.GONE)
                _FabToolbarLayout.hide()
            }
        } else
        {
            _MenuSelectCountText.setVisibility(View.GONE)
        }
    }

    override fun setFloatingToolbarPlayCount(count : Int)
    {
        Log.f("count : $count")
        _MenuSelectCountText.setVisibility(View.VISIBLE)
        if(count < 10)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_1)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(Feature.IS_TABLET) 1787f else 410f,
                if(Feature.IS_TABLET) 175f else 10f,
                if(Feature.IS_TABLET) 30f else 40f,
                if(Feature.IS_TABLET) 30f else 40f
            )
        } else if(count < 100)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_2)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(Feature.IS_TABLET) 1787f else 410f,
                if(Feature.IS_TABLET) 175f else 10f,
                if(Feature.IS_TABLET) 40f else 50f,
                if(Feature.IS_TABLET) 30f else 40f
            )
        } else
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_3)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(Feature.IS_TABLET) 1787f else 410f,
                if(Feature.IS_TABLET) 175f else 10f,
                if(Feature.IS_TABLET) 50f else 60f,
                if(Feature.IS_TABLET) 30f else 40f
            )
        }
        _MenuSelectCountText.setText(count.toString())
    }

    override fun showContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE)
    }

    override fun hideContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE)
    }

    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog!!.show()
    }

    override fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainContentLayout, message)
    }

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainContentLayout, message)
    }

    override fun showSeriesDataView(seriesType : String, level : Int, count : Int, isSingleSeries : Boolean, arLevelData : String)
    {
        Log.f("level : $level, count : $count")
        var arLevelDataText = ""
        val collapsingToolbarLayoutParams : CollapsingToolbarLayout.LayoutParams
        var informationTextWidth = 0
        if(LittlefoxLocale.getDeviceLocale().contains(Locale.ENGLISH.toString())
            || LittlefoxLocale.getDeviceLocale().contains(Locale.JAPANESE.toString())
        )
        {
            if(level == 0 || isSingleSeries)
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(420)
            } else
            {
                if(LittlefoxLocale.getDeviceLocale().contains(Locale.ENGLISH.toString()))
                {
                    informationTextWidth = CommonUtils.getInstance(this).getPixel(690)
                } else
                {
                    informationTextWidth = CommonUtils.getInstance(this).getPixel(620)
                }
            }
            collapsingToolbarLayoutParams = CollapsingToolbarLayout.LayoutParams(
                informationTextWidth,
                CommonUtils.getInstance(this).getHeightPixel(84)
            )
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).displayWidthPixel - informationTextWidth
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT) - CommonUtils.getInstance(this).getHeightPixel(84)
        }
        else
        {
            if(level == 0 || isSingleSeries)
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(310)
            } else
            {
                informationTextWidth = CommonUtils.getInstance(this).getPixel(550)
            }
            collapsingToolbarLayoutParams = CollapsingToolbarLayout.LayoutParams(
                informationTextWidth,
                CommonUtils.getInstance(this).getHeightPixel(84)
            )
            collapsingToolbarLayoutParams.leftMargin = CommonUtils.getInstance(this).displayWidthPixel - informationTextWidth
            collapsingToolbarLayoutParams.topMargin = CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT) - CommonUtils.getInstance(this).getHeightPixel(84)
        }
        _DetailInformationText.setLayoutParams(collapsingToolbarLayoutParams)
        _DetailInformationText.setGravity(Gravity.CENTER)

        val arButtonParams : CollapsingToolbarLayout.LayoutParams =
            CollapsingToolbarLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(50), CommonUtils.getInstance(this).getHeightPixel(60))
        arButtonParams.leftMargin =
            CommonUtils.getInstance(this).displayWidthPixel - CommonUtils.getInstance(this).getPixel(70)
        arButtonParams.topMargin =
            CommonUtils.getInstance(this).getHeightPixel(DEFAULT_THUMBNAIL_HEIGHT) - CommonUtils.getInstance(this).getHeightPixel(70)

        _DetailARHelpButton.layoutParams = arButtonParams
        _DetailInformationText.setVisibility(View.VISIBLE)
        if(level == 0)
        {
            if(seriesType == Common.CONTENT_TYPE_SONG)
            {
                _DetailInformationText.setText(
                    String.format(getString(R.string.text_count_songs), CommonUtils.getInstance(this).getDecimalNumber(count)))
            }
            else
            {
                _DetailInformationText.setText(
                    String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count)))
            }
        }
        else
        {
            if(isSingleSeries)
            {
                _DetailInformationText.setText(
                    String.format(getString(R.string.text_count_level), level)
                            + " | " + java.lang.String.format(getString(R.string.text_count_stories), CommonUtils.getInstance(this).getDecimalNumber(count))
                )
            }
            else
            {

                if(LittlefoxLocale.getDeviceLocale().contains(Locale.ENGLISH.toString())
                    || LittlefoxLocale.getDeviceLocale().contains(Locale.JAPANESE.toString()))
                {
                    arLevelDataText = "ATOS $arLevelData"
                } else
                {
                    arLevelDataText = "AR $arLevelData"
                }
                _DetailInformationText.setText(
                    String.format(getString(R.string.text_count_level), level)
                            + " | " + String.format(getString(R.string.text_count_series_stories), CommonUtils.getInstance(this).getDecimalNumber(count)) + " | " + arLevelDataText)
                _DetailInformationText.setGravity(Gravity.CENTER_VERTICAL)
                _DetailInformationText.setPadding(CommonUtils.getInstance(this).getPixel(30), 0, 0, 0)
                _DetailARHelpButton!!.visibility = View.VISIBLE
            }
        }
    }

    override fun showSeriesDataViewTablet(seriesType : String, level : Int, count : Int, categoryData : String,isSingleSeries : Boolean, arLevelData : String)
    {
        Log.f("")
        var categoryInformation = ""
        if(categoryData == "" == false)
        {
            categoryInformation = " | " + categoryData.replace("|", " | ")
        }
        var arLevelDataText = ""
        if(level == 0)
        {
            if(seriesType == Common.CONTENT_TYPE_SONG)
            {
                _DetailInformationText.setText(
                    java.lang.String.format(
                        getString(R.string.text_count_songs),
                        CommonUtils.getInstance(this).getDecimalNumber(count)
                    )
                )
            } else
            {
                _DetailInformationText.setText(
                    java.lang.String.format(
                        getString(R.string.text_count_stories),
                        CommonUtils.getInstance(this).getDecimalNumber(count)
                    )
                )
            }
        } else
        {
            if(isSingleSeries)
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_level), level)
                        + " | " + String.format(
                        getString(R.string.text_count_stories, CommonUtils.getInstance(this).getDecimalNumber(count)).toString() + categoryInformation))
            }
            else
            {
                _DetailInformationText.setText(String.format(getString(R.string.text_count_level), level)
                        + " | " + String.format(getString(R.string.text_count_series_stories, CommonUtils.getInstance(this).getDecimalNumber(count)).toString() + categoryInformation))
                _ArDataText.setVisibility(View.VISIBLE)
                _DetailARHelpButton.visibility = View.VISIBLE
                _MenuInformationLayout.setScaleSize(640f, 1200f)
                if(LittlefoxLocale.getDeviceLocale().contains(Locale.ENGLISH.toString())
                    || LittlefoxLocale.getDeviceLocale().contains(Locale.JAPANESE.toString()))
                {
                    arLevelDataText = "ATOS $arLevelData"
                    _MenuInformationLayout.moveChildView(_ArDataText, 20f, 1050f, 120f, 70f)
                    _MenuInformationLayout.moveChildView(_DetailARHelpButton, 150f, 1063f, 44f, 44f)
                } else
                {
                    arLevelDataText = "AR $arLevelData"
                    _MenuInformationLayout.moveChildView(_ArDataText, 20f, 1050f, 80f, 70f)
                    _MenuInformationLayout.moveChildView(_DetailARHelpButton, 110f, 1063f, 44f, 44f)
                }
                _ArDataText.setText(arLevelDataText)
            }
        }
    }

    override fun showSeriesInformationIntroduceTablet(text : String)
    {
        _DetailInformationIntroduceText.setText(text)
    }

    override fun showSeriesInformationView()
    {
        _TopbarInformationView!!.visibility = View.VISIBLE
    }

    override fun showStoryDetailListView(storyDetailItemAdapter : DetailListItemAdapter)
    {
        isListSettingComplete = true
        _DetailInformationList.setVisibility(View.VISIBLE)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        _DetailInformationList.setLayoutManager(linearLayoutManager)
        val animationController : LayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _DetailInformationList.setLayoutAnimation(animationController)
        _DetailInformationList.setAdapter(storyDetailItemAdapter)
    }

    override fun showLastWatchSeriesInformation(seriesName : String, nickName : String, position : Int, isLastMovie : Boolean)
    {
        Log.f("nickName : " + nickName + ", position : " + position + " Locale : " + Locale.getDefault().toString() + ", isLastMovie : " + isLastMovie)
        var snackbar : Snackbar? = null
        val message = ""
        val htmlText : Spanned? = null
        val snackbarText = SpannableStringBuilder()
        var firstSpannablePosition = 0
        var secondSpannablePosition = 0
        if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
        {
            snackbarText.append(nickName + "님은 현재 ")
            firstSpannablePosition = snackbarText.length
            snackbarText.append(position.toString() + "편")
            secondSpannablePosition = snackbarText.length
            snackbarText.append(" 까지 학습했어요.")
            snackbarText.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            snackbarText.setSpan(
                StyleSpan(Typeface.BOLD),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        else if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString()))
        {
            snackbarText.append("Last viewed story in this series: $seriesName ")
            firstSpannablePosition = snackbarText.length
            snackbarText.append(position.toString())
            secondSpannablePosition = snackbarText.length
            snackbarText.append(".")
            snackbarText.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            snackbarText.setSpan(
                StyleSpan(Typeface.BOLD),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        else if(Locale.getDefault().toString().contains(Locale.JAPAN.toString()))
        {
            snackbarText.append(nickName + "さんは")
            firstSpannablePosition = snackbarText.length
            snackbarText.append(position.toString() + "話")
            secondSpannablePosition = snackbarText.length
            snackbarText.append("までみました。")
            snackbarText.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            snackbarText.setSpan(
                StyleSpan(Typeface.BOLD),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        else if(Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString()))
        {
            snackbarText.append("你已学习了 ")
            firstSpannablePosition = snackbarText.length
            snackbarText.append(position.toString() + "篇")
            secondSpannablePosition = snackbarText.length
            snackbarText.append("。")
            snackbarText.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            snackbarText.setSpan(
                StyleSpan(Typeface.BOLD),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        else if(Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
        {
            snackbarText.append("你已學習了 ")
            firstSpannablePosition = snackbarText.length
            snackbarText.append(position.toString() + "篇")
            secondSpannablePosition = snackbarText.length
            snackbarText.append("。")
            snackbarText.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.color_ed433e)),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            snackbarText.setSpan(
                StyleSpan(Typeface.BOLD),
                firstSpannablePosition,
                secondSpannablePosition,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        snackbar = if(isLastMovie)
        {
            Snackbar.make(_MainContentLayout, message, Snackbar.LENGTH_LONG)
        } else
        {
            Snackbar.make(_MainContentLayout, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.text_auto_play, View.OnClickListener {
                    Log.f("")
                    if(isLastMovie == false)
                    {
                        mSeriesContentsListPresenter.onClickNextMovieAfterLastMovie(position)
                    }
                }).setActionTextColor(getResources().getColor(R.color.color_fd1c51))
        }
        val view : View = snackbar.getView()
        view.alpha = 0.9f
        view.setBackgroundColor(getResources().getColor(R.color.color_000000))

        val textView : TextView = view.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f, getResources().getDisplayMetrics()), 1.0f)
        textView.setTextColor(getResources().getColor(R.color.color_ffffff))
        textView.setText(snackbarText)
        textView.setTypeface(Font.getInstance(this).getRobotoRegular())
        textView.setMaxLines(3)
        val actionText : TextView = view.findViewById<View>(R.id.snackbar_action) as TextView
        actionText.setTypeface(Font.getInstance(this).getRobotoMedium())
        snackbar.show()
    }

    fun animateRevealColorFromCoordinates(viewRoot : ViewGroup, color : Int, x : Int, y : Int, duration : Long)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            _BackgroundView.setBackgroundColor(color)
            return
        }
        val finalRadius = Math.hypot(viewRoot.getWidth().toDouble(), viewRoot.getHeight().toDouble()).toFloat()
        var anim : Animator? = null
        anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0f, finalRadius)
        viewRoot.setBackgroundColor(color)
        anim.duration = duration
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.addListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(animation : Animator) {}

            override fun onAnimationEnd(animation : Animator)
            {
                _BackgroundView.setBackgroundColor(color)
            }

            override fun onAnimationCancel(animation : Animator) {}

            override fun onAnimationRepeat(animation : Animator) {}
        })
        anim.start()
    }

    private fun showARInfomrationDialog()
    {
        mTempleteAlertDialog = TempleteAlertDialog(this)
        mTempleteAlertDialog.setMessage(getResources().getString(R.string.message_ar_information))
        mTempleteAlertDialog.setButtonType(DialogButtonType.BUTTON_1)
        mTempleteAlertDialog.setGravity(Gravity.LEFT)
        mTempleteAlertDialog.show()
    }

    @Optional
    @OnClick(R.id._backButtonRect, R.id._floatingMenuButton, R.id._menuSelectAllImage, R.id._menuSelectAllText, R.id._menuSelectPlayImage, R.id._menuSelectPlayText, R.id._menuAddBookshelfImage,
        R.id._menuAddBookshelfText, R.id._menuCancelImage, R.id._menuCancelText, R.id._detailARHelpButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
            R.id._floatingMenuButton ->
            if(isListSettingComplete)
            {
                _FabToolbarLayout.show()
            }
            R.id._menuSelectAllImage, R.id._menuSelectAllText ->
            if(isListSettingComplete)
            {
                mSeriesContentsListPresenter.onClickSelectAll()
            }
            R.id._menuSelectPlayImage, R.id._menuSelectPlayText ->
            if(isListSettingComplete)
            {
                mSeriesContentsListPresenter.onClickSelectPlay()
            }
            R.id._menuAddBookshelfImage, R.id._menuAddBookshelfText ->
            if(isListSettingComplete)
            {
                mSeriesContentsListPresenter.onClickAddBookshelf()
            }
            R.id._menuCancelImage, R.id._menuCancelText ->
            if(isListSettingComplete)
            {
                _MenuSelectCountText.setVisibility(View.GONE)
                mSeriesContentsListPresenter.onClickCancel()
                if(Feature.IS_TABLET === false)
                {
                    _FabToolbarLayout.hide()
                }
            }
            R.id._detailARHelpButton -> showARInfomrationDialog()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun initPairTransition()
    {
        Log.f("")
        ViewCompat.setTransitionName(_DetailThumbnailImage, Common.STORY_DETAIL_LIST_HEADER_IMAGE)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun initSlideTransition()
    {
        Log.f("")
        val explode = Explode()
        explode.excludeTarget(android.R.id.statusBarBackground, true)
        explode.duration = Common.DURATION_SHORT
        getWindow().setEnterTransition(explode)
        getWindow().setExitTransition(explode)
        getWindow().setAllowEnterTransitionOverlap(true)
        getWindow().setAllowReturnTransitionOverlap(true)
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
    private val mMenuItemClickListener = View.OnClickListener {v ->
        when(v.id)
        {
            R.id._topMenuBack -> super@SeriesContentsListActivity.onBackPressed()
            R.id._infoButton, R.id._topMenuInfo -> mSeriesContentsListPresenter.onClickSeriesInformation()
        }
    }
}