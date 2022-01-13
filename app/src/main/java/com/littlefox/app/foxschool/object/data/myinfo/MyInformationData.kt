package com.littlefox.app.foxschool.`object`.data.myinfo

import android.content.Context
import com.littlefox.app.foxschool.`object`.result.login.UserInfoSectionResult
import com.littlefox.app.foxschool.common.CommonUtils

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

    fun addPhoneHyphen(mContext : Context) : String
    {
        this.phone = CommonUtils.getInstance(mContext).getPhoneTypeNumber(phone)
        return this.phone
    }

    fun removePhoneHyphen() : String
    {
        this.phone = phone.replace("-", "")
        return this.phone
    }

    fun isCompleteInformationData() : Boolean
    {
        if (this.name.isNotEmpty() && this.email.isNotEmpty())
        {
            return true
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