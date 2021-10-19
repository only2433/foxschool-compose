package com.littlefox.app.foxschool.`object`.result.homework

class HomeworkListBaseResult
{
    private val start_date : String = ""
    private val end_date : String = ""
    private val student_comment : String = ""
    private val teacher_comment : String = ""
    private var is_eval : String    = ""
    private var eval_comment : String    = ""
    private var eval : String       = ""
    private val list : ArrayList<HomeworkListItemData> = ArrayList()

    fun getStartDate() : String = start_date

    fun getEndDate() : String = end_date

    fun isEvaluationComplete() : Boolean
    {
        return if(is_eval == "Y") true else false
    }

    fun getEvaluationComment() : String = eval_comment

    fun getEvaluationState() : String
    {
        if(eval != "")
        {
            return eval
        }
        return "E0"
    }

    fun getStudentComment() : String = student_comment

    fun getTeacherComment() : String = teacher_comment

    fun getHomeworkItemList() : ArrayList<HomeworkListItemData> = list
}