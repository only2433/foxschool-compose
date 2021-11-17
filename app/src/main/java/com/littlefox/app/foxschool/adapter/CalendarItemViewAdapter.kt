package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData
import com.littlefox.app.foxschool.`object`.result.homework.calendar.HomeworkCalendarItemData
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.CalendarDateType
import com.littlefox.app.foxschool.enumerate.CalendarImageType
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.ArrayList

/**
 * 숙제관리 달력 아이템 Adapter
 * - 학생용, 선생님용 같이 사용
 * @author 김태은
 */
class CalendarItemViewAdapter : RecyclerView.Adapter<CalendarItemViewAdapter.ViewHolder?>
{
    private val mContext : Context
    private lateinit var mCalendarStatusList : ArrayList<CalendarData>              // 달력 날짜에 대한 상태 리스트
    private lateinit var mHomeworkDataList : ArrayList<HomeworkCalendarItemData>    // 숙제 데이터 리스트
    private var mCalendarItemListener : OnItemViewClickListener? = null
    private var isTeacher : Boolean = false

    private lateinit var mLayout : LinearLayout
    private var itemWidth : Int     = 0
    private var itemHeight : Int    = 0

    constructor(context: Context, isTeacher : Boolean)
    {
        mContext = context
        this.isTeacher = isTeacher
    }

    // 레이아웃 셀 크기 동일하게 고정시키기 위해 사용
    fun setParentLayout(layout : LinearLayout)
    {
        mLayout = layout
    }

    fun setItemList(list : ArrayList<CalendarData>) : CalendarItemViewAdapter
    {
        mCalendarStatusList = list
        return this
    }

    fun setHomeworkList(list : ArrayList<HomeworkCalendarItemData>) : CalendarItemViewAdapter
    {
        mHomeworkDataList = list
        return this
    }

    override fun getItemCount() : Int
    {
        return mCalendarStatusList.size
    }

