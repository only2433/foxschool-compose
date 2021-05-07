package com.littlefox.app.foxschool.`object`.result


import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.news.ForumBaseResult
import com.littlefox.app.foxschool.`object`.result.news.MetaDataResult
import java.util.*

class NewsListBaseObject : BaseResult()
{
    private val data : ArrayList<ForumBaseResult> = ArrayList<ForumBaseResult>()
    private val meta : MetaDataResult? = null

    val currentPageIndex : Int
        get()
        {
            if(meta != null)
            {
                return meta.current_page
            }
            else
                return 0
        }

    val isLastPage : Boolean
        get()
        {
            if(meta != null)
            {
                if(meta.current_page === meta.last_page)
                {
                    return true
                }
            }
            return false
        }

    val totalItemCount : Int
        get()
        {
            if(meta != null)
            {
                return meta.total
            }
            else
                return 0
        }

    fun getNewsList() : ArrayList<ForumBaseResult>
    {
        return data;
    }
}