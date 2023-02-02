package com.littlefox.app.foxschool.`object`.result.login

import com.google.gson.annotations.SerializedName

class TeacherSectionResult
{
    @SerializedName("name")
    private var name : String = ""

    @SerializedName("type_name")
    private var type_name : String = ""

    fun getOrganizationName() : String = name
    fun getOrganizationTypeName() : String = type_name
}