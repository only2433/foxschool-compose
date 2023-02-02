package com.littlefox.app.foxschool.`object`.result.login

import com.google.gson.annotations.SerializedName

class StudentSectionResult
{
    @SerializedName("school_id")
    private var school_id : String = ""

    @SerializedName("grade")
    private var grade : Int = 0

    @SerializedName("hasclass")
    private var hasclass : String = ""

    @SerializedName("class_name")
    private var class_name : String = ""

    @SerializedName("name")
    private var name : String = ""

    @SerializedName("type_name")
    private var type_name : String = ""

    @SerializedName("teacher_count")
    private var teacher_count : Int = 0

    @SerializedName("student_count")
    private var student_count : Int = 0

    @SerializedName("class_count")
    private var class_count : Int = 0

    @SerializedName("address")
    private var address : String = ""

    @SerializedName("address_detail")
    private var address_detail : String = ""

    @SerializedName("package_name")
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