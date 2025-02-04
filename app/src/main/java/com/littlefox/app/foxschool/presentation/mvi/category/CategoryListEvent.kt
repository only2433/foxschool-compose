package com.littlefox.app.foxschool.presentation.mvi.category

import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class CategoryListEvent: Event
{
    data class NotifyContentsList(val contentsList: ArrayList<SeriesInformationResult>): CategoryListEvent()
    data class SetTotalContentsCount(val count: Int): CategoryListEvent()
    data class SetTitle(val title: String): CategoryListEvent()
    data class SettingBackground(val data: TopThumbnailViewData): CategoryListEvent()
    data class EnableContentsLoading(val isLoading: Boolean): CategoryListEvent()
}