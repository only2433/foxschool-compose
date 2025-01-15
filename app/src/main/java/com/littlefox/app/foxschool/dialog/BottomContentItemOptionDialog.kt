package com.littlefox.app.foxschool.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.common.*
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ContentItemType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class BottomContentItemOptionDialog : BottomSheetDialog
{
    companion object
    {
        private const val SERVICE_INFO_QUIZ : Int           = 1
        private const val SERVICE_INFO_VOCA : Int           = 2
        private const val SERVICE_INFO_ORIGINAL_TEXT : Int  = 3
        private const val SERVICE_INFO_EBOOK : Int          = 4
        private const val SERVICE_INFO_STARWORDS : Int      = 5
        private const val SERVICE_INFO_CROSSWORD : Int     = 6
        private const val SERVICE_INFO_FLASHCARD : Int      = 7
        private const val SERVICE_INFO_RECORD_PLAYER : Int  = 8
    }

    @BindView(R.id._thumbnailInformationLayout)
    lateinit var _ThumbnailInformationLayout : ScalableLayout

    @BindView(R.id._optionAddItemLayout)
    lateinit  var _OptionAddItemLayout : ScalableLayout

    @BindView(R.id._thumbnailImage)
    lateinit var _ThumbnailImage : ImageView

    @BindView(R.id._contentIndexText)
    lateinit var _ContentIndexText : TextView

    @BindView(R.id._contentTitleText)
    lateinit var _ContentTitleText : TextView

    private var mContext : Context
    private var mPosition : Int = 0
    private var isFullName : Boolean = false
    private var isDeleteItemInBookshelf : Boolean  = false
    private var isDisableBookshelf : Boolean  = false
    private var mIndexColor : String = ""
    private var mItemOptionListener : ItemOptionListener? = null
    private var mContentsInformationResult : ContentsBaseResult
    private var isFullScreen : Boolean = false
    private lateinit var mAddItemTypeList : ArrayList<ContentItemType>

    constructor(context : Context, result : ContentsBaseResult) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_content_option)
        ButterKnife.bind(this)
        mContext = context
        mContentsInformationResult = result
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        Log.f("")
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            getWindow()!!.setLayout(CommonUtils.getInstance(mContext).getPixel(800), ViewGroup.LayoutParams.MATCH_PARENT)
        }

        if(isFullScreen)
        {
            getWindow()!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun setItemOptionListener(listener : ItemOptionListener?) : BottomContentItemOptionDialog
    {
        mItemOptionListener = listener
        return this
    }

    fun setIndexColor(color : String) : BottomContentItemOptionDialog
    {
        mIndexColor = color
        return this
    }

    fun setPosition(position : Int) : BottomContentItemOptionDialog
    {
        mPosition = position
        return this
    }

    fun setFullName() : BottomContentItemOptionDialog
    {
        isFullName = true
        return this
    }

    fun setDeleteMode() : BottomContentItemOptionDialog
    {
        isDeleteItemInBookshelf = true
        return this
    }

    fun disableBookshelf() : BottomContentItemOptionDialog
    {
        isDisableBookshelf = true
        return this
    }

    fun setFullScreen() : BottomContentItemOptionDialog
    {
        isFullScreen = true
        return this
    }

    fun setView() : BottomContentItemOptionDialog
    {
        initView()
        initFont()
        return this
    }

    private fun initView()
    {
        mAddItemTypeList = ArrayList<ContentItemType>()
        _OptionAddItemLayout.removeAllViews()
        Glide.with(mContext).load(mContentsInformationResult.thumbnail_url)
            .transition(DrawableTransitionOptions.withCrossFade()).into(_ThumbnailImage)

        if(mIndexColor.equals("") == false)
        {
            _ContentIndexText.setTextColor(Color.parseColor(mIndexColor))
        }

        if(mPosition == 0 || mIndexColor == "")
        {
            _ContentIndexText.setVisibility(View.GONE)
            _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 414f, 0f)
        }
        else
        {
            _ContentIndexText.setText(mPosition.toString())
            _ThumbnailInformationLayout.moveChildView(_ContentTitleText, 514f, 0f)
        }

        if(isFullName)
        {
            _ContentTitleText.setText(mContentsInformationResult.getContentsName())
        }
        else
        {
            Log.f("Name : " + mContentsInformationResult.name.toString() + ", SubName : " + mContentsInformationResult.sub_name)
            _ContentTitleText.setText(
                if(mContentsInformationResult.sub_name.equals(""))
                    mContentsInformationResult.name
                else
                    mContentsInformationResult.name
            )
        }
        checkContentItem()
        addContentItemView()
    }

    private fun initFont()
    {
        _ContentIndexText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
        _ContentTitleText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
    }

    private fun checkContentItem()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(CommonUtils.getInstance(mContext).checkTablet || Feature.IS_SUPPORT_EBOOK)
            {
                if(mContentsInformationResult.service_info?.ebook.equals(Common.SERVICE_SUPPORTED_PAID))
                {
                    mAddItemTypeList.add(ContentItemType.EBOOK)
                }
            }
        }

        if(mContentsInformationResult.service_info?.quiz.equals(Common.SERVICE_SUPPORTED_PAID))
        {
            mAddItemTypeList.add(ContentItemType.QUIZ)
        }

        if(mContentsInformationResult.service_info?.vocabulary.equals(Common.SERVICE_SUPPORTED_PAID))
        {
            mAddItemTypeList.add(ContentItemType.VOCABULARY)
        }

        if(mContentsInformationResult.service_info?.flash_card.equals(Common.SERVICE_SUPPORTED_PAID))
        {
            mAddItemTypeList.add(ContentItemType.FLASHCARD)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(mContentsInformationResult.service_info?.starwords.equals(Common.SERVICE_SUPPORTED_PAID))
            {
                mAddItemTypeList.add(ContentItemType.STARWORDS)
            }

            if(mContentsInformationResult.service_info?.crossword.equals(Common.SERVICE_SUPPORTED_PAID))
            {
                mAddItemTypeList.add(ContentItemType.CROSSWORD)
            }
        }

        if(mContentsInformationResult.service_info?.record.equals(Common.SERVICE_SUPPORTED_PAID))
        {
            mAddItemTypeList.add(ContentItemType.RECORDER)
        }

        if(mContentsInformationResult.service_info?.original_text.equals(Common.SERVICE_SUPPORTED_PAID))
        {
            mAddItemTypeList.add(ContentItemType.TRANSLATE)
        }

        if(isDisableBookshelf == false)
        {
            mAddItemTypeList.add(ContentItemType.BOOKSHELF)
        }
    }

    private fun addContentItemView()
    {
        val MAX_DIALOG_WIDTH = 1080
        val ADD_ITEM_WIDTH = 280
        val ADD_ITEM_HEIGHT = 252
        val MAX_COLUMN_COUNT = 3
        val MARGIN_LEFT_COLUMN_0 = 52
        val MARGIN_LEFT_COLUMN_DEFAULT = 68
        var lineIndex = 0
        var columnIndex = 0
        var maxLineCount = 0
        maxLineCount = if(mAddItemTypeList.size % MAX_COLUMN_COUNT == 0)
        {
            mAddItemTypeList.size / MAX_COLUMN_COUNT
        } else
        {
            mAddItemTypeList.size / MAX_COLUMN_COUNT + 1
        }
        _OptionAddItemLayout.setScaleSize(MAX_DIALOG_WIDTH.toFloat(), (maxLineCount * ADD_ITEM_HEIGHT).toFloat())
        for(i in mAddItemTypeList.indices)
        {
            val view = getViewItem(mAddItemTypeList[i])
            _OptionAddItemLayout.addView(
                view,
                (MARGIN_LEFT_COLUMN_0 + MARGIN_LEFT_COLUMN_DEFAULT * columnIndex + ADD_ITEM_WIDTH * columnIndex).toFloat(),
                (lineIndex * ADD_ITEM_HEIGHT).toFloat(),
                ADD_ITEM_WIDTH.toFloat(),
                ADD_ITEM_HEIGHT.toFloat()
            )

            if((i + 1) % MAX_COLUMN_COUNT == 0)
            {
                lineIndex++
                columnIndex = 0
            }
            else
            {
                columnIndex++
            }
        }
    }

    private fun getViewItem(type : ContentItemType) : View
    {
        val resultView : View = LayoutInflater.from(mContext).inflate(R.layout.dialog_content_option_item, null)
        val iconLayout : ScalableLayout = resultView.findViewById<View>(R.id._iconLayout) as ScalableLayout
        val icon = resultView.findViewById<View>(R.id._iconImage) as ImageView
        val title : TextView = resultView.findViewById<View>(R.id._iconText) as TextView
        title.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        when(type)
        {
            ContentItemType.EBOOK ->
            {
                icon.setImageResource(R.drawable.learning_06)
                title.setText(mContext.resources.getString(R.string.text_ebook))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_EBOOK))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.EBOOK)
                    }
                })
            }
            ContentItemType.QUIZ ->
            {
                icon.setImageResource(R.drawable.learning_01)
                title.setText(mContext.resources.getString(R.string.title_quiz))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_QUIZ))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.QUIZ)
                    }
                })
            }
            ContentItemType.VOCABULARY ->
            {
                icon.setImageResource(R.drawable.learning_03)
                title.setText(mContext.resources.getString(R.string.text_wordbook))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_VOCA))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.VOCABULARY)
                    }
                })
            }
            ContentItemType.FLASHCARD ->
            {
                icon.setImageResource(R.drawable.learning_08)
                title.setText(mContext.resources.getString(R.string.text_flashcards))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_FLASHCARD))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.FLASHCARD)
                    }
                })
            }
            ContentItemType.STARWORDS ->
            {
                icon.setImageResource(R.drawable.learning_07)
                title.setText(mContext.resources.getString(R.string.text_starwords))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_STARWORDS))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.STARWORDS)
                    }
                })
            }
            ContentItemType.CROSSWORD ->
            {
                icon.setImageResource(R.drawable.learning_09)
                title.setText(mContext.resources.getString(R.string.text_crossword))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_CROSSWORD))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.CROSSWORD)
                    }
                })
            }
            ContentItemType.RECORDER ->
            {
                icon.setImageResource(R.drawable.learning_10)
                title.setText(mContext.resources.getString(R.string.text_recorder))
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if (isServiceAvailable(SERVICE_INFO_RECORD_PLAYER))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.RECORD_PLAYER)
                    }
                })
            }
            ContentItemType.TRANSLATE ->
            {
                icon.setImageResource(R.drawable.learning_02)
                if(LittlefoxLocale.getCurrentLocale().equals(Locale.KOREA.toString()) === false)
                {
                    title.setText(mContext.resources.getString(R.string.text_original_text))
                } else
                {
                    title.setText(mContext.resources.getString(R.string.text_original_translate))
                }
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isServiceAvailable(SERVICE_INFO_ORIGINAL_TEXT))
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.TRANSLATE)
                    }
                })
            }
            ContentItemType.BOOKSHELF ->
            {
                if(isDeleteItemInBookshelf)
                {
                    icon.setImageResource(R.drawable.learning_05)
                    title.setText(mContext.resources.getString(R.string.text_delete))
                } else
                {
                    icon.setImageResource(R.drawable.learning_04)
                    title.setText(mContext.resources.getString(R.string.text_contain_bookshelf))
                }
                iconLayout.setOnClickListener(View.OnClickListener {
                    dismiss()
                    if(isDeleteItemInBookshelf)
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.DELETE_BOOKSHELF)
                    }
                    else
                    {
                        mItemOptionListener?.onClickItem(ActionContentsType.ADD_BOOKSHELF)
                    }
                })
            }
        }
        return resultView
    }

    private fun isServiceAvailable(type : Int) : Boolean
    {
        var serviceCheck = ""
        when(type)
        {
            SERVICE_INFO_QUIZ -> serviceCheck = mContentsInformationResult.service_info!!.quiz
            SERVICE_INFO_ORIGINAL_TEXT -> serviceCheck = mContentsInformationResult.service_info!!.original_text
            SERVICE_INFO_VOCA -> serviceCheck = mContentsInformationResult.service_info!!.vocabulary
            SERVICE_INFO_EBOOK -> serviceCheck = mContentsInformationResult.service_info!!.ebook
            SERVICE_INFO_STARWORDS -> serviceCheck = mContentsInformationResult.service_info!!.starwords
            SERVICE_INFO_CROSSWORD -> serviceCheck = mContentsInformationResult.service_info!!.crossword
            SERVICE_INFO_FLASHCARD -> serviceCheck = mContentsInformationResult.service_info!!.flash_card
            SERVICE_INFO_RECORD_PLAYER -> serviceCheck = mContentsInformationResult.service_info!!.record
        }
        return true
    }

}