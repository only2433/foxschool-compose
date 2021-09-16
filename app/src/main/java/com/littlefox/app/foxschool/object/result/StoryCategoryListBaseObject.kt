package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.ArrayList

class StoryCategoryListBaseObject : BaseResult()
{
    private var data : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()

    fun getData() : ArrayList<SeriesInformationResult>
    {
        return data
    }
}