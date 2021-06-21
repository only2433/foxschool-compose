package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.main.contract.base.BaseContract


class SeriesContentsListContract
{
    interface View : BaseContract.View
    {
        fun initTransition(transitionType : TransitionType)
        fun setStatusBar(statusColor : String)
        fun settingTitleView(title : String)
        fun settingBackgroundView(thumbnailUrl : String, topbarColor : String)
        fun settingTitleViewTablet(title : String)
        fun settingBackgroundViewTablet(thumbnailUrl : String, topbarColor : String, animationType : TransitionType)
        fun showFloatingToolbarLayout()
        fun hideFloatingToolbarLayout()
        fun setFloatingToolbarPlayCount(count : Int)
        fun showContentListLoading()
        fun hideContentListLoading()
        fun showSeriesDataView(seriesType : String, level : Int, count : Int, isSingleSeries : Boolean, arLevelData : String)
        fun showSeriesDataViewTablet(seriesType : String, level : Int, count : Int, categoryData : String, isSingleSeries : Boolean, arLevelData : String)
        fun showSeriesInformationIntroduceTablet(text : String)
        fun showSeriesInformationView()
        fun showStoryDetailListView(storyDetailItemAdapter : DetailListItemAdapter)
        fun showLastWatchSeriesInformation(seriesName : String, nickName : String, position : Int, isLastMovie : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickSeriesInformation()
        fun onClickNextMovieAfterLastMovie(playNumber : Int)
        fun onClickSelectAll()
        fun onClickSelectPlay()
        fun onClickAddBookshelf()
        fun onClickCancel()
    }
}