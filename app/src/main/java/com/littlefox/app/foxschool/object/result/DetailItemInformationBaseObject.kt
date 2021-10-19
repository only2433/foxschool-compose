package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult


class DetailItemInformationBaseObject : BaseResult()
{
    private var data : DetailItemInformationResult? = null

    fun getData() : DetailItemInformationResult = data!!
}