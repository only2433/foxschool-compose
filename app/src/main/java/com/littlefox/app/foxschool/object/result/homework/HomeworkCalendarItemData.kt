package com.littlefox.app.foxschool.`object`.result.homework

class HomeworkCalendarItemData
{
    private var hw_no : Int         = 0
    private var start_date : String = ""
    private var end_date : String   = ""
    private var hw_cnt : Int        = 0
    private var hw_end_cnt : Int    = 0
    private var is_eval : String    = ""
    private var color : String      = ""
    private var eval_comment : String    = ""
    private var eval : String       = ""

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

    fun getEvaluationComment() : String
    {
        return eval_comment
    }

    fun getEvaluationState() : String
    {
        if(eval != "")
        {
            return eval
        }
        return "E0"
    }
}