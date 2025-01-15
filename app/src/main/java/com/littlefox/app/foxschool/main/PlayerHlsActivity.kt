package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.provider.Settings
import android.text.Html
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.PlayerListAdapter
import com.littlefox.app.foxschool.adapter.PlayerSpeedListAdapter
import com.littlefox.app.foxschool.api.viewmodel.factory.PlayerFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.common.listener.OrientationChangeListener
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import com.littlefox.app.foxschool.main.presenter.PlayerHlsPresenter
import com.littlefox.app.foxschool.`object`.data.player.PlayerEndViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.library.view.controller.FadeAnimationController
import com.littlefox.library.view.controller.FadeAnimationInformation
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.dialog.ProgressWheel
import com.littlefox.library.view.layoutmanager.LinearLayoutScrollerManager
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by only340 on 2018-03-19.
 */
@AndroidEntryPoint
class PlayerHlsActivity() : BaseActivity(), OrientationChangeListener
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainContentLayout : CoordinatorLayout

    @BindView(R.id._basePlayerLayout)
    lateinit var _BasePlayerLayout : RelativeLayout

    @BindView(R.id._playerView)
    lateinit var _PlayerView : PlayerView

    @BindView(R.id._playerListBaseLayout)
    lateinit var _PlayerListBaseLayout : LinearLayout

    @BindView(R.id._playerListView)
    lateinit var _PlayerListView : RecyclerView

    @BindView(R.id._playerSpeedListBaseLayout)
    lateinit var _PlayerSpeedListBaseLayout : LinearLayout

    @BindView(R.id._playerSpeedListView)
    lateinit var _PlayerSpeedListView : RecyclerView

    @BindView(R.id._seekbarPlayBar)
    lateinit var _SeekbarPlayBar : SeekBar

    @BindView(R.id._seekbarPortraitPlayBar)
    lateinit var _SeekbarPortraitPlayBar : SeekBar

    @BindView(R.id._playListTitleLayout)
    lateinit var _PlayListTitleLayout : ScalableLayout

    @BindView(R.id._playSpeedListTitleLayout)
    lateinit var _PlaySpeedListTitleLayout : ScalableLayout

    @BindView(R.id._progressWheelLayout)
    lateinit var _ProgressWheelLayout : ScalableLayout

    @BindView(R.id._playerTopBaseLayout)
    lateinit var _PlayerTopBaseLayout : ScalableLayout

    @BindView(R.id._playerCaptionLayout)
    lateinit var _PlayerCaptionLayout : ScalableLayout

    @BindView(R.id._playerBottomBaseLayout)
    lateinit var _PlayerBottomBaseLayout : ScalableLayout

    @BindView(R.id._playerPlayButtonLayout)
    lateinit var _PlayerPlayButtonLayout : ScalableLayout

    @BindView(R.id._playerEndBaseLayout)
    lateinit var _PlayerEndBaseLayout : RelativeLayout

    @BindView(R.id._playerEndLayout)
    lateinit var _PlayerEndLayout : ScalableLayout

    @BindView(R.id._playerEndButtonLayout)
    lateinit var _PlayerEndButtonLayout : ScalableLayout

    @BindView(R.id._playerPortraitTitleLayout)
    lateinit var _PlayerPortraitTitleLayout : ScalableLayout

    @BindView(R.id._playerTopTitle)
    lateinit var _PlayerTopTitle : TextView

    @BindView(R.id._playerCaptionTitle)
    lateinit var _PlayerCaptionTitle : TextView

    @BindView(R.id._playListTitleText)
    lateinit var _PlayListTitleText : TextView

    @BindView(R.id._playSpeedListTitleText)
    lateinit var _PlaySpeedListTitleText : TextView

    @BindView(R.id._playerCurrentPlayTime)
    lateinit var _PlayerCurrentPlayTime : TextView

    @BindView(R.id._playerRemainPlayTime)
    lateinit var _PlayerRemainPlayTime : TextView

    @BindView(R.id._playerPortraitTitleText)
    lateinit var _PlayerPortraitTitleText : TextView

    @BindView(R.id._playerCoachmarkImage)
    lateinit var _PlayerCoachmarkImage : ImageView

    @BindView(R.id._playerBackground)
    lateinit var _PlayerBackground : ImageView

    @BindView(R.id._playerOptionBackground)
    lateinit var _PlayerOptionBackground : ImageView

    @BindView(R.id._playerPageByPageButton)
    lateinit var _PlayerPageByPageButton : ImageView

    @BindView(R.id._playerListButton)
    lateinit var _PlayerListButton : ImageView

    @BindView(R.id._playerCaptionButton)
    lateinit var _PlayerCaptionButton : ImageView

    @BindView(R.id._playerRepeatButton)
    lateinit var _PlayerRepeatButton : ImageView

    @BindView(R.id._playerChangePortraitButton)
    lateinit var _PlayerChangePortraitButton : ImageView

    @BindView(R.id._playerChangeLandscapeButton)
    lateinit var _PlayerChangeLandscapeButton : ImageView

    @BindView(R.id._playerPrevButton)
    lateinit var _PlayerPrevButton : ImageView

    @BindView(R.id._playerNextButton)
    lateinit var _PlayerNextButton : ImageView

    @BindView(R.id._playerPlayButton)
    lateinit var _PlayerPlayButton : ImageView

    @BindView(R.id._playerCloseButton)
    lateinit var _PlayerCloseButton : ImageView

    @BindView(R.id._playListCloseButton)
    lateinit var _PlayListCloseButton : ImageView

    @BindView(R.id._playListCloseButtonRect)
    lateinit var _PlayListCloseButtonRect : ImageView

    @BindView(R.id._playSpeedListCloseButton)
    lateinit var _PlaySpeedListCloseButton : ImageView

    @BindView(R.id._playSpeedListCloseButtonRect)
    lateinit var _PlaySpeedListCloseButtonRect : ImageView

    @BindView(R.id._playerEndCloseButton)
    lateinit var _PlayerEndCloseButton : ImageView

    @BindView(R.id._ebookButtonImage)
    lateinit var _EbookButtonImage : ImageView

    @BindView(R.id._quizButtonImage)
    lateinit var _QuizButtonImage : ImageView

    @BindView(R.id._vocabularyButtonImage)
    lateinit var _VocabulraryButtonImage : ImageView

    @BindView(R.id._starwordsButtonImage)
    lateinit var _StarwordsButtonImage : ImageView

    @BindView(R.id._crosswordButtonImage)
    lateinit var _CrosswordButtonImage : ImageView

    @BindView(R.id._translateButtonImage)
    lateinit var _OriginalTranslateButtonImage : ImageView

    @BindView(R.id._flashcardButtonImage)
    lateinit var _FlashcardButtonImage : ImageView

    @BindView(R.id._replayButtonBoxImage)
    lateinit var _ReplayButtonBoxImage : ImageView

    @BindView(R.id._replayButtonIconImage)
    lateinit var _ReplayButtonIconImage : ImageView

    @BindView(R.id._replayButtonIconText)
    lateinit var _ReplayButtonIconText : TextView

    @BindView(R.id._nextButtonBoxImage)
    lateinit var _NextButtonBoxImage : ImageView

    @BindView(R.id._nextButtonIconImage)
    lateinit var _NextButtonIconImage : ImageView

    @BindView(R.id._nextButtonIconText)
    lateinit var _NextButtonIconText : TextView

    @BindView(R.id._progressWheelView)
    lateinit var _ProgressWheelView : ProgressWheel

    @BindView(R.id._playerLockInfoLayout)
    lateinit var _PlayerLockInfoLayout : ScalableLayout

    @BindView(R.id._lockCountTimeText)
    lateinit var _LockCountTimeText : TextView

    @BindView(R.id._lockInfoText)
    lateinit var _LockInfoText : TextView

    @BindView(R.id._playerSpeedButton)
    lateinit var _PlayerSpeedButton : ImageView

    @BindView(R.id._playerSpeedText)
    lateinit var _PlayerSpeedText : TextView

    @BindView(R.id._playerPortraitTitleOption)
    lateinit var _PlayerPortraitTitleOption : ImageView

    @BindView(R.id._playerPageByPageLayout)
    lateinit var _PlayerPageByPageLayout : ScalableLayout

    @BindView(R.id._playerPrevPageButton)
    lateinit var _PlayerPrevPageButton : ImageView

    @BindViews(R.id._player1PageButton, R.id._player2PageButton, R.id._player3PageButton, R.id._player4PageButton, R.id._player5PageButton)
    lateinit var _PageButtonList : List<@JvmSuppressWildcards ImageView>

    @BindViews(R.id._player1PageText, R.id._player2PageText, R.id._player3PageText, R.id._player4PageText, R.id._player5PageText)
    lateinit var _PageTextList : List<@JvmSuppressWildcards TextView>

    @BindView(R.id._playerNextPageButton)
    lateinit var _PlayerNextPageButton : ImageView

    private enum class LAYOUT_TYPE
    {
        INIT, PLAY, END
    }

    companion object
    {
        private const val DURATION_MENU_GONE : Int              = 5000
        private const val RIGHT_LIST_WIDTH : Int                = 654
        private const val BOTTOM_LAYOUT_HEIGHT_PORTRAIT : Int   = 112
        private const val BOTTOM_LAYOUT_HEIGHT : Int            = 234
        private const val BOTTOM_LAYOUT_EXCEPT_CAPTION : Int    = 150

        private const val PORTRAIT_DISPLAY_WIDTH : Int          = 1080
        private const val LANDSCAPE_DISPLAY_WIDTH : Int         = 1920
        private const val PAGE_MAX_VISIBLE_COUNT : Int          = 5
    }

    private lateinit var mFadeAnimationController : FadeAnimationController

    private var mCurrentLayoutMode = LAYOUT_TYPE.PLAY
    private var isNextMovieVisibleFromEndView = false
    private var mCurrentSeekProgress = 0
    private var isMoviePlaying = false
    private var isTitleMovieEnd = false

    /**
     * 자막 지원 여부
     */
    private var isSupportCaption = true

    /**
     * Page By Page 사용자 체크 유/무
     */
    private var isEnablePageByPage = false

    /**
     * 자막 사용자 체크 유/무
     */
    private var isEnableCaption = false
    private var mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT
    private var mOrientationManager : OrientationManager? = null
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mCurrentPlayPosition = 0
    private val mPlayEndStudyOptionIconList = ArrayList<View?>()
    private var mBottomHeight = 0
    private var mBottomHeightExceptCaption = 0
    private var isBottomLayoutAnimationing = false

    private var mAutoMenuGoneJob: Job? = null
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null
    private var mBottomBookAddDialog: BottomBookAddDialog? = null
    private val factoryViewModel: PlayerFactoryViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            mCurrentOrientation = Configuration.ORIENTATION_LANDSCAPE
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            mCurrentOrientation = this.resources.configuration.orientation
        }
        setContentView(R.layout.activity_player_hls)
        ButterKnife.bind(this)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        initView()
        initFont()
        setupObserverViewModel()

        factoryViewModel.init(this, _PlayerView, mCurrentOrientation)

        mOrientationManager = OrientationManager.getInstance(this)
        mOrientationManager?.setOrientationChangedListener(this)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            mOrientationManager?.disable()
        }
        else
        {
            mOrientationManager?.enable()
        }
    }

    override fun onConfigurationChanged(newConfig : Configuration)
    {
        super.onConfigurationChanged(newConfig)
        Log.f("mCurrentOrientation : " + mCurrentOrientation + ", newConfig : " + newConfig.orientation)
        if(mCurrentOrientation != newConfig.orientation)
        {
            mCurrentOrientation = newConfig.orientation
            if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                hideBottomDialog()
            }
            factoryViewModel.onChangeOrientation(mCurrentOrientation)
            changeLayout()
        }
    }

    override fun onResume()
    {
        super.onResume()
        factoryViewModel.resume()
    }

    override fun onStop()
    {
        super.onStop()
        Log.f("")
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

    override fun onBackPressed()
    {
        super.onBackPressed()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun initView()
    {
        initFadeControllerView()
        changeLayout()
        _PlayerTopBaseLayout.setOnTouchListener {v, event -> true}
        _PlayerBottomBaseLayout.setOnTouchListener {v, event -> true}
        _PlayerListBaseLayout.setOnTouchListener {v, event -> true}
        _PlayerSpeedListBaseLayout.setOnTouchListener {v, event -> true}
        _PlayerEndBaseLayout.setOnTouchListener {v, event -> true}
        _PlayerView.setOnTouchListener(mDisplayTouchListener)

        initSeekbar()
    }

    override fun initFont()
    {
        _PlayerTopTitle.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayerCaptionTitle.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayerCurrentPlayTime.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayerRemainPlayTime.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayListTitleText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlaySpeedListTitleText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayerPortraitTitleText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _ReplayButtonIconText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _NextButtonIconText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _PlayerSpeedText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _LockCountTimeText.setTypeface(Font.getInstance(this).getTypefaceBold())
        _LockInfoText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        for(i in _PageTextList.indices)
        {
            _PageTextList[i].setTypeface(Font.getInstance(this).getTypefaceBold())
        }
    }

    override fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this) {loading ->
            if(loading)
            {
                showLoading()
            } else
            {
                hideLoading()
            }
        }

        factoryViewModel.toast.observe(this) {message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        factoryViewModel.successMessage.observe(this) {message ->
            showSuccessMessage(message)
        }

        factoryViewModel.errorMessage.observe(this) {message ->
            showErrorMessage(message)
        }

        factoryViewModel.showMovieLoading.observe(this) {
            showMovieLoading()
        }

        factoryViewModel.hideMovieLoading.observe(this) {
            hideMovieLoading()
        }

        factoryViewModel.initPlayListView.observe(this) {data ->
            initPlayListView(data.first, data.second)
        }

        factoryViewModel.initPlaySpeedListView.observe(this) {adapter ->
            initPlaySpeedListView(adapter)
        }

        factoryViewModel.initMovieLayout.observe(this) {
            initMovieLayout()
        }

        factoryViewModel.settingSpeedTextLayout.observe(this) {data ->
            settingSpeedTextLayout(data.first, data.second)
        }

        factoryViewModel.settingCoachmarkView.observe(this) {type ->
            settingCoachmarkView(type)
        }

        factoryViewModel.initCaptionText.observe(this) {
            initCaptionText()
        }

        factoryViewModel.setMovieTitle.observe(this) {data ->
            setMovieTitle(data)
        }

        factoryViewModel.setCaptionText.observe(this) {data ->
            setCaptionText(data)
        }

        factoryViewModel.setRemainMovieTime.observe(this) {text ->
            setRemainMovieTime(text)
        }

        factoryViewModel.setCurrentMovieTime.observe(this) {text ->
            setCurrentMovieTime(text)
        }

        factoryViewModel.setSeekProgress.observe(this) {progress ->
            setSeekProgress(progress)
        }

        factoryViewModel.setMaxProgress.observe(this) {progress ->
            setMaxProgress(progress)
        }

        factoryViewModel.enablePlayMovie.observe(this) {enable ->
            enablePlayMovie(enable)
        }

        factoryViewModel.showPlayerStartView.observe(this) {
            showPlayerStartView()
        }

        factoryViewModel.showPlayerEndView.observe(this) {
            showPlayerEndView()
        }

        factoryViewModel.settingPlayerEndView.observe(this) {data ->
            settingPlayerEndView(data)
        }

        factoryViewModel.playFirstIndexMovie.observe(this) {
            playFirstIndexMovie()
        }

        factoryViewModel.playNormalIndexMovie.observe(this) {
            playNormalIndexMovie()
        }

        factoryViewModel.playLastIndexMovie.observe(this) {
            playLastIndexMovie()
        }

        factoryViewModel.playOneItemMovie.observe(this) {
            playOneItemMovie()
        }

        factoryViewModel.checkSupportCaptionView.observe(this) {support ->
            checkSupportCaptionView(support)
        }

        factoryViewModel.settingCaptionOption.observe(this) {data ->
            settingCaptionOption(data.first, data.second)
        }

        factoryViewModel.enableRepeatView.observe(this) {enable ->
            enableRepeatView(enable)
        }

        factoryViewModel.scrollPosition.observe(this) {position ->
            scrollPosition(position)
        }

        factoryViewModel.setCurrentPageLine.observe(this) {data ->
            settingCurrentPageLine(data.first, data.second)
        }

        factoryViewModel.setCurrentPage.observe(this) {data ->
            enableCurrentPage(data)
        }

        factoryViewModel.activatePageView.observe(this) {activate ->
            Log.f("isActivate : $activate")
            activatePageView(activate)
        }

        factoryViewModel.enableSpeedButton.observe(this) {enable ->
            if(enable)
            {
                enableSpeedButton()
            } else
            {
                disableSpeedButton()
            }
        }


        factoryViewModel.enablePortraitOptionButton.observe(this) {enable ->
            if(enable)
            {
                enablePortraitOptionButton()
            } else
            {
                disablePortraitOptionButton()
            }
        }

        factoryViewModel.availableMovieOptionButton.observe(this) {available ->
            availableMovieOptionButton(available)
        }

        factoryViewModel.dialogBottomOption.observe(this) {data ->
            showBottomItemOptionDialog(data)
        }

        factoryViewModel.dialogBottomBookshelfContentAdd.observe(this) {data ->
            showBottomBookAddDialog(data)
        }

        factoryViewModel.dialogWarningRecordPermission.observe(this) {
            showChangeRecordPermissionDialog()
        }

        factoryViewModel.dialogWarningAPIException.observe(this) {message ->
            showWarningAPIExceptionDialog(message)
        }

        factoryViewModel.dialogWarningWatchingMovie.observe(this) {
            showWarningWatchingMovieDialog()
        }
    }

    private fun showWarningAPIExceptionDialog(message: String)
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(message)
            setDialogEventType(PlayerFactoryViewModel.DIALOG_TYPE_WARNING_API_EXCEPTION)
            setButtonText(
                resources.getString(R.string.text_retry),
                resources.getString(R.string.text_close))
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showWarningWatchingMovieDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_longtime_play_warning))
            setDialogEventType(PlayerFactoryViewModel.DIALOG_TYPE_WARNING_WATCH_MOVIE)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_record_permission))
            setDialogEventType(PlayerFactoryViewModel.DIALOG_TYPE_WARNING_RECORD_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(
                resources.getString(R.string.text_cancel),
                resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    private fun showBottomItemOptionDialog(result : ContentsBaseResult)
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(this ,result)
        mBottomContentItemOptionDialog
            ?.setFullName()
            ?.setFullScreen()
            ?.disableBookshelf()
            ?.setItemOptionListener(mItemOptionListener)
            ?.setView()
            ?.setOnCancelListener(object : DialogInterface.OnCancelListener
            {
                override fun onCancel(dialog : DialogInterface)
                {
                    factoryViewModel.onClickBottomOptionDialogCancel()
                }
            })
        mBottomContentItemOptionDialog?.show()
    }

    private fun showBottomBookAddDialog(list: ArrayList<MyBookshelfResult>)
    {
        mBottomBookAddDialog = BottomBookAddDialog(this)?.apply {
            setCancelable(true)
            setBookshelfData(list)
            setFullScreen()
            setBookSelectListener(mBookAddListener)
            setOnCancelListener(object : DialogInterface.OnCancelListener
            {
                override fun onCancel(dialog : DialogInterface)
                {
                    factoryViewModel.onClickBottomOptionDialogCancel()
                }
            })
            show()
        }
    }

    private fun hideBottomDialog()
    {
        mBottomContentItemOptionDialog?.cancel()
        mBottomBookAddDialog?.cancel()
    }


    private fun initSeekbar()
    {
        _SeekbarPlayBar.run {
            thumbOffset = CommonUtils.getInstance(context).getPixel(0)
            progress = 0
            secondaryProgress = 0
            setOnSeekBarChangeListener(mOnSeekBarListener)
        }

        _SeekbarPortraitPlayBar.run {
            thumbOffset = CommonUtils.getInstance(context).getPixel(0)
            progress = 0
            secondaryProgress = 0
            setOnSeekBarChangeListener(mOnSeekBarListener)
        }

        if(CommonUtils.getInstance(this).checkTablet)
        {
            Log.i("")
            val params = _SeekbarPortraitPlayBar!!.layoutParams as RelativeLayout.LayoutParams
            params.height = CommonUtils.getInstance(this).getPixel(30)
            _SeekbarPortraitPlayBar.thumb = resources.getDrawable(R.drawable.seekbar_thumb)
            _SeekbarPortraitPlayBar.layoutParams = params
        }

        if(CommonUtils.getInstance(this).checkTablet)
        {
            _PlayerBottomBaseLayout.run{
                moveChildView(_SeekbarPlayBar, 266f, 8f, 1230f, 62f)
                moveChildView(_PlayerRemainPlayTime, 1514f, 0f, 110f, 71f)
                moveChildView(_PlayerSpeedButton, 1684f, 7f, 60f, 60f)
                moveChildView(_PlayerSpeedText, 1764f, 0f, 110f, 71f)
            }
        }

        val layerDrawable = resources.getDrawable(R.drawable.seekbar_thumb) as LayerDrawable
        val rectDrawable = layerDrawable.findDrawableByLayerId(R.id._thumbRect) as GradientDrawable
        val circleDrawable = layerDrawable.findDrawableByLayerId(R.id._thumbCircle) as GradientDrawable
        rectDrawable.setSize(CommonUtils.getInstance(this).getPixel(45), CommonUtils.getInstance(this).getPixel(45))
        circleDrawable.setSize(CommonUtils.getInstance(this).getPixel(40), CommonUtils.getInstance(this).getPixel(40))
    }

    private fun initFadeControllerView()
    {
        val playListWidth : Int = CommonUtils.getInstance(this).getPixel(RIGHT_LIST_WIDTH)
        mBottomHeight = CommonUtils.getInstance(this).getPixel(BOTTOM_LAYOUT_HEIGHT)
        mBottomHeightExceptCaption = CommonUtils.getInstance(this).getPixel(BOTTOM_LAYOUT_EXCEPT_CAPTION)
        mFadeAnimationController = FadeAnimationController(this)
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerTopBaseLayout,
                        CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_NORMAL, -mBottomHeight.toFloat(), 0f),
                        CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_NORMAL, 0f, -mBottomHeight.toFloat())))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerPlayButtonLayout,
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 0.2f, 1.0f),
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 1.0f, 0.2f)))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(
                        _PlayerCaptionLayout,
                        CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_NORMAL, mBottomHeight.toFloat(), 0f),
                        CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_NORMAL, 0f, mBottomHeight.toFloat())))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerListBaseLayout,
                        CommonUtils.getInstance(this).getTranslateXAnimation(Common.DURATION_NORMAL, playListWidth.toFloat(), 0f),
                        CommonUtils.getInstance(this).getTranslateXAnimation(Common.DURATION_NORMAL, 0f, playListWidth.toFloat())))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerSpeedListBaseLayout,
                        CommonUtils.getInstance(this).getTranslateXAnimation(Common.DURATION_NORMAL, playListWidth.toFloat(), 0f),
                        CommonUtils.getInstance(this).getTranslateXAnimation(Common.DURATION_NORMAL, 0f, playListWidth.toFloat())))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerOptionBackground,
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 0.0f, 1.0f),
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 1.0f, 0.0f)))
        mFadeAnimationController.addControlView(
                FadeAnimationInformation(_PlayerPageByPageLayout,
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 0.0f, 1.0f),
                        CommonUtils.getInstance(this).getAlphaAnimation(Common.DURATION_NORMAL, 1.0f, 0.0f)))
    }

    private fun initLayoutSetting()
    {
        Log.f("mCurrentLayoutMode : $mCurrentLayoutMode")
        when(mCurrentLayoutMode)
        {
            LAYOUT_TYPE.INIT ->
            {
                if(_PlayerEndBaseLayout.visibility == View.VISIBLE)
                {
                    _PlayerEndBaseLayout.clearAnimation()
                    _PlayerEndBaseLayout.visibility = View.GONE
                }
                _SeekbarPortraitPlayBar.progress = 0
                _SeekbarPlayBar.progress = 0
            }
            LAYOUT_TYPE.PLAY ->
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    _SeekbarPortraitPlayBar.visibility = View.VISIBLE
                }
                if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    _PlayerListButton.visibility = View.VISIBLE
                }
            }
            LAYOUT_TYPE.END ->
            {
                _PlayerEndBaseLayout.visibility = View.VISIBLE
                _PlayerEndLayout.visibility = View.VISIBLE
            }
        }
    }


    /**
     * Rotate가 될 시에 해당 orientation에 맞게 Layout을 세팅
     */
    private fun changeLayout()
    {
        Log.i("mCurrentOrientation : $mCurrentOrientation")
        changeModePlayerView()
        changeModeTopLayout()
        changeModePlayButtonLayout()
        changeModePageByPageLayout()
        changeModeCaptionLayout()
        changeModeBottomLayout()
        changeModePlayListLayout()
        changeModeLoadingLayout()
        changeModePlayEndLayout()
    }

    private fun changeModePlayerView()
    {
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, CommonUtils.getInstance(this).getPixel(602))
            _PlayerView.layoutParams = baseLayoutParams
            _PlayerBackground.layoutParams = baseLayoutParams
            _PlayerOptionBackground.layoutParams = baseLayoutParams
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            _PlayerBackground.layoutParams = baseLayoutParams
            _PlayerOptionBackground.layoutParams = baseLayoutParams

            if(CommonUtils.getInstance(this).checkTablet
                && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
            {
                baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                baseLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
                _PlayerView.layoutParams = baseLayoutParams
            }
            else
            {
                _PlayerView.layoutParams = baseLayoutParams
                _PlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }
    }

    private fun changeModeTopLayout()
    {
        Log.f("mCurrentLayoutMode : $mCurrentLayoutMode")
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _PlayerTopBaseLayout.setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 112f)
            _PlayerTopTitle.visibility = View.GONE
            _PlayerListButton.visibility = View.GONE
            if(isSupportCaption)
            {
                _PlayerPageByPageButton.visibility = View.GONE
            }
            _PlayerTopBaseLayout.moveChildView(_PlayerCaptionButton, 812f, 47f, 73f, 57f)
            _PlayerTopBaseLayout.moveChildView(_PlayerCloseButton, 974f, 46f, 62f, 60f)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _PlayerTopBaseLayout.setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 150f)
            _PlayerTopTitle.visibility = View.VISIBLE
            if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY)
            {
                if(isSupportCaption)
                {
                    _PlayerPageByPageButton.visibility = View.VISIBLE
                }
                _PlayerListButton.visibility = View.VISIBLE
            }
            _PlayerTopBaseLayout.moveChildView(_PlayerCaptionButton, 1370f, 57f, 70f, 55f)
            _PlayerTopBaseLayout.moveChildView(_PlayerCloseButton, 1815f, 52f, 57f, 58f)
        }
    }

    private fun changeModePlayButtonLayout()
    {
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            baseLayoutParams = _PlayerPlayButtonLayout.layoutParams as RelativeLayout.LayoutParams
            baseLayoutParams.removeRule(RelativeLayout.CENTER_VERTICAL)
            baseLayoutParams.topMargin = CommonUtils.getInstance(this).getPixel(239)

            _PlayerPlayButtonLayout.run {
                layoutParams = baseLayoutParams
                setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 109f)
                moveChildView(_PlayerPrevButton, 113f, 32f, 49f, 58f)
                moveChildView(_PlayerPlayButton, 495f, 0f, 98f, 109f)
                moveChildView(_PlayerNextButton, 918f, 32f, 49f, 58f)
            }
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            baseLayoutParams = _PlayerPlayButtonLayout.layoutParams as RelativeLayout.LayoutParams
            baseLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL)

            _PlayerPlayButtonLayout.run {
                layoutParams = baseLayoutParams
                setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 167f)
                moveChildView(_PlayerPrevButton, 434f, 42f, 68f, 81f)
                moveChildView(_PlayerPlayButton, 885f, 0f, 147f, 167f)
                moveChildView(_PlayerNextButton, 1419f, 42f, 68f, 81f)
            }
        }
        setPlayIconDrawable()
    }

    private fun changeModePageByPageLayout()
    {
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            if(isMenuVisible)
            {
                mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
            }
            else
            {
                if(isEnablePageByPage && isTitleMovieEnd)
                {
                    mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, true)
                }
                else
                {
                    mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
                }
            }
        }
    }

    private fun changeModeCaptionLayout()
    {
        _PlayerCaptionLayout.run {
            if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
            {
                setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 112f)
                moveChildView(_PlayerCaptionTitle, 0f, 0f, PORTRAIT_DISPLAY_WIDTH.toFloat(), 112f)
                setScale_TextSize(_PlayerCaptionTitle, 26f)

            } else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 140f)
                moveChildView(_PlayerCaptionTitle, 0f, 0f, LANDSCAPE_DISPLAY_WIDTH.toFloat(), 140f)
                setScale_TextSize(_PlayerCaptionTitle, 43f)
            }
        }

    }

    private fun changeModeBottomLayout()
    {
        Log.f("")
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY)
        {
            setNormalBottomLayout()
        }
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _PlayerBottomBaseLayout.setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), BOTTOM_LAYOUT_HEIGHT_PORTRAIT.toFloat())
            baseLayoutParams = _SeekbarPortraitPlayBar.layoutParams as RelativeLayout.LayoutParams
            baseLayoutParams.topMargin = CommonUtils.getInstance(this).getPixel(602) - _SeekbarPortraitPlayBar!!.layoutParams.height / 2
            _SeekbarPortraitPlayBar.layoutParams = baseLayoutParams
            _PlayerBottomBaseLayout.moveChildView(_PlayerRepeatButton, 47f, 15f, 58f, 62f)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _PlayerBottomBaseLayout.setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), BOTTOM_LAYOUT_HEIGHT.toFloat())
            _PlayerBottomBaseLayout.moveChildView(_PlayerRepeatButton, 48f, 0f, 71f, 70f)
        }
        if(isMenuVisible)
        {
            if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                if(isCaptionVisible)
                {
                    moveBottomLayoutAnimation(mBottomHeight, 0, 0)
                }
                else
                {
                    moveBottomLayoutAnimation(mBottomHeight, mBottomHeightExceptCaption, 0)
                }
            }
            else
            {
                moveBottomLayoutAnimation(mBottomHeight, 0, 0)
            }
        }
    }

    private fun setNormalBottomLayout()
    {
        _PlayerRepeatButton.visibility = View.VISIBLE
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _PlayerCurrentPlayTime.visibility = View.GONE
            _SeekbarPlayBar.visibility = View.GONE
            _PlayerRemainPlayTime.visibility = View.GONE
            _PlayerChangePortraitButton.visibility = View.GONE
            _PlayerChangeLandscapeButton.visibility = View.VISIBLE
            _PlayerPortraitTitleLayout.visibility = View.VISIBLE
            _SeekbarPortraitPlayBar.visibility = View.VISIBLE
            _PlayerSpeedButton.visibility = View.GONE
            _PlayerSpeedText.visibility = View.GONE
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _PlayerCurrentPlayTime.visibility = View.VISIBLE
            _SeekbarPlayBar.visibility = View.VISIBLE
            _PlayerRemainPlayTime.visibility = View.VISIBLE
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                _PlayerSpeedButton.visibility = View.VISIBLE
                _PlayerSpeedText.visibility = View.VISIBLE
            }
            else
            {
                _PlayerSpeedButton.visibility = View.GONE
                _PlayerSpeedText.visibility = View.GONE
            }
            if(CommonUtils.getInstance(this).checkTablet)
            {
                _PlayerChangePortraitButton.visibility = View.GONE
                _PlayerChangeLandscapeButton.visibility = View.GONE
            }
            else
            {
                _PlayerChangePortraitButton.visibility = View.VISIBLE
                _PlayerChangeLandscapeButton.visibility = View.GONE
            }
            _PlayerPortraitTitleLayout.visibility = View.GONE
            _SeekbarPortraitPlayBar.visibility = View.GONE
        }
    }

    private fun changeModePlayListLayout()
    {
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            baseLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            baseLayoutParams.addRule(RelativeLayout.BELOW, R.id._playerPortraitTitleLayout)
            _PlayerListBaseLayout.run {
                layoutParams = baseLayoutParams
                setBackgroundColor(resources.getColor(R.color.color_edeef2))
                visibility = View.VISIBLE
            }
            _PlayListTitleLayout.run {
                setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 115f)
                moveChildView(_PlayListTitleText, 40f, 0f, 500f, 115f)
                setScale_TextSize(_PlayListTitleText, 40f)
            }
            _PlayListTitleText.setTextColor(resources.getColor(R.color.color_black))
            _PlayListCloseButton.visibility = View.GONE
            _PlayListCloseButtonRect.visibility = View.GONE
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(CommonUtils.getInstance(this).getPixel(RIGHT_LIST_WIDTH), RelativeLayout.LayoutParams.MATCH_PARENT)
            baseLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            baseLayoutParams.removeRule(RelativeLayout.BELOW)
            _PlayerListBaseLayout.run {
                layoutParams = baseLayoutParams
                setBackgroundColor(resources.getColor(R.color.color_alpha_07_black))
                visibility = View.GONE
            }
            _PlayListTitleLayout.run {
                setScaleSize(RIGHT_LIST_WIDTH.toFloat(), 120f)
                moveChildView(_PlayListTitleText, 40f, 0f, 500f, 120f)
                setScale_TextSize(_PlayListTitleText, 40f)
            }
            _PlayListTitleText.setTextColor(resources.getColor(R.color.color_white))
            _PlayerSpeedListBaseLayout.layoutParams = baseLayoutParams
            _PlayerSpeedListBaseLayout.setBackgroundColor(resources.getColor(R.color.color_alpha_07_black))
            _PlayListCloseButton.visibility = View.VISIBLE
            _PlayListCloseButtonRect.visibility = View.VISIBLE
        }
        _PlayerSpeedListBaseLayout.visibility = View.GONE
    }

    private fun changeModeLoadingLayout()
    {
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            baseLayoutParams = _ProgressWheelLayout.layoutParams as RelativeLayout.LayoutParams
            baseLayoutParams.removeRule(RelativeLayout.CENTER_VERTICAL)
            _ProgressWheelLayout.setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 602f)
            _ProgressWheelLayout.moveChildView(_ProgressWheelView, 0f, 0f, 150f, 150f)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            baseLayoutParams = _ProgressWheelLayout.layoutParams as RelativeLayout.LayoutParams
            baseLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            _ProgressWheelLayout.setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 150f)
            _ProgressWheelLayout.moveChildView(_ProgressWheelView, 0f, 0f, 150f, 150f)
        }
    }

    private fun changeModePlayEndLayout()
    {
        Log.f("")
        var baseLayoutParams : RelativeLayout.LayoutParams? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, CommonUtils.getInstance(this).getPixel(602))
            _PlayerEndBaseLayout.layoutParams = baseLayoutParams
            _PlayerEndButtonLayout.setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 112f)
            _PlayerEndButtonLayout.moveChildView(_PlayerEndCloseButton, 974f, 46f, 57f, 58f)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            baseLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            _PlayerEndBaseLayout.layoutParams = baseLayoutParams
            _PlayerEndButtonLayout.setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 100f)
            _PlayerEndButtonLayout.moveChildView(_PlayerEndCloseButton, 1800f, 40f, 57f, 58f)
        }
        if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY || mCurrentLayoutMode == LAYOUT_TYPE.END)
        {
            setFullPlayEndLayout()
        }
    }

    private fun moveBottomLayoutAnimation(start : Int, desc : Int, duration : Long)
    {
        isBottomLayoutAnimationing = true
        ViewAnimator.animate(_PlayerBottomBaseLayout)
                .translationY(start.toFloat(), desc.toFloat())
                .duration(Common.DURATION_NORMAL)
                .onStart {_PlayerBottomBaseLayout!!.visibility = View.VISIBLE}
                .onStop {isBottomLayoutAnimationing = false}
                .start()
    }

    private fun hideBottomLayoutAnimation(start : Int, desc : Int, duration : Long)
    {
        isBottomLayoutAnimationing = true
        ViewAnimator.animate(_PlayerBottomBaseLayout).translationY(start.toFloat(), desc.toFloat())
                .duration(duration)
                .onStop {
                    _PlayerBottomBaseLayout.visibility = View.GONE
                    isBottomLayoutAnimationing = false}
                .start()
    }

    private fun initPlayListView(adapter : PlayerListAdapter, position : Int)
    {
        Log.f("position : $position")
        _PlayerListView.layoutManager = LinearLayoutScrollerManager(this)
        _PlayerListView.adapter = adapter

        lifecycleScope.launch(Dispatchers.Main) {
            delay(Common.DURATION_NORMAL)
            forceScrollView(position)
        }
    }

    private fun initPlaySpeedListView(adapter : PlayerSpeedListAdapter)
    {
        Log.f("")
        _PlayerSpeedListView.layoutManager = LinearLayoutScrollerManager(this)
        _PlayerSpeedListView.adapter = adapter
    }

    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(this, CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE))
        mMaterialLoadingDialog?.show()
    }

    override fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    private fun showMovieLoading()
    {
        _ProgressWheelLayout.visibility = View.VISIBLE
    }

    private fun hideMovieLoading()
    {
        _ProgressWheelLayout.visibility = View.GONE
    }

    private fun initMovieLayout()
    {
        Log.f("")
        mCurrentLayoutMode = LAYOUT_TYPE.INIT
        initLayoutSetting()
        hideMenuAndList()
        mFadeAnimationController.run {
            promptViewStatus(_PlayerTopBaseLayout, false)
            promptViewStatus(_PlayerPlayButtonLayout, false)
            promptViewStatus(_PlayerPageByPageLayout, false)
            promptViewStatus(_PlayerCaptionLayout, false)
        }
        _PlayerBottomBaseLayout.visibility = View.GONE
        _SeekbarPortraitPlayBar.visibility = View.INVISIBLE
        _PlayerBackground.visibility = View.VISIBLE
        isTitleMovieEnd = false
    }

    private fun settingSpeedTextLayout(speedIndex : Int, isMenuHide : Boolean)
    {
        if(isMenuHide)
        {
            hideMenuAndList()
        }
        val data = resources.getStringArray(R.array.text_list_speed)
        if(data[speedIndex].contains(("(Normal)")))
        {
            data[speedIndex] = data[speedIndex].replace("(Normal)", "")
        }
        _PlayerSpeedText.text = data.get(speedIndex)
    }

    private fun settingCoachmarkView(type : String)
    {
        Log.f("type : $type")
        if((type == Common.CONTENT_TYPE_STORY))
        {
            _PlayerCoachmarkImage.setBackgroundResource(coachmarkStoryDrawable)
        }
        else
        {
            _PlayerCoachmarkImage.setBackgroundResource(coachmarkSongDrawable)
        }
        _PlayerCoachmarkImage.visibility = View.VISIBLE
        _PlayerCoachmarkImage.setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(v : View)
            {
                _PlayerCoachmarkImage.visibility = View.GONE
                factoryViewModel.onCoachMarkNeverSeeAgain(type)
            }
        })
    }

    private fun initCaptionText()
    {
        _PlayerCaptionTitle.text = ""
    }

    private fun setMovieTitle(title : String)
    {
        _PlayerPortraitTitleText.text = title
        _PlayerTopTitle.text = title
    }

    private fun setCaptionText(text : String)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            _PlayerCaptionTitle.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        }
        else
        {
            _PlayerCaptionTitle.text = Html.fromHtml(text)
        }
    }

    private fun setRemainMovieTime(remainTime : String)
    {
        _PlayerRemainPlayTime.text = remainTime
    }

    private fun setCurrentMovieTime(currentTime : String)
    {
        _PlayerCurrentPlayTime.text = currentTime
    }

    private fun setSeekProgress(progress : Int)
    {
        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _SeekbarPlayBar.progress = progress
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _SeekbarPortraitPlayBar.progress = progress
        }
    }

    private fun setMaxProgress(maxProgress : Int)
    {
        _SeekbarPlayBar.max = maxProgress
        _SeekbarPortraitPlayBar.max = maxProgress
    }

    private fun enablePlayMovie(isPlaying : Boolean)
    {
        isMoviePlaying = isPlaying
        setPlayIconDrawable()
    }

    private fun showPlayerStartView()
    {
        Log.f("")
        mCurrentLayoutMode = LAYOUT_TYPE.PLAY
        initLayoutSetting()
        if(isEnableCaption && isSupportCaption)
        {
            enableCaptionAnimation(true)
        }
        enablePageByPageAnimation(true)
        changeModeBottomLayout()
        changeModePlayEndLayout()
        lifecycleScope.launch(Dispatchers.Main){
            delay(Common.DURATION_NORMAL)
            _PlayerBackground.visibility = View.INVISIBLE
        }
    }

    private fun showPlayerEndView()
    {
        Log.f("")
        hideMenuAndList()
        mCurrentLayoutMode = LAYOUT_TYPE.END
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _SeekbarPortraitPlayBar!!.visibility = View.GONE
        }
        initLayoutSetting()
        enablePageByPageAnimation(false)
        enableCaptionAnimation(false)
        showPlayerEndLayoutAnimation()
    }

    private fun settingPlayerEndView(data : PlayerEndViewData)
    {
        mPlayEndStudyOptionIconList.clear()
        isNextMovieVisibleFromEndView = data.isNextButtonVisible

        if(data.isEbookAvailable)
        {
            mPlayEndStudyOptionIconList.add(_EbookButtonImage)
        }
        else
        {
            _EbookButtonImage!!.visibility = View.GONE
        }

        if(data.isQuizAvailable)
        {
            mPlayEndStudyOptionIconList.add(_QuizButtonImage)
        }
        else
        {
            _QuizButtonImage!!.visibility = View.GONE
        }

        if(data.isVocabularyAvailable)
        {
            mPlayEndStudyOptionIconList.add(_VocabulraryButtonImage)
        }
        else
        {
            _VocabulraryButtonImage!!.visibility = View.GONE
        }

        if(data.isFlashcardAvailable)
        {
            mPlayEndStudyOptionIconList.add(_FlashcardButtonImage)
        }
        else
        {
            _FlashcardButtonImage!!.visibility = View.GONE
        }

        if(data.isStarwordsAvailable)
        {
            mPlayEndStudyOptionIconList.add(_StarwordsButtonImage)
        }
        else
        {
            _StarwordsButtonImage!!.visibility = View.GONE
        }

        if(data.isCrosswordAvailable)
        {
            mPlayEndStudyOptionIconList.add(_CrosswordButtonImage)
        }
        else
        {
            _CrosswordButtonImage!!.visibility = View.GONE
        }

        if(data.isTranslateAvailable)
        {
            mPlayEndStudyOptionIconList.add(_OriginalTranslateButtonImage)
        }
        else
        {
            _OriginalTranslateButtonImage!!.visibility = View.GONE
        }

        if(data.isNextButtonVisible)
        {
            _NextButtonBoxImage!!.visibility = View.VISIBLE
            _NextButtonIconImage!!.visibility = View.VISIBLE
            _NextButtonIconText!!.visibility = View.VISIBLE
        }
        else
        {
            _NextButtonBoxImage!!.visibility = View.GONE
            _NextButtonIconImage!!.visibility = View.GONE
            _NextButtonIconText!!.visibility = View.GONE
        }

        Log.f("size : "+ mPlayEndStudyOptionIconList.size)

        settingPlayEndButtonLayout(isNextMovieVisibleFromEndView, mPlayEndStudyOptionIconList)
    }

    private fun playFirstIndexMovie()
    {
        Log.f("")
        _PlayerPrevButton.visibility = View.INVISIBLE
    }

    private fun playNormalIndexMovie()
    {
        Log.f("")
        _PlayerPrevButton.visibility = View.VISIBLE
        _PlayerNextButton.visibility = View.VISIBLE
    }

    private fun playLastIndexMovie()
    {
        Log.f("")
        _PlayerNextButton.visibility = View.INVISIBLE
    }

    private fun playOneItemMovie()
    {
        Log.f("")
        _PlayerPrevButton.visibility = View.INVISIBLE
        _PlayerNextButton.visibility = View.INVISIBLE
    }

    private fun checkSupportCaptionView(isSupport : Boolean)
    {
        Log.f("isSupport : $isSupport")
        isSupportCaption = isSupport
        if(isSupportCaption)
        {
            _PlayerCaptionButton!!.visibility = View.VISIBLE
            _PlayerPageByPageButton!!.visibility = View.VISIBLE
        }
        else
        {
            _PlayerCaptionButton!!.visibility = View.GONE
            _PlayerPageByPageButton!!.visibility = View.GONE
        }
    }

    private fun settingCaptionOption(isEnableCaption : Boolean, isEnablePage : Boolean)
    {
        Log.f("isEnableCaption : $isEnableCaption, isEnablePage : $isEnablePage")
        this.isEnableCaption = isEnableCaption
        isEnablePageByPage = isEnablePage
        enableCaptionView(isEnableCaption)
        enablePageByPageView(isEnablePage)
    }

    private fun enableRepeatView(isOn : Boolean)
    {
        if(isOn)
        {
            _PlayerRepeatButton!!.setImageResource(R.drawable.player__replay_on)
        }
        else
        {
            _PlayerRepeatButton!!.setImageResource(R.drawable.player__replay_off)
        }
    }

    private fun scrollPosition(position : Int)
    {
        Log.f("position : $position")
        mCurrentPlayPosition = position
        _PlayerListView!!.post(object : Runnable
        {
            override fun run()
            {
                _PlayerListView!!.smoothScrollToPosition(mCurrentPlayPosition)
            }
        })
    }

    private fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainContentLayout, message)
    }

    private fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainContentLayout, message)
    }

    private fun settingCurrentPageLine(startIndex : Int, maxPageCount : Int)
    {
        Log.f("startIndex : $startIndex,  maxPageCount : $maxPageCount")
        var index = 0
        val maxItemCountInLine = getCurrentPageCountInLine(startIndex, maxPageCount)
        for(i in startIndex until startIndex + maxItemCountInLine)
        {
            _PageTextList.get(index).text = i.toString()
            _PageButtonList.get(index).tag = i
            index++
        }
        for(i in 1..PAGE_MAX_VISIBLE_COUNT)
        {
            if(i <= maxItemCountInLine)
            {
                _PageTextList.get(i - 1).visibility = View.VISIBLE
                _PageButtonList.get(i - 1).visibility = View.VISIBLE
            }
            else
            {
                _PageTextList.get(i - 1).visibility = View.GONE
                _PageButtonList.get(i - 1).visibility = View.GONE
            }
        }
        if(startIndex == 1)
        {
            _PlayerPrevPageButton.visibility = View.GONE
        }
        else
        {
            _PlayerPrevPageButton.visibility = View.VISIBLE
        }
        if(((maxItemCountInLine < Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE) || (startIndex + maxItemCountInLine - 1 == maxPageCount)))
        {
            _PlayerNextPageButton.visibility = View.GONE
        }
        else
        {
            _PlayerNextPageButton.visibility = View.VISIBLE
        }
    }

    private fun enableCurrentPage(page : Int)
    {
        for(i in 0 until PAGE_MAX_VISIBLE_COUNT)
        {
            if(page == -1)
            {
                _PageButtonList[i].setImageResource(R.drawable.player__page_default)
            }
            else
            {
                if(_PageButtonList[i].tag as Int == page)
                {
                    _PageButtonList[i].setImageResource(R.drawable.player__page_now)
                }
                else
                {
                    _PageButtonList[i].setImageResource(R.drawable.player__page_default)
                }
            }
        }
    }

    private fun activatePageView(isActivate : Boolean)
    {
        Log.f("isActivate : $isActivate")
        if(isActivate)
        {
            isTitleMovieEnd = true
            if(isEnablePageByPage && isMenuVisible == false)
                mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, true)
        }
        else
        {
            isTitleMovieEnd = false
            if(isEnablePageByPage && isMenuVisible == false)
                mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
        }
    }

    private fun enableSpeedButton()
    {
        _PlayerSpeedButton.isEnabled = true
        _PlayerSpeedText.isEnabled = true
        _PlayerSpeedButton.alpha = 1.0f
        _PlayerSpeedText.alpha = 1.0f
    }

    private fun disableSpeedButton()
    {
        _PlayerSpeedButton.isEnabled = false
        _PlayerSpeedText.isEnabled = false
        _PlayerSpeedButton.alpha = 0.0f
        _PlayerSpeedText.alpha = 0.0f
    }

    private fun disablePortraitOptionButton()
    {
        _PlayerPortraitTitleOption.isEnabled = false
    }

    private fun enablePortraitOptionButton()
    {
        _PlayerPortraitTitleOption.isEnabled = true
    }

    private fun availableMovieOptionButton(isAvailable : Boolean)
    {
        if(isAvailable)
        {
            _PlayerPortraitTitleOption.visibility = View.VISIBLE
        }
        else
        {
            _PlayerPortraitTitleOption.visibility = View.GONE
        }
    }

    private val coachmarkStoryDrawable : Int
        private get()
        {
            if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_story_portrait_kr
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_story_landscape_kr_tablet_4_3
                    }
                    else
                    {
                        return R.drawable.coachmark_story_landscape_kr
                    }
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.JAPANESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_story_portrait_jp
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_story_landscape_jp_tablet_4_3
                    }
                    else
                    {
                        return R.drawable.coachmark_story_landscape_jp
                    }
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_story_portrait_cn
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return  R.drawable.coachmark_story_landscape_cn_tablet_4_3
                    }
                    else
                    {
                        return R.drawable.coachmark_story_landscape_cn
                    }
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_story_portrait_tw
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_story_landscape_tw_tablet_4_3
                    }
                    else
                    {
                        return R.drawable.coachmark_story_landscape_tw
                    }
                }
            }
            else
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_story_portrait_en
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_story_landscape_en_tablet_4_3
                    }
                    else
                    {
                        return R.drawable.coachmark_story_landscape_en
                    }
                }
            }
        }

    private val coachmarkSongDrawable : Int
        private get()
        {
            if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_song_portrait_kr
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_song_landscape_kr_tablet_4_3
                    }
                    else
                        return R.drawable.coachmark_song_landscape_kr
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.JAPANESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_song_portrait_jp
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_song_landscape_jp_tablet_4_3
                    }
                    else
                        return R.drawable.coachmark_song_landscape_jp
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_song_portrait_cn
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_song_landscape_cn_tablet_4_3
                    }
                    else
                        return R.drawable.coachmark_song_landscape_cn
                }
            }
            else if(Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_song_portrait_tw
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_song_landscape_tw_tablet_4_3
                    }
                    else
                        return R.drawable.coachmark_song_landscape_tw
                }
            }
            else
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    return R.drawable.coachmark_song_portrait_en
                }
                else
                {
                    if(CommonUtils.getInstance(this).checkTablet
                        && CommonUtils.getInstance(this).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
                    {
                        return R.drawable.coachmark_song_landscape_en_tablet_4_3
                    }
                    else
                        return R.drawable.coachmark_song_landscape_en
                }
            }
        }

    private fun setPlayIconDrawable()
    {
        if(isMoviePlaying)
        {
            _PlayerPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.player__pause))
        }
        else
        {
            _PlayerPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.player__play))
        }
    }

    private val isMenuVisible : Boolean
        private get()
        {
            if((_PlayerTopBaseLayout!!.visibility == View.VISIBLE)
                    || (_PlayerBottomBaseLayout!!.visibility == View.VISIBLE)
                    || (_PlayerPlayButtonLayout!!.visibility == View.VISIBLE))
            {
                return true
            }
            else
            {
                return false
            }
        }
    private val isPlayListVisible : Boolean
        private get()
        {
            if(_PlayerListBaseLayout!!.visibility == View.VISIBLE)
            {
                return true
            }
            else
            {
                return false
            }
        }
    private val isPlaySpeedListVisible : Boolean
        private get()
        {
            if(_PlayerSpeedListBaseLayout!!.visibility == View.VISIBLE)
            {
                return true
            }
            else
            {
                return false
            }
        }

    private fun hideMenuAndList()
    {
        if(isMenuVisible)
        {
            enableMenuAnimation(false)
            enableBackgroudAnimation(false)
        }
        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            Log.f("isPlayListVisible : $isPlayListVisible, mCurrentLayoutMode : $mCurrentLayoutMode")
            if(isPlayListVisible)
            {
                enablePlayListAnimation(false)
                enableBackgroudAnimation(false)
            }
            if(isPlaySpeedListVisible)
            {
                enablePlaySpeedListAnimation(false)
                enableBackgroudAnimation(false)
                enablePageByPageAnimation(true)
            }
        }
    }

    private fun enablePageByPageView(isEnable : Boolean)
    {
        if(isEnable)
        {
            _PlayerPageByPageButton.setImageResource(R.drawable.player__repeat_on)
        }
        else
        {
            _PlayerPageByPageButton.setImageResource(R.drawable.player__repeat_off)
        }
        adjustPageByPageLayout(isEnableCaption)
    }

    private fun adjustBottomControlLayout(isCaptionVisible : Boolean)
    {
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            return
        }
        if(isCaptionVisible)
        {
            moveBottomLayoutAnimation(mBottomHeightExceptCaption, 0, Common.DURATION_NORMAL)
        }
        else
        {
            moveBottomLayoutAnimation(0, mBottomHeightExceptCaption, Common.DURATION_NORMAL)
        }
    }

    private fun enableCaptionView(isEnable : Boolean)
    {
        if(isEnable)
        {
            Log.f("caption enable")
            _PlayerCaptionButton.setImageResource(R.drawable.player__caption_on)
        }
        else
        {
            Log.f("caption disable")
            _PlayerCaptionButton.setImageResource(R.drawable.player__caption_off)
        }
    }

    private fun enablePlayListAnimation(isVisible : Boolean)
    {
        if((mCurrentLayoutMode == LAYOUT_TYPE.PLAY || mCurrentLayoutMode == LAYOUT_TYPE.INIT))
        {
            if(isVisible)
            {
                mFadeAnimationController.startAnimation(_PlayerListBaseLayout, FadeAnimationController.TYPE_FADE_IN)
            }
            else
            {
                mFadeAnimationController.startAnimation(_PlayerListBaseLayout, FadeAnimationController.TYPE_FADE_OUT)
            }
        }
    }

    private fun enablePlaySpeedListAnimation(isVisible : Boolean)
    {
        if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY)
        {
            if(isVisible)
            {
                mFadeAnimationController.startAnimation(_PlayerSpeedListBaseLayout, FadeAnimationController.TYPE_FADE_IN)
            }
            else
            {
                mFadeAnimationController.startAnimation(_PlayerSpeedListBaseLayout, FadeAnimationController.TYPE_FADE_OUT)
            }
        }
    }

    private fun enableCaptionAnimation(isVisible : Boolean)
    {
        if(isVisible)
        {
            mFadeAnimationController.startAnimation(_PlayerCaptionLayout, FadeAnimationController.TYPE_FADE_IN)
        }
        else
        {
            mFadeAnimationController.startAnimation(_PlayerCaptionLayout, FadeAnimationController.TYPE_FADE_OUT)
        }
    }

    private fun enablePageByPageAnimation(isVisible : Boolean)
    {
        if(isSupportCaption && isEnablePageByPage && isTitleMovieEnd)
        {
            if(mCurrentLayoutMode != LAYOUT_TYPE.INIT)
            {
                if(isVisible)
                {
                    mFadeAnimationController.startAnimation(_PlayerPageByPageLayout, FadeAnimationController.TYPE_FADE_IN)
                }
                else
                {
                    mFadeAnimationController.startAnimation(_PlayerPageByPageLayout, FadeAnimationController.TYPE_FADE_OUT)
                }
            }
        }
    }

    private fun enableBackgroudAnimation(isVisible : Boolean)
    {
        if(isVisible)
        {
            mFadeAnimationController.startAnimation(_PlayerOptionBackground, FadeAnimationController.TYPE_FADE_IN)
        }
        else
        {
            mFadeAnimationController.startAnimation(_PlayerOptionBackground, FadeAnimationController.TYPE_FADE_OUT)
        }
    }

    private fun enableMenuAnimation(isVisible : Boolean)
    {
        if(isVisible)
        {
            mFadeAnimationController.startAnimation(_PlayerTopBaseLayout, FadeAnimationController.TYPE_FADE_IN)
            if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY)
            {
                mFadeAnimationController.startAnimation(_PlayerPlayButtonLayout, FadeAnimationController.TYPE_FADE_IN)
                if(isCaptionVisible || mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    moveBottomLayoutAnimation(mBottomHeight, 0, Common.DURATION_NORMAL)
                }
                else
                {
                    moveBottomLayoutAnimation(mBottomHeight, mBottomHeightExceptCaption, Common.DURATION_NORMAL)
                }
            }
            startMenuGoneTimer()
        }
        else
        {
            mFadeAnimationController.startAnimation(_PlayerTopBaseLayout, FadeAnimationController.TYPE_FADE_OUT)
            if(mCurrentLayoutMode == LAYOUT_TYPE.PLAY)
            {
                mFadeAnimationController.startAnimation(_PlayerPlayButtonLayout, FadeAnimationController.TYPE_FADE_OUT)
                if(isCaptionVisible || mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    hideBottomLayoutAnimation(0, mBottomHeight, Common.DURATION_NORMAL)
                }
                else
                {
                    hideBottomLayoutAnimation(mBottomHeightExceptCaption, mBottomHeight, Common.DURATION_NORMAL)
                }
            }
            removeMenuGoneTimer()
        }
    }

    private fun startMenuGoneTimer()
    {
        removeMenuGoneTimer()
        if(mAutoMenuGoneJob?.isActive == true)
        {
            mAutoMenuGoneJob?.cancel()
            mAutoMenuGoneJob = null
        }
        mAutoMenuGoneJob = lifecycleScope.launch(Dispatchers.Main){
            delay(DURATION_MENU_GONE.toLong())
            forceMenuGone()
        }
    }

    private fun removeMenuGoneTimer()
    {
        if(mAutoMenuGoneJob?.isActive == true)
        {
            mAutoMenuGoneJob?.cancel()
            mAutoMenuGoneJob = null
        }
    }

    private fun setFullPlayEndLayout()
    {
        Log.f("")
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _PlayerEndLayout.setScaleSize(PORTRAIT_DISPLAY_WIDTH.toFloat(), 337f)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _PlayerEndLayout.setScaleSize(LANDSCAPE_DISPLAY_WIDTH.toFloat(), 375f)
        }
        settingPlayEndButtonLayout(isNextMovieVisibleFromEndView, mPlayEndStudyOptionIconList)
    }

    private fun settingPlayEndButtonLayout(isNextMovieHave : Boolean, views : ArrayList<View?>)
    {
        Log.f("orientation : $mCurrentOrientation, isNextMovieHave : $isNextMovieHave")
        Log.f("views size : " + views.size)
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            if(isNextMovieVisibleFromEndView)
            {
                _ReplayButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box_column)
                _NextButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box_column)
                _PlayerEndLayout.run {
                    moveChildView(_ReplayButtonBoxImage, 73f, 189f, 457f, 120f)
                    moveChildView(_ReplayButtonIconImage, 185f, 220f, 51f, 58f)
                    moveChildView(_ReplayButtonIconText, 273f, 214f, 200f, 69f)
                    moveChildView(_NextButtonBoxImage, 548f, 189f, 457f, 120f)
                    moveChildView(_NextButtonIconImage, 660f, 231f, 55f, 36f)
                    moveChildView(_NextButtonIconText, 748f, 214f, 200f, 69f)
                }
            }
            else
            {
                _ReplayButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box_column)
                _PlayerEndLayout.run {
                    moveChildView(_ReplayButtonBoxImage, 312f, 189f, 457f, 120f)
                    moveChildView(_ReplayButtonIconImage, 424f, 220f, 51f, 58f)
                    moveChildView(_ReplayButtonIconText, 512f, 214f, 200f, 69f)
                }
            }

            _PlayerEndLayout.run {
                if(views.size > 0)
                {
                    when(views.size)
                    {
                        1 -> moveChildView(views[0], 483f, 0f, 114f, 114f)
                        2 ->
                        {
                            moveChildView(views[0], 406f, 0f, 114f, 114f)
                            moveChildView(views[1], 560f, 0f, 114f, 114f)
                        }
                        3 ->
                        {
                            moveChildView(views[0], 329f, 0f, 114f, 114f)
                            moveChildView(views[1], 483f, 0f, 114f, 114f)
                            moveChildView(views[2], 637f, 0f, 114f, 114f)
                        }
                        4 ->
                        {
                            moveChildView(views[0], 252f, 0f, 114f, 114f)
                            moveChildView(views[1], 406f, 0f, 114f, 114f)
                            moveChildView(views[2], 560f, 0f, 114f, 114f)
                            moveChildView(views[3], 714f, 0f, 114f, 114f)
                        }
                        5 ->
                        {
                            moveChildView(views[0], 175f, 0f, 114f, 114f)
                            moveChildView(views[1], 329f, 0f, 114f, 114f)
                            moveChildView(views[2], 483f, 0f, 114f, 114f)
                            moveChildView(views[3], 637f, 0f, 114f, 114f)
                            moveChildView(views[4], 791f, 0f, 114f, 114f)
                        }
                        6 ->
                        {
                            moveChildView(views[0], 134f, 0f, 114f, 114f)
                            moveChildView(views[1], 274f, 0f, 114f, 114f)
                            moveChildView(views[2], 414f, 0f, 114f, 114f)
                            moveChildView(views[3], 553f, 0f, 114f, 114f)
                            moveChildView(views[4], 693f, 0f, 114f, 114f)
                            moveChildView(views[5], 833f, 0f, 114f, 114f)
                        }
                        7 ->
                        {
                            moveChildView(views[0], 64f, 0f, 114f, 114f)
                            moveChildView(views[1], 204f, 0f, 114f, 114f)
                            moveChildView(views[2], 344f, 0f, 114f, 114f)
                            moveChildView(views[3], 484f, 0f, 114f, 114f)
                            moveChildView(views[4], 623f, 0f, 114f, 114f)
                            moveChildView(views[5], 763f, 0f, 114f, 114f)
                            moveChildView(views[6], 903f, 0f, 114f, 114f)
                        }
                    }
                }
            }

        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            if(isNextMovieVisibleFromEndView)
            {
                _ReplayButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box)
                _ReplayButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box)
                _PlayerEndLayout.run {
                    moveChildView(_ReplayButtonBoxImage, 452f, 227f, 497f, 148f)
                    moveChildView(_ReplayButtonIconImage, 564f, 267f, 69f, 69f)
                    moveChildView(_ReplayButtonIconText, 682f, 267f, 250f, 69f)
                    moveChildView(_NextButtonBoxImage, 972f, 227f, 497f, 148f)
                    moveChildView(_NextButtonIconImage, 1084f, 267f, 69f, 69f)
                    moveChildView(_NextButtonIconText, 1202f, 267f, 250f, 69f)
                }

            }
            else
            {
                _ReplayButtonBoxImage.setBackgroundResource(R.drawable.replay_btn_box)
                _PlayerEndLayout.run {
                    moveChildView(_ReplayButtonBoxImage, 712f, 227f, 497f, 148f)
                    moveChildView(_ReplayButtonIconImage, 824f, 267f, 69f, 69f)
                    moveChildView(_ReplayButtonIconText, 942f, 267f, 250f, 69f)
                }
            }
            _PlayerEndLayout.run {
                if(views.size > 0)
                {
                    when(views.size)
                    {
                        1 -> moveChildView(views[0], 885f, 0f, 150f, 150f)
                        2 ->
                        {
                            moveChildView(views[0], 780f, 0f, 150f, 150f)
                            moveChildView(views[1], 990f, 0f, 150f, 150f)
                        }
                        3 ->
                        {
                            moveChildView(views[0], 675f, 0f, 150f, 150f)
                            moveChildView(views[1], 885f, 0f, 150f, 150f)
                            moveChildView(views[2], 1095f, 0f, 150f, 150f)
                        }
                        4 ->
                        {
                            moveChildView(views[0], 570f, 0f, 150f, 150f)
                            moveChildView(views[1], 780f, 0f, 150f, 150f)
                            moveChildView(views[2], 990f, 0f, 150f, 150f)
                            moveChildView(views[3], 1200f, 0f, 150f, 150f)
                        }
                        5 ->
                        {
                            moveChildView(views[0], 465f, 0f, 150f, 150f)
                            moveChildView(views[1], 675f, 0f, 150f, 150f)
                            moveChildView(views[2], 885f, 0f, 150f, 150f)
                            moveChildView(views[3], 1095f, 0f, 150f, 150f)
                            moveChildView(views[4], 1305f, 0f, 150f, 150f)
                        }
                        6 ->
                        {
                            moveChildView(views[0], 360f, 0f, 150f, 150f)
                            moveChildView(views[1], 570f, 0f, 150f, 150f)
                            moveChildView(views[2], 780f, 0f, 150f, 150f)
                            moveChildView(views[3], 990f, 0f, 150f, 150f)
                            moveChildView(views[4], 1200f, 0f, 150f, 150f)
                            moveChildView(views[5], 1410f, 0f, 150f, 150f)
                        }
                        7 ->
                        {
                            moveChildView(views[0], 255f, 0f, 150f, 150f)
                            moveChildView(views[1], 465f, 0f, 150f, 150f)
                            moveChildView(views[2], 675f, 0f, 150f, 150f)
                            moveChildView(views[3], 885f, 0f, 150f, 150f)
                            moveChildView(views[4], 1095f, 0f, 150f, 150f)
                            moveChildView(views[5], 1305f, 0f, 150f, 150f)
                            moveChildView(views[6], 1515f, 0f, 150f, 150f)
                        }
                    }
                }
            }
        }
    }

    private fun showPlayerEndLayoutAnimation()
    {
        Log.i("")
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            _PlayerEndBaseLayout.startAnimation(CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_LONG, -CommonUtils.getInstance(this).getPixel(602).toFloat(), 0f, LinearOutSlowInInterpolator()))
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            _PlayerEndBaseLayout.startAnimation(CommonUtils.getInstance(this).getTranslateYAnimation(Common.DURATION_LONG, -CommonUtils.getInstance(this).getPixel(1080).toFloat(), 0f, LinearOutSlowInInterpolator()))
        }
    }

    private val isCaptionVisible : Boolean
        private get()
        {
            if(_PlayerCaptionLayout!!.visibility == View.VISIBLE)
            {
                return true
            }
            else
            {
                return false
            }
        }

    private fun adjustPageByPageLayout(isCaptionVisible : Boolean)
    {
        if(isCaptionVisible)
        {
            _PlayerPageByPageLayout.scaleHeight = 279f
        }
        else
        {
            _PlayerPageByPageLayout.scaleHeight = 139f
        }
    }

    private fun forceScrollView(position : Int)
    {
        Log.f("orientation : " + mCurrentOrientation + "position : " + position)
        mCurrentPlayPosition = position
        _PlayerListView.post(object : Runnable
        {
            override fun run()
            {
                if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    _PlayerListView.smoothScrollToPosition(mCurrentPlayPosition)
                }
                else
                {
                    _PlayerListView.scrollToPosition(mCurrentPlayPosition)
                }
            }
        })
    }

    private fun getCurrentPageCountInLine(startIndex : Int, maxPageCount : Int) : Int
    {
        val pageIndex = startIndex - 1
        if(pageIndex + Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE < maxPageCount)
        {
            return Common.MAX_PAGE_BY_PAGE_COUNT_IN_LINE
        }
        else
        {
            return maxPageCount - pageIndex
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @OnClick(
        R.id._playerPlayButton, R.id._playerPrevButton, R.id._playerNextButton, R.id._playerCaptionButton, R.id._playerCloseButton, R.id._playerListButton,
        R.id._playerEndCloseButton, R.id._playListCloseButtonRect, R.id._playerChangePortraitButton, R.id._playerChangeLandscapeButton,
        R.id._nextButtonBoxImage, R.id._replayButtonBoxImage, R.id._playerRepeatButton, R.id._playerPortraitTitleOption,
        R.id._ebookButtonImage, R.id._quizButtonImage, R.id._vocabularyButtonImage, R.id._translateButtonImage, R.id._flashcardButtonImage, R.id._starwordsButtonImage,
        R.id._playerPageByPageButton, R.id._player1PageButton, R.id._player2PageButton, R.id._player3PageButton, R.id._player4PageButton, R.id._player5PageButton,
        R.id._playerPrevPageButton, R.id._playerNextPageButton, R.id._playerSpeedButton, R.id._playerSpeedText, R.id._playSpeedListCloseButtonRect, R.id._crosswordButtonImage
    )
    fun onClickView(view : View)
    {
        Log.f("View Click id : " + view.id)
        when(view.id)
        {
            R.id._playerPlayButton ->
            {
                factoryViewModel.onHandlePlayButton()
                startMenuGoneTimer()
            }
            R.id._playerPrevButton -> factoryViewModel.onPrevButton()
            R.id._playerNextButton -> factoryViewModel.onNextButton()
            R.id._playerPageByPageButton ->
            {
                isEnablePageByPage = !isEnablePageByPage
                enablePageByPageView(isEnablePageByPage)
                factoryViewModel.onClickPageByPageButton(isEnablePageByPage)
                startMenuGoneTimer()
            }
            R.id._playerCaptionButton ->
            {
                isEnableCaption = !isEnableCaption
                enableCaptionView(isEnableCaption)
                enableCaptionAnimation(isEnableCaption)
                adjustBottomControlLayout(isEnableCaption)
                adjustPageByPageLayout(isEnableCaption)
                factoryViewModel.onClickCaptionButton(isEnableCaption)
                startMenuGoneTimer()
            }
            R.id._playerCloseButton, R.id._playerEndCloseButton -> factoryViewModel.onCloseButton()
            R.id._playerListButton ->
            {
                Log.f("_playerListButton Click")
                scrollPosition(mCurrentPlayPosition)
                enableMenuAnimation(false)
                mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
                enablePlayListAnimation(true)
            }
            R.id._playListCloseButtonRect ->
            {
                Log.f("_playerListClose Click")
                enablePlayListAnimation(false)
                enableMenuAnimation(true)
            }
            R.id._playerSpeedButton, R.id._playerSpeedText ->
            {
                Log.f("_playerSpeedButton Click")
                scrollPosition(mCurrentPlayPosition)
                enableMenuAnimation(false)
                mFadeAnimationController.promptViewStatus(_PlayerPageByPageLayout, false)
                enablePlaySpeedListAnimation(true)
            }
            R.id._playSpeedListCloseButtonRect ->
            {
                Log.f("_playerListClose Click")
                enablePlaySpeedListAnimation(false)
                enableMenuAnimation(true)
            }
            R.id._playerChangePortraitButton ->
            {
                Log.f("button SCREEN_ORIENTATION_SENSOR_PORTRAIT")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
            R.id._playerChangeLandscapeButton ->
            {
                Log.f("button SCREEN_ORIENTATION_SENSOR_LANDSCAPE")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            R.id._replayButtonBoxImage ->
            {
                if(_PlayerEndBaseLayout.visibility == View.VISIBLE)
                {
                    _PlayerEndBaseLayout.clearAnimation()
                    _PlayerEndBaseLayout.visibility = View.GONE
                }
                factoryViewModel.onReplayButton()
            }
            R.id._playerRepeatButton -> factoryViewModel.onRepeatButton()
            R.id._playerPortraitTitleOption -> factoryViewModel.onClickMovieOptionButton()
            R.id._nextButtonBoxImage -> factoryViewModel.onNextMovieButton()
            R.id._ebookButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.EBOOK)
            R.id._quizButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.QUIZ)
            R.id._vocabularyButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.VOCABULARY)
            R.id._translateButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.TRANSLATE)
            R.id._starwordsButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.STARWORDS)
            R.id._crosswordButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.CROSSWORD)
            R.id._flashcardButtonImage -> factoryViewModel.onClickItemOption(ActionContentsType.FLASHCARD)
            R.id._player1PageButton -> factoryViewModel.onPageByPageIndex((view.tag as Int))
            R.id._player2PageButton -> factoryViewModel.onPageByPageIndex((view.tag as Int))
            R.id._player3PageButton -> factoryViewModel.onPageByPageIndex((view.tag as Int))
            R.id._player4PageButton -> factoryViewModel.onPageByPageIndex((view.tag as Int))
            R.id._player5PageButton -> factoryViewModel.onPageByPageIndex((view.tag as Int))
            R.id._playerPrevPageButton -> factoryViewModel.onMovePrevPage(_PageButtonList[0].tag as Int)
            R.id._playerNextPageButton -> factoryViewModel.onMoveNextPage(_PageButtonList[4].tag as Int)
        }
    }

    private fun forceMenuGone()
    {
        if(isPlayListVisible && (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE))
        {
            return
        }
        else if(isPlaySpeedListVisible && (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE))
        {
            return
        }
        else
        {
            if(isMenuVisible)
            {
                Log.f("Menu FADE OUT")
                enableMenuAnimation(false)
                enablePageByPageAnimation(true)
                enableBackgroudAnimation(false)
            }
        }
    }

    private val mDisplayTouchListener : OnTouchListener = object : OnTouchListener
    {
        override fun onTouch(v : View, event : MotionEvent) : Boolean
        {
            if(_ProgressWheelLayout!!.visibility == View.VISIBLE)
            {
                return false
            }
            if(mCurrentLayoutMode == LAYOUT_TYPE.END)
            {
                return false
            }
            if((mFadeAnimationController!!.isAnimationing(_PlayerTopBaseLayout) || isBottomLayoutAnimationing || mFadeAnimationController!!.isAnimationing(_PlayerPlayButtonLayout) || mFadeAnimationController!!.isAnimationing(_PlayerListBaseLayout) || mFadeAnimationController!!.isAnimationing(_PlayerSpeedListBaseLayout)))
            {
                return false
            }
            if(isPlayListVisible && (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE))
            {
                Log.f("PlayList FADE OUT")
                enablePlayListAnimation(false)
                enableMenuAnimation(true)
            }
            else if(isPlaySpeedListVisible && (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE))
            {
                Log.f("PlayList FADE OUT")
                enablePlaySpeedListAnimation(false)
                enableMenuAnimation(true)
            }
            else
            {
                if(isMenuVisible)
                {
                    Log.f("Menu FADE OUT")
                    enableMenuAnimation(false)
                    enablePageByPageAnimation(true)
                    enableBackgroudAnimation(false)
                }
                else
                {
                    Log.f("Menu FADE IN")
                    enableMenuAnimation(true)
                    enablePageByPageAnimation(false)
                    enableBackgroudAnimation(true)
                }
            }
            return true
        }
    }

    private val mOnSeekBarListener : OnSeekBarChangeListener = object : OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar : SeekBar, progress : Int, fromUser : Boolean)
        {
            mCurrentSeekProgress = progress
        }

        override fun onStartTrackingTouch(seekBar : SeekBar)
        {
            factoryViewModel.onStartTrackingSeek()
            removeMenuGoneTimer()
        }

        override fun onStopTrackingTouch(seekBar : SeekBar)
        {
            factoryViewModel.onStopTrackingSeek(seekBar.progress)
            startMenuGoneTimer()
        }
    }

    override fun onOrientationChanged(newOrientation : Int)
    {
        /**
         * 사용자가 자동 로테이션을 선택했을 시 에만 회전을 적용을 한다.
         */
        if(Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
        {
            if(mCurrentOrientation != newOrientation)
            {
                Log.f("newOrientation : $newOrientation")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            }
        }
    }

    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickItem(type : ActionContentsType)
        {
            factoryViewModel.onClickItemOption(type)
        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            factoryViewModel.onDialogAddBookshelfClick(index)
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            factoryViewModel.onDialogClick(eventType)
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            factoryViewModel.onDialogChoiceClick(buttonType, eventType)
        }
    }

}