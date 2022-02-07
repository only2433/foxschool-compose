package com.littlefox.app.foxschool.`object`.data.myinfo

import com.littlefox.app.foxschool.`object`.result.login.UserInfoSectionResult

/**
 * 나의정보 변경 데이터
 */
class MyInformationData
{
    private var id : String = ""
    private var name : String = ""
    private var email : String = ""
    private var phone : String = ""

    constructor()

    constructor(userInfo : UserInfoSectionResult)
    {
        this.id = userInfo.getLoginID()
        this.name = userInfo.getName()
        this.email = userInfo.getEmail()
        this.phone = userInfo.getPhone()
    }

    fun getId() = id
    fun getName() = name
    fun getEmail() = email
    fun getPhone() = phone

    fun setId(id : String)
    {
        this.id = id
    }

    fun setName(name : String)
    {
        this.name = name
    }

    fun setEmail(email : String)
    {
        this.email = email
    }

    fun setPhone(phone : String)
    {
        this.phone = phone
    }

    fun isCompleteInformationData(isTeacher : Boolean) : Boolean
    {
        // 선생님은 이름, 이메일, 전화번호 입력해야 활성화
        // 학생은 이름, 이메일만 입력해도 활성화
        if (this.name.isNotEmpty() && this.email.isNotEmpty())
        {
            if (isTeacher)
            {
                if (this.phone.isNotEmpty())
                {
                    return true
                }
            }
            else
            {
                return true
            }
        }
        return false
    }

    fun clearData()
    {
        id = ""
        name = ""
        email = ""
        phone = ""
    }
}