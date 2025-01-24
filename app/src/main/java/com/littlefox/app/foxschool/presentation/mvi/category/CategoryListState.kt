package com.littlefox.app.foxschool.presentation.mvi.category

import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class CategoryListState(
    val categoryList: ArrayList<SeriesInformationResult> = arrayListOf(),
    val contentsCount: Int = 0,
    val title: String = "",
    val backgroundViewData: TopThumbnailViewData = TopThumbnailViewData(),
    val isContentsLoading: Boolean = false
): State
