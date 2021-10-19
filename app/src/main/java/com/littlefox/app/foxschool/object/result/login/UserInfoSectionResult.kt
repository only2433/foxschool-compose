package com.littlefox.app.foxschool.`object`.result.login

import com.littlefox.app.foxschool.common.Common

class UserInfoSectionResult
{
    private var fu_id : String = ""
    private var login_id : String = ""
    private var name : String = ""
    private var nick_name : String = ""
    private var sex : String = ""
    private var birth : String = ""
    private var email : String = ""
    private var phone : String = ""
    private var user_type : String = ""

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
}