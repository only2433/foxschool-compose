package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult


class BookshelfBaseObject : BaseResult()
{
    private val data : MyBookshelfResult? = null

    fun getData() : MyBookshelfResult = data!!
}