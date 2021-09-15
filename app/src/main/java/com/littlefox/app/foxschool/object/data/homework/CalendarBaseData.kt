package com.littlefox.app.foxschool.`object`.data.homework

import android.content.Context
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkBaseResult
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.CalendarDateType
import java.util.*
import kotlin.collections.ArrayList

/**
 * 달력 아이템 생성
 */
class CalendarBaseData
{
    private lateinit var mContext : Context
    private lateinit var homeworkData : HomeworkBaseResult

    private lateinit var mCurrentCalendar : Calendar // 선택된 달에 대한 Calendar
    private lateinit var mTodayCalendar : Calendar // 오늘 날짜에 대한 Calendar

    private var dateList = ArrayList<CalendarData>() // 날짜 아이템 리스트

    constructor(context : Context, data : HomeworkBaseResult)
    {
        this.mContext = context
        homeworkData = data
        makeMonthData()
    }

    fun getDataList() : ArrayList<CalendarData>
    {
        return dateList
    }

    private fun makeMonthData() {
        dateList.clear()

        // 현재 선택된 달
        mCurrentCalendar = Calendar.getInstance()
        mCurrentCalendar.timeInMillis = CommonUtils.getInstance(mContext).getMillisecondFromDate("${homeworkData.getCurrentYear()}-${homeworkData.getCurrentMonth()}-01")

        // 통신으로 받은 오늘 날짜
        mTodayCalendar = Calendar.getInstance()
        mTodayCalendar.timeInMillis = CommonUtils.getInstance(mContext).getMillisecondFromDate(homeworkData.getToday())

        makePrevDateDataInCurrentMonth()
        makeCurrentMonthData()
        makeNextDateDataInCurrentMonth()
    }

    /**
     * 이전달 아이템 생성
     * - 달력 시작일 ~ 해당 월 말일
     */
    private fun makePrevDateDataInCurrentMonth()
    {
        // 통신으로 받은 달력 시작일
        val prevCal = Calendar.getInstance()
        prevCal.timeInMillis = CommonUtils.getInstance(mContext).getMillisecondFromDate(homeworkData.getMonthStartDate())

        if (prevCal.before(mCurrentCalendar)) // 달력 시작일이 선택된 달보다 과거인 경우만 (방어코드 유지?)
        {
            var date = prevCal.get(Calendar.DATE)                           // 날짜 : 달력 시작일
            val maxDate = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)   // 해당 월의 최대 날짜 수
            val remain = maxDate - date                                     // 남은 날짜 : 최대 날짜 수 - 달력 시작일

            // 남은 날짜만큼 달력 아이템 생성
            for (i in 0..remain)
            {
                prevCal.set(Calendar.DATE, date)    // 달력의 날짜를 변경하여
                val dateType = getDateType(prevCal) // 요일정보를 가져온다.
                val item = CalendarData(date = date.toString(), dateType = dateType, isCurrentMonth = false) // 달력 아이템 생성
                if (prevCal.get(Calendar.YEAR) == mTodayCalendar.get(Calendar.YEAR) &&
                    prevCal.get(Calendar.MONTH) == mTodayCalendar.get(Calendar.MONTH) &&
                    date == mTodayCalendar.get(Calendar.DATE))
                {
                    // 현재 선택된 달력의 년도, 월과 서버에서 내려온 오늘 날짜 기준의 년도, 월이 같은 경우 이면서
                    // 날짜가 같은 경우 '오늘' 플래그 true
                    item.setToday(true)
                }
                dateList.add(item)
                date++
            }
        }
    }

    /**
     * 이번달 아이템 생성
     * - 한 달
     */
    private fun makeCurrentMonthData()
    {
        for (i in 1..mCurrentCalendar.getActualMaximum(Calendar.DATE)) // 1일부터 말일까지
        {
            mCurrentCalendar.set(Calendar.DATE, i)          // 달력의 날짜를 변경하여
            val dateType = getDateType(mCurrentCalendar)    // 요일정보를 가져온다.
            val item = CalendarData(date = i.toString(), dateType = dateType, isCurrentMonth = true) // 달력 아이템 생성
            if (mCurrentCalendar.get(Calendar.YEAR) == mTodayCalendar.get(Calendar.YEAR) &&
                mCurrentCalendar.get(Calendar.MONTH) == mTodayCalendar.get(Calendar.MONTH) &&
                i == mTodayCalendar.get(Calendar.DATE))
            {
                // 현재 선택된 달력의 년도, 월과 서버에서 내려온 오늘 날짜 기준의 년도, 월이 같은 경우 이면서
                // 날짜가 같은 경우 '오늘' 플래그 true
                item.setToday(true)
            }
            dateList.add(item)
        }
    }

    /**
     * 다음달 아이템 생성
     * - 1일 ~ 달력 종료일
     */
    private fun makeNextDateDataInCurrentMonth()
    {
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = CommonUtils.getInstance(mContext).getMillisecondFromDate(homeworkData.getMonthEndDate())

        if (endCal.after(mCurrentCalendar)) // 달력 종료일이 선택된 달보다 미래인 경우만 (방어코드 유지?)
        {
            val date = endCal.get(Calendar.DATE)
            for (i in 1..date) // 1일부터 달력 종료일까지
            {
                endCal.set(Calendar.DATE, i)        // 달력의 날짜를 변경하여
                val dateType = getDateType(endCal)  // 요일정보를 가져온다.
                val item = CalendarData(date = i.toString(), dateType = dateType, isCurrentMonth = false) // 달력 아이템 생성
                if (endCal.get(Calendar.YEAR) == mTodayCalendar.get(Calendar.YEAR) &&
                    endCal.get(Calendar.MONTH) == mTodayCalendar.get(Calendar.MONTH) &&
                    i == mTodayCalendar.get(Calendar.DATE))
                {
                    // 현재 선택된 달력의 년도, 월과 서버에서 내려온 오늘 날짜 기준의 년도, 월이 같은 경우 이면서
                    // 날짜가 같은 경우 '오늘' 플래그 true
                    item.setToday(true)
                }
                dateList.add(item)
            }
        }
    }

    /**
     * 요일 정보 가져오기
     */
    private fun getDateType(calendar : Calendar) : CalendarDateType
    {
        when(calendar.get(Calendar.DAY_OF_WEEK))
        {
            Calendar.SUNDAY -> return CalendarDateType.SUN
            Calendar.MONDAY -> return CalendarDateType.MON
            Calendar.TUESDAY -> return CalendarDateType.TUE
            Calendar.WEDNESDAY -> return CalendarDateType.WED
            Calendar.THURSDAY -> return CalendarDateType.THU
            Calendar.FRIDAY -> return CalendarDateType.FRI
            Calendar.SATURDAY -> return CalendarDateType.SAT
        }
        return CalendarDateType.MON
    }
}