package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult

class LoginBaseObject : BaseResult()
{
    private var data : LoginInformationResult? = null

    fun getData() : LoginInformationResult
    {
        return data!!
    }

}