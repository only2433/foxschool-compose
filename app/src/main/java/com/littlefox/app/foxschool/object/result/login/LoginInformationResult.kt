package com.littlefox.app.foxschool.`object`.result.login

class LoginInformationResult
{
    private var change_date : Int = 0
    private var user : UserInfoSectionResult? = null
    private var school : UserSchoolSectionResult? = null

    fun getChangeDate() : Int = change_date

    fun getUserInformation() : UserInfoSectionResult = user!!

    fun getSchoolInformation() : UserSchoolSectionResult = school!!
}