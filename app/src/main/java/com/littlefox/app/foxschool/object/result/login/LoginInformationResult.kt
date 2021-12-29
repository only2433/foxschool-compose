package com.littlefox.app.foxschool.`object`.result.login

import com.littlefox.app.foxschool.enumerate.PasswordGuideType

class LoginInformationResult
{
    private var change_90 : String = ""
    private var change_180 : String = ""
    private var user : UserInfoSectionResult? = null
    private var school : UserSchoolSectionResult? = null

    fun getUserInformation() : UserInfoSectionResult = user!!

    fun getSchoolInformation() : UserSchoolSectionResult = school!!

    /**
     * 비밀번호 변경이 필요한지 체크
     */
    fun isNeedChangePassword() : Boolean
    {
        if (change_90 == "Y" || change_180 == "Y")
        {
            return true
        }
        else
        {
            return false
        }
    }

    /**
     * 비밀번호 변경 타입 가져오기
     */
    fun getPasswordChangeType() : PasswordGuideType
    {
        if (change_180 == "Y")
        {
            return PasswordGuideType.CHANGE180
        }
        else
        {
            return PasswordGuideType.CHANGE90
        }
    }
}