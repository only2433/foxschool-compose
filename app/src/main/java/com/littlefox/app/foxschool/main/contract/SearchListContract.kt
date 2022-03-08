package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class SearchListContract
{
    interface View : BaseContract.View
    {
        fun showContentsListLoading()
        fun hideContentsListLoading()
        fun showSearchListView(detailListItemAdapter: DetailListItemAdapter)
        fun cancelRefreshView()
    }
    interface Presenter : BaseContract.Presenter
    {
        fun onClickSearchType(type: String)
        fun onClickSearchExecute(keyword: String)
        fun requestRefresh()
    }
}