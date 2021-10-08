package com.littlefox.app.foxschool.`object`.data.homework

import com.littlefox.app.foxschool.enumerate.CalendarDateType
import com.littlefox.app.foxschool.enumerate.CalendarImageType

/**
 * 달력 아이템
 */
class CalendarData
{
    private var date : String = ""                                      // 날짜 (dd 부분만)
    private var dateType : CalendarDateType = CalendarDateType.SUN      // 요일
    private var isCurrentMonth : Boolean = false                        // 선택된 달 인지 (색 차이를 위해)
    private var isToday : Boolean = false                               // 오늘 날짜인지 (오늘날짜 표시를 위해)
    private var homeworkPosition : Int = -1                             // 숙제 포지션
    private var imageType : CalendarImageType = CalendarImageType.ONE   // 색 바 종류 (하루, 시작, 중간, 끝)

    constructor(date : String, dateType : CalendarDateType, isCurrentMonth : Boolean)
    {
        // 기본정보 : 날짜(dd), 요일, 선택된 달 인지
        this.date = date
        this.dateType = dateType
        this.isCurrentMonth = isCurrentMonth
    }

    fun getDate() : String
    {
        return date
    }

    fun getDateType() : CalendarDateType
    {
        return dateType
    }

    fun isCurrentMonth() : Boolean
    {
        return isCurrentMonth
    }

    fun hasHomework() : Boolean
    {
        return homeworkPosition != -1
    }

    fun isToday() : Boolean
    {
        return isToday
    }

    fun setToday(isToday : Boolean)
    {
        this.isToday = isToday
    }

    fun getHomeworkPosition() : Int
    {
        return homeworkPosition
    }

    fun setHomeworkPosition(position : Int)
    {
        homeworkPosition = position
    }

    fun getImageType() : CalendarImageType
    {
        return imageType
    }

    fun setImageType(type : CalendarImageType)
    {
        imageType = type
    }
}