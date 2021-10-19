package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult

class MainInformationBaseObject : BaseResult()
{
    private val data : MainInformationResult? = null

    fun getData() : MainInformationResult = data!!
}