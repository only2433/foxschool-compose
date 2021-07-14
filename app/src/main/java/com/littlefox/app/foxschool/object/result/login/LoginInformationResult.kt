package com.littlefox.app.foxschool.`object`.result.login

import java.util.*

class LoginInformationResult
{
    private var user : UserInfoSectionResult? = null
    private var school : UserSchoolSectionResult? = null

    fun getUserInformation() : UserInfoSectionResult
    {
        return user!!
    }

    fun getSchoolInformation() : UserSchoolSectionResult
    {
        return school!!
    }
}