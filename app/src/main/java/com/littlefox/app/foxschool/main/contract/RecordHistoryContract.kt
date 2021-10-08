package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.RecordHistoryListAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class RecordHistoryContract
{
    interface View : BaseContract.View
    {
        fun showRecordHistoryListView(adapter : RecordHistoryListAdapter)
        fun showRecordHistoryEmptyMessage()
    }

    interface Presenter : BaseContract.Presenter
    {

    }
}