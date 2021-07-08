package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult

class SchoolListBaseObject : BaseResult()
{
    private var data : ArrayList<SchoolItemDataResult> = ArrayList()

    fun getData() : ArrayList<SchoolItemDataResult>
    {
        return data
    }
}
