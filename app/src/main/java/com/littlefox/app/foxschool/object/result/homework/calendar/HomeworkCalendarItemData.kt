package com.littlefox.app.foxschool.`object`.result.homework.calendar

class HomeworkCalendarItemData
{
    private var hw_no : Int         = 0
    private var start_date : String = ""
    private var end_date : String   = ""
    private var color : String      = ""

    // 학생 데이터
    private var hw_cnt : Int        = 0
    private var hw_end_cnt : Int    = 0
    private var is_eval : String    = ""
    private var eval : String       = ""
    // ------ END

    // 선생님 데이터
    private var is_complete : String = ""
    private var student_cnt : Int = 0
    private var eval_complete_cnt : Int = 0
    // ------- END

    fun getHomeworkNumber() : Int = hw_no

    fun getStartDate() : String = start_date

    fun getEndDate() : String = end_date


    fun getColor() : String
    {
        if(color != "")
        {
            return color
        }
        return "gray"
    }

    // 학생 사용 함수 ----
    fun getHomeworkTotalItemCount() : Int = hw_cnt

    fun getHomeworkCompleteItemCount() : Int = hw_end_cnt

    fun isEvaluationComplete() : Boolean
    {
        return if(is_eval == "Y") true else false
    }

    fun getEvaluationState() : String
    {
        if(eval != "")
        {
            return eval
        }
        return "E0"
    }
    // ------- END

    // 선생님 사용 함수 ----
    val isComplete : Boolean
        get()
        {
            if(is_complete.equals("Y"))
                return true
            return false
        }

    fun getStudentCount() : Int = student_cnt

    fun getEvaluationCompleteCount() : Int = eval_complete_cnt
    // ----- END

}