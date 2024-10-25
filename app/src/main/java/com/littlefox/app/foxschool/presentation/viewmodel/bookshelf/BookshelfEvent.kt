package com.littlefox.app.foxschool.presentation.viewmodel.bookshelf

import com.littlefox.app.foxschool.enumerate.BottomDialogContentsType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent

sealed class BookshelfEvent : BaseEvent()
{
    object onClickSelectAll : BookshelfEvent()
    object onClickSelectPlay : BookshelfEvent()
    object onClickDeleteBookshelf : BookshelfEvent()
    object onClickCancel : BookshelfEvent()

    data class onSelectedItem(val index: Int) : BookshelfEvent()
    data class onClickThumbnail(val item: ContentsBaseResult) : BookshelfEvent()
    data class onClickOption(val item: ContentsBaseResult) : BookshelfEvent()
    data class onClickBottomContentsType(val type: BottomDialogContentsType) : BookshelfEvent()
    data class onDeleteContentsInBookshelf(val index: Int): BookshelfEvent()
}