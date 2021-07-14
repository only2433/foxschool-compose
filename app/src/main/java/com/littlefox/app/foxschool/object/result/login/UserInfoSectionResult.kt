package com.littlefox.app.foxschool.`object`.result.login

import com.littlefox.app.foxschool.common.Common

class UserInfoSectionResult
{
    private var fu_id : String = ""
    private var login_id : String = ""
    private var name : String = ""
    private var user_type : String = ""

    fun getFoxUserID() : String
    {
        return fu_id
    }

    fun getLoginID() : String
    {
        return login_id
    }

    fun getName() : String
    {
        return name
    }

    fun getUserType() : String
    {
        if(user_type == "")
        {
            return Common.USER_TYPE_STUDENT
        }
        return user_type
    }
}