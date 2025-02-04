package com.littlefox.app.foxschool.presentation.mvi.series_contents_list

import com.littlefox.app.foxschool.`object`.data.series.SeriesViewData
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class SeriesContentsListState(
    val contentsList: ArrayList<ContentsBaseResult> = arrayListOf(),
    val selectItemCount: Int = 0,
    val isContentsLoading: Boolean = false,
    val isSingleSeries: Boolean = false,
    val isShowInformationTooltip: Boolean = false,
    val title: String = "",
    val backgroundViewData: TopThumbnailViewData = TopThumbnailViewData(),
    val seriesViewData: SeriesViewData = SeriesViewData(),
): State