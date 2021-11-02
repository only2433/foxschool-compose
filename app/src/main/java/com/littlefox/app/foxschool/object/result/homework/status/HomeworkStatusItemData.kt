package com.littlefox.app.foxschool.`object`.result.homework.status

class HomeworkStatusItemData
{
    private val fu_id : String = ""
    private val user_id : String = ""
    private val user_name : String = ""
    private val hw_cnt : Int = 0
    private val hw_end_cnt : Int = 0
    private val eval : String = ""
    private val is_complete : String = ""
    private val is_student_comment : String = ""
    private val is_comment : String = ""

    fun getUserID() : String = fu_id

    fun getLoginID() : String = user_id

    fun getUserName() : String = user_name

    fun getHomeworkCount(): Int = hw_cnt

    fun getHomeworkCompleteCount(): Int = hw_end_cnt

    fun getEvaluationState() : String
    {
        if(eval != "")
        {
            return eval
        }
        return "E0"
    }

    val isHomeworkAllComplete : Boolean
    get()
    {
        if(is_complete == "Y")
        {
            return true
        }

        return false
    }

    val isHaveStudentComment : Boolean
    get()
    {
        if(is_student_comment == "Y")
        {
            return true
        }
        return false
    }

    val isHaveTeacherComment : Boolean
    get()
    {
        if(is_comment == "Y")
        {
            return true
        }
        return false
    }
}