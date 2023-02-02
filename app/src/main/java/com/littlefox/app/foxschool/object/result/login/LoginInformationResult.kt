package com.littlefox.app.foxschool.`object`.result.login

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.enumerate.PasswordGuideType

class LoginInformationResult
{
    @SerializedName("change_90")
    private var change_90 : String = ""

    @SerializedName("change_180")
    private var change_180 : String = ""

    @SerializedName("user")
    private var user : UserInfoSectionResult? = null

    @SerializedName("school")
    private var school : StudentSectionResult? = null

    @SerializedName("teacher")
    private var teacher : TeacherSectionResult? = null

    fun getUserInformation() : UserInfoSectionResult = user!!

    fun getSchoolInformation() : StudentSectionResult = school!!

    fun getTeacherInformation() : TeacherSectionResult = teacher!!

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