package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.SeriesCardViewAdapter
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.main.contract.base.BaseContract


class StoryCategoryListContract
{
    interface View : BaseContract.View
    {
        fun initTransition(transitionType : TransitionType)
        fun setStatusBar(statusColor : String)
        fun settingTitleView(title : String)
        fun settingBackgroundView(thumbnailUrl : String, topbarColor : String)
        fun settingTitleViewTablet(title : String)
        fun settingBackgroundViewTablet(thumbnailUrl : String, topbarColor : String)
        fun showCategoryCardListView(seriesCardViewAdapter : SeriesCardViewAdapter)
        fun showSeriesCountView(count : Int)
    }

    interface Presenter : BaseContract.Presenter {}
}