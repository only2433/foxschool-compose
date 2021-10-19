package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult

class ForumListBaseObject : BaseResult()
{
    private val data : ForumBaseListResult? = null

    fun getData() : ForumBaseListResult = data!!
}