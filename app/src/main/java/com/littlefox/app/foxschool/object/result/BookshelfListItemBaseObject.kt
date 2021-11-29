package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import java.util.ArrayList

class BookshelfListItemBaseObject : BaseResult()
{
    private val data : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()

    fun getData() : ArrayList<ContentsBaseResult> = data
}