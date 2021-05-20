package com.littlefox.app.foxschool.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

import java.util.*

class BottomItemOptionDialog: BottomSheetDialog
{
    @BindView(R.id._thumbnailInformationLayout)
    lateinit var _ThumbnailInformationLayout : ScalableLayout

    @BindView(R.id._optionQuizLayout)
    lateinit var _OptionQuizLayout : ScalableLayout

    @BindView(R.id._optionTranslateLayout)
    lateinit var _OptionTranslateLayout : ScalableLayout

    @BindView(R.id._optionWordBookLayout)
    lateinit var _OptionWordBookLayout : ScalableLayout

    @BindView(R.id._optionBookshelfLayout)
    lateinit var _OptionBookshelfLayout : ScalableLayout

    @BindView(R.id._optionEBookLayout)
    lateinit var _OptionEBookLayout : ScalableLayout

    @BindView(R.id._optionStarWordsLayout)
    lateinit var _OptionStarwordsLayout : ScalableLayout

    @BindView(R.id._thumbnailImage)
    lateinit var _ThumbnailImage : ImageView

    @BindView(R.id._quizImage)
    lateinit var _QuizImage : ImageView

    @BindView(R.id._translateImage)
    lateinit var _TranslateImage : ImageView

    @BindView(R.id._wordBookImage)
    lateinit var _WordBookImage : ImageView

    @BindView(R.id._bookshelfImage)
    lateinit var _BookshelfImage : ImageView

    @BindView(R.id._eBookImage)
    lateinit var _EBookImage : ImageView

    @BindView(R.id._starWordsImage)
    lateinit var _StarwordsImage : ImageView

    @BindView(R.id._contentIndexText)
    lateinit var _ContentIndexText : TextView

    @BindView(R.id._contentTitleText)
    lateinit var _ContentTitleText : TextView

    @BindView(R.id._quizTitleText)
    lateinit var _QuizTitleText : TextView

    @BindView(R.id._translateTitleText)
    lateinit var _TranslateTitleText : TextView

    @BindView(R.id._wordBookTitleText)
    lateinit var _WordBookTitleText : TextView

    @BindView(R.id._bookshelfTitleText)
    lateinit var _BookshelfTitleText : TextView

    @BindView(R.id._eBookTitleText)
    lateinit var _eBookTitleText : TextView

    @BindView(R.id._starWordsTitleText)
    lateinit var _StarwordsTitleText : TextView

    companion object
    {
        private const val SERVICE_INFO_BASE : Int           = 0
        private const val SERVICE_INFO_QUIZ : Int           = 1
        private const val SERVICE_INFO_VOCA : Int           = 2
        private const val SERVICE_INFO_ORIGINAL_TEXT : Int  = 3
        private const val SERVICE_INFO_EBOOK : Int          = 4
        private const val SERVICE_INFO_STARWORDS : Int      = 5;
    }

    private val mContext : Context
    private var mPosition : Int = 0
    private var isFullName : Boolean = false
    private var isDeleteItemInBookshelf : Boolean = false
    private var isDisableBookshelf : Boolean = false
    private var isDisableGame : Boolean = false
    private var mIndexColor : String = ""
    lateinit private var mItemOptionListener : ItemOptionListener
    lateinit private var mContentsInformationResult : ContentsBaseResult
    private var isFullScreen : Boolean  = false

    constructor(context : Context) : super(context)
    {
        Log.f("")
        mContext = context
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if(Feature.IS_TABLET)
        {
            setContentView(R.layout.dialog_story_detail_option_tablet)
        }
        else
        {
            setContentView(R.layout.dialog_story_detail_option)
        }
        ButterKnife.bind(this)
    }

