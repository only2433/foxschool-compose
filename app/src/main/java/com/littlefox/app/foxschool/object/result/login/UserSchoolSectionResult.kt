package com.littlefox.app.foxschool.`object`.result.login

class UserSchoolSectionResult
{
    private var school_id : String = ""
    private var grade : Int = 0
    private var hasclass : String = ""
    private var class_name : String = ""
    private var name : String = ""
    private var type_name = ""
    private var teacher_count : Int = 0
    private var student_count : Int = 0
    private var class_count : Int = 0
    private var address : String = ""
    private var address_detail : String = ""
    private var package_name : String = ""

    fun getSchoolID() : String
    {
        return school_id
    }

    fun getGrade() : Int
    {
        return grade
    }

    fun isHaveClass() : Boolean
    {
        if(hasclass == "")
        {
            return false
        }

        return if(hasclass == "Y") true else false
    }

    fun getOrganizationName() : String
    {
        return name
    }

    fun getClassName() : String
    {
        return class_name
    }

    fun getOrganizationTypeName() : String
    {
        return type_name
    }

    fun getTeacherCount() : Int
    {
        return teacher_count
    }

    fun getStudentCount() : Int
    {
        return student_count
    }

    fun getClassCount() : Int
    {
        return class_count
    }

    fun getAddress() : String
    {
        return address
    }

    fun getAddressDetail() : String
    {
        return address_detail
    }

    fun getProductPackageName() : String
    {
        return package_name
    }
}