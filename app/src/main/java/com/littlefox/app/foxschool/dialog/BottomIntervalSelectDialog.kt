package com.littlefox.app.foxschool.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
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

class BottomIntervalSelectDialog : BottomSheetDialog
{
    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindViews(R.id._intervalNoHaveButton, R.id._interval1SecButton, R.id._interval2SecButton, R.id._interval3SecButton, R.id._interval5SecButton,
        R.id._interval7SecButton, R.id._interval10SecButton, R.id._interval15SecButton, R.id._interval20SecButton, R.id._interval30SecButton)
    lateinit var _IntervalIDList : List<@JvmSuppressWildcards TextView>

    companion object
    {
        private val INTERVAL_SECONDS = intArrayOf(0, 1, 2, 3, 5, 7, 10, 15, 20, 30)
    }

    private val mContext : Context
    private var mCurrentIntervalSecond : Int = 0
    private var mIntervalSelectListener : IntervalSelectListener? = null

    constructor(context : Context, currentIntervalSecond : Int) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_interval_option)
        ButterKnife.bind(this)
        mContext = context
        mCurrentIntervalSecond = currentIntervalSecond
    }

    protected override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            getWindow()!!.setLayout(CommonUtils.getInstance(mContext).getPixel(800), ViewGroup.LayoutParams.MATCH_PARENT)
        }
        initView()
        initFont()
        setCurrentIntervalStatus()
    }

    override fun onBackPressed()
    {
        dismiss()
    }

    fun setOnIntervalSelectListener(intervalSelectListener : IntervalSelectListener?)
    {
        mIntervalSelectListener = intervalSelectListener
    }

    private fun initView()
    {
        for(i in _IntervalIDList.indices)
        {
            if(i == 0)
            {
                _IntervalIDList[i].setText(mContext.resources.getString(R.string.text_not_have_interval))
            }
            else
            {
                _IntervalIDList[i].setText(String.format(mContext.resources.getString(R.string.text_sec_interval), INTERVAL_SECONDS[i]))
            }
            _IntervalIDList[i].setOnClickListener(View.OnClickListener {
                mCurrentIntervalSecond = INTERVAL_SECONDS[i]
                setCurrentIntervalStatus()
                mIntervalSelectListener!!.onClickIntervalSecond(INTERVAL_SECONDS[i])
                dismiss()
            })
        }
    }

    private fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        for(i in _IntervalIDList.indices)
        {
            _IntervalIDList[i].setTypeface(Font.getInstance(mContext).getRobotoMedium())
        }
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
}