    protected override fun onCreate(savedInstanceState : Bundle)
    {
        super.onCreate(savedInstanceState)
        if(Feature.IS_TABLET)
        {
            getWindow()!!.setLayout(CommonUtils.getInstance(mContext).getPixel(800), ViewGroup.LayoutParams.MATCH_PARENT)
        }
        if(isFullScreen)
        {
            getWindow()!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun setItemOptionListener(listener : ItemOptionListener) : BottomItemOptionDialog
    {
        mItemOptionListener = listener
        return this
    }

    fun setData(result : ContentsBaseResult) : BottomItemOptionDialog
    {
        mContentsInformationResult = result
        return this
    }

    fun setIndexColor(color : String) : BottomItemOptionDialog
    {
        mIndexColor = color
        return this
    }

    fun setPosition(position : Int) : BottomItemOptionDialog
    {
        mPosition = position + 1
        return this
    }

    fun setFullName() : BottomItemOptionDialog
    {
        isFullName = true
        return this
    }

    fun setDeleteMode() : BottomItemOptionDialog
    {
        isDeleteItemInBookshelf = true
        return this
    }

    fun disableBookshelf() : BottomItemOptionDialog
    {
        isDisableBookshelf = true
        return this
    }

    fun disableGame() : BottomItemOptionDialog
    {
        isDisableGame = true
        return this
    }

    fun setFullScreen() : BottomItemOptionDialog
    {
        isFullScreen = true
        return this
    }

    fun setView() : BottomItemOptionDialog
    {
        initView()
        initFont()
        return this
    }

    private fun initView()
    {
        if(LittlefoxLocale.getCurrentLocale().equals(Locale.KOREA.toString()) === false)
        {
            _TranslateTitleText.setText(mContext.resources.getString(R.string.text_original_text))
        }
        if(mContentsInformationResult.getServiceInformation()!!.getVocabularySupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            _OptionWordBookLayout.setVisibility(View.GONE)
        }
        if(mContentsInformationResult.getServiceInformation()!!.getQuizSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            _OptionQuizLayout.setVisibility(View.GONE)
        }
        if(mContentsInformationResult.getServiceInformation()!!.getOriginalTextSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
        {
            _OptionTranslateLayout.setVisibility(View.GONE)
        }
        if(Feature.IS_TABLET && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Log.f("mContentsInformationResult.getServiceInformation().getEbookSupportType() : " + mContentsInformationResult.getServiceInformation()?.getEbookSupportType())
            if(mContentsInformationResult.getServiceInformation()?.getEbookSupportType().equals(Common.SERVICE_NOT_SUPPORTED) === false)
            {
                _OptionEBookLayout.setVisibility(View.VISIBLE)
            }
        }
        if(isDisableGame == false)
        {
            if(mContentsInformationResult.getServiceInformation()?.getStarwordsSupportType().equals(Common.SERVICE_NOT_SUPPORTED))
            {
                _OptionStarwordsLayout.setVisibility(View.GONE)
            }
        }
        else
        {
            _OptionStarwordsLayout.setVisibility(View.GONE)
        }

        if(isDeleteItemInBookshelf)
        {
            _BookshelfImage.setImageResource(R.drawable.learning_05)
            _BookshelfTitleText.setText(mContext.resources.getString(R.string.text_delete))
        }
        else
        {
            _BookshelfImage.setImageResource(R.drawable.learning_04)
            _BookshelfTitleText.setText(mContext.resources.getString(R.string.text_contain_bookshelf))
        }
        if(isDisableBookshelf)
        {
            _OptionBookshelfLayout.setVisibility(View.GONE)
        }
        else
        {
            _OptionBookshelfLayout.setVisibility(View.VISIBLE)
        }
        Glide.with(mContext).load(
                mContentsInformationResult.getThumbnailUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(_ThumbnailImage)
        if(mIndexColor.equals("") == false)
        {
            _ContentIndexText.setTextColor(Color.parseColor(mIndexColor))
        }
        if(mPosition == 0 || mIndexColor == "")
        {
            _ContentIndexText.setVisibility(View.GONE)
            if(Feature.IS_TABLET)
            {
                _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 300f, 25f)
            }
            else
            {
                _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 414f, 0f)
            }
        }
        else
        {
            _ContentIndexText.setText(mPosition.toString())
            if(Feature.IS_TABLET)
            {
                _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 370f, 25f)
            }
            else
            {
                _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 514f, 0f)
            }
        }
        if(isFullName)
        {
            _ContentTitleText.setText(CommonUtils.getInstance(mContext).getContentsName(mContentsInformationResult))
        }
        else
        {
            Log.f("Name : " + mContentsInformationResult?.getName().toString() + ", SubName : " + mContentsInformationResult?.getSubName())

            if(mContentsInformationResult?.getSubName().equals(""))
                _ContentTitleText.setText(mContentsInformationResult!!.getName())
            else
                _ContentTitleText.setText(mContentsInformationResult!!.getSubName())
        }
    }

