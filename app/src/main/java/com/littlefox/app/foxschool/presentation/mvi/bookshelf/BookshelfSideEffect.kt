package com.littlefox.app.foxschool.presentation.mvi.bookshelf

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class BookshelfSideEffect: SideEffect()
{
    object ShowContentsDeleteDialog: BookshelfSideEffect()
    object ShowRecordPermissionDialog: BookshelfSideEffect()
    data class ShowBottomOptionDialog(val data: ContentsBaseResult): BookshelfSideEffect()
}