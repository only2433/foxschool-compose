package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult

class IntroduceSeriesBaseObject : BaseResult()
{
    private var data : IntroduceSeriesInformationResult? = null

    fun getData() : IntroduceSeriesInformationResult = data!!
}