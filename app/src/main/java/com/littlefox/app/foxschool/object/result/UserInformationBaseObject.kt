package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.UserInformationResult

class UserInformationBaseObject : BaseResult()
{
    private var data : UserInformationResult? = null

    fun getData() : UserInformationResult
    {
        return data!!
    }

}