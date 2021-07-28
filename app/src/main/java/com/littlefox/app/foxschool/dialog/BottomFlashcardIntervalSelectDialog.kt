package com.littlefox.app.foxschool.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.listener.IntervalSelectListener

/**
 * 플래시카드 자동 넘기기 간격 선택 다이얼로그
 */
class BottomFlashcardIntervalSelectDialog : BottomSheetDialog
{
    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindViews(
        R.id._intervalButton1,
        R.id._intervalButton2,
        R.id._intervalButton3,
        R.id._intervalButton4,
        R.id._intervalButton5
    )
    lateinit var _IntervalIDList : List<@JvmSuppressWildcards TextView>

    private lateinit var mContext : Context
    private var mIntervalSelectListener : IntervalSelectListener? = null

    private val INTERVAL_SECONDS : IntArray = intArrayOf(2, 3, 5, 7, 10)
    private var mCurrentIntervalSecond : Int = 0

    constructor(context : Context, currentIntervalSecond : Int) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_flashcard_interval_option)
        ButterKnife.bind(this)
        mContext = context
        mCurrentIntervalSecond = currentIntervalSecond
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            window!!.setLayout(CommonUtils.getInstance(mContext).getPixel(800), ViewGroup.LayoutParams.MATCH_PARENT)
        }
        else
        {
            window!!.setLayout(CommonUtils.getInstance(mContext).getPixel(1000), ViewGroup.LayoutParams.MATCH_PARENT)
        }
        initView()
        initFont()
        setCurrentIntervalStatus()
    }

    private fun initView()
    {
        for(i in _IntervalIDList.indices)
        {
            val text = "${INTERVAL_SECONDS[i]}${mContext.resources.getString(R.string.text_second)}"
            _IntervalIDList[i].text = text
            _IntervalIDList[i].setOnClickListener {
                mCurrentIntervalSecond = INTERVAL_SECONDS[i]
                setCurrentIntervalStatus()
                mIntervalSelectListener!!.onClickIntervalSecond(INTERVAL_SECONDS[i])
                dismiss()
            }
        }
    }

    private fun initFont()
    {
        _TitleText.typeface = Font.getInstance(mContext).getRobotoMedium()
        for(i in _IntervalIDList.indices)
        {
            _IntervalIDList[i].typeface = Font.getInstance(mContext).getRobotoMedium()
        }
    }

    fun setOnIntervalSelectListener(intervalSelectListener : IntervalSelectListener?)
    {
        mIntervalSelectListener = intervalSelectListener
    }

    private fun setCurrentIntervalStatus()
    {
        for(i in _IntervalIDList.indices)
        {
            if(mCurrentIntervalSecond == INTERVAL_SECONDS[i])
            {
                _IntervalIDList[i].setBackgroundResource(R.drawable.btn_interval_on)
                _IntervalIDList[i].setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            }
            else
            {
                _IntervalIDList[i].setBackgroundResource(R.drawable.btn_interval_off)
                _IntervalIDList[i].setTextColor(mContext.resources.getColor(R.color.color_b9b9b9))
            }
        }
    }

    override fun onBackPressed()
    {
        dismiss()
    }
}