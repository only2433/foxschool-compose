package com.littlefox.app.foxschool.`object`.result.base

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.forum.MetaDataResult
import java.util.*

class SearchListBaseObject : BaseResult()
{
    private val data : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private val meta : MetaDataResult? = null

    fun getSearchList() : ArrayList<ContentsBaseResult> = data

    fun getCurrentPageIndex() : Int
    {
        if(meta != null)
        {
            return meta.current_page
        }

        return 0
    }

    fun isLastPage() : Boolean
    {
        if(meta != null)
        {
            if(meta.current_page == meta.last_page)
            {
                return true
            }
        }

        return false
    }

    fun getTotalItemCount() : Int
    {
        if(meta != null)
        {
            return meta.total
        }

        return 0
    }
}