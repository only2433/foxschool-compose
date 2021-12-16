package com.littlefox.app.foxschool.`object`.result.login

import com.littlefox.app.foxschool.enumerate.PasswordGuideType

class LoginInformationResult
{
    private var change_date : Int = 0
    private var user : UserInfoSectionResult? = null
    private var school : UserSchoolSectionResult? = null

    fun getChangeDate() : Int = change_date

    fun getUserInformation() : UserInfoSectionResult = user!!

    fun getSchoolInformation() : UserSchoolSectionResult = school!!

    /**
     * 비밀번호 변경이 필요한지 체크
     */
    fun isNeedChangePassword() : Boolean
    {
        return change_date >= 90
    }

    /**
     * 비밀번호 변경 타입 가져오기
     */
    fun getPasswordChangeType() : PasswordGuideType
    {
        if (change_date >= 180)
        {
            return PasswordGuideType.CHANGE180
        }
        else
        {
            return PasswordGuideType.CHANGE90
        }
    }
}