package com.littlefox.app.foxschool.presentation.mvi.search

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class SearchSideEffect: SideEffect()
{
    object ShowRecordPermissionDialog: SearchSideEffect()
    data class ShowBottomOptionDialog(val data: ContentsBaseResult): SearchSideEffect()
    data class ShowBookshelfContentsAddDialog(val itemList: ArrayList<MyBookshelfResult>): SearchSideEffect()
}