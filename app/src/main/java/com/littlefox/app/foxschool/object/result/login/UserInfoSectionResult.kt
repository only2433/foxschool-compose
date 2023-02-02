package com.littlefox.app.foxschool.`object`.result.login

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.common.Common

class UserInfoSectionResult
{
    @SerializedName("fu_id")
    private var fu_id : String = ""

    @SerializedName("login_id")
    private var login_id : String = ""

    @SerializedName("name")
    private var name : String = ""

    @SerializedName("nick_name")
    private var nick_name : String = ""

    @SerializedName("sex")
    private var sex : String = ""

    @SerializedName("birth")
    private var birth : String = ""

    @SerializedName("email")
    private var email : String = ""

    @SerializedName("phone")
    private var phone : String = ""

    @SerializedName("user_type")
    private var user_type : String = ""

    @SerializedName("hasclass")
    private var hasclass : String = ""

    fun getFoxUserID() : String = fu_id

    fun getLoginID() : String = login_id

    fun getName() : String = name

    fun getNickName() : String = nick_name

    fun getSex() : String = sex

    fun getBirth() : String = birth

    fun getEmail() : String = email

    fun getPhone() : String = phone

    fun getUserType() : String
    {
        if(user_type == "")
        {
            return Common.USER_TYPE_STUDENT
        }
        return user_type
    }

    fun isHaveClass() : Boolean
    {
        if(hasclass == "")
        {
            return false
        }

        return if(hasclass == "Y") true else false
    }
}