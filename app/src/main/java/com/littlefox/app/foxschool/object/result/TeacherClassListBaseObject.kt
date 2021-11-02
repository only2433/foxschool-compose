package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.TeacherClassItemData

class TeacherClassListBaseObject : BaseResult()
{
    private val data : ArrayList<TeacherClassItemData>? = null

    fun getClassList() : ArrayList<TeacherClassItemData> = data!!
}
