package com.littlefox.app.foxschool.`object`.result.homework

import com.littlefox.app.foxschool.`object`.result.homework.calendar.HomeworkCalendarItemData


/**
 * 숙제관리 학생용 통신 응답 데이터
 */
class HomeworkCalendarBaseResult
{
    private val today : String          = ""
    private val year : String           = ""
    private val month : String          = ""
    private val prev_year : String      = ""
    private val prev_month : String     = ""
    private val next_year : String      = ""
    private val next_month : String     = ""
    private val month_start : String    = ""
    private val month_end : String      = ""
    private val homework : ArrayList<HomeworkCalendarItemData> = ArrayList()

    fun getToday() : String = today

    fun getCurrentYear() : String = year

    fun getCurrentMonth() : String = month

    fun getPrevYear() : String = prev_year

    fun getPrevMonth() : String = prev_month

    fun getNextYear(): String = next_year

    fun getNextMonth() : String = next_month

    fun getMonthStartDate() : String = month_start

    fun getMonthEndDate() : String = month_end

    fun isPossiblePrevMonth() : Boolean
    {
        if(prev_year != "" || prev_month != "")
        {
            return true
        }
        return false
    }

    fun isPossibleNextMonth() : Boolean
    {
        if(next_year != "" || next_month != "")
        {
            return true
        }
        return false
    }

    fun getHomeworkDataList() : ArrayList<HomeworkCalendarItemData>
    {
        return homework
    }
}