package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBaseListPagingResult

class ForumListBaseObject : BaseResult()
{
    private val data : ForumBaseListPagingResult? = null

    fun getData() : ForumBaseListPagingResult = data!!
}