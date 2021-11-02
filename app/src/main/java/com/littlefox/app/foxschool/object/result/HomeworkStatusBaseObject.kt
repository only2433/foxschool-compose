package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkStatusBaseResult

class HomeworkStatusBaseObject : BaseResult()
{
    private val data : HomeworkStatusBaseResult? = null

    fun getData() : HomeworkStatusBaseResult = data!!
}