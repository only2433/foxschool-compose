package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesCharacterResult
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesCreatorsResult
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.IntroduceSeriesContract
import com.littlefox.app.foxschool.main.presenter.IntroduceSeriesPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class IntroduceSeriesActivity : BaseActivity(), MessageHandlerCallback, IntroduceSeriesContract.View
{
    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._introduceInformationScrollView)
    lateinit var _IntroduceInformationScrollView : ScrollView

    @BindView(R.id._introduceBaseLayout)
    lateinit var _IntroduceBaseLayout : LinearLayout

    @BindView(R.id._introductionTitleLayout)
    lateinit var _IntroductionTitleLayout : ScalableLayout

    @BindView(R.id._introductionContentsBackground)
    lateinit var _IntroductionContentsBackground : ImageView

    @BindView(R.id._introduceInformationText)
    lateinit var _IntroduceInformationText : TextView

    @BindView(R.id._introductionTitleText)
    lateinit var _IntroductionTitleText : TextView

    @BindView(R.id._introductionContentsText)
    lateinit var _IntroductionContentsText : TextView

    @BindView(R.id._introductionContentsImage)
    lateinit var _IntroductionContentsImage : ImageView

    @BindView(R.id._introductionContentsPlayButton)
    lateinit var _IntroductionContentsPlayButton : ImageView

    private lateinit var mIntroduceSeriesPresenter: IntroduceSeriesPresenter
    private var mLoadingDialog : MaterialLoadingDialog? = null

    private var DISPLAY_WIDTH : Int                           = 1080  // 디스플레이 가로 사이즈
    private var LAYOUT_CONTENTS_VIEW_WIDTH : Int              = 1020  // 컨텐츠박스 가로 사이즈 (하얀색)
    private var LAYOUT_TITLE_VIEW_HEIGHT : Int                = 121   // 컨텐츠 타이틀박스 높이 (파란색)
    private var LAYOUT_CONTENTS_VIEW_MARGIN_LEFT : Int        = 30    // 컨텐츠박스 외부 왼쪽 여백 (하얀색)
    private var LAYOUT_VIEW_PADDING : Int                     = 44    // 컨텐츠박스 내부 여백 (하얀색)
    private var LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT : Int     = 32    // 컨텐츠박스 외부 하단 여백 (하얀색)

    private var CHARACTER_THUMBNAIL_IMAGE_WIDTH : Int         = 282   // 캐릭터 썸네일 가로 사이즈
    private var CHARACTER_THUMBNAIL_IMAGE_HEIGHT : Int        = 282   // 캐릭터 썸네일 높이
    private var CHARACTER_THUMBNAIL_TITLE_WIDTH : Int         = 282   // 캐릭터 타이틀박스 가로 사이즈 (파란색)
    private var CHARACTER_THUMBNAIL_TITLE_HEIGHT : Int        = 80    // 캐릭터 타이틀박스 높이 (파란색)

    private var CREATOR_TEXTVIEW_HEIGHT : Int                 = 55    // 제작자 텍스트 높이
    private var CREATOR_TEXTVIEW_TITLE_MARGIN_TERM : Int      = 20    // 제작자 텍스트 간격

    private var DIVIDE_LINE_HEIGHT : Int                     = 2     // 구분선 두께
    private var DIVIDE_TITLE_MARGIN_TERM : Int               = 40    // 구분선 위아래 여백

    private var MAX_INTRODUCTION_CONTENTS_SIZE_TABLET : Int   = 6     // 텍스트 사이즈 (태블릿)
    private var MAX_INTRODUCTION_CONTENTS_SIZE_PHONE : Int    = 8     // 텍스트 사이즈 (폰)
    private var TITLE_TEXT_SIZE : Int = 0

    private val CREATORS_TYPE_NAME = arrayOf("Story", "Animation", "Cast")

    private var mContentsBaseLayoutHeight : Int = -1
    private var mContentsBackgroundHeight : Int= -1
    private var mCurrentViewMarginTop : Int= -1

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_introduce_series_tablet)
        } else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_introduce_series)
        }

        ButterKnife.bind(this)
        mIntroduceSeriesPresenter = IntroduceSeriesPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mIntroduceSeriesPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mIntroduceSeriesPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mIntroduceSeriesPresenter.destroy()
    }

    override fun initView()
    {
        initViewSize()
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.title_introduce)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _IntroduceInformationText.typeface = Font.getInstance(this).getRobotoRegular()
        _IntroductionTitleText.typeface = Font.getInstance(this).getRobotoMedium()
        _IntroductionContentsText.typeface = Font.getInstance(this).getRobotoRegular()
    }

    private fun initViewSize()
    {
        if(CommonUtils.getInstance(this).checkTablet)
        {
            // 태블릿 사이즈 설정
            DISPLAY_WIDTH = 1920
            LAYOUT_CONTENTS_VIEW_WIDTH = 960
            LAYOUT_TITLE_VIEW_HEIGHT = 92
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT = 480
            LAYOUT_VIEW_PADDING = 42
            LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT = 28
            CHARACTER_THUMBNAIL_IMAGE_WIDTH = 265
            CHARACTER_THUMBNAIL_IMAGE_HEIGHT = 265
            CHARACTER_THUMBNAIL_TITLE_WIDTH = 265
            CHARACTER_THUMBNAIL_TITLE_HEIGHT = 80
            CREATOR_TEXTVIEW_HEIGHT = 55
            CREATOR_TEXTVIEW_TITLE_MARGIN_TERM = 20
            DIVIDE_LINE_HEIGHT = 2
            DIVIDE_TITLE_MARGIN_TERM = 38
            TITLE_TEXT_SIZE = 32
        }
        else
        {
            // 폰 사이즈 설정
            DISPLAY_WIDTH = 1080
            LAYOUT_CONTENTS_VIEW_WIDTH = 1020
            LAYOUT_TITLE_VIEW_HEIGHT = 121
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT = 30
            LAYOUT_VIEW_PADDING = 44
            LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT = 32
            CHARACTER_THUMBNAIL_IMAGE_WIDTH = 282
            CHARACTER_THUMBNAIL_IMAGE_HEIGHT = 282
            CHARACTER_THUMBNAIL_TITLE_WIDTH = 282
            CHARACTER_THUMBNAIL_TITLE_HEIGHT = 80
            CREATOR_TEXTVIEW_HEIGHT = 55
            CREATOR_TEXTVIEW_TITLE_MARGIN_TERM = 20
            DIVIDE_LINE_HEIGHT = 2
            DIVIDE_TITLE_MARGIN_TERM = 40
            TITLE_TEXT_SIZE = 46
        }
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

    override fun onBackPressed()
    {
        super.onBackPressed()
    }

    /**
     * Introduction 영역 스크롤 활성화
     */
    private fun enableIntroductionContentScroll()
    {
        _IntroductionContentsText.movementMethod = ScrollingMovementMethod()
        _IntroductionContentsText.setOnTouchListener {v, event ->
            _IntroductionContentsText.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    /**
     * Introduction 영역에 표시될 영상이 있는지 체크
     */
    private fun checkSupportVideo(result : IntroduceSeriesInformationResult)
    {
        if(!Locale.getDefault().toString().contains(Locale.KOREA.toString()) || result.getIntroduceVideoMp4() == "")
        {
            _IntroductionContentsPlayButton.visibility = View.GONE
            _IntroductionContentsImage.visibility = View.GONE
        }
        else
        {
            _IntroductionContentsPlayButton.visibility = View.VISIBLE
            _IntroductionContentsImage.visibility = View.VISIBLE
        }
    }

    /**
     * Introduction 영역 레이아웃 설정
     */
    private fun createIntroductionLayout(result : IntroduceSeriesInformationResult)
    {
        val BASE_LAYOUT_HEIGHT = if(CommonUtils.getInstance(this).checkTablet) 1055 else 1330       // 베이스 영역 높이 (회색포함)
        val BASE_BACKGROUND_HEIGHT = if(CommonUtils.getInstance(this).checkTablet) 905 else 1140    // 컨텐츠박스 높이 (하얀색)
        val INFORMATION_TEXTVIEW_TOP = if(CommonUtils.getInstance(this).checkTablet) 668 else 750   // 키워드 영역 위쪽 여백백
        val CONTENTS_TEXTVIEW_TOP = if(CommonUtils.getInstance(this).checkTablet) 756 else 850      // 컨텐츠 영역 위쪽 여백

        Log.f("result.getIntroduceThumbnail() : ${result.getIntroduceThumbnail()}, Locale.getDefault().toString() : ${Locale.getDefault().toString()}")
        if(result.getIntroduceThumbnail() != "" && Locale.getDefault().toString().contains(Locale.KOREA.toString()))
        {
            // 조건 : 한국이용자 이면서 Introduction 영상이 있는 경우
            if(CommonUtils.getInstance(this).checkTablet)
            {
                // 태블릿 레이아웃 설정
                _IntroductionTitleLayout.setScaleSize(
                    DISPLAY_WIDTH.toFloat(),
                    BASE_LAYOUT_HEIGHT.toFloat()
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroductionContentsBackground,
                    480f,
                    121f,
                    LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
                    BASE_BACKGROUND_HEIGHT.toFloat()
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroduceInformationText,
                    521f,
                    INFORMATION_TEXTVIEW_TOP.toFloat(),
                    884f,
                    88f
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroductionContentsText,
                    521f,
                    CONTENTS_TEXTVIEW_TOP.toFloat(),
                    884f,
                    243f
                )
            }
            else
            {
                // 폰 레이아웃 설정
                _IntroductionTitleLayout.setScaleSize(
                    DISPLAY_WIDTH.toFloat(),
                    BASE_LAYOUT_HEIGHT.toFloat()
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroductionContentsBackground,
                    30f,
                    150f,
                    LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
                    BASE_BACKGROUND_HEIGHT.toFloat()
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroduceInformationText,
                    74f,
                    INFORMATION_TEXTVIEW_TOP.toFloat(),
                    939f,
                    100f
                )
                _IntroductionTitleLayout.moveChildView(
                    _IntroductionContentsText,
                    74f,
                    CONTENTS_TEXTVIEW_TOP.toFloat(),
                    939f,
                    431f
                )
            }
        }
        Log.f("result.getIntroduceThumbnail() : " + result.getIntroduceThumbnail())

        // ===== 키워드 데이터  조합 =====
        var categoryData = ""
        if(result.getCategories() != "")
        {
            categoryData = " | " + result.getCategories().replace("|", " | ")
        }
        Log.i("categoryData : $categoryData")

        val levelData = String.format(resources.getString(R.string.text_count_level), result.getLevel())
        var textData = ""
        if(result.isSingleSeries)
        {
            textData = String.format(resources.getString(R.string.text_count_stories, java.lang.String.valueOf(result.getCurrentReleaseCount())) + categoryData)
        }
        else
        {
            textData = String.format(resources.getString(R.string.text_count_series_stories, java.lang.String.valueOf(result.getCurrentReleaseCount())) + categoryData)
        }
        val informationTextData = "$levelData | $textData"
        // ===== 키워드 데이터 조합 =====

        _IntroduceInformationText.text = informationTextData
        _IntroductionContentsText.text = result.getIntroduction()

        _IntroductionContentsText.post {
            // 컨텐츠영역 라인수 최대값 넘어가면 스크롤 활성화
            if(CommonUtils.getInstance(this).checkTablet)
            {
                Log.f("태블릿 _IntroductionContentsText.getLineCount() : " + _IntroductionContentsText.lineCount)
                if(_IntroductionContentsText.lineCount > MAX_INTRODUCTION_CONTENTS_SIZE_TABLET)
                {
                    enableIntroductionContentScroll()
                }
            }
            else
            {
                Log.f("폰 _IntroductionContentsText.getLineCount() : " + _IntroductionContentsText.lineCount)
                if(_IntroductionContentsText.lineCount > MAX_INTRODUCTION_CONTENTS_SIZE_PHONE)
                {
                    enableIntroductionContentScroll()
                }
            }
        }

        // Introduction 영상 세팅
        Glide.with(this)
            .load(result.getIntroduceThumbnail())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(_IntroductionContentsImage)

        _IntroductionTitleText.setPadding(
            CommonUtils.getInstance(this).getPixel(LAYOUT_VIEW_PADDING),
            0,
            0,
            0
        )
    }

    /**
     * Character 영역 레이아웃 설정
     */
    private fun createCharacterThumbnailLayout(characterList : ArrayList<IntroduceSeriesCharacterResult>)
    {
        val MAX_ROW_COUNT = 3                                   // 표시 개수 (가로 최대 3개)
        val BASE_ROW_MARGIN_TOP = 44                            // 윗쪽 여백
        val COLUMN_SPACING = 22                                 // 줄간격
        val ROW_SPACING = if(CommonUtils.getInstance(this).checkTablet) 35 else 42      // 가로 여백
        val NAME_TEXT_SIZE = if(CommonUtils.getInstance(this).checkTablet) 28 else 30   // 글씨 크기 (캐릭터 이름)
        var maxColumnCount = characterList.size / 3             // 줄 수 계산
        if(characterList.size % 3 > 0)
        {
            maxColumnCount += 1
        }

        // 위치
        var currentRowIndex = 0
        var currentColumnIndex = 0

        // ===== 타이틀 영역 생성 =====
        val titleLayout = ScalableLayout(this)
        titleLayout.setScaleSize(
            DISPLAY_WIDTH.toFloat(),
            LAYOUT_TITLE_VIEW_HEIGHT.toFloat()
        )

        val titleText = TextView(this)
        titleText.setPadding(
            CommonUtils.getInstance(this).getPixel(LAYOUT_VIEW_PADDING),
            0,
            0,
            0
        )
        titleText.gravity = Gravity.CENTER_VERTICAL
        titleText.background = resources.getDrawable(R.drawable.info_box_b)
        titleText.text = resources.getString(R.string.text_character)
        titleText.setTextColor(resources.getColor(R.color.color_ffffff))
        titleText.typeface = Font.getInstance(this).getRobotoMedium()

        titleLayout.addView(
            titleText,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            0f,
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            LAYOUT_TITLE_VIEW_HEIGHT.toFloat()
        )
        titleLayout.setScale_TextSize(
            titleText,
            TITLE_TEXT_SIZE.toFloat()
        )
        _IntroduceBaseLayout.addView(titleLayout)
        // ===== 타이틀 영역 생성 =====

        // ===== 컨텐츠 영역 생성 =====
        // 컨텐츠박스 높이 계산 (하얀색)
        mContentsBackgroundHeight = (((CHARACTER_THUMBNAIL_IMAGE_HEIGHT + CHARACTER_THUMBNAIL_TITLE_HEIGHT) * maxColumnCount)
                + (COLUMN_SPACING * (maxColumnCount - 1)) + BASE_ROW_MARGIN_TOP * 2)

        // 베이스 영역 높이 (회색포함)
        mContentsBaseLayoutHeight = mContentsBackgroundHeight + LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT

        val baseCharacterLayout = ScalableLayout(this)
        baseCharacterLayout.setScaleSize(
            DISPLAY_WIDTH.toFloat(),
            mContentsBaseLayoutHeight.toFloat()
        )

        val backgroundView = ImageView(this)
        backgroundView.scaleType = ImageView.ScaleType.FIT_XY
        backgroundView.setImageResource(R.drawable.info_box_w)

        baseCharacterLayout.addView(
            backgroundView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            -1f, // -1로 하는 이유 : 박스 이미지 윗줄 제거용
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            mContentsBackgroundHeight.toFloat()
        )
        // ===== 컨텐츠 영역 생성 =====

        // ===== 캐릭터 이미지 생성 =====
        for(i in characterList.indices)
        {
            // 썸네일
            val thumbnail = ImageView(this)
            thumbnail.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(this)
                .load(characterList[i].getImage())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(thumbnail)

            // 이름
            val titleView = TextView(this)
            titleView.gravity = Gravity.CENTER
            titleView.setTextColor(resources.getColor(R.color.color_444444))
            titleView.typeface = Font.getInstance(this).getRobotoBold()
            titleView.text = characterList[i].getName()

            // 표시될 인덱스 계산
            currentColumnIndex = i / MAX_ROW_COUNT
            currentRowIndex = i % MAX_ROW_COUNT
            Log.i("currentColumnIndex : $currentColumnIndex, currentRowIndex : $currentRowIndex")

            if(currentColumnIndex == 0)
            {
                baseCharacterLayout.addView(
                    thumbnail,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (CHARACTER_THUMBNAIL_IMAGE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    BASE_ROW_MARGIN_TOP.toFloat(),
                    CHARACTER_THUMBNAIL_IMAGE_WIDTH.toFloat(),
                    CHARACTER_THUMBNAIL_IMAGE_HEIGHT.toFloat()
                )
                baseCharacterLayout.addView(
                    titleView,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (CHARACTER_THUMBNAIL_TITLE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + CHARACTER_THUMBNAIL_IMAGE_HEIGHT).toFloat(),
                    CHARACTER_THUMBNAIL_TITLE_WIDTH.toFloat(),
                    CHARACTER_THUMBNAIL_TITLE_HEIGHT.toFloat()
                )
                baseCharacterLayout.setScale_TextSize(
                    titleView,
                    NAME_TEXT_SIZE.toFloat()
                )
            }
            else
            {
                baseCharacterLayout.addView(
                    thumbnail,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (CHARACTER_THUMBNAIL_IMAGE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + (CHARACTER_THUMBNAIL_IMAGE_HEIGHT + CHARACTER_THUMBNAIL_TITLE_HEIGHT + COLUMN_SPACING) * currentColumnIndex).toFloat(),
                    CHARACTER_THUMBNAIL_IMAGE_WIDTH.toFloat(),
                    CHARACTER_THUMBNAIL_IMAGE_HEIGHT.toFloat()
                )
                baseCharacterLayout.addView(
                    titleView,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (CHARACTER_THUMBNAIL_TITLE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + CHARACTER_THUMBNAIL_IMAGE_HEIGHT + (CHARACTER_THUMBNAIL_IMAGE_HEIGHT + CHARACTER_THUMBNAIL_TITLE_HEIGHT + COLUMN_SPACING) * currentColumnIndex).toFloat(),
                    CHARACTER_THUMBNAIL_TITLE_WIDTH.toFloat(),
                    CHARACTER_THUMBNAIL_TITLE_HEIGHT.toFloat()
                )
                baseCharacterLayout.setScale_TextSize(
                    titleView,
                    NAME_TEXT_SIZE.toFloat()
                )
            }
        }
        // ===== 캐릭터 이미지 생성 =====
        _IntroduceBaseLayout.addView(baseCharacterLayout)
    }

    /**
     * Creator 영역 레이아웃 설정
     */
    private fun createCreatorLayout(creatorsResult : IntroduceSeriesCreatorsResult)
    {
        // ===== 타이틀 영역 생성 =====
        val titleLayout = ScalableLayout(this)
        titleLayout.setScaleSize(
            DISPLAY_WIDTH.toFloat(),
            LAYOUT_TITLE_VIEW_HEIGHT.toFloat()
        )

        val titleText = TextView(this)
        titleText.setPadding(
            CommonUtils.getInstance(this).getPixel(LAYOUT_VIEW_PADDING),
            0,
            0,
            0
        )
        titleText.gravity = Gravity.CENTER_VERTICAL
        titleText.background = resources.getDrawable(R.drawable.info_box_b)
        titleText.text = resources.getString(R.string.text_creators)
        titleText.setTextColor(resources.getColor(R.color.color_ffffff))
        titleText.typeface = Font.getInstance(this).getRobotoMedium()

        titleLayout.addView(
            titleText,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            0f,
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            LAYOUT_TITLE_VIEW_HEIGHT.toFloat()
        )
        titleLayout.setScale_TextSize(
            titleText,
            TITLE_TEXT_SIZE.toFloat()
        )
        _IntroduceBaseLayout.addView(titleLayout)
        // ===== 타이틀 영역 생성 =====

        // ===== 컨텐츠 영역 생성 =====
        // 컨텐츠박스 높이 계산 (하얀색)
        mContentsBackgroundHeight = (CREATORS_TYPE_NAME.size * CREATOR_TEXTVIEW_HEIGHT +
                CREATORS_TYPE_NAME.size * CREATOR_TEXTVIEW_TITLE_MARGIN_TERM +
                creatorsResult.getStoryList().size * CREATOR_TEXTVIEW_HEIGHT +
                creatorsResult.getAnimationList().size * CREATOR_TEXTVIEW_HEIGHT +
                creatorsResult.getCastList().size * CREATOR_TEXTVIEW_HEIGHT +
                (DIVIDE_TITLE_MARGIN_TERM * 2 + DIVIDE_LINE_HEIGHT) * 2 +
                LAYOUT_VIEW_PADDING * 2)

        // 베이스 영역 높이 (회색포함)
        mContentsBaseLayoutHeight = mContentsBackgroundHeight + LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT

        val contentsLayout = ScalableLayout(this)
        contentsLayout.setScaleSize(
            DISPLAY_WIDTH.toFloat(),
            mContentsBaseLayoutHeight.toFloat()
        )

        val backgroundView = ImageView(this)
        backgroundView.setBackgroundResource(R.drawable.info_box_w)
        contentsLayout.addView(
            backgroundView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            -1f, // -1로 하는 이유 : 박스 이미지 윗줄 제거용
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            mContentsBackgroundHeight.toFloat()
        )
        // ===== 컨텐츠 영역 생성 =====

        mCurrentViewMarginTop = LAYOUT_VIEW_PADDING
        drawCreatorInformationView(
            contentsLayout,
            CREATORS_TYPE_NAME[0],
            creatorsResult.getStoryList()
        )
        drawDivideLineView(contentsLayout)
        drawCreatorInformationView(
            contentsLayout,
            CREATORS_TYPE_NAME[1],
            creatorsResult.getAnimationList()
        )
        drawDivideLineView(contentsLayout)
        drawCreatorInformationView(
            contentsLayout,
            CREATORS_TYPE_NAME[2],
            creatorsResult.getCastList()
        )

        _IntroduceBaseLayout.addView(contentsLayout)
    }

    /**
     * Creator 리스트 그리기
     */
    private fun drawCreatorInformationView(contentsLayout : ScalableLayout, title : String, data : ArrayList<IntroduceSeriesCreatorsResult.CreatorsData>)
    {
        val TEXT_SIZE = if(CommonUtils.getInstance(this).checkTablet) 32 else 44
        val titleView = TextView(this)
        titleView.setTextColor(resources.getColor(R.color.color_23a3e5))
        titleView.text = title
        titleView.gravity = Gravity.CENTER_VERTICAL
        titleView.setPadding(
            CommonUtils.getInstance(this).getPixel(LAYOUT_VIEW_PADDING),
            0,
            0,
            0
        )
        titleView.typeface = Font.getInstance(this).getRobotoMedium()

        contentsLayout.addView(
            titleView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            mCurrentViewMarginTop.toFloat(),
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            CREATOR_TEXTVIEW_HEIGHT.toFloat()
        )
        contentsLayout.setScale_TextSize(
            titleView,
            TEXT_SIZE.toFloat()
        )

        mCurrentViewMarginTop += CREATOR_TEXTVIEW_HEIGHT + CREATOR_TEXTVIEW_TITLE_MARGIN_TERM

        for(i in data.indices)
        {
            val castInformationTextView = SeparateTextView(this)
            castInformationTextView.typeface = Font.getInstance(this).getRobotoRegular()
            castInformationTextView
                .setSeparateText(data[i].getName(), "     " + data[i].getPart())
                .setSeparateColor(resources.getColor(R.color.color_444444), resources.getColor(R.color.color_8e8e8e))
                .showView()
            castInformationTextView.setPadding(
                CommonUtils.getInstance(this).getPixel(LAYOUT_VIEW_PADDING),
                0,
                0,
                0
            )

            contentsLayout.addView(
                castInformationTextView,
                LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
                mCurrentViewMarginTop.toFloat(),
                LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
                CREATOR_TEXTVIEW_HEIGHT.toFloat()
            )
            contentsLayout.setScale_TextSize(
                castInformationTextView,
                TEXT_SIZE.toFloat()
            )
            mCurrentViewMarginTop += CREATOR_TEXTVIEW_HEIGHT
        }
    }

    /**
     * Creator 구분선 그리기
     */
    private fun drawDivideLineView(contentsLayout : ScalableLayout)
    {
        val DIVIDE_LINE_WIDTH = 937
        val DIVIDE_LINE_HEIGHT = 2
        val DIVIDE_TITLE_MARGIN_TERM = 40
        mCurrentViewMarginTop += DIVIDE_TITLE_MARGIN_TERM
        val divideLineView = ImageView(this)
        divideLineView.setBackgroundResource(R.color.color_f3f3f3)
        contentsLayout.addView(
            divideLineView,
            (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + DIVIDE_TITLE_MARGIN_TERM).toFloat(),
            mCurrentViewMarginTop.toFloat(),
            DIVIDE_LINE_WIDTH.toFloat(),
            DIVIDE_LINE_HEIGHT.toFloat()
        )
        mCurrentViewMarginTop += DIVIDE_TITLE_MARGIN_TERM
    }

    /**
     * Introduction 영역 비디오 재생 (웹링크로 띄움)
     */
    private fun showIntroduceVideo(url : String)
    {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "video/*")
        startActivity(intent)
    }

    override fun showLoading()
    {
        if(mLoadingDialog == null)
        {
            mLoadingDialog = MaterialLoadingDialog(
                this,
                CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
            )
        }
        mLoadingDialog!!.show()
    }

    override fun hideLoading()
    {
        if(mLoadingDialog != null)
        {
            mLoadingDialog!!.dismiss()
            mLoadingDialog = null
        }
    }

    override fun showIntroduceSeriesData(result : IntroduceSeriesInformationResult)
    {
        _IntroduceBaseLayout.visibility = View.VISIBLE
        createIntroductionLayout(result)
        createCharacterThumbnailLayout(result.getCharacterInformationList())
        createCreatorLayout(result.getCreatorInformation()!!)

        checkSupportVideo(result)
        _IntroductionContentsImage.setOnClickListener {showIntroduceVideo(result.getIntroduceVideoMp4())}
    }

    override fun handlerMessage(message : Message)
    {
        mIntroduceSeriesPresenter.sendMessageEvent(message)
    }

    override fun showSuccessMessage(message : String) { }

    override fun showErrorMessage(message : String) { }

    @OnClick(R.id._closeButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
        }
    }
}