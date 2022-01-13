package com.littlefox.app.foxschool.dialog

import android.app.Dialog
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.*
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
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class IntroduceSeriesTabletDialog : Dialog
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

    @BindView(R.id._introduceInformationText)
    lateinit var _IntroduceInformationText : TextView

    @BindView(R.id._introductionTitleText)
    lateinit var _IntroductionTitleText : TextView

    @BindView(R.id._introductionContentsText)
    lateinit var _IntroductionContentsText : TextView

    companion object
    {
        private const val LAYOUT_CONTENTS_VIEW_WIDTH = 1020
        private const val LAYOUT_TITLE_VIEW_HEIGHT = 120
        private const val LAYOUT_CONTENTS_VIEW_MARGIN_LEFT = 30
        private const val LAYOUT_VIEW_PADDING = 50
        private const val LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT = 28
        private const val THUMBNAIL_IMAGE_WIDTH = 280
        private const val THUMBNAIL_IMAGE_HEIGHT = 280
        private const val THUMBNAIL_TITLE_WIDTH = 280
        private const val THUMBNAIL_TITLE_HEIGHT = 80
        private const val CREATOR_TEXTVIEW_HEIGHT = 55
        private const val CREATOR_TEXTVIEW_TITLE_MARGIN_TERM = 20
        private const val DIVIDE_LINE_HEIGHT = 2
        private const val DIVIDE_TITLE_MARGIN_TERM = 40
        private val CREATORS_TYPE_NAME = arrayOf("Story", "Animation", "Cast")
    }

    private var mContext : Context;
    private var mContentsBaseLayoutHeight : Int = -1
    private var mContentsBackgroundHeight : Int  = -1
    private var mCurrentViewMarginTop : Int  = -1
    private val DIALOG_WIDTH : Int  = 756

    constructor(context : Context, result : IntroduceSeriesInformationResult) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_introduce_series)
        ButterKnife.bind(this)
        mContext = context
        val params : WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
            copyFrom(window!!.attributes)
            width = CommonUtils.getInstance(mContext).getPixel(DIALOG_WIDTH)
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window!!.attributes = params
        initView()
        showIntroduceSeriesView(result)
    }

    private fun initView()
    {
        _TitleBaselayout.setBackgroundColor(mContext.resources.getColor(R.color.color_26d0df))
        _TitleText.setText(mContext.resources.getString(R.string.text_introduction))
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    private fun showIntroduceSeriesView(result : IntroduceSeriesInformationResult)
    {
        _IntroduceBaseLayout.setVisibility(View.VISIBLE)
        createIntroductionLayout(result)
        createCharacterThumbnailLayout(result.getCharacterInformationList())
        createCreatorLayout(result.getCreatorInformation()!!)
    }

    private fun createIntroductionLayout(result : IntroduceSeriesInformationResult)
    {
        _IntroductionContentsText.setMovementMethod(ScrollingMovementMethod())
        _IntroductionContentsText.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(v : View, event : MotionEvent) : Boolean
            {
                _IntroductionContentsText.getParent().requestDisallowInterceptTouchEvent(true)
                return false
            }
        })
        var categoryData = ""
        if(result.getCategories().equals("") === false)
        {
            categoryData = " | " + result.getCategories().replace("|", " | ")
        }
        Log.i("categoryData : $categoryData")
        if(result.isSingleSeries)
        {
            _IntroduceInformationText.setText(
                String.format(mContext.getString(R.string.text_count_level), result.getLevel())
                        + " | " + String.format(mContext.getString(R.string.text_count_stories, result.getSchoolContentsCount().toString())
                        + categoryData
                )
            )
        }
        else
        {
            _IntroduceInformationText.setText(
                String.format(mContext.getString(R.string.text_count_level), result.getLevel())
                        + " | " + String.format(mContext.getString(R.string.text_count_series_stories, result.getSchoolContentsCount().toString())
                        + categoryData
                )
            )
        }
        _IntroductionContentsText.setText(result.getIntroduction())
        _IntroductionTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _IntroductionContentsText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        _IntroductionTitleText.setPadding(CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING), 0, 0, 0)
        _IntroductionContentsText.setPadding(
            CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING),
            CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING),
            CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING),
            CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING)
        )
    }

    private fun createCharacterThumbnailLayout(characterList : ArrayList<IntroduceSeriesCharacterResult>)
    {
        val MAX_ROW_COUNT : Int = 3
        val BASE_ROW_MARGIN_TOP : Int = 63
        val COLUMN_SPACING : Int = 22
        val ROW_SPACING : Int = 38
        var currentRowIndex : Int = 0
        var currentColumnIndex : Int = 0
        var maxColumnCount : Int = characterList.size / 3
        if(characterList.size % 3 > 0)
        {
            maxColumnCount += 1
        }
        mContentsBackgroundHeight = ((THUMBNAIL_IMAGE_HEIGHT + THUMBNAIL_TITLE_HEIGHT) * maxColumnCount
                + COLUMN_SPACING * (maxColumnCount - 1) + BASE_ROW_MARGIN_TOP * 2)
        mContentsBaseLayoutHeight = mContentsBackgroundHeight + LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT
        val baseCharacterLayout = ScalableLayout(mContext)
        baseCharacterLayout.setScaleSize(Common.TARGET_PHONE_DISPLAY_WIDTH, mContentsBaseLayoutHeight.toFloat())
        val backgoundView = ImageView(mContext)
        backgoundView.scaleType = ImageView.ScaleType.FIT_XY
        backgoundView.setImageResource(R.drawable.info_box_1w)
        baseCharacterLayout.addView(
            backgoundView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            0f,
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            mContentsBackgroundHeight.toFloat()
        )

        for(i in characterList.indices)
        {
            val thumbnail = ImageView(mContext)
            thumbnail.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(mContext).load(characterList[i].getImage())
                .transition(DrawableTransitionOptions.withCrossFade()).into(thumbnail)
            val titleView = TextView(mContext).apply {
                setGravity(Gravity.CENTER)
                setTextColor(mContext.resources.getColor(R.color.color_444444))
                setTypeface(Font.getInstance(mContext).getRobotoBold())
                setText(characterList[i].getName())
            }
            currentColumnIndex = i / MAX_ROW_COUNT
            currentRowIndex = i % MAX_ROW_COUNT
            Log.i("currentColumnIndex : $currentColumnIndex, currentRowIndex : $currentRowIndex")
            if(currentColumnIndex == 0)
            {
                baseCharacterLayout.addView(
                    thumbnail,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (THUMBNAIL_IMAGE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    BASE_ROW_MARGIN_TOP.toFloat(),
                    THUMBNAIL_IMAGE_WIDTH.toFloat(),
                    THUMBNAIL_IMAGE_HEIGHT.toFloat()
                )
                baseCharacterLayout.addView(
                    titleView,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (THUMBNAIL_TITLE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + THUMBNAIL_IMAGE_HEIGHT).toFloat(),
                    THUMBNAIL_TITLE_WIDTH.toFloat(),
                    THUMBNAIL_TITLE_HEIGHT.toFloat()
                )
                baseCharacterLayout.setScale_TextSize(titleView, 30f)
            }
            else
            {
                baseCharacterLayout.addView(
                    thumbnail,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (THUMBNAIL_IMAGE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + (THUMBNAIL_IMAGE_HEIGHT + THUMBNAIL_TITLE_HEIGHT + COLUMN_SPACING) * currentColumnIndex).toFloat(),
                    THUMBNAIL_IMAGE_WIDTH.toFloat(),
                    THUMBNAIL_IMAGE_HEIGHT.toFloat()
                )
                baseCharacterLayout.addView(
                    titleView,
                    (LAYOUT_CONTENTS_VIEW_MARGIN_LEFT + LAYOUT_VIEW_PADDING + (THUMBNAIL_TITLE_WIDTH + ROW_SPACING) * currentRowIndex).toFloat(),
                    (BASE_ROW_MARGIN_TOP + THUMBNAIL_IMAGE_HEIGHT + (THUMBNAIL_IMAGE_HEIGHT + THUMBNAIL_TITLE_HEIGHT + COLUMN_SPACING) * currentColumnIndex).toFloat(),
                    THUMBNAIL_TITLE_WIDTH.toFloat(),
                    THUMBNAIL_TITLE_HEIGHT.toFloat()
                )
                baseCharacterLayout.setScale_TextSize(titleView, 30f)
            }
        }
        _IntroduceBaseLayout.addView(baseCharacterLayout)
    }

    private fun createCreatorLayout(creatorsResult : IntroduceSeriesCreatorsResult)
    {
        val titleLayout = ScalableLayout(mContext)
        titleLayout.setScaleSize(Common.TARGET_PHONE_DISPLAY_WIDTH, LAYOUT_TITLE_VIEW_HEIGHT.toFloat())
        val titleText = TextView(mContext).apply {
            setPadding(CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING), 0, 0, 0)
            setGravity(Gravity.CENTER_VERTICAL)
            setBackground(mContext.resources.getDrawable(R.drawable.info_box_b))
            setText("Creators")
            setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            setTypeface(Font.getInstance(mContext).getRobotoMedium())
        }

        titleLayout.addView(
            titleText,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            0f,
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            LAYOUT_TITLE_VIEW_HEIGHT.toFloat()
        )
        titleLayout.setScale_TextSize(titleText, 44f)
        _IntroduceBaseLayout.addView(titleLayout)

        mContentsBackgroundHeight =
            CREATORS_TYPE_NAME.size * CREATOR_TEXTVIEW_HEIGHT
        + CREATORS_TYPE_NAME.size * CREATOR_TEXTVIEW_TITLE_MARGIN_TERM
        + creatorsResult.getStoryList().size * CREATOR_TEXTVIEW_HEIGHT
        + creatorsResult.getAnimationList().size * CREATOR_TEXTVIEW_HEIGHT
        + creatorsResult.getCastList().size * CREATOR_TEXTVIEW_HEIGHT
        + (DIVIDE_TITLE_MARGIN_TERM * 2 + DIVIDE_LINE_HEIGHT) * 2 + LAYOUT_VIEW_PADDING * 2

        mContentsBaseLayoutHeight = mContentsBackgroundHeight + LAYOUT_BASE_VIEW_LAST_MARGIN_HEIGHT
        val contentsLayout = ScalableLayout(mContext)
        contentsLayout.setScaleSize(Common.TARGET_PHONE_DISPLAY_WIDTH, mContentsBaseLayoutHeight.toFloat())

        val backgroundView = ImageView(mContext)
        backgroundView.setBackgroundResource(R.drawable.info_box_w)
        contentsLayout.addView(
            backgroundView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            0f,
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            mContentsBackgroundHeight.toFloat()
        )
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

    private fun drawCreatorInformationView(contentsLayout : ScalableLayout, title : String, data : ArrayList<IntroduceSeriesCreatorsResult.CreatorsData>)
    {
        if(data.size <= 0)
        {
            return
        }
        val titleView = TextView(mContext).apply {
            setTextColor(mContext.resources.getColor(R.color.color_23a3e5))
            setText(title)
            setGravity(Gravity.CENTER_VERTICAL)
            setPadding(
                CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING),
                0,
                0,
                0
            )
            setTypeface(Font.getInstance(mContext).getRobotoMedium())
        }

        contentsLayout.addView(
            titleView,
            LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
            mCurrentViewMarginTop.toFloat(),
            LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
            CREATOR_TEXTVIEW_HEIGHT.toFloat()
        )
        contentsLayout.setScale_TextSize(titleView, 44f)
        mCurrentViewMarginTop += CREATOR_TEXTVIEW_HEIGHT + CREATOR_TEXTVIEW_TITLE_MARGIN_TERM
        for(i in data.indices)
        {
            val castInformationTextView = SeparateTextView(mContext)
            castInformationTextView.setTypeface(Font.getInstance(mContext).getRobotoRegular())
            castInformationTextView.setSeparateText(data[i].getName(), "     " + data[i].getPart())
                .setSeparateColor(
                    mContext.resources.getColor(R.color.color_444444),
                    mContext.resources.getColor(R.color.color_8e8e8e)
                ).showView()
            castInformationTextView.setPadding(CommonUtils.getInstance(mContext).getPixel(LAYOUT_VIEW_PADDING), 0, 0, 0)
            contentsLayout.addView(
                castInformationTextView,
                LAYOUT_CONTENTS_VIEW_MARGIN_LEFT.toFloat(),
                mCurrentViewMarginTop.toFloat(),
                LAYOUT_CONTENTS_VIEW_WIDTH.toFloat(),
                CREATOR_TEXTVIEW_HEIGHT.toFloat()
            )
            contentsLayout.setScale_TextSize(castInformationTextView, 44f)
            mCurrentViewMarginTop += CREATOR_TEXTVIEW_HEIGHT
        }
    }

    private fun drawDivideLineView(contentsLayout : ScalableLayout)
    {
        val DIVIDE_LINE_WIDTH : Int = 937
        val DIVIDE_LINE_HEIGHT : Int = 2
        val DIVIDE_TITLE_MARGIN_TERM : Int = 40
        mCurrentViewMarginTop += DIVIDE_TITLE_MARGIN_TERM
        val divideLineView = ImageView(mContext)
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

    @OnClick(R.id._closeButtonRect)
    fun OnClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> dismiss()
        }
    }
}