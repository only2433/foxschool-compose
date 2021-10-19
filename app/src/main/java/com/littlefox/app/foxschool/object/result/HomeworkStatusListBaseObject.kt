package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult

class HomeworkStatusListBaseObject : BaseResult()
{
    private val data : HomeworkListBaseResult? = null

    fun getData() : HomeworkListBaseResult = data!!
}
