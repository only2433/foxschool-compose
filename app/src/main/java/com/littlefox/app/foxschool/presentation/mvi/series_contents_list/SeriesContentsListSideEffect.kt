package com.littlefox.app.foxschool.presentation.mvi.series_contents_list

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class SeriesContentsListSideEffect: SideEffect()
{
    data class SetStatusBarColor(val color: String): SeriesContentsListSideEffect()
    data class ShowBottomOptionDialog(val data: ContentsBaseResult): SeriesContentsListSideEffect()
    data class ShowBookshelfContentsAddDialog(val list: ArrayList<MyBookshelfResult>): SeriesContentsListSideEffect()
    object ShowRecordPermissionDialog: SeriesContentsListSideEffect()
}