    private fun initFont()
    {
        _ContentIndexText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        _ContentTitleText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        _QuizTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TranslateTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _WordBookTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _BookshelfTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _eBookTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _StarwordsTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
    }

    @OnClick(R.id._optionQuizLayout, R.id._optionTranslateLayout, R.id._optionWordBookLayout, R.id._optionBookshelfLayout, R.id._optionEBookLayout, R.id._optionStarWordsLayout)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._optionQuizLayout ->
            {
                dismiss()
                if(isServiceAvailable(SERVICE_INFO_QUIZ))
                {
                    mItemOptionListener.onClickQuiz()
                }
            }
            R.id._optionTranslateLayout ->
            {
                dismiss()
                if(isServiceAvailable(SERVICE_INFO_ORIGINAL_TEXT))
                {
                    mItemOptionListener.onClickTranslate()
                }
            }
            R.id._optionWordBookLayout ->
            {
                dismiss()
                if(isServiceAvailable(SERVICE_INFO_VOCA))
                {
                    mItemOptionListener.onClickVocabulary()
                }
            }
            R.id._optionBookshelfLayout ->
            {
                dismiss();
                if(Feature.IS_FREE_USER)
                {
                    mItemOptionListener.onErrorMessage(mContext.resources.getString(R.string.message_payment_service_login))
                    return;
                }
                else if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    mItemOptionListener.onErrorMessage(mContext.resources.getString(R.string.message_payment_service_paid_using))
                    return;
                }
                mItemOptionListener.onClickBookshelf()
            }
            R.id._optionEBookLayout ->
            {
                dismiss()
                if(isServiceAvailable(SERVICE_INFO_EBOOK))
                {
                    mItemOptionListener.onClickEbook()
                }
            }
            R.id._optionStarWordsLayout ->
            {
                dismiss()
                if(isServiceAvailable(SERVICE_INFO_STARWORDS))
                {
                    mItemOptionListener.onClickGameStarwords()
                }
            }
        }
    }

    private fun isServiceAvailable(type : Int) : Boolean
    {
        var serviceCheck : String? = ""
        when(type)
        {
            SERVICE_INFO_QUIZ -> serviceCheck = mContentsInformationResult.getServiceInformation()?.getQuizSupportType()
            SERVICE_INFO_ORIGINAL_TEXT -> serviceCheck = mContentsInformationResult.getServiceInformation()?.getOriginalTextSupportType()
            SERVICE_INFO_VOCA -> serviceCheck = mContentsInformationResult.getServiceInformation()?.getVocabularySupportType()
            SERVICE_INFO_EBOOK -> serviceCheck = mContentsInformationResult.getServiceInformation()?.getEbookSupportType()
            SERVICE_INFO_STARWORDS -> serviceCheck = mContentsInformationResult.getServiceInformation()?.getStarwordsSupportType()
        }

        if(Feature.IS_FREE_USER || Feature.IS_REMAIN_DAY_END_USER)
        {
            if(serviceCheck == Common.SERVICE_SUPPORTED_FREE)
            {
                return true
            }
            else
            {
                if(Feature.IS_FREE_USER)
                {
                    mItemOptionListener.onErrorMessage(mContext.resources.getString(R.string.message_payment_service_login))
                }
                else if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    mItemOptionListener.onErrorMessage(mContext.resources.getString(R.string.message_payment_service_paid_using))
                }
                return false
            }
        }
        else
        {
            return true
        }
    }


}