package com.littlefox.app.foxschool.presentation.viewmodel.categoty_list

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent

sealed class CategoryListEvent : BaseEvent()
{
    data class onClickContentsItem(val data : SeriesInformationResult) : CategoryListEvent()
}