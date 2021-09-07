package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkBaseResult

class HomeworkManageBaseObject : BaseResult()
{
    private val data : HomeworkBaseResult? = null

    fun getData() : HomeworkBaseResult
    {
        return data!!
    }
}
