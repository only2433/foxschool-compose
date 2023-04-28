package com.littlefox.app.foxschool.`object`.result.search.paging

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.`object`.result.common.MetaDataPagingResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import java.util.ArrayList

data class SearchListPagingResult(
    @SerializedName("list")
    val list : ArrayList<ContentBasePagingResult> = ArrayList<ContentBasePagingResult>(),

    @SerializedName("meta")
    val meta : MetaDataPagingResult? = null
)
{

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

    val lastPageIndex : Int
        get()
        {
            if(meta != null)
            {
                return meta.last_page
            }
            else
                return 0
        }

    val isLastPage : Boolean
        get()
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

    fun getSearchList() : ArrayList<ContentBasePagingResult> = list



    fun getTotalItemCount() : Int
    {
        if(meta != null)
        {
            return meta.total
        }

        return 0
    }
}