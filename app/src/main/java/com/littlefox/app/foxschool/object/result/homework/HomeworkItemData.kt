package com.littlefox.app.foxschool.`object`.result.homework

import com.littlefox.app.foxschool.enumerate.CalendarImageType

class HomeworkItemData
{
    private var hw_no : Int         = 0
    private var start_date : String = ""
    private var end_date : String   = ""
    private var hw_cnt : Int        = 0
    private var hw_end_cnt : Int    = 0
    private var is_eval : String    = ""
    private var color : String      = ""
    private var comment : String    = ""
    private var eval : String       = ""
    private var imageType : CalendarImageType = CalendarImageType.ONE

    constructor() {}

    // 아이템 복사용
    constructor(itemData : HomeworkItemData)
    {
        this.hw_no = itemData.getHomeworkNumber()
        this.start_date = itemData.getStartDate()
        this.end_date = itemData.getEndDate()
        this.hw_cnt = itemData.getHomeworkTotalItemCount()
        this.hw_end_cnt = itemData.getHomeworkCompleteItemCount()
        this.is_eval = if (itemData.isEvaluationComplete()) "Y" else "N"
        this.color = itemData.getColor()
        this.comment = itemData.getTeacherComment()
        this.eval = itemData.getEvaluationState()
    }

    fun getHomeworkNumber() : Int
    {
        return hw_no
    }

    fun getStartDate() : String
    {
        return start_date
    }

    fun getEndDate() : String
    {
        return end_date
    }

    fun getHomeworkTotalItemCount() : Int
    {
        return hw_cnt
    }

    fun getHomeworkCompleteItemCount() : Int
    {
        return hw_end_cnt
    }

    fun isEvaluationComplete() : Boolean
    {
        return if(is_eval == "Y") true else false
    }

    fun getColor() : String
    {
        if(color != "")
        {
            return color
        }
        return "gray"
    }

    fun getTeacherComment() : String
    {
        return comment
    }

    fun getEvaluationState() : String
    {
        if(eval != "")
        {
            return eval
        }
        return "E0"
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