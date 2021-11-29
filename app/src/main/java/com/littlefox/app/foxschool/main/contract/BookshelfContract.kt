package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class BookshelfContract
{
    interface View : BaseContract.View
    {
        fun setTitle(title : String?)
        fun showContentListLoading()
        fun hideContentListLoading()
        fun showFloatingToolbarLayout()
        fun hideFloatingToolbarLayout()
        fun setFloatingToolbarPlayCount(count : Int)
        fun showBookshelfDetailListView(adapter : DetailListItemAdapter)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickSelectAll()
        fun onClickSelectPlay()
        fun onClickRemoveBookshelf()
        fun onClickCancel()
    }
}