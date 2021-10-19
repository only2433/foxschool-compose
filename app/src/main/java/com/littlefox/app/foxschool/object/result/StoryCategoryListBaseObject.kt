package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.`object`.result.story.StoryCategoryListResult
import java.util.ArrayList

class StoryCategoryListBaseObject : BaseResult()
{
    private var data : StoryCategoryListResult? = null

    fun getData() : StoryCategoryListResult
    {
        return data!!
    }
}