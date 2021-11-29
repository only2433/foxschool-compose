package com.littlefox.app.foxschool.`object`.result.bookshelf

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import java.util.*

class BookshelfListItemDataResult
{
    private val id = ""
    private val contents : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()

    fun getBookshelfID() : String?
    {
        return id
    }

    fun getContentsList() : ArrayList<ContentsBaseResult>?
    {
        return contents
    }
}