package com.littlefox.app.foxschool.`object`.result.login

import android.content.Context
import com.littlefox.app.foxschool.R

class StudentSectionResult
{
    private var school_id : String = ""
    private var grade : Int = 0
    private var hasclass : String = ""
    private var class_name : String = ""
    private var name : String = ""
    private var type_name : String = ""
    private var teacher_count : Int = 0
    private var student_count : Int = 0
    private var class_count : Int = 0
    private var address : String = ""
    private var address_detail : String = ""
    private var package_name : String = ""

    fun getSchoolID() : String = school_id

    fun getGrade() : Int = grade

    fun isHaveClass() : Boolean
    {
        if(hasclass == "")
        {
            return false
        }

        return if(hasclass == "Y") true else false
    }

    fun getOrganizationName() : String = name

    fun getClassName(mContext : Context) : String
    {
        if (isHaveClass() == false)
        {
            // 반 배정이 되지 않은 학생 : (미배정)
            return mContext.getString(R.string.text_student_class_unassigned)
        }
        else if (grade > 0)
        {
            // 학년 정보 있는 경우 : %d학년 %s반
            val front = String.format(mContext.resources.getString(R.string.text_student_class_grade), grade)
            val end = String.format(mContext.resources.getString(R.string.text_student_class_class), class_name)
            return "$front $end"
        }
        else
        {
            // 학년 정보가 없는 유치원, 어린이집은 학년 표기 없이 반명만 노출 : %s반
            return String.format(mContext.resources.getString(R.string.text_student_class_class), class_name)
        }
    }

    fun getOrganizationTypeName() : String = type_name

    fun getTeacherCount() : Int = teacher_count

    fun getStudentCount() : Int = student_count

    fun getClassCount() : Int = class_count

    fun getAddress() : String = address

    fun getAddressDetail() : String = address_detail

    fun getProductPackageName() : String = package_name
}