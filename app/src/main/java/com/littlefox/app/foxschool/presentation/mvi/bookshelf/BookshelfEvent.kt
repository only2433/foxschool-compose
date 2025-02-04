package com.littlefox.app.foxschool.presentation.mvi.bookshelf

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class BookshelfEvent: Event
{
    data class NotifyContentsList(val contentsList : ArrayList<ContentsBaseResult>): BookshelfEvent()
    data class SelectItemCount(val count: Int): BookshelfEvent()
    data class SetTitle(val title: String): BookshelfEvent()
    data class EnableContentsLoading(val isLoading: Boolean): BookshelfEvent()
}