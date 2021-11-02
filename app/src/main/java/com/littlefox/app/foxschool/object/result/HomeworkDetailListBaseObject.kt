package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult

class HomeworkDetailListBaseObject : BaseResult()
{
    private val data : HomeworkDetailBaseResult? = null

    fun getData() : HomeworkDetailBaseResult = data!!
}
