package com.littlefox.app.foxschool.presentation.mvi.series_contents_list

import com.littlefox.app.foxschool.`object`.data.series.SeriesViewData
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class SeriesContentsListEvent: Event
{
    data class NotifyContentsList(val list: ArrayList<ContentsBaseResult>): SeriesContentsListEvent()
    data class SelectItemCount(val count: Int): SeriesContentsListEvent()
    data class EnableContentsLoading(val isLoading: Boolean): SeriesContentsListEvent()
    data class EnableSingleSeries(val isSingleSeries: Boolean): SeriesContentsListEvent()
    data class EnableInformationTooltip(val isHaveInformationTooltip : Boolean): SeriesContentsListEvent()
    data class SetTitle(val title: String): SeriesContentsListEvent()
    data class SetBackgroundViewData(val data: TopThumbnailViewData): SeriesContentsListEvent()
    data class SetSeriesData(val data: SeriesViewData): SeriesContentsListEvent()
}