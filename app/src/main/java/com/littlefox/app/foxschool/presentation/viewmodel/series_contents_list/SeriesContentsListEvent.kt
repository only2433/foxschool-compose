package com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list

import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class SeriesContentsListEvent : BaseEvent()
{
    object onClickSelectAll : SeriesContentsListEvent()
    object onClickSelectPlay : SeriesContentsListEvent()
    object onClickAddBookshelf : SeriesContentsListEvent()
    object onClickCancel : SeriesContentsListEvent()

    data class onSelectedItem(val index: Int) : SeriesContentsListEvent()
    data class onClickThumbnail(val item: ContentsBaseResult) : SeriesContentsListEvent()
    data class onClickOption(val item: ContentsBaseResult) : SeriesContentsListEvent()
    data class onClickBottomContentsType(val type: BottomDialogContentsType) : SeriesContentsListEvent()
    data class onAddContentsInBookshelf(val index: Int) : SeriesContentsListEvent()
}