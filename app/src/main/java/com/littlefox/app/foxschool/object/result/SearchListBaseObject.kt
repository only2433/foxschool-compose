package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.forum.MetaDataResult
import com.littlefox.app.foxschool.`object`.result.search.SearchListResult
import java.util.*

class SearchListBaseObject : BaseResult()
{
    private var data : SearchListResult? = null

    fun getData() : SearchListResult
    {
        return data!!
    }
}