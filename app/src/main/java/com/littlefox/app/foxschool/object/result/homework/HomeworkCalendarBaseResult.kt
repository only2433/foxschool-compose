package com.littlefox.app.foxschool.`object`.result.homework

import com.littlefox.app.foxschool.`object`.result.base.BaseResult

/**
 * 숙제관리 학생용 통신 응답 데이터
 */
class HomeworkCalendarBaseResult : BaseResult()
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
    private val homeworkCalendars : ArrayList<HomeworkCalendarItemData> = ArrayList()

    fun getToday() : String
    {
        return today
    }

    fun getCurrentYear() : String
    {
        return year
    }

    fun getCurrentMonth() : String
    {
        return month
    }

    fun getPrevYear() : String
    {
        return prev_year
    }

    fun getPrevMonth() : String
    {
        return prev_month
    }

    fun getNextYear(): String
    {
        return next_year
    }

    fun getNextMonth() : String
    {
        return next_month
    }

    fun getMonthStartDate() : String
    {
        return month_start
    }

    fun getMonthEndDate() : String
    {
        return month_end
    }

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
        return homeworkCalendars
    }
}