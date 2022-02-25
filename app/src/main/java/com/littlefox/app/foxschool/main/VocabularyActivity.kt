package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.vocabulary.VocabularySelectData
import com.littlefox.app.foxschool.adapter.VocabularyItemListAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.VocabularyContract
import com.littlefox.app.foxschool.main.presenter.VocabularyPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.layoutmanager.LinearLayoutScrollerManager
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 단어장 화면
 */
class VocabularyActivity : BaseActivity(), VocabularyContract.View, MessageHandlerCallback
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._checkAllIcon)
    lateinit var _CheckAllIcon : ImageView

    @BindView(R.id._checkAllText)
    lateinit var _CheckAllText : TextView

    @BindView(R.id._checkWordIcon)
    lateinit var _CheckWordIcon : ImageView

    @BindView(R.id._checkWordText)
    lateinit var _CheckWordText : TextView

    @BindView(R.id._checkMeaningIcon)
    lateinit var _CheckMeaningIcon : ImageView

    @BindView(R.id._checkMeaningText)
    lateinit var _CheckMeaningText : TextView

    @BindView(R.id._checkExampleIcon)
    lateinit var _CheckExampleIcon : ImageView

    @BindView(R.id._checkExampleText)
    lateinit var _CheckExampleText : TextView

    @BindView(R.id._wordItemList)
    lateinit var _WordItemList : RecyclerView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : ScalableLayout

    @BindView(R.id._bottomControlLayout)
    lateinit var _BottomControlLayout : ScalableLayout

    @Nullable
    @BindView(R.id._menuBarBackground)
    lateinit var _MenuBarBackground : ImageView

    @BindView(R.id._bottomIntervalIcon)
    lateinit var _BottomIntervalIcon : ImageView

    @BindView(R.id._bottomIntervalText)
    lateinit var _BottomIntervalText : TextView

    @BindView(R.id._bottomSelectIcon)
    lateinit var _BottomSelectIcon : ImageView

    @BindView(R.id._bottomSelectText)
    lateinit var _BottomSelectText : TextView

    @BindView(R.id._bottomSelectCountText)
    lateinit var _BottomSelectCountText : TextView

    @BindView(R.id._bottomPlayIcon)
    lateinit var _BottomPlayIcon : ImageView

    @BindView(R.id._bottomPlayText)
    lateinit var _BottomPlayText : TextView

    @BindView(R.id._bottomWordsActionIcon)
    lateinit var _BottomWordsActionIcon : ImageView

    @BindView(R.id._bottomWordsActionText)
    lateinit var _BottomWordsActionText : TextView

    @Nullable
    @BindView(R.id._bottomFlashCardActionIcon)
    lateinit var _BottomFlashCardActionIcon : ImageView

    @Nullable
    @BindView(R.id._bottomFlashCardActionText)
    lateinit var _BottomFlashCardActionText : TextView

    @Nullable
    @BindView(R.id._lineImage1)
    lateinit var _LineImage1 : ImageView

    @Nullable
    @BindView(R.id._lineImage2)
    lateinit var _LineImage2 : ImageView

    @Nullable
    @BindView(R.id._lineImage3)
    lateinit var _LineImage3 : ImageView

    @Nullable
    @BindView(R.id._lineImage4)
    lateinit var _LineImage4 : ImageView

    companion object
    {
        private var MARGIN_TOP_TABLET_ITEM_COUNT : Int = 0
        private var MARGIN_LEFT_PHONE_ITEM_COUNT : Int = 0
    }

    private lateinit var mVocabularyPresenter : VocabularyPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    private var isItemSelected : Boolean = false
    private var isScrollingDisable : Boolean = false
    private var mVocabularyType : VocabularyType? = null
    private var mAnimationController : LayoutAnimationController? = null

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_vocabulary_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_vocabulary)
        }
        ButterKnife.bind(this)
        mVocabularyPresenter = VocabularyPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mVocabularyPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mVocabularyPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mVocabularyPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mVocabularyPresenter.activityResult(requestCode, resultCode, data)
    }
    /** LifeCycle end **/

    /** Init **/
    override fun initView()
    {
        settingLayoutColor()
        _BackButton.visibility = View.VISIBLE
        _BackButtonRect.visibility = View.VISIBLE
        _WordItemList.setLayoutManager(LinearLayoutScrollerManager(this))
        _WordItemList.addOnLayoutChangeListener(View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            mVocabularyPresenter.onListLayoutChangedComplete()
        })

        _WordItemList.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(v : View, event : MotionEvent) : Boolean
            {
                return isScrollingDisable
            }
        })

        if(CommonUtils.getInstance(this).checkTablet)
        {
            val TABLET_LIST_WIDTH = 960
            val params : LinearLayout.LayoutParams = (_WordItemList.getLayoutParams() as LinearLayout.LayoutParams).apply {
                width = CommonUtils.getInstance(baseContext).getPixel(TABLET_LIST_WIDTH)
                gravity = Gravity.CENTER_HORIZONTAL
            }
            _WordItemList.setLayoutParams(params)
        }
    }

    override fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(this).getTypefaceBold())
        _CheckAllText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _CheckWordText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _CheckMeaningText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _CheckExampleText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _BottomIntervalText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _BottomPlayText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _BottomSelectText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _BottomWordsActionText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _BottomFlashCardActionText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _BottomSelectCountText.setTypeface(Font.getInstance(this).getTypefaceMedium())
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

    /**
     * 타이틀 설정
     */
    override fun setTitle(title : String)
    {
        _TitleText.setText(title)
    }

    /**
     * 컨트롤 바 레이아웃 설정 (스마트폰 용)
     * - 플래시카드 숨김
     */
    private fun setBottomControllerPhoneLayout()
    {
        Log.f("")
        if(mVocabularyType === VocabularyType.VOCABULARY_CONTENTS)
        {
            _BottomControlLayout.run {
                setScaleSize(1080f, 176f)
                moveChildView(_BottomIntervalIcon, 30f, 0f, 210f, 90f)
                moveChildView(_BottomIntervalText, 0f, 90f, 270f, 86f)
                moveChildView(_BottomSelectIcon, 300f, 0f, 210f, 90f)
                moveChildView(_BottomSelectText, 270f, 90f, 270f, 86f)
                moveChildView(_BottomPlayIcon, 570f, 0f, 210f, 90f)
                moveChildView(_BottomSelectCountText, 680f, 10f, 30f, 30f)
                moveChildView(_BottomPlayText, 540f, 90f, 270f, 86f)
                moveChildView(_BottomWordsActionIcon, 840f, 0f, 210f, 90f)
                moveChildView(_BottomWordsActionText, 810f, 90f, 270f, 86f)
            }
            _BottomFlashCardActionIcon.visibility = View.GONE
            _BottomFlashCardActionText.visibility = View.GONE
        }
    }

    /**
     * 컨트롤 바 레이아웃 설정 (태블릿 용)
     * - 플래시카드 숨김
     */
    private fun setBottomControllerTabletLayout()
    {
        Log.f("")
        if(mVocabularyType === VocabularyType.VOCABULARY_CONTENTS)
        {
            _BottomControlLayout.run {
                setScaleSize(1920f, 787f)
                moveChildView(_MenuBarBackground, 1495f, 193f, 138f, 594f)
                moveChildView(_BottomIntervalIcon, 1495f, 200f, 138f, 94f)
                moveChildView(_BottomIntervalText, 1495f, 294f, 138f, 50f)
                moveChildView(_LineImage1, 1495f, 344f, 138f, 2f)
                moveChildView(_BottomSelectIcon, 1495f, 346f, 138f, 94f)
                moveChildView(_BottomSelectText, 1495f, 440f, 138f, 50f)
                moveChildView(_LineImage2, 1495f, 490f, 138f, 2f)
                moveChildView(_BottomPlayIcon, 1495f, 492f, 138f, 94f)
                moveChildView(_BottomSelectCountText, 1495f, 512f, 30f, 30f)
                moveChildView(_BottomPlayText, 1495f, 586f, 138f, 50f)
                moveChildView(_LineImage3, 1495f, 636f, 138f, 2f)
                moveChildView(_BottomWordsActionIcon, 1495f, 638f, 138f, 94f)
                moveChildView(_BottomWordsActionText, 1495f, 732f, 138f, 50f)
            }

            _BottomFlashCardActionIcon.visibility = View.GONE
            _BottomFlashCardActionText.visibility = View.GONE
            _LineImage4.visibility = View.GONE
        }
    }

    override fun setBottomWordsActionType(type : VocabularyType)
    {
        mVocabularyType = type
        if(type === VocabularyType.VOCABULARY_CONTENTS)
        {
            // 컨텐츠 단어장
            if(CommonUtils.getInstance(this).checkTablet)
            {
                setBottomControllerTabletLayout()
            }
            else
            {
                setBottomControllerPhoneLayout()
            }
            MARGIN_TOP_TABLET_ITEM_COUNT = 512
            MARGIN_LEFT_PHONE_ITEM_COUNT = 680
            _BottomWordsActionIcon.setImageResource(if(CommonUtils.getInstance(this).checkTablet) R.drawable.tablet_voca else R.drawable.bottom_voca)
            _BottomWordsActionText.setText(resources.getString(R.string.text_add_vocabulary))
        }
        else if(type === VocabularyType.VOCABULARY_SHELF)
        {
            // 사용자 단어장
            MARGIN_TOP_TABLET_ITEM_COUNT = 497
            MARGIN_LEFT_PHONE_ITEM_COUNT = 548
            _BottomWordsActionIcon.setImageResource(if(CommonUtils.getInstance(this).checkTablet) R.drawable.tablet_delete else R.drawable.bottom_delete)
            _BottomWordsActionText.setText(resources.getString(R.string.text_delete))
        }
    }

    override fun setBottomIntervalValue(interval : Int)
    {
        if(interval == 0)
        {
            _BottomIntervalText.setText(resources.getString(R.string.text_not_have_interval))
        }
        else
        {
            _BottomIntervalText.setText(java.lang.String.format(resources.getString(R.string.text_sec_interval), interval))
        }
    }

    /**
     * 아이템 선택한 갯수에 따른 뷰 세팅
     */
    override fun setBottomPlayItemCount(count : Int)
    {
        Log.f("count : $count")
        val isTablet = CommonUtils.getInstance(this).checkTablet
        if(count == 0)
        {
            _BottomSelectCountText.visibility = View.GONE
            setBottomSelectStatusIcon(false)
            return
        }
        else
        {
            setBottomSelectStatusIcon(true)
            _BottomSelectCountText.visibility = View.VISIBLE
        }

        if(count < 10)
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_1)
            _BottomControlLayout.moveChildView(
                _BottomSelectCountText,
                if(isTablet) 1562f else MARGIN_LEFT_PHONE_ITEM_COUNT.toFloat(),
                if(isTablet) MARGIN_TOP_TABLET_ITEM_COUNT.toFloat() else 10f,
                if(isTablet) 30f else 40f,
                if(isTablet) 30f else 40f
            )
        }
        else if(count < 100)
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_2)
            _BottomControlLayout.moveChildView(
                _BottomSelectCountText,
                if(isTablet) 1562f else MARGIN_LEFT_PHONE_ITEM_COUNT.toFloat(),
                if(isTablet) MARGIN_TOP_TABLET_ITEM_COUNT.toFloat() else 10f,
                if(isTablet) 40f else 50f,
                if(isTablet) 30f else 40f
            )
        }
        else
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_3)
            _BottomControlLayout.moveChildView(
                _BottomSelectCountText,
                if(isTablet) 1562f else MARGIN_LEFT_PHONE_ITEM_COUNT.toFloat(),
                if(isTablet) MARGIN_TOP_TABLET_ITEM_COUNT.toFloat() else 10f,
                if(isTablet) 50f else 60f,
                if(isTablet) 30f else 40f
            )
        }
        _BottomSelectCountText.setText(count.toString())
    }

    /**
     * 리스트뷰
     */
    override fun showListView(adapter : VocabularyItemListAdapter)
    {
        Log.f("")
        mAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _WordItemList.setLayoutAnimation(mAnimationController)
        _WordItemList.setAdapter(adapter)
    }

    /**
     * 상단 체크 아이콘 상태 변경
     * - 전체, 단어, 뜻, 예문
     */
    override fun checkIconStatusMenu(vocabularySelectData : VocabularySelectData)
    {
        if(vocabularySelectData.isSelectAll())
        {
            Log.f("Check ALL")
            _CheckAllIcon.setImageResource(R.drawable.check_on)
            _CheckWordIcon.setImageResource(R.drawable.check_on)
            _CheckMeaningIcon.setImageResource(R.drawable.check_on)
            _CheckExampleIcon.setImageResource(R.drawable.check_on)
        }
        else
        {
            Log.f("Check word: " + vocabularySelectData.isSelectedWord())
            Log.f("Check meaning: " + vocabularySelectData.isSelectedMeaning())
            Log.f("Check example: " + vocabularySelectData.isSelectedExample())
            _CheckAllIcon.setImageResource(R.drawable.check_off)
            _CheckWordIcon.setImageResource(if(vocabularySelectData.isSelectedWord()) R.drawable.check_on else R.drawable.check_off)
            _CheckMeaningIcon.setImageResource(if(vocabularySelectData.isSelectedMeaning()) R.drawable.check_on else R.drawable.check_off)
            _CheckExampleIcon.setImageResource(if(vocabularySelectData.isSelectedExample()) R.drawable.check_on else R.drawable.check_off)
        }
    }

    /**
     * 단어장 상태 설정 : 재생
     */
    override fun setBottomPlayStatus()
    {
        enableMenu(false)
        isScrollingDisable = true
        _BottomSelectCountText.visibility = View.INVISIBLE

        val playIcon : Int
        if(CommonUtils.getInstance(this).checkTablet)
        {
            playIcon = R.drawable.tablet_stop
        }
        else
        {
            playIcon = R.drawable.bottom_stop
        }
        _BottomPlayIcon.setImageResource(playIcon)
        _BottomPlayText.setText(resources.getString(R.string.text_stop_play))
    }

    /**
     * 단어장 상태 설정 : 정지
     */
    override fun setBottomStopStatus()
    {
        enableMenu(true)
        isScrollingDisable = false
        _BottomSelectCountText.visibility = View.VISIBLE

        val playIcon : Int
        if(CommonUtils.getInstance(this).checkTablet)
        {
            playIcon = R.drawable.tablet_play
        }
        else
        {
            playIcon = R.drawable.bottom_play
        }
        _BottomPlayIcon.setImageResource(playIcon)
        _BottomPlayText.setText(resources.getString(R.string.text_select_play))
    }

    /**
     * 스크롤
     * - 단어 자동재생 했을 때 선택된 포지션 바뀌는 동작
     */
    override fun scrollPosition(position : Int)
    {
        Log.f("position : $position")
        if(position == 0)
        {
            _WordItemList.scrollToPosition(0)
        }
        else
        {
            _WordItemList.smoothScrollToPosition(position)
        }
    }

    /**
     * 선택 아이콘 상태 변경
     * - 전체 선택 / 선택 해제
     */
    private fun setBottomSelectStatusIcon(isItemSelected : Boolean)
    {
        Log.f("isItemSelected : $isItemSelected")
        this.isItemSelected = isItemSelected
        if(isItemSelected)
        {
            _BottomSelectText.setText(resources.getString(R.string.text_select_init))
            _BottomSelectIcon.setImageResource(if(CommonUtils.getInstance(this).checkTablet) R.drawable.tablet_close else R.drawable.bottom_close)
        }
        else
        {
            _BottomSelectText.setText(resources.getString(R.string.text_select_all))
            _BottomSelectIcon.setImageResource(if(CommonUtils.getInstance(this).checkTablet) R.drawable.tablet_all else R.drawable.bottom_all)
        }
    }

    /**
     * 메뉴 활성/비활성
     */
    private fun enableMenu(isEnable : Boolean)
    {
        if(isEnable)
        {
            _BottomSelectText.alpha = 1.0f
            _BottomSelectIcon.alpha = 1.0f
            _BottomWordsActionIcon.alpha = 1.0f
            _BottomWordsActionText.alpha = 1.0f
            _BottomIntervalText.alpha = 1.0f
            _BottomIntervalIcon.alpha = 1.0f
            _BottomFlashCardActionIcon.alpha = 1.0f
            _BottomFlashCardActionText.alpha = 1.0f
        }
        else
        {
            _BottomSelectText.alpha = 0.5f
            _BottomSelectIcon.alpha = 0.5f
            _BottomWordsActionIcon.alpha = 0.5f
            _BottomWordsActionText.alpha = 0.5f
            _BottomIntervalText.alpha = 0.5f
            _BottomIntervalIcon.alpha = 0.5f
            _BottomFlashCardActionIcon.alpha = 0.5f
            _BottomFlashCardActionText.alpha = 0.5f
        }
        _BottomSelectText.isClickable = isEnable
        _BottomSelectIcon.isClickable = isEnable
        _BottomWordsActionText.isClickable = isEnable
        _BottomWordsActionIcon.isClickable = isEnable
        _BottomIntervalText.isClickable = isEnable
        _BottomIntervalIcon.isClickable = isEnable
        _BottomFlashCardActionText.isClickable = isEnable
        _BottomFlashCardActionIcon.isClickable = isEnable
        _CheckAllText.isClickable = isEnable
        _CheckAllIcon.isClickable = isEnable
        _CheckWordText.isClickable= isEnable
        _CheckWordIcon.isClickable = isEnable
        _CheckMeaningText.isClickable= isEnable
        _CheckMeaningIcon.isClickable = isEnable

        _CheckExampleText.isClickable= isEnable
        _CheckExampleIcon.isClickable = isEnable
    }

    override fun showContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE)
        _WordItemList.setVisibility(View.GONE)
    }

    override fun hideContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE)
        _WordItemList.setVisibility(View.VISIBLE)
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
        mVocabularyPresenter.sendMessageEvent(message)
    }

    @OnClick(R.id._backButtonRect, R.id._checkAllIcon, R.id._checkAllText, R.id._checkWordIcon, R.id._checkWordText, R.id._checkMeaningIcon, R.id._checkMeaningText,
        R.id._checkExampleIcon, R.id._checkExampleText, R.id._bottomIntervalIcon, R.id._bottomIntervalText, R.id._bottomSelectIcon, R.id._bottomSelectText,
        R.id._bottomPlayIcon, R.id._bottomPlayText, R.id._bottomWordsActionIcon, R.id._bottomWordsActionText, R.id._bottomFlashCardActionIcon, R.id._bottomFlashCardActionText)
    fun onClickView(view : View)
    {
        if(_LoadingProgressLayout.visibility == View.VISIBLE) return

        Log.f("view.getId() : " + view.id)
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()

            // 상단 필터링 - 전체
            R.id._checkAllIcon, R.id._checkAllText -> mVocabularyPresenter.onClickMenuSelectAll()

            // 상단 필터링 - 단어
            R.id._checkWordIcon, R.id._checkWordText -> mVocabularyPresenter.onClickMenuWord()

            // 상단 필터링 - 뜻
            R.id._checkMeaningIcon, R.id._checkMeaningText -> mVocabularyPresenter.onClickMenuMeaning()

            // 상단 필터링 - 예문
            R.id._checkExampleIcon, R.id._checkExampleText -> mVocabularyPresenter.onClickMenuExample()

            // 간격
            R.id._bottomIntervalIcon, R.id._bottomIntervalText -> mVocabularyPresenter.onClickBottomInterval()

            // 선택재생
            R.id._bottomPlayIcon, R.id._bottomPlayText -> mVocabularyPresenter.onClickBottomPlayAction()

            // 플래시카드
            R.id._bottomFlashCardActionIcon, R.id._bottomFlashCardActionText -> mVocabularyPresenter.onClickBottomFlashcard()

            // 단어장 추가
            R.id._bottomWordsActionIcon, R.id._bottomWordsActionText ->
            {
                if(mVocabularyType === VocabularyType.VOCABULARY_CONTENTS)
                {
                    mVocabularyPresenter.onClickBottomPutInVocabularyShelf()
                }
                else
                {
                    mVocabularyPresenter.onClickBottomDeleteInVocabularyShelf()
                }
            }

            // 전체선택
            R.id._bottomSelectIcon, R.id._bottomSelectText ->
            {
                if(isItemSelected)
                {
                    mVocabularyPresenter.onClickBottomRemoveAll()
                }
                else
                {
                    mVocabularyPresenter.onClickBottomSelectAll()
                }
            }
        }
    }
}