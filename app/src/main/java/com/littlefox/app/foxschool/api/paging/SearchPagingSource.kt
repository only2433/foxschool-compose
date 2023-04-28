package com.littlefox.app.foxschool.api.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.base.safeApiCall
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBaseListPagingResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.`object`.result.search.paging.SearchListPagingResult
import com.littlefox.logmonitor.Log
import retrofit2.HttpException
import java.io.IOException

class SearchPagingSource(
    private val service : ApiService,
    private val searchType : String = "",
    private val keyword: String) : PagingSource<Int, ContentBasePagingResult>()
{
    override suspend fun load(params : LoadParams<Int>) : LoadResult<Int, ContentBasePagingResult>
    {
        return try
        {
            val page = params.key ?: 1
            val pageSize = params.loadSize.coerceAtMost(Common.PAGE_LOAD_COUNT)

            Log.f("page : $page , pageSize : $pageSize")
            when(val result = safeApiCall {
                if(searchType.equals(""))
                {
                    service.getSearchList(
                        keyword = keyword,
                        pageCount = pageSize,
                        currentPage = page)
                }
                else
                {
                    service.getSearchList(
                        searchType = searchType,
                        keyword = keyword,
                        pageCount = pageSize,
                        currentPage = page)
                }
                })
            {
                is ResultData.Success ->
                {
                    val data = result.data as SearchListPagingResult
                    val totalPage = data.lastPageIndex
                    val currentPage = data.currentPageIndex

                    LoadResult.Page(
                        data = data.getSearchList() ?: ArrayList(),
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (currentPage >= totalPage) null else page + 1
                    )

                }
                is ResultData.Fail ->
                {
                    Log.f("------------- sdljasdlkjaslkjdklasjd")
                    LoadResult.Error(Throwable(result.message))
                }
                else ->
                {
                    LoadResult.Error(Throwable("Unknown Error"))
                }
            }
        }
        catch (exception: IOException)
        {
            LoadResult.Error(exception)
        } catch (exception: HttpException)
        {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state : PagingState<Int, ContentBasePagingResult>) : Int?
    {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }


}