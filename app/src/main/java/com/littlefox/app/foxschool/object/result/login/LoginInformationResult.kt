package com.littlefox.app.foxschool.`object`.result.login

class LoginInformationResult
{
    private var change_date : Int = 0
    private var user : UserInfoSectionResult? = null
    private var school : UserSchoolSectionResult? = null

    fun getChangeDate() : Int
    {
        return change_date
    }

    fun getUserInformation() : UserInfoSectionResult
    {
        return user!!
    }

    fun getSchoolInformation() : UserSchoolSectionResult
    {
        return school!!
    }
}