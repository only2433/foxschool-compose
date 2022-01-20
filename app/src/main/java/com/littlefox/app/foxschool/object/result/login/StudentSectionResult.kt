package com.littlefox.app.foxschool.`object`.result.login

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

    fun getClassName() : String = class_name

    fun getOrganizationTypeName() : String = type_name

    fun getTeacherCount() : Int = teacher_count

    fun getStudentCount() : Int = student_count

    fun getClassCount() : Int = class_count

    fun getAddress() : String = address

    fun getAddressDetail() : String = address_detail

    fun getProductPackageName() : String = package_name
}