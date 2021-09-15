package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData
import com.littlefox.app.foxschool.adapter.CalendarItemViewAdapter
import com.littlefox.app.foxschool.adapter.listener.CalendarItemListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.HomeworkCalendarFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 달력 화면 (학생용)
 */
class HomeworkCalendarFragment : Fragment()
{
    @BindView(R.id._mainBackgroundView)
    lateinit var _MainBackgroundView : ScalableLayout

    @BindView(R.id._scrollView)
    lateinit var _ScrollView : ScrollView

    @BindView(R.id._calendarPickLayout)
    lateinit var _CalendarPickLayout : ScalableLayout

    @BindView(R.id._beforeButton)
    lateinit var _BeforeButton : ImageView
    
    @BindView(R.id._beforeButtonRect)
    lateinit var _BeforeButtonRect : ImageView

    @BindView(R.id._afterButton)
    lateinit var _AfterButton : ImageView
    
    @BindView(R.id._afterButtonRect)
    lateinit var _AfterButtonRect : ImageView

    @BindView(R.id._monthTitleText)
    lateinit var _MonthTitleText : TextView

    @BindView(R.id._calendarListLayout)
    lateinit var _CalendarListLayout : LinearLayout

    @BindView(R.id._calendarView)
    lateinit var _CalendarView : RecyclerView

    @BindView(R.id._textSunday)
    lateinit var _TextSunday : TextView

    @BindView(R.id._textMonday)
    lateinit var _TextMonday : TextView

    @BindView(R.id._textTuesday)
    lateinit var _TextTuesday : TextView

    @BindView(R.id._textWednesday)
    lateinit var _TextWednesday : TextView

    @BindView(R.id._textThursday)
    lateinit var _TextThursday : TextView

    @BindView(R.id._textFriday)
    lateinit var _TextFriday : TextView

    @BindView(R.id._textSaturday)
    lateinit var _TextSaturday : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private var mCalendarItemList : ArrayList<CalendarData>? = ArrayList<CalendarData>()
    private lateinit var mCalendarItemViewAdapter : CalendarItemViewAdapter

    private lateinit var mHomeworkCalendarFragmentObserver : HomeworkCalendarFragmentObserver
    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        var view : View? = null
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            view = inflater.inflate(R.layout.fragment_homework_calendar_tablet, container, false)
        } else
        {
            view = inflater.inflate(R.layout.fragment_homework_calendar, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.f("")
        initView()
        initFont()
        setupObserverViewModel()
    }

    override fun onStart()
    {
        super.onStart()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onStop()
    {
        super.onStop()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        mUnbinder.unbind()
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initView()
    {
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            if (Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY)
            {
                // 태블릿 4:3 모델 대응
                _MainBackgroundView.moveChildView(_CalendarListLayout, 0f, 200f, 1920f, 860f)
            }
            else
            {
                _MainBackgroundView.moveChildView(_CalendarListLayout, 0f, 200f, 1920f, 852f)
            }
        }
        initRecyclerView()
    }

    private fun initFont()
    {
        _MonthTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextSunday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextMonday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextTuesday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextWednesday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextThursday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextFriday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _TextSaturday.setTypeface(Font.getInstance(mContext).getRobotoMedium())
    }

    private fun initRecyclerView()
    {
        val gridLayoutManager = GridLayoutManager(mContext, 7)
        _CalendarView.layoutManager = gridLayoutManager
        mCalendarItemViewAdapter = CalendarItemViewAdapter(mContext, mCalendarItemList!!, _CalendarListLayout)
        mCalendarItemViewAdapter.setCalendarItemListener(mCalendarItemListener)
        _CalendarView.adapter = mCalendarItemViewAdapter
        _ScrollView.scrollTo(0, 0)
//        _ScrollView.fullScroll(ScrollView.FOCUS_UP)
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mHomeworkCalendarFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCalendarFragmentObserver::class.java)
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)

        // 달력 아이템
        mHomeworkManagePresenterObserver.setCalendarData.observe((mContext as AppCompatActivity), Observer {calendarItems ->
            mCalendarItemList = calendarItems
            mCalendarItemViewAdapter.setItemList(mCalendarItemList!!)
            mCalendarItemViewAdapter.notifyDataSetChanged()
            _ScrollView.fullScroll(ScrollView.FOCUS_UP)
            mHomeworkCalendarFragmentObserver.onCompletedListSet(true)
        })

        // 달력 타이틀
        mHomeworkManagePresenterObserver.setCalendarMonthTitle.observe((mContext as AppCompatActivity), Observer {title ->
            _MonthTitleText.text = title
        })

        // 달력 이전버튼 표시여부
        mHomeworkManagePresenterObserver.setCalendarPrevButton.observe((mContext as AppCompatActivity), Observer {isEnable ->
            if (isEnable) showPrevButton()
            else hidePrevButton()
        })

        // 달력 다음버튼 표시여부
        mHomeworkManagePresenterObserver.setCalendarNextButton.observe((mContext as AppCompatActivity), Observer {isEnable ->
            if (isEnable) showNextButton()
            else hideNexButton()
        })
    }

    /**
     * 달력 이전 버튼 표시
     */
    private fun showPrevButton()
    {
        _BeforeButton.visibility = View.VISIBLE
        _BeforeButtonRect.visibility = View.VISIBLE
    }

    /**
     * 달력 이전 버튼 숨김
     */
    private fun hidePrevButton()
    {
        _BeforeButton.visibility = View.GONE
        _BeforeButtonRect.visibility = View.GONE
    }

    /**
     * 달력 다음 버튼 표시
     */
    private fun showNextButton()
    {
        _AfterButton.visibility = View.VISIBLE
        _AfterButtonRect.visibility = View.VISIBLE
    }

    /**
     * 달력 다음 버튼 숨김
     */
    private fun hideNexButton()
    {
        _AfterButton.visibility = View.GONE
        _AfterButtonRect.visibility = View.GONE
    }

    @Optional
    @OnClick(R.id._beforeButtonRect, R.id._afterButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._beforeButtonRect -> mHomeworkCalendarFragmentObserver.onClickCalendarBefore()
            R.id._afterButtonRect -> mHomeworkCalendarFragmentObserver.onClickCalendarAfter()
        }
    }

    private val mCalendarItemListener : CalendarItemListener = object : CalendarItemListener
    {
        override fun onClickItem(position : Int)
        {
            mCalendarItemList?.let { list ->
                if (list[position].hasHomework())
                {
                    mHomeworkCalendarFragmentObserver.onClickCalendarItem(list[position].getHomework())
                }
            }
        }
    }
}