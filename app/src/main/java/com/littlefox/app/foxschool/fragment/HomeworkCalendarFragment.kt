package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.homework.CalendarBaseData
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.adapter.CalendarItemViewAdapter
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.api.viewmodel.factory.StudentHomeworkFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.fragment.HomeworkFragmentViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.CalendarImageType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 달력 화면 (학생용)
 * @author 김태은
 */
class HomeworkCalendarFragment : Fragment()
{
    @BindView(R.id._mainBackgroundView)
    lateinit var _MainBackgroundView : ScalableLayout

    @BindView(R.id._scrollView)
    lateinit var _ScrollView : ScrollView

    @BindView(R.id._homeworkPickLayout)
    lateinit var _HomeworkPickLayout : ScalableLayout

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

    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null    // 통신에서 응답받은 달력 데이터
    private var mCalendarItemViewAdapter : CalendarItemViewAdapter? = null          // 숙제관리 리스트 Adapter
    private var mCalendarItemList : ArrayList<CalendarData> = ArrayList<CalendarData>()

    private var mLastClickTime : Long = 0L              // 중복클릭 방지용

    private val factoryViewModel : StudentHomeworkFactoryViewModel by activityViewModels()
    private val fragmentViewModel : HomeworkFragmentViewModel by activityViewModels()

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
        }
        else
        {
            if (CommonUtils.getInstance(mContext).isTeacherMode)
            {
                view = inflater.inflate(R.layout.fragment_homework_teacher_calendar, container, false)
            }
            else
            {
                view = inflater.inflate(R.layout.fragment_homework_student_calendar, container, false)
            }
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
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
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
            if (CommonUtils.getInstance(mContext).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
            {
                // 태블릿 4:3 모델 대응
                _MainBackgroundView.moveChildView(_CalendarListLayout, 0f, 200f, 1920f, 860f)
            }
            else
            {
                _MainBackgroundView.moveChildView(_CalendarListLayout, 0f, 200f, 1920f, 852f)
            }
        }
    }

    private fun initFont()
    {
        _MonthTitleText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextSunday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextMonday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextTuesday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextWednesday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextThursday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextFriday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _TextSaturday.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        // 달력 아이템
        fragmentViewModel.calendarData.observe(viewLifecycleOwner, Observer {result ->
            mHomeworkCalendarBaseResult = result
            makeCalendarItemList()
            setCalendarTitle()
            setCalendarButton()
        })
    }

    /**
     * 달력 아이템 생성
     */
    private fun makeCalendarItemList()
    {
        val baseCalendar = CalendarBaseData(mContext, mHomeworkCalendarBaseResult!!)
        mCalendarItemList = baseCalendar.getDataList()
        setHomeworkInCalendarItem(mCalendarItemList)

        if (mCalendarItemViewAdapter == null)
        {
            // 초기생성
            Log.f("mCalendarItemViewAdapter create")
            mCalendarItemViewAdapter = CalendarItemViewAdapter(mContext, CommonUtils.getInstance(mContext).isTeacherMode)
                .setItemList(mCalendarItemList)
                .setHomeworkList(mHomeworkCalendarBaseResult!!.getHomeworkDataList())
                .setCalendarItemListener(mCalendarItemListener)
            settingRecyclerView()
        }
        else
        {
            // 데이터 변경
            Log.f("mCalendarItemViewAdapter notifyDataSetChanged")
            mCalendarItemViewAdapter!!.setItemList(mCalendarItemList)
            mCalendarItemViewAdapter!!.setHomeworkList(mHomeworkCalendarBaseResult!!.getHomeworkDataList())
            settingRecyclerView()
            mCalendarItemViewAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 숙제 데이터 달력 아이템에 넣기
     */
    private fun setHomeworkInCalendarItem(dateList : ArrayList<CalendarData>)
    {
        // 숙제 데이터 수 만큼 반복
        for (pos in mHomeworkCalendarBaseResult!!.getHomeworkDataList().indices)
        {
            val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[pos]
            val homeworkTerm = CommonUtils.getInstance(mContext).getTerm(homework.getStartDate(), homework.getEndDate())                                // 숙제 기간 : 숙제 종료일 - 숙제 시작일
            val startPosition = CommonUtils.getInstance(mContext).getTerm(mHomeworkCalendarBaseResult!!.getMonthStartDate(), homework.getStartDate())   // 숙제 시작 포지션 : 숙제 시작일 - 달력 시작일
            val lastPosition = startPosition + homeworkTerm                                                                                             // 숙제 종료 포지션 : 숙제 시작 포지션 + 숙제 기간

            if (startPosition == lastPosition)
            {
                // 숙제 시작 포지션 == 숙제 종료 포지션 : 숙제가 1일 짜리
                dateList[startPosition].setImageType(CalendarImageType.ONE_DAY) // 색 바 이미지 타입 지정 (하루)
                dateList[startPosition].setHomeworkPosition(pos)                // 숙제 아이템 포지션 저장
            }
            else
            {
                // 숙제 기간이 하루 이상일 때
                // 숙제 기간만큼
                for (i in 0..homeworkTerm)
                {
                    val index = startPosition + i // 날짜 인덱스
                    if (index > dateList.size - 1)
                    {
                        // 날짜 인덱스가 달력 데이터 사이즈보다 넘어가면 반복문 정지
                        break
                    }

                    // 색 바 이미지 타입 지정
                    when
                    {
                        // 숙제 시작일
                        (index == startPosition) -> dateList[index].setImageType(CalendarImageType.SEVERAL_DAY_START)
                        // 숙제 종료일
                        (index == lastPosition) -> dateList[index].setImageType(CalendarImageType.SEVERAL_DAY_END)
                        // 숙제 중간날짜
                        else -> dateList[index].setImageType(CalendarImageType.SEVERAL_DAY_CENTER)
                    }
                    dateList[startPosition + i].setHomeworkPosition(pos)    // 숙제 아이템 포지션 저장
                }
            }
        }
    }

    private fun settingRecyclerView()
    {
        _ScrollView.scrollTo(0, 0)
        val gridLayoutManager = GridLayoutManager(mContext, 7)
        _CalendarView.layoutManager = gridLayoutManager
        mCalendarItemViewAdapter!!.setParentLayout(_CalendarListLayout)
        _CalendarView.adapter = mCalendarItemViewAdapter
        factoryViewModel.onCompletedCalendarSet()
    }

    /**
     * 달력 타이틀 설정
     * - 0000년 00월 구조
     * - 월이 0으로 시작하는 경우 0 제거
     * Ex) 2021년 9월, 2021년 12월
     */
    private fun setCalendarTitle()
    {
        if (mHomeworkCalendarBaseResult != null)
        {
            val month = mHomeworkCalendarBaseResult!!.getCurrentMonth().toInt()
            val title = "${mHomeworkCalendarBaseResult!!.getCurrentYear()}년 ${month}월"
            _MonthTitleText.text = title
        }
    }

    /**
     * 달력 이전/다음 화살표 버튼 설정
     */
    private fun setCalendarButton()
    {
        if (mHomeworkCalendarBaseResult!!.isPossiblePrevMonth())
        {
            showPrevButton()
        }
        else
        {
            hidePrevButton()
        }

        if (mHomeworkCalendarBaseResult!!.isPossibleNextMonth())
        {
            showNextButton()
        }
        else
        {
            hideNexButton()
        }
    }

    private fun showPrevButton()
    {
        _BeforeButton.visibility = View.VISIBLE
        _BeforeButtonRect.visibility = View.VISIBLE
    }

    private fun hidePrevButton()
    {
        _BeforeButton.visibility = View.GONE
        _BeforeButtonRect.visibility = View.GONE
    }

    private fun showNextButton()
    {
        _AfterButton.visibility = View.VISIBLE
        _AfterButtonRect.visibility = View.VISIBLE
    }

    private fun hideNexButton()
    {
        _AfterButton.visibility = View.GONE
        _AfterButtonRect.visibility = View.GONE
    }

    @Optional
    @OnClick(R.id._beforeButtonRect, R.id._afterButtonRect)
    fun onClickView(view : View)
    {
        // 중복이벤트 방지
        if(SystemClock.elapsedRealtime() - mLastClickTime < Common.HALF_SECOND)
        {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when(view.id)
        {
            R.id._beforeButtonRect ->
            {
                factoryViewModel.onClickCalendarBefore()
            }
            R.id._afterButtonRect ->
            {
                factoryViewModel.onClickCalendarAfter()
            }
        }
    }

    private val mCalendarItemListener : OnItemViewClickListener = object : OnItemViewClickListener
    {
        override fun onItemClick(position : Int)
        {
            if(SystemClock.elapsedRealtime() - mLastClickTime < Common.HALF_SECOND)
            {
                return
            }

            if (mCalendarItemList[position].hasHomework())
            {
                mLastClickTime = SystemClock.elapsedRealtime()
                factoryViewModel.onClickCalendarItem(mCalendarItemList[position].getHomeworkPosition())
                _ScrollView.scrollTo(0, 0)
            }
        }
    }
}