    fun setCalendarItemListener(calendarItemListener : OnItemViewClickListener) : CalendarItemViewAdapter
    {
        mCalendarItemListener = calendarItemListener
        return this
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.calendar_item_tablet, parent, false)
        }
        else
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.calendar_item, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        // 셀 가로/세로 사이즈 설정
        if (itemWidth == 0 || itemHeight == 0)
        {
            itemWidth = mLayout.width / 7   // 가로 : 7일
            itemHeight = mLayout.height / 6 // 세로 : 6주
        }
        holder.itemView.layoutParams.width = itemWidth
        holder.itemView.layoutParams.height = itemHeight

        // 아이템 변수로 빼기
        val item = mCalendarStatusList[position]
        holder._DateText.text = item.getDate()

        // 현재 선택된 달이 아닌 미리보기로 보여지는 날짜는 불투명하게 표시한다.
        val color = getDateTextColor(position) // 날짜 텍스트 컬러 가져오기 (주말)
        if (item.isCurrentMonth())
        {
            holder._DateText.setTextColor(color)
            holder._DateText.alpha = 1.0f
            holder._TodayImage.alpha = 1.0f
            holder._ColorBarImage.alpha = 1.0f
            holder._HomeworkStateText.alpha = 1.0f
        }
        else
        {
            holder._DateText.setTextColor(color)
            holder._DateText.alpha = 0.5f
            holder._TodayImage.alpha = 0.7f
            holder._ColorBarImage.alpha = 0.7f
            holder._HomeworkStateText.alpha = 0.7f
        }

        // Visibility 초기화
        holder._ColorBarImage.visibility = View.GONE
        holder._HomeworkStateText.visibility = View.GONE
        holder._StampImage.visibility = View.GONE
        holder._TeacherCheckedImage.visibility = View.GONE

        if (item.isToday())
        {
            // 오늘 날짜인 경우 날짜에 배경 추가
            holder._TodayImage.visibility = View.VISIBLE
            holder._DateText.setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            if (isTeacher)
            {
                holder._TodayImage.setImageResource(R.drawable.icon_calendar_today_blue)
            }
            else
            {
                holder._TodayImage.setImageResource(R.drawable.icon_calendar_today_green)
            }
        }
        else
        {
            holder._TodayImage.visibility = View.GONE
        }

        if (item.hasHomework()) // 숙제 있을 때
        {
            val homework = mHomeworkDataList[item.getHomeworkPosition()]

            // 숙제 있을 경우 색 바 이미지 세팅
            holder._ColorBarImage.visibility = View.VISIBLE
            holder._ColorBarImage.background = CommonUtils.getInstance(mContext).getCalendarBarImage(homework.getColor(), item.getImageType())

            // 검사결과 도장, 숙제 진행상황 텍스트 : 숙제가 하루짜리 이거나 첫번째 날 일 때에만 표시
            if(item.getImageType() == CalendarImageType.ONE_DAY ||
               item.getImageType() == CalendarImageType.SEVERAL_DAY_START)
            {
                if (isTeacher)
                {
                    // [선생님용]
                    if (homework.isComplete)
                    {
                        holder._TeacherCheckedImage.visibility = View.VISIBLE
                    }
                    else
                    {
                        // 검사 진행상황 텍스트 "검사완료한수/전체학생수 검사 중"
                        var evalState = "${homework.getEvaluationCompleteCount()}/${homework.getStudentCount()}"
                        if (CommonUtils.getInstance(mContext).checkTablet)
                        {
                            // 태블릿인 경우
                            evalState += " 검사 중"
                        }
                        else
                        {
                            // 스마트폰인 경우
                            evalState += "\n검사 중"
                        }

                        holder._HomeworkStateText.text = evalState
                        holder._HomeworkStateText.visibility = View.VISIBLE
                    }
                }
                else
                {
                    // [학생용]
                    if (homework.isEvaluationComplete())
                    {
                        // 선생님 평가 있는 경우 아이콘 표시
                        val image = CommonUtils.getInstance(mContext).getHomeworkEvalImage(homework.getEvaluationState())
                        if (image != null)
                        {
                            holder._StampImage.visibility = View.VISIBLE
                            holder._StampImage.background = image
                        }
                    }
                    else
                    {
                        // 숙제 진행상황 텍스트 "숙제완료한수/전체숙제수 완료"
                        var homeworkState = "${homework.getHomeworkCompleteItemCount()}/${homework.getHomeworkTotalItemCount()}"
                        if (CommonUtils.getInstance(mContext).checkTablet)
                        {
                            // 태블릿인 경우
                            homeworkState += " 완료"
                        }
                        else
                        {
                            // 스마트폰인 경우
                            homeworkState += "\n완료"
                        }

                        holder._HomeworkStateText.text = homeworkState
                        holder._HomeworkStateText.visibility = View.VISIBLE
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            mCalendarItemListener?.onItemClick(position)
        }
    }

    /**
     * 날짜 텍스트 컬러 가져오기
     * - 일요일 : 주황색
     * - 토요일 : 파란색
     * - 그 외 : 회색
     */
    private fun getDateTextColor(position : Int) : Int
    {
        when(mCalendarStatusList[position].getDateType())
        {
            CalendarDateType.SUN -> return mContext.resources.getColor(R.color.color_ff974b)
            CalendarDateType.SAT -> return mContext.resources.getColor(R.color.color_29c8e6)
            else -> return mContext.resources.getColor(R.color.color_444444)
        }
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._baseView)
        lateinit var _BaseView : ScalableLayout

        @BindView(R.id._todayImage)
        lateinit var _TodayImage : ImageView

        @BindView(R.id._dateText)
        lateinit var _DateText : TextView

        @BindView(R.id._colorBarImage)
        lateinit var _ColorBarImage : ImageView

        @BindView(R.id._stampImage)
        lateinit var _StampImage : ImageView

        @BindView(R.id._teacherCheckedImage)
        lateinit var _TeacherCheckedImage : ImageView

        @BindView(R.id._homeworkStateText)
        lateinit var _HomeworkStateText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _DateText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
            _HomeworkStateText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        }
    